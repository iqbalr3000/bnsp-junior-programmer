CREATE TABLE chat_message (
    id          BIGSERIAL PRIMARY KEY,
    staff_id    BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    role        VARCHAR(10) NOT NULL CHECK (role IN ('USER','MODEL')),
    content     TEXT NOT NULL,
    tool_trace  TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_message_staff_created ON chat_message(staff_id, created_at);
