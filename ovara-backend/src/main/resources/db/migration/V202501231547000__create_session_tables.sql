CREATE UNLOGGED TABLE IF NOT EXISTS virkailija_session (
    primary_id CHAR(36) NOT NULL,
    session_id CHAR(36) NOT NULL,
    creation_time BIGINT NOT NULL,
    last_access_time BIGINT NOT NULL,
    max_inactive_interval INT NOT NULL,
    expiry_time BIGINT NOT NULL,
    principal_name VARCHAR(100),
    CONSTRAINT virkailija_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS virkailija_session_ix1 ON virkailija_session (session_id);

CREATE INDEX IF NOT EXISTS virkailija_session_ix2 ON virkailija_session (expiry_time);

CREATE INDEX IF NOT EXISTS virkailija_session_ix3 ON virkailija_session (principal_name);

CREATE UNLOGGED TABLE IF NOT EXISTS virkailija_session_attributes (
    session_primary_id CHAR(36) NOT NULL,
    attribute_name VARCHAR(200) NOT NULL,
    attribute_bytes BYTEA NOT NULL,
    CONSTRAINT virkailija_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT virkailija_session_attributes_fk FOREIGN KEY (session_primary_id) REFERENCES virkailija_session(primary_id) ON DELETE CASCADE
);

CREATE UNLOGGED TABLE IF NOT EXISTS virkailija_cas_client_session (
    mapped_ticket_id VARCHAR PRIMARY KEY,
    virkailija_session_id CHAR(36) NOT NULL UNIQUE,
    CONSTRAINT virkailija_cas_client_session_fk FOREIGN KEY (virkailija_session_id) REFERENCES virkailija_session(session_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS virkailija_cas_client_session_ix1 ON virkailija_cas_client_session (mapped_ticket_id);