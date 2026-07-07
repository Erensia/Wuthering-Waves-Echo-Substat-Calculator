ALTER TABLE loadout
    ADD COLUMN character_name VARCHAR(100);

UPDATE loadout
SET character_name = name
WHERE name IS NOT NULL
  AND trim(name) <> ''
  AND user_id IS NOT NULL;

CREATE INDEX loadout_user_character_score_idx
    ON loadout(user_id, character_name, score DESC);

ALTER TABLE loadout
    ADD CONSTRAINT loadout_user_id_required CHECK (user_id IS NOT NULL) NOT VALID;
