# Code review AffiliateLink : Partie 2 (module `offer`)

> Review faite le 01/11/2026 sur la branche `feature/offer`, en comptant aussi les changements pas encore commités
> (`Campaign.java`, `CampaignService.java`, `CampaignController.java`, `GlobalExceptionHandler.java`,
> `CampaignNotFoundException.java`, `UpdateCampaignStatusRequestDTO.java`).
> Je n'ai trouvé aucun brief (PDF ou autre) dans le dépôt. La référence utilisée est donc la liste d'exigences que tu m'as donnée.
> Aucun fichier du projet n'a été modifié. Ce fichier est le seul que j'ai créé.

---

## 1. Score global

| Indicateur | Résultat |
|---|---|
| **Exigences terminées** | **28 ✅ / 8 ⚠️ / 11 ❌ sur 47 points vérifiés, soit environ 68 %** (un ⚠️ compte pour moitié). En ne comptant que les ✅ : 60 % |
| **Compilation** (`./mvnw clean test`) | ✅ Ça compile. MapStruct génère bien `OfferMapperImpl` et `CampaignMapperImpl` |
| **`ModularityTest`** (`ApplicationModules.verify()`) | ✅ **Passe**. Les modules détectés sont `common` (open), `profile` et `offer` |
| **`AffiliatelinkApplicationTests.contextLoads`** | ❌ **Échoue** : `PSQLException: FATAL: authentification par mot de passe échouée pour l'utilisateur « affiliate »` |
| Docker | ❌ Pas disponible sur la machine (`failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`). Je n'ai donc pas pu démarrer l'application. Pour tout le reste, la review est **statique** (lecture du code) |

**Pourquoi `contextLoads` échoue :** `application.yaml:8` lit `${DB_PASSWORD}`, mais cette variable est seulement dans `.env`. Ce fichier est lu par Docker Compose, **pas par Spring ni par Maven**. En plus, `spring-boot-docker-compose` est **désactivé par défaut pendant les tests**. Le test a donc essayé de se connecter à un PostgreSQL local sur le port 5432, sans le bon mot de passe. Correction en tâche T13.

**Ce qui est solide :** le modèle métier (`Campaign`, `CampaignStatus`, `PayoutModel`, `Tag`), l'usage de `compareTo` sur les `BigDecimal`, la recherche par Specifications, les DTO en records et l'architecture Modulith (le test `verify()` passe).

**Ce qui manque pour finir :** la règle « une seule campagne ACTIVE » n'est pas appliquée, `GET /offers/{id}` ne renvoie pas les campagnes, l'API publique `OfferApi` n'existe pas, tout le flux de suspension manque (listener, pause des campagnes, email), aucun log n'est écrit, il n'y a ni tests unitaires, ni README, ni collection Postman.

---

## 2. Tableau des exigences

Légende : ✅ fait · ⚠️ partiel ou incorrect · ❌ manquant

### 1. Architecture Spring Modulith

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Sous-packages controller, dto, entity, repository, service | ✅ | `offer/controller`, `offer/dto/{request,response}`, `offer/model/{entity,enums}`, `offer/repository`, `offer/service`, `offer/mapper` | Rien. `model/entity` au lieu de `entity` ne pose pas de problème |
| Package racine `offer` = API publique (`OfferApi` + record `CampaignInfo`, `getCampaign(id)`) | ❌ | Aucun fichier directement dans `ma/youcode/affiliatelink/offer/` | Créer `offer/OfferApi.java` et `offer/CampaignInfo.java` (tâche T4) |
| `offer` n'importe que `ProfileApi`, ses records et l'événement | ✅ | Seuls imports vers profile : `OfferService.java:17` et `CampaignService.java:16` (`profile.ProfileApi`). `verify()` passe | L'événement doit s'appeler `ProfileSuspended` (voir section 9) |
| `Niche` dans `common` | ✅ | `common/enums/Niche.java:3` | Rien |
| `common` OPEN + test `verify()` passe | ✅ | `common/package-info.java:1-3`, `ModularityTest.java:12`, surefire : *Tests run: 1, Failures: 0* | Rien |

### 2. Base de données

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Tables préfixées `offer_offres`, `offer_tags`, `offer_offre_tags`, `offer_campaigns` | ⚠️ | `V2__create_offer_tables.sql:1` → `offer_offers`, `:29` → `offer_offer_tags` | Le brief écrit **offres** et le code **offers**. Le préfixe `offer_` est bien présent. Si le brief impose les noms exacts, il faut une migration `V4` de renommage (ne **jamais** modifier V2 déjà appliquée) et changer `@Table` dans `Offer.java:16` et `@JoinTable` dans `Offer.java:51` |
| `advertiser_id` BIGINT sans FK vers profile | ✅ | `V2:3` (`BIGINT NOT NULL`, aucune contrainte), `V2:37` index simple | Rien. C'est voulu : les modules ne se lient pas par la base |
| Tag `name` UNIQUE | ✅ | `V2:26`, `Tag.java:24` | Rien |
| NUMERIC / DATE / VARCHAR | ✅ | `V2:14-19` | Rien |
| Table `event_publication` compatible Modulith 2.1 | ✅ | `V3__create_event_publication_table.sql:1-15` : colonnes `status`, `completion_attempts`, `last_resubmission_date` (schéma 2.x) | Rien |

