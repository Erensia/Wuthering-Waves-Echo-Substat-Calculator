CREATE TABLE loadout (
    id UUID PRIMARY KEY,
    name VARCHAR(100),
    first_echo_main_stat VARCHAR(30) NOT NULL,
    score NUMERIC(6, 1) NOT NULL,
    grade VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE echo_stat (
    id BIGSERIAL PRIMARY KEY,
    loadout_id UUID NOT NULL REFERENCES loadout(id) ON DELETE CASCADE,
    slot_number SMALLINT NOT NULL CHECK (slot_number BETWEEN 1 AND 5),
    crit_rate NUMERIC(4, 1) NOT NULL CHECK (crit_rate >= 0),
    crit_damage NUMERIC(4, 1) NOT NULL CHECK (crit_damage >= 0),
    UNIQUE (loadout_id, slot_number)
);
