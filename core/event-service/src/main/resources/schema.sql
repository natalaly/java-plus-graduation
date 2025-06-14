CREATE TABLE IF NOT EXISTS category
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS event
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    annotation         VARCHAR(2000)               NOT NULL,
    category_id        BIGINT                      NOT NULL,
    description        VARCHAR(7000)               NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    latitude           FLOAT                       NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude          FLOAT                       NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    paid               BOOLEAN                     NOT NULL DEFAULT FALSE,
    created_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    initiator_id       BIGINT                      NOT NULL,
    participant_limit  INT                         NOT NULL DEFAULT 0,
    title              VARCHAR(120)                NOT NULL,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN                     NOT NULL DEFAULT TRUE,
    state              VARCHAR(20)                 NOT NULL CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED')),
    CONSTRAINT pk_event PRIMARY KEY (id),
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS compilation
(
    id     BIGINT GENERATED ALWAYS AS IDENTITY,
    title  VARCHAR(50) NOT NULL,
    pinned BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_compilation PRIMARY KEY (id),
    CONSTRAINT uq_compilation_title UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS compilation_event
(
    compilation_id BIGINT NOT NULL,
    event_id       BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_compilation FOREIGN KEY (compilation_id) REFERENCES compilation (id) ON DELETE CASCADE,
    CONSTRAINT fk_compilation_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comment
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT                      NOT NULL,
    event_id     BIGINT                      NOT NULL,
    content      VARCHAR(5000)               NOT NULL,
    is_initiator BOOLEAN                     NOT NULL DEFAULT FALSE,
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_event_category ON event (category_id);
CREATE INDEX IF NOT EXISTS idx_event_initiator ON event (initiator_id);
CREATE INDEX IF NOT EXISTS idx_event_id_initiator ON event (id, initiator_id);
CREATE INDEX IF NOT EXISTS idx_event_state ON event (state);
CREATE INDEX IF NOT EXISTS idx_event_date ON event (event_date);