### 3. Entités et règles métier

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Enums `CampaignStatus` / `PayoutModel` + `EnumType.STRING` | ✅ | `CampaignStatus.java:3-7`, `PayoutModel.java:5-9`, `Campaign.java:38,45` | Rien |
| Offer ↔ Campaign bidirectionnelle, Campaign propriétaire, `addCampaign()` sur les deux côtés | ✅ | `Offer.java:45` (`mappedBy`), `Campaign.java:50-52` (`@JoinColumn`), `Offer.java:82-85` | Rien |
| Offer ↔ Tag `@ManyToMany` + `@JoinTable`, minuscules, sans doublon | ✅ | `Offer.java:49-55`, `Tag.java:38-40` (`trim().toLowerCase()`), `Set` + `@EqualsAndHashCode` sur `name` (`Tag.java:16,23`) | Rien |
| BigDecimal / LocalDate / statut initial DRAFT | ✅ | `Campaign.java:30-43,71` | Rien |
| Validations endDate > startDate, budget > 0, payout > 0 et ≤ 100 si CPA_PERCENT | ✅ | Entité : `Campaign.java:56-64`. DTO : `CreateCampaignRequestDTO.java:35-43`. `PayoutModel.java:13-18` utilise bien `compareTo` | Les `IllegalArgumentException` de l'entité tombent en 500 si elles sont levées (bug 🟠 B5) |
| Transitions autorisées / refusées (409) | ✅ | `CampaignStatus.java:9-16`, `Campaign.java:74-105`, handler `GlobalExceptionHandler.java:67-77` | Rien |
| Campagne dont la date de fin est dépassée → pas d'activation | ✅ | `Campaign.java:85-87` → `BusinessRuleException` → 409 | Rien |
| **Une seule campagne ACTIVE par offre, de façon atomique** | ❌ | La méthode existe (`Offer.java:87-95`) mais **n'est jamais appelée** : `CampaignService.java:57` appelle directement `campaign.transitionTo(...)` | Bug 🔴 B1 |

### 4. Repositories et recherche

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Specifications combinées, critères optionnels, Pageable | ✅ | `OfferSpecifications.java:14-35`, `OfferService.java:78-96` (`Specification.allOf`), `OfferRepository.java:7` | Rien |
| Pas de N+1, `distinct` sur la jointure tags | ⚠️ | `distinct` présent (`OfferSpecifications.java:24`) | **N+1 sur les tags** : le mapping de chaque offre déclenche une requête de plus (bug 🟠 B6) |

### 5. DTO et MapStruct

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Records d'entrée + Bean Validation + `@Valid` | ✅ | `CreateOfferRequestDTO.java`, `UpdateOfferRequestDTO.java`, `CreateCampaignRequestDTO.java`, `UpdateCampaignStatusRequestDTO.java`, `@Valid` dans `OfferController.java:32,67,77` et `CampaignController.java:33` | Remarque : `@Pattern` est utilisé à la place de `@URL` (Hibernate Validator). Ça marche, mais `@org.hibernate.validator.constraints.URL` est plus lisible si le brief le demande |
| Records de sortie, aucune entité exposée | ✅ | `OfferResponseDTO.java`, `CampaignResponseDTO.java`, les contrôleurs ne renvoient que des DTO | Rien |
| MapStruct `componentModel="spring"`, aucune conversion à la main, processor configuré | ⚠️ | `OfferMapper.java:8`, `CampaignMapper.java:8`, `pom.xml:136-151` (lombok + mapstruct + binding) | Il n'existe pas de mapping « offre + campagnes » (voir 7). Entrée → entité se fait par constructeur (`OfferService.java:48`, `CampaignService.java:41-44`). C'est un choix défendable, car le constructeur protège les règles. Prépare juste l'explication pour la soutenance |

### 6. Services

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| `@Transactional`, `readOnly` sur les lectures, injection par constructeur | ✅ | `OfferService.java:38,56,64,77`, `CampaignService.java:32,52,60`. Champs `final` + `@AllArgsConstructor` | Mineur : préfère `@RequiredArgsConstructor` (bug 🟡 B14) |
| Advertiser vérifié via `ProfileApi` (404 / 409) | ✅ | Offre : `OfferService.java:40-46` (404 puis 409). Campagne : `CampaignService.java:37-39` (409) | Rien |

### 7. Endpoints et codes HTTP

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Chemin `/api/offres` | ⚠️ | `OfferController.java:25` → `/api/offers` | Aligner sur le brief si celui-ci impose `/api/offres` (une ligne à changer) |
| POST offres → 201/400/404/409 | ✅ | `OfferController.java:31-42` + `Location` | Format du 400 incohérent (section 8) |
| GET offres + filtres + pagination → 200/400 | ✅ | `OfferController.java:44-55`, `PagedModel` | Rien |
| **GET offres/{id} avec ses campagnes** → 200/404 | ❌ | `OfferResponseDTO.java:10-19` ne contient pas de champ `campaigns` | Bug 🔴 B2 |
| PUT offres/{id} → 200/400/404 | ✅ | `OfferController.java:64-72`, `OfferService.java:64-75` | Rien |
| POST offres/{id}/campaigns → 201/400/404/409 | ✅ | `OfferController.java:74-88` | Rien |
| GET /api/campaigns?status= → 200 | ✅ | `CampaignController.java:22-28` | Rien |
| PATCH /api/campaigns/{id}/status → 204/404/409 | ⚠️ | `CampaignController.java:30-38` renvoie bien 204, `GlobalExceptionHandler.java:55-65` gère le 404 | Ne respecte pas la règle « une seule ACTIVE » (B1) |

### 8. Gestion des erreurs

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| 404 / 409 via exceptions dédiées | ✅ | `GlobalExceptionHandler.java:31-89` | Rien |
| 400 pour `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `PropertyReferenceException` | ⚠️ | Handlers présents pour `MethodArgumentTypeMismatchException` (`:91`) et `PropertyReferenceException` (`:103`) | **Aucun handler** pour `MethodArgumentNotValidException` ni `HttpMessageNotReadableException`. Spring renvoie bien 400, mais avec son format par défaut (`timestamp/status/error/path`), **sans les messages de validation** (bug 🟠 B4) |
| Format cohérent, pas de stacktrace, rien en 500 par erreur | ⚠️ | `ProblemDetail` partout dans le handler | `IllegalArgumentException` (`Campaign.java:57-63`, `Tag.java:33`) et `DataIntegrityViolationException` donnent un 500. Pas de handler de secours `Exception.class` (B4, B5, B9) |

### 9. Événement ProfileSuspended

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Publié par profile quand un compte passe SUSPENDED | ⚠️ | `ProfileService.java:32-34` publie bien dans la transaction (il sera enregistré dans `event_publication`) | Il s'appelle `AdvertiserSuspended` (`profile/AdvertiserSuspended.java:3`) au lieu de `ProfileSuspended`. Renomme-le si le brief impose ce nom |
| Écouté dans offer avec `@ApplicationModuleListener` + `@EnableAsync` | ❌ | Aucun listener, aucun `@EnableAsync` (`AffiliatelinkApplication.java:6`) | Tâche T5 |
| Campagnes ACTIVE de l'advertiser → PAUSED | ❌ | Pas de requête « campagnes actives d'un advertiser » dans `CampaignRepository.java` | Tâche T5 |
| Email aux publishers (JavaMailSender, Mailpit, destinataires via ProfileApi) | ❌ | `spring-boot-starter-mail` est dans `pom.xml:49-51`, mais il n'y a ni Mailpit dans `compose.yaml`, ni `spring.mail.*`, ni méthode pour récupérer les publishers dans `ProfileApi.java` | Tâche T6 |

### 10. Logging

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Logger SLF4J par classe, placeholders `{}` | ❌ | Recherche de `Slf4j\|LoggerFactory` dans `src/` : **0 résultat** | Tâche T8 |
| Bons niveaux (INFO, WARN, ERROR, DEBUG) | ❌ | Aucun log | Tâche T8 |
| `logging.file.name` | ❌ | Absent de `application.yaml` | Tâche T8 |
| Aucune donnée personnelle dans les logs | (non noté) | Rien n'est loggé pour l'instant | À respecter quand tu ajouteras les logs : jamais d'email ni de nom, seulement des ids |

### 11. Qualité et rendu

| Exigence | Statut | Preuve | Ce qui manque |
|---|---|---|---|
| Tests unitaires des transitions de Campaign | ❌ | `src/test` ne contient que `AffiliatelinkApplicationTests.java` et `ModularityTest.java` | Tâche T7 |
| Test `verify()` | ✅ | `ModularityTest.java:9-13` passe | Rien |
| Endpoints de la partie 1 non cassés | ⚠️ | Le code de profile n'a pas été touché par la partie 2 (statiquement OK). Impossible de vérifier à l'exécution : pas de Docker et `contextLoads` échoue | Rejouer la collection de la partie 1. Voir aussi le bug 🟠 B8 (website) qui existait déjà en partie 1 |
| README, collection Postman, `.gitignore` (logs/, target/) | ❌ | Pas de `README.md` ni de collection. `.gitignore:2` contient `target/` mais **pas `logs/`** ni `.env` | Tâche T10 et bug 🔴 B3 (`.env` est commité) |

---

## 3. Bugs et problèmes trouvés

### 🔴 Bloquants

#### B1. La règle « une seule campagne ACTIVE par offre » n'est jamais appliquée
- **Où :** `CampaignService.java:57` et `Offer.java:87-95`
- **Explication :** tu as bien écrit la méthode `Offer.activateCampaign(...)`, qui met les autres campagnes en PAUSED. Mais le service ne l'appelle pas : il appelle `campaign.transitionTo(ACTIVE)`, qui passe par `Campaign.activate()` directement. Résultat : deux PATCH `{"status":"ACTIVE"}` sur deux campagnes de la même offre donnent **deux campagnes ACTIVE**. En plus, `Campaign.activate()` est `public`, donc n'importe quel code peut contourner la règle.
- **Atomicité :** les deux changements doivent se faire dans **la même transaction**, sinon on peut se retrouver avec l'ancienne pausée et la nouvelle pas activée. Deux requêtes en parallèle peuvent aussi chacune lire « aucune autre ACTIVE ». Un verrou sur l'offre règle les deux problèmes.
- **Correction suggérée :** l'entité porte la règle et `Campaign` délègue à son `Offer` :

```java
// Campaign.java : activate() devient package-private (même package que Offer)
public void transitionTo(CampaignStatus target, LocalDate today) {
    switch (target) {
        case ACTIVE -> offer.activateCampaign(this, today); // passe toujours par l'offre
        case PAUSED -> pause();
        case ENDED  -> end();
        case DRAFT  -> throw new InvalidStatusTransitionException("Invalid status transition: " + status + " -> " + target);
    }
}
void activate(LocalDate today) { ... }   // plus "public"

// Offer.java : on valide d'abord la cible, PUIS on met les autres en pause
public void activateCampaign(Campaign campaign, LocalDate today) {
    if (campaign.getOffer() != this) throw new BusinessRuleException("The campaign does not belong to this offer");
    campaign.activate(today);                                 // lève 409 si interdit, rien n'a encore changé
    campaigns.stream()
             .filter(c -> c != campaign && c.getStatus() == CampaignStatus.ACTIVE)
             .forEach(Campaign::pause);
}

// OfferRepository.java : verrou contre deux activations simultanées
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select o from Offer o where o.id = :id")
Optional<Offer> findByIdForUpdate(@Param("id") Long id);
```

Dans `CampaignService.updateStatus`, si la cible est ACTIVE, verrouille d'abord l'offre (`offerRepository.findByIdForUpdate(campaign.getOffer().getId())`) avant d'appeler `transitionTo`. Tout se passe dans la méthode `@Transactional` existante, donc c'est atomique.

> Note : un index unique partiel en base (`CREATE UNIQUE INDEX ... ON offer_campaigns(offer_id) WHERE status = 'ACTIVE'`) serait une sécurité de plus. Mais Hibernate peut exécuter l'UPDATE « ACTIVE » avant l'UPDATE « PAUSED » et violer l'index en plein flush. Si tu l'ajoutes, fais un `campaignRepository.flush()` après la mise en pause. Pour un apprenant, le verrou suffit.

#### B2. `GET /api/offers/{id}` ne renvoie pas les campagnes
- **Où :** `OfferResponseDTO.java:10-19`, `OfferMapper.java:11`, `OfferService.java:57-62`
- **Explication :** le brief demande l'offre **avec ses campagnes**. Le DTO n'a pas de champ `campaigns`, donc MapStruct ne les mappe pas. Le chargement lazy ne pose pas de problème ici, parce que le mapping se fait dans la transaction `readOnly` du service.
- **Correction suggérée :** créer un DTO de détail, pour ne pas alourdir la liste paginée :

```java
public record OfferDetailResponseDTO(Long id, Long advertiserId, String title, String description,
        String landingUrl, Niche niche, LocalDateTime createdAt, Set<String> tags,
        List<CampaignResponseDTO> campaigns) {}

@Mapper(componentModel = "spring", uses = CampaignMapper.class)
public interface OfferMapper {
    OfferResponseDTO toResponseDTO(Offer offer);
    OfferDetailResponseDTO toDetailDTO(Offer offer); // MapStruct utilise CampaignMapper pour la liste
    default String toTagName(Tag tag) { return tag.getName(); }
}
```

#### B3. Le fichier `.env` (mot de passe de la base) est commité dans Git
- **Où :** `.env` (suivi depuis le commit `2cbf0f6`) et `.gitignore` (pas de ligne `.env`)
- **Explication :** un secret dans Git reste dans l'historique pour toujours, même si tu le supprimes ensuite. Tous ceux qui ont accès au dépôt voient le mot de passe.
- **Correction suggérée :** ajouter `.env` et `logs/` dans `.gitignore`, faire `git rm --cached .env`, commiter un `.env.example` avec `DB_PASSWORD=changeme` et **changer le mot de passe**.

### 🟠 Importants

#### B4. Erreurs 400 de validation et de JSON illisible : format incohérent et messages perdus
- **Où :** `GlobalExceptionHandler.java:16-114`
- **Explication :** sans handler pour `MethodArgumentNotValidException` (un `@Valid` qui échoue) ni pour `HttpMessageNotReadableException` (enum inconnu, date `2026-13-45`, JSON cassé), Spring répond 400 avec son JSON par défaut. Le client ne sait pas **quel champ** est faux. Par exemple, `"End date must be after start date"` n'apparaît jamais.
- **Correction suggérée :** étendre `ResponseEntityExceptionHandler` (qui produit du `ProblemDetail` pour toutes les exceptions MVC standard) et enrichir la validation :

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(e -> errors.put(e.getObjectName(), e.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class) // filet de sécurité
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);  // la stacktrace va dans le LOG, jamais dans la réponse
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
```

> Point d'attention : les `@AssertTrue` de `CreateCampaignRequestDTO.java:35-43` sont des contraintes sur une méthode. L'erreur arrive sous le nom de « champ » `endDateAfterStartDate`. C'est normal.

#### B5. Les `IllegalArgumentException` des entités donnent un 500
- **Où :** `Campaign.java:57,60,63`, `Tag.java:33`
- **Explication :** aujourd'hui le DTO valide avant l'entité, donc ça arrive rarement. Mais dès qu'un autre chemin crée une `Campaign` (un jour, `OfferApi` ou un test), une donnée invalide renverra 500 au lieu de 400. Une règle métier violée ne doit jamais donner un 500.
- **Correction suggérée :** ajouter `@ExceptionHandler(IllegalArgumentException.class)` → 400, ou lever une exception métier de `common` (ex. `InvalidCampaignException`).

#### B6. N+1 sur les tags dans la recherche paginée
- **Où :** `OfferService.java:94-95` + `Offer.java:49` (`@ManyToMany` lazy)
- **Explication :** pour une page de 20 offres, Hibernate fait 1 requête pour les offres, 1 pour le `count`, puis **20 requêtes** (une par offre) quand MapStruct appelle `getTags()`. C'est le problème « N+1 ». Un `join fetch` sur une collection avec pagination n'est pas une bonne solution : Hibernate pagine alors en mémoire (avertissement HHH90003004).
- **Correction suggérée :** chargement par lots (une seule ligne) :

```java
@BatchSize(size = 50)   // org.hibernate.annotations.BatchSize
@ManyToMany
@JoinTable(...)
private Set<Tag> tags = new HashSet<>();
```

ou globalement : `spring.jpa.properties.hibernate.default_batch_fetch_size: 50`.

#### B7. Aucune API publique pour les autres modules (`OfferApi`)
- **Où :** package `ma.youcode.affiliatelink.offer` vide
- **Explication :** avec Modulith, seul le package racine d'un module est visible des autres. La partie 3 (tracking/conversions) aura besoin de lire une campagne sans toucher à `Campaign` ni au repository. Sans `OfferApi`, le module suivant devra importer une classe interne, et `verify()` échouera.
- **Correction :** voir tâche T4.

#### B8. (Partie 1) `website` optionnel dans le DTO mais `NOT NULL` en base → 500
- **Où :** `CreateAdvertiserRequestDTO` (`website` n'a pas `@NotBlank`) vs `V1__create_profile_tables.sql:22` (`website VARCHAR(255) NOT NULL`) et `Advertiser.java:25`
- **Explication :** un `POST /api/advertisers` sans `website` passe la validation, puis PostgreSQL refuse l'insert. Résultat : `DataIntegrityViolationException`, donc 500.
- **Correction :** ajouter `@NotBlank` sur `website`, ou rendre la colonne nullable (nouvelle migration).

#### B9. Conflits d'unicité en base → 500
- **Où :** `OfferService.java:113-116` (création de tags), `V1:20` (`company_name UNIQUE`)
- **Explication :** si deux requêtes créent le tag `"tech"` au même moment, ou si deux advertisers ont le même `companyName`, la base lève `DataIntegrityViolationException`. Personne ne l'attrape, donc 500.
- **Correction :** `@ExceptionHandler(DataIntegrityViolationException.class)` → 409, avec un message générique (sans recopier le message SQL, qui peut contenir des données).

#### B10. `contextLoads` dépend d'une base réelle et d'une variable d'environnement
- **Où :** `AffiliatelinkApplicationTests.java:6-11`, `application.yaml:6-8`
- **Explication :** voir la section 1. Sur une autre machine (ou en CI), `mvn test` échoue.
- **Correction :** voir tâche T13 (Testcontainers ou `spring.docker.compose.skip.in-tests: false`).

### 🟡 Mineurs

| # | Où | Problème | Correction |
|---|---|---|---|
| B11 | `application.yaml` | `spring.jpa.open-in-view` est à `true` par défaut : la session Hibernate reste ouverte pendant la génération du JSON, ce qui cache des erreurs lazy | `spring.jpa.open-in-view: false` (tes mappings sont déjà dans les transactions, donc rien ne casse) |
| B12 | `OfferService.java:72` | `offerRepository.save(offer)` est inutile sur une entité déjà chargée (le *dirty checking* suffit) | Le retirer et mapper `offer` directement. Ce n'est pas faux, juste superflu |
| B13 | `OfferSpecifications.java:33` | `%` et `_` dans `keyword` sont interprétés comme jokers SQL | Échapper : `keyword.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")` + `cb.like(..., pattern, '\\')` |
| B14 | `OfferService.java:31`, `CampaignService.java:25` | `@AllArgsConstructor` sur un service : si tu ajoutes un jour un champ non `final`, il devient un paramètre d'injection | `@RequiredArgsConstructor` (comme dans tes contrôleurs) |
| B15 | `common/exception/OfferNotFoundException.java`, `CampaignNotFoundException.java` | `common` connaît le vocabulaire métier de `offer`, ce qui couple les modules | Une `ResourceNotFoundException` générique dans `common`, ou garder tel quel et l'assumer à la soutenance |
| B16 | `CampaignService.java:37` | Si l'advertiser de l'offre a disparu, on renvoie 409 au lieu de 404 | Réutiliser le même enchaînement que `OfferService.java:40-46` |
| B17 | `Profile.java:12`, `Advertiser.java:11`, `Publisher.java:9` (partie 1) | `@Setter` public sur les entités : `setEmail`, `setId`… contournent les règles. `@SuperBuilder` ignore `status = PENDING` sans `@Builder.Default` | Retirer `@Setter` au niveau de la classe et garder des méthodes métier. Le `@PrePersist` (`Profile.java:46-55`) rattrape le statut |
| B18 | `AdvertiserService.java:37-64` (partie 1) | Lectures sans `@Transactional(readOnly = true)`. `isActiveAdvertiser` charge toute l'entité | `readOnly = true`. Pour `isActiveAdvertiser`, une requête `existsByIdAndStatus(id, ACTIVE)` |
| B19 | `compose.yaml:3` | `postgres:latest` : la version peut changer d'un jour à l'autre. Avec Postgres 18, le volume monté sur `/var/lib/postgresql` est correct, mais une image fixée évite les surprises | `postgres:17` ou `postgres:18` |
| B20 | `Campaign.java:85` | `endDate.isBefore(today)` autorise l'activation le jour même de la fin. C'est cohérent avec « date dépassée », mais vérifie l'interprétation du brief | Documenter le choix |

**Vérifié, aucun problème trouvé :** comparaison des `BigDecimal` (toujours `compareTo` : `PayoutModel.java:14,17`, `Campaign.java:59`), boucle JSON (aucune entité sérialisée), `LazyInitializationException` (`CampaignMapper` lit `offer.id` sur un proxy sans l'initialiser, `OfferMapper` mappe dans la transaction), injection par champ (aucune), logique métier dans les contrôleurs (aucune), `save()` oublié (`updateStatus` repose correctement sur le dirty checking).

---

## 4. Ce qu'il me reste à faire (par priorité)

| # | Tâche | Fichiers | Taille |
|---|---|---|---|
| **T1** | Appliquer « une seule ACTIVE par offre » de façon atomique (B1) | `Campaign.java`, `Offer.java`, `CampaignService.java`, `OfferRepository.java` | M |
| **T2** | Sortir `.env` de Git, ajouter `.env` et `logs/` au `.gitignore`, créer `.env.example` (B3) | `.gitignore`, `.env.example` | S |
| **T3** | `GET /offers/{id}` avec les campagnes (B2) | nouveau `OfferDetailResponseDTO.java`, `OfferMapper.java`, `OfferService.java`, `OfferController.java` | S |
| **T4** | API publique `OfferApi` + `CampaignInfo` (B7) | nouveaux `offer/OfferApi.java`, `offer/CampaignInfo.java` | S |
| **T5** | Listener `ProfileSuspended` : pause des campagnes ACTIVE de l'advertiser + `@EnableAsync` | `AffiliatelinkApplication.java`, renommer `profile/AdvertiserSuspended.java` → `ProfileSuspended.java`, `ProfileService.java`, nouveau `offer/service/ProfileSuspendedListener.java`, `CampaignRepository.java`, `CampaignService.java` | M |
| **T6** | Email aux publishers via Mailpit | `compose.yaml`, `application.yaml`, `ProfileApi.java` (+ record public `PublisherContact`), `PublisherRepository.java`, nouveau `offer/service/SuspensionNotifier.java` | M |
| **T7** | Tests unitaires des transitions de `Campaign` et de `Offer.activateCampaign` | nouveaux `src/test/.../offer/model/entity/CampaignTest.java`, `OfferTest.java` | M |
| **T8** | Logging SLF4J + fichier | `application.yaml`, tous les services, le listener, `GlobalExceptionHandler.java` | M |
| **T9** | Gestion d'erreurs complète : validation (400 détaillé), JSON illisible, `IllegalArgumentException`, `DataIntegrityViolationException`, filet `Exception` (B4, B5, B9) | `GlobalExceptionHandler.java` | S |
| **T10** | README + collection Postman | `README.md`, `postman/AffiliateLink.postman_collection.json` | M |
| **T11** | N+1 des tags (B6) | `Offer.java` ou `application.yaml` | S |
| **T12** | Vérifier les noms du brief : `/api/offres`, tables `offer_offres`/`offer_offre_tags`, événement `ProfileSuspended` | `OfferController.java:25`, nouvelle migration `V4__rename_offer_tables.sql`, `Offer.java:16,51` | S |
| **T13** | Faire passer `contextLoads` sans base installée (B10) | `pom.xml`, `src/test/.../TestcontainersConfiguration.java` ou `src/test/resources/application.yaml` | S |
| **T14** | Correctifs partie 1 (B8, B17, B18) et mineurs (B11-B16) | voir le tableau des mineurs | S |

### Détails et extraits pour les tâches les plus longues

**T4 : `OfferApi`**

```java
package ma.youcode.affiliatelink.offer;

public record CampaignInfo(Long id, Long offerId, Long advertiserId, String name,
                           CampaignStatus status, PayoutModel payoutModel, BigDecimal payoutValue) {}
```

Attention : `CampaignStatus` et `PayoutModel` sont dans `offer.model.enums`, un package **interne**. Si un autre module utilise `CampaignInfo`, il touchera ces enums, et `verify()` le signalera. Deux solutions : les déplacer dans le package racine `offer`, ou les exposer avec `@NamedInterface`. Le plus simple est de les déplacer.

```java
package ma.youcode.affiliatelink.offer;

@Component
@RequiredArgsConstructor
public class OfferApi {
    private final CampaignRepository campaignRepository;

    @Transactional(readOnly = true)
    public Optional<CampaignInfo> getCampaign(Long id) {
        return campaignRepository.findById(id).map(c -> new CampaignInfo(
                c.getId(), c.getOffer().getId(), c.getOffer().getAdvertiserId(), c.getName(),
                c.getStatus(), c.getPayoutModel(), c.getPayoutValue()));
        // ou mieux, pour respecter "aucune conversion à la main" : une méthode CampaignMapper.toInfo(c)
    }
}
```

**T5 : listener**

```java
// AffiliatelinkApplication.java
@SpringBootApplication
@EnableAsync
public class AffiliatelinkApplication { ... }

// CampaignRepository.java
@Query("select c from Campaign c where c.offer.advertiserId = :advertiserId and c.status = :status")
List<Campaign> findByAdvertiserAndStatus(@Param("advertiserId") Long advertiserId,
                                         @Param("status") CampaignStatus status);

// CampaignService.java
@Transactional
public int pauseActiveCampaignsOfAdvertiser(Long advertiserId) {
    List<Campaign> active = campaignRepository.findByAdvertiserAndStatus(advertiserId, CampaignStatus.ACTIVE);
    active.forEach(Campaign::pause);   // dirty checking : pas besoin de save()
    return active.size();
}

// offer/service/ProfileSuspendedListener.java
@Component
@RequiredArgsConstructor
class ProfileSuspendedListener {
    private static final Logger log = LoggerFactory.getLogger(ProfileSuspendedListener.class);
    private final CampaignService campaignService;
    private final SuspensionNotifier notifier;

    @ApplicationModuleListener   // = @Async + @Transactional(REQUIRES_NEW) + @TransactionalEventListener(AFTER_COMMIT)
    void on(ProfileSuspended event) {
        int paused = campaignService.pauseActiveCampaignsOfAdvertiser(event.advertiserId());
        log.info("Advertiser {} suspended: {} active campaign(s) paused", event.advertiserId(), paused);
        if (paused > 0) notifier.notifyPublishers(event.advertiserId(), paused);
    }
}
```

**Pourquoi `@EnableAsync` ?** `@ApplicationModuleListener` contient `@Async`. Sans `@EnableAsync`, l'annotation est ignorée et le listener tourne dans le thread de la requête, juste après le commit. Le PATCH de l'admin attend alors la fin de l'envoi des emails. **Pourquoi après le commit ?** Si la suspension est annulée (rollback), on ne veut pas avoir déjà mis les campagnes en pause. Et grâce à la table `event_publication`, un événement dont le listener a échoué reste « incomplet » et peut être rejoué.

**T6 : Mailpit + email**

```yaml
# compose.yaml
  mailpit:
    image: 'axllent/mailpit:v1.27'
    ports:
      - '1025:1025'   # SMTP
      - '8025:8025'   # interface web : http://localhost:8025

# application.yaml
spring:
  mail:
    host: localhost
    port: 1025
```

```java
// profile/ProfileApi.java : public, rien d'interne n'est exposé
public List<PublisherContact> getActivePublisherContacts() { ... }
// profile/PublisherContact.java
public record PublisherContact(Long id, String email) {}
```

```java
// offer/service/SuspensionNotifier.java
@Component
@RequiredArgsConstructor
class SuspensionNotifier {
    private static final Logger log = LoggerFactory.getLogger(SuspensionNotifier.class);
    private final JavaMailSender mailSender;
    private final ProfileApi profileApi;

    void notifyPublishers(Long advertiserId, int pausedCount) {
        for (PublisherContact p : profileApi.getActivePublisherContacts()) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(p.email());
                msg.setSubject("Campaigns paused");
                msg.setText(pausedCount + " campaign(s) you may promote have been paused.");
                mailSender.send(msg);
                log.debug("Suspension email sent to publisher {}", p.id());   // l'id, JAMAIS l'email
            } catch (MailException e) {
                log.error("Failed to send suspension email to publisher {}", p.id(), e);
            }
        }
    }
}
```

> À confirmer dans le brief : **quels** publishers reçoivent l'email (tous les actifs ? ceux de la même niche que les offres ?). Le lien publisher ↔ campagne n'existe pas encore en partie 2.

**T7 : tests unitaires (JUnit pur, sans Spring, rapides)**

```java
class CampaignTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);

    private Campaign draft() {
        return new Campaign("Black Friday", TODAY, TODAY.plusDays(30),
                new BigDecimal("1000.00"), PayoutModel.CPC, new BigDecimal("0.50"));
    }

    @Test void newCampaignIsDraft() { assertEquals(CampaignStatus.DRAFT, draft().getStatus()); }

    @ParameterizedTest
    @CsvSource({"DRAFT,ACTIVE,true", "ACTIVE,PAUSED,true", "PAUSED,ACTIVE,true", "ACTIVE,ENDED,true",
                "PAUSED,ENDED,true", "DRAFT,PAUSED,false", "DRAFT,ENDED,false", "ENDED,ACTIVE,false",
                "ACTIVE,ACTIVE,false", "ACTIVE,DRAFT,false"})
    void transitions(CampaignStatus from, CampaignStatus to, boolean allowed) {
        assertEquals(allowed, from.canChangeStatus(to));
    }

    @Test void cannotActivateWhenEndDatePassed() {
        Campaign c = draft();
        // offre nécessaire depuis T1 : new Offer(...).addCampaign(c)
        assertThrows(BusinessRuleException.class, () -> c.transitionTo(CampaignStatus.ACTIVE, TODAY.plusDays(31)));
    }

    @Test void cpaPercentAbove100IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Campaign("x", TODAY, TODAY.plusDays(1),
                BigDecimal.TEN, PayoutModel.CPA_PERCENT, new BigDecimal("100.01")));
    }
}
// OfferTest : activer B alors que A est ACTIVE → A passe PAUSED et B ACTIVE
```

**T8 : logging**

```yaml
logging:
  file:
    name: logs/affiliatelink.log
  level:
    root: INFO
    ma.youcode.affiliatelink: DEBUG
```

| Niveau | Quand l'utiliser | Exemple |
|---|---|---|
| INFO | action métier réussie | `log.info("Offer {} created by advertiser {}", offer.getId(), advertiserId);` |
| WARN | règle refusée (transition invalide, advertiser non actif) | `log.warn("Refused transition {} -> {} for campaign {}", from, to, id);` (dans le handler 409 ou le service) |
| ERROR | erreur inattendue, envoi d'email raté | `log.error("Unexpected error", ex);` |
| DEBUG | détail technique | `log.debug("Search offers with criteria {}", criteria);` |

Toujours utiliser `{}` et jamais la concaténation `"..." + x`. Avec `{}`, le message n'est construit que si le niveau est actif, et les valeurs sont bien séparées. **Ne jamais logger** `email`, `firstName`, `lastName` ni le DTO de création d'un profil.

**T13 : `contextLoads` sans base installée**

```xml
<!-- pom.xml, scope test -->
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-testcontainers</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.testcontainers</groupId><artifactId>testcontainers-postgresql</artifactId><scope>test</scope></dependency>
```

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean @ServiceConnection
    PostgreSQLContainer postgres() { return new PostgreSQLContainer("postgres:17"); }
}
// @SpringBootTest @Import(TestcontainersConfiguration.class)
```

Ces deux solutions demandent que **Docker Desktop soit démarré**.

---

## 5. Scénarios Postman pour valider la partie 2

> Base URL : `http://localhost:8080`. Les chemins sont ceux **du code actuel** (`/api/offers`). Remplace par `/api/offres` si tu les renommes.
> Dans Postman, enregistre les ids dans des variables (`pm.collectionVariables.set("offerId", pm.response.json().id)`).

### Préparation (partie 1)

| # | Requête | Corps | Attendu |
|---|---|---|---|
| P1 | `POST /api/advertisers` | `{"email":"adv1@test.ma","companyName":"Acme","sector":"Tech","website":"https://acme.ma"}` | 201, garder `advertiserId` (statut PENDING) |
| P2 | `POST /api/advertisers` | `{"email":"adv2@test.ma","companyName":"Beta","sector":"Food","website":"https://beta.ma"}` | 201, `pendingAdvertiserId` (reste PENDING) |
| P3 | `PATCH /api/admin/profiles/{{advertiserId}}/status` | `{"status":"ACTIVE"}` | 200 |
| P4 | `POST /api/publishers` | `{"email":"pub1@test.ma","firstName":"Sara","lastName":"B","niche":"TECH","bio":"Tech reviewer","country":"MA"}` puis activer (P3 sur cet id) | 201 puis 200 |
| P5 | `POST /api/advertisers` **sans** `website` | `{"email":"adv3@test.ma","companyName":"Gamma","sector":"Tech"}` | Attendu 201 ou 400. **Aujourd'hui 500** (B8) |

### Offres

| # | Requête | Corps | Attendu |
|---|---|---|---|
| O1 | `POST /api/offers` | `{"advertiserId":{{advertiserId}},"title":"Casque Gaming X","description":"Casque sans fil","landingUrl":"https://acme.ma/casque","niche":"GAMING","tags":["Gaming"," audio ","GAMING"]}` | **201**, header `Location`, `tags` = `["gaming","audio"]` (minuscules, sans doublon) |
| O2 | `POST /api/offers` | idem avec `"title":""`, `"landingUrl":"pas-une-url"` | **400** avec le détail des champs (aujourd'hui 400 sans détail, B4) |
| O3 | `POST /api/offers` | `"niche":"CRYPTO"` | **400** (enum invalide → `HttpMessageNotReadableException`) |
| O4 | `POST /api/offers` | `"advertiserId":999999` | **404** |
| O5 | `POST /api/offers` | `"advertiserId":{{pendingAdvertiserId}}` | **409** |
| O6 | `GET /api/offers` | (aucun) | **200**, `content` + `page` (`PagedModel`) |
| O7 | `GET /api/offers?niche=GAMING&tag=GAMING&keyword=casque&advertiserId={{advertiserId}}&page=0&size=5&sort=title,asc` | | **200**, uniquement l'offre O1 (tag insensible à la casse) |
| O8 | `GET /api/offers?niche=FOO` | | **400** (`MethodArgumentTypeMismatchException`) |
| O9 | `GET /api/offers?sort=unknownField` | | **400** « Invalid sort property » |
| O10 | `GET /api/offers/{{offerId}}` | | **200** avec `campaigns: [...]` (aujourd'hui pas de campagnes, B2) |
| O11 | `GET /api/offers/999999` | | **404** |
| O12 | `PUT /api/offers/{{offerId}}` | `{"title":"Casque Gaming X2","description":"v2","landingUrl":"https://acme.ma/casque2","niche":"GAMING","tags":["gaming","promo"]}` | **200**, `tags` = `["gaming","promo"]` |
| O13 | `PUT /api/offers/{{offerId}}` | `{"title":""}` | **400** |
| O14 | `PUT /api/offers/999999` | corps valide | **404** |

### Campagnes

| # | Requête | Corps | Attendu |
|---|---|---|---|
| C1 | `POST /api/offers/{{offerId}}/campaigns` | `{"name":"Lancement","startDate":"2026-11-01","endDate":"2026-12-31","budget":5000.00,"payoutModel":"CPA_PERCENT","payoutValue":12.5}` | **201**, `status: "DRAFT"`, garder `campaignA` |
| C2 | idem | `"name":"Black Friday"` | **201**, `campaignB` |
| C3 | idem | `"endDate":"2026-10-01"` (avant `startDate`) | **400** |
| C4 | idem | `"budget":0` | **400** |
| C5 | idem | `"payoutModel":"CPA_PERCENT","payoutValue":150` | **400** |
| C6 | idem | `"startDate":"2026-13-45"` | **400** (date invalide) |
| C7 | `POST /api/offers/999999/campaigns` | corps valide | **404** |
| C8 | idem sur une offre d'un advertiser suspendu (après S1) | corps valide | **409** |
| C9 | `POST /api/offers/{{offerId}}/campaigns` | `"name":"Expirée","startDate":"2026-01-01","endDate":"2026-02-01"` | **201**, `campaignExpired` |

### Transitions

| # | Requête | Corps | Attendu |
|---|---|---|---|
| T-1 | `PATCH /api/campaigns/{{campaignA}}/status` | `{"status":"ACTIVE"}` | **204** |
| T-2 | `PATCH /api/campaigns/{{campaignB}}/status` | `{"status":"ACTIVE"}` | **204**, puis `GET /api/campaigns?status=ACTIVE` ne contient **que B** et A est `PAUSED` (aujourd'hui A et B sont ACTIVE, B1) |
| T-3 | `PATCH /api/campaigns/{{campaignB}}/status` | `{"status":"PAUSED"}` | **204** |
| T-4 | `PATCH /api/campaigns/{{campaignB}}/status` | `{"status":"ENDED"}` | **204** |
| T-5 | `PATCH /api/campaigns/{{campaignB}}/status` | `{"status":"ACTIVE"}` | **409** (ENDED est final) |
| T-6 | `PATCH /api/campaigns/{{campaignA}}/status` | `{"status":"DRAFT"}` | **409** |
| T-7 | `PATCH /api/campaigns/{{campaignExpired}}/status` | `{"status":"ACTIVE"}` | **409** (date de fin dépassée) |
| T-8 | `PATCH /api/campaigns/{{campaignA}}/status` | `{"status":"FOO"}` | **400** |
| T-9 | `PATCH /api/campaigns/{{campaignA}}/status` | `{}` | **400** (« Status is required ») |
| T-10 | `PATCH /api/campaigns/999999/status` | `{"status":"ACTIVE"}` | **404** |
| T-11 | `GET /api/campaigns?status=ACTIVE&page=0&size=10&sort=startDate,desc` | | **200** |
| T-12 | `GET /api/campaigns?status=RUNNING` | | **400** |

### Suspension (après T5 et T6)

| # | Requête | Corps | Attendu |
|---|---|---|---|
| S1 | Avoir une campagne ACTIVE (T-1), puis `PATCH /api/admin/profiles/{{advertiserId}}/status` | `{"status":"SUSPENDED"}` | **200** immédiatement (la suite est asynchrone) |
| S2 | Attendre 1 à 2 s, puis `GET /api/campaigns?status=ACTIVE` | | **200**, plus aucune campagne de cet advertiser |
| S3 | Ouvrir `http://localhost:8025` (Mailpit) | | Un email par publisher actif (pub1@test.ma) |
| S4 | `SELECT completion_date, status FROM event_publication;` | | Ligne de l'événement `ProfileSuspended` **complétée** |
| S5 | `tail logs/affiliatelink.log` | | Ligne INFO « Advertiser X suspended: N campaign(s) paused », **aucun email dans les logs** |

---

*Fin de la review.*
