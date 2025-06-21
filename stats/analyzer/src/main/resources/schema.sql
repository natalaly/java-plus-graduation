CREATE TABLE IF NOT EXISTS event_similarity
(
    event_a_id       BIGINT           NOT NULL,
    event_b_id       BIGINT           NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    timestamp        TIMESTAMP        NOT NULL,
    CONSTRAINT pk_event_similarity PRIMARY KEY (event_a_id, event_b_id)
);


CREATE TABLE IF NOT EXISTS user_action
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT           NOT NULL,
    event_id    BIGINT           NOT NULL,
    action_type VARCHAR(20)      NOT NULL CHECK (action_type IN ('VIEW', 'REGISTER', 'LIKE')),
    weight      DOUBLE PRECISION NOT NULL,
    timestamp   TIMESTAMP        NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_event_similarity_event_b_id ON event_similarity (event_b_id, event_a_id);

CREATE INDEX IF NOT EXISTS idx_user_action_user_id ON user_action (user_id);
CREATE INDEX IF NOT EXISTS idx_user_action_event_id ON user_action (event_id);
--TODO Potentially a composite index if you frequently query by user, event, and action type
CREATE INDEX IF NOT EXISTS idx_user_action_user_event_type ON user_action (user_id, event_id, action_type);
