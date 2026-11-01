CREATE TABLE offer_offers (
    id             BIGSERIAL PRIMARY KEY,
    advertiser_id  BIGINT       NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    landing_url    VARCHAR(255) NOT NULL,
    niche          VARCHAR(30)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL
);

CREATE TABLE offer_campaigns (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255)   NOT NULL,
    start_date    DATE           NOT NULL,
    end_date      DATE           NOT NULL,
    budget        NUMERIC(12, 2) NOT NULL,
    payout_model  VARCHAR(20)    NOT NULL,
    payout_value  NUMERIC(12, 2) NOT NULL,
    status        VARCHAR(20)    NOT NULL,
    offer_id      BIGINT         NOT NULL,
    CONSTRAINT fk_offer_campaigns_offer FOREIGN KEY (offer_id) REFERENCES offer_offers (id)
);

CREATE TABLE offer_tags (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE offer_offer_tags (
    offer_id  BIGINT NOT NULL,
    tag_id    BIGINT NOT NULL,
    PRIMARY KEY (offer_id, tag_id),
    CONSTRAINT fk_offer_offer_tags_offer FOREIGN KEY (offer_id) REFERENCES offer_offers (id),
    CONSTRAINT fk_offer_offer_tags_tag FOREIGN KEY (tag_id) REFERENCES offer_tags (id)
);

CREATE INDEX idx_offer_offers_advertiser_id ON offer_offers (advertiser_id);
CREATE INDEX idx_offer_campaigns_offer_id ON offer_campaigns (offer_id);
CREATE INDEX idx_offer_offer_tags_tag_id ON offer_offer_tags (tag_id);
