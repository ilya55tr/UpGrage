--liquibase formatted sql

--changeset ilya55tr:01-create-client-table
CREATE TABLE client
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name       VARCHAR(255),
    citizenship     VARCHAR(100),
    client_type     VARCHAR(100),
    document_number VARCHAR(100),
    document_series VARCHAR(100),
    document_type   VARCHAR(100),
    mdm_code        BIGINT
);

--changeset ilya55tr:02-create-account_status-table
CREATE TABLE account_status
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

--changeset ilya55tr:03-account-table
CREATE TABLE account
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status_id     INT          NOT NULL,
    client_id     UUID         NOT NULL,
    account_type  VARCHAR(100) NOT NULL,
    currency_code VARCHAR(10)  NOT NULL,

    CONSTRAINT fk_account_status
        FOREIGN KEY (status_id)
            REFERENCES account_status (id),

    CONSTRAINT fk_account_client
        FOREIGN KEY (client_id)
            REFERENCES client (id)
);
