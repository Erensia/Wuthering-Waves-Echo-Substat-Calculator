ALTER TABLE echo_stat ADD COLUMN cost SMALLINT;

UPDATE echo_stat
SET cost = CASE
    WHEN slot_number = 1 THEN 4
    WHEN slot_number IN (2, 3) THEN 3
    ELSE 1
END;

ALTER TABLE echo_stat ALTER COLUMN cost SET NOT NULL;
ALTER TABLE echo_stat ADD CONSTRAINT echo_stat_cost_check CHECK (cost IN (1, 3, 4));
