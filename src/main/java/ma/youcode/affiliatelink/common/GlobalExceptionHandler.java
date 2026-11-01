package ma.youcode.affiliatelink.common;

import lombok.extern.slf4j.Slf4j;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.common.exception.CampaignNotFoundException;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.InvalidStatusTransitionException;
import ma.youcode.affiliatelink.common.exception.OfferNotFoundException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.ServletException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        log.warn("409 Email already exists: profile creation rejected");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Email already exists");

        return problem;
    }

    @ExceptionHandler(ProfileNotExistException.class)
    public ProblemDetail handleProfileNotExiste(ProfileNotExistException exception) {
        log.warn("404 Profile not found: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Profile not found");

        return problem;
    }

    @ExceptionHandler(OfferNotFoundException.class)
    public ProblemDetail handleOfferNotFound(OfferNotFoundException exception) {
        log.warn("404 Offer not found: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Offer not found");

        return problem;
    }

    @ExceptionHandler(CampaignNotFoundException.class)
    public ProblemDetail handleCampaignNotFound(CampaignNotFoundException exception) {
        log.warn("404 Campaign not found: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Campaign not found");

        return problem;
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ProblemDetail handleInvalidStatusTransition(InvalidStatusTransitionException exception) {
        log.warn("409 Invalid status transition: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Invalid status transition");

        return problem;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException exception) {
        log.warn("409 Business rule violation: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Business rule violation");

        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("400 Invalid request parameter: name={}, expectedType={}",
                exception.getName(),
                exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "unknown");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid value '" + exception.getValue() + "' for parameter '" + exception.getName() + "'"
        );

        problem.setTitle("Invalid request parameter");

        return problem;
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropertyReference(PropertyReferenceException exception) {
        log.warn("400 Invalid sort property: {}", exception.getPropertyName());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid sort property: " + exception.getPropertyName()
        );

        problem.setTitle("Invalid sort parameter");

        return problem;
    }

    /**
     * Logs bean validation failures (field names only, never rejected values) and rethrows,
     * so Spring keeps producing the exact same 400 response as before.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidation(MethodArgumentNotValidException exception) throws MethodArgumentNotValidException {
        List<String> fields = exception.getFieldErrors().stream()
                .map(error -> error.getField())
                .distinct()
                .toList();

        log.warn("400 Validation failed: errorCount={}, fields={}", exception.getErrorCount(), fields);

        throw exception;
    }

    /**
     * Catch-all. Exceptions that Spring already maps to a specific HTTP status are logged and
     * rethrown untouched (same status code as before); anything else is an unexpected error,
     * logged at ERROR with its stacktrace, and answered with 500 as before.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) throws Exception {
        if (isHandledByFramework(exception)) {
            log.warn("Request rejected: {}", exception.getClass().getSimpleName());
            throw exception;
        }

        log.error("Unexpected error: {}", exception.getClass().getSimpleName(), exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );

        problem.setTitle("Internal server error");

        return problem;
    }

    private boolean isHandledByFramework(Exception exception) {
        return exception instanceof ErrorResponse
                || exception instanceof ServletException
                || exception instanceof HttpMessageConversionException
                || exception instanceof TypeMismatchException
                || exception instanceof BindException
                || AnnotatedElementUtils.hasAnnotation(exception.getClass(), ResponseStatus.class);
    }
}
