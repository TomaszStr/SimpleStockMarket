--liquibase formatted sql
--changeset TomaszStr:1
--comment: Initial schema for stock market simulation
CREATE TABLE IF NOT EXISTS stocks
(
    ticker VARCHAR(10) PRIMARY KEY,
    name   VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS bank_inventory
(
    ticker   VARCHAR(10) PRIMARY KEY,
    quantity BIGINT NOT NULL,
    CONSTRAINT fk_bank_stock FOREIGN KEY (ticker) REFERENCES stocks (ticker)
);

CREATE TABLE IF NOT EXISTS wallets
(
    id         UUID PRIMARY KEY,
    owner_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS wallet_inventory
(
    wallet_id UUID,
    ticker    VARCHAR(10),
    quantity  BIGINT NOT NULL,
    PRIMARY KEY (wallet_id, ticker),
    CONSTRAINT fk_inventory_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id),
    CONSTRAINT fk_inventory_stock FOREIGN KEY (ticker) REFERENCES stocks (ticker)
);

CREATE TABLE IF NOT EXISTS audit_logs
(
    id          BIGSERIAL PRIMARY KEY,
    wallet_id   UUID        NOT NULL,
    ticker      VARCHAR(10) NOT NULL,
    action_type VARCHAR(10) NOT NULL, -- BUY or SELL
    quantity    BIGINT      NOT NULL,
    timestamp   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE audit_logs;
--rollback DROP TABLE wallet_inventory;
--rollback DROP TABLE wallets;
--rollback DROP TABLE bank_inventory;
--rollback DROP TABLE stocks;