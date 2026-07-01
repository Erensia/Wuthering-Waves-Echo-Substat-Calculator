CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE loadout
    ADD COLUMN user_id UUID REFERENCES app_user(id) ON DELETE CASCADE;

CREATE INDEX loadout_user_created_at_idx
    ON loadout(user_id, created_at DESC);
