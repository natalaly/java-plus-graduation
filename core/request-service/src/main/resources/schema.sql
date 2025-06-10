CREATE TABLE IF NOT EXISTS request
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status   VARCHAR(10)                 NOT NULL CHECK (status IN ('PENDING', 'CANCELED', 'CONFIRMED', 'REJECTED')),
    user_id  BIGINT                      NOT NULL,
    event_id BIGINT                      NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_participation_request_event ON request (event_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_requester ON request (user_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_event_status ON request (event_id, status);
CREATE INDEX IF NOT EXISTS idx_participation_request_requester_event ON request (user_id, event_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_id_event_status ON request (id, event_id, status);