CREATE TABLE IF NOT EXISTS comment
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT                      NOT NULL,
    event_id     BIGINT                      NOT NULL,
    content      VARCHAR(5000)               NOT NULL,
    is_initiator BOOLEAN                     NOT NULL DEFAULT FALSE,
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
