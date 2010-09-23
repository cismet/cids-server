BEGIN;

DELETE FROM cs_config_attr_jt;
DELETE FROM cs_config_attr_key;
DELETE FROM cs_config_attr_value;

ALTER SEQUENCE cs_config_attr_key_sequence   RESTART 1;
ALTER SEQUENCE cs_config_attr_value_sequence RESTART 1;
ALTER SEQUENCE cs_config_attr_jt_sequence    RESTART 1;

INSERT INTO cs_config_attr_key (key) VALUES ('abc');

INSERT INTO cs_config_attr_value (value) VALUES ('alphabeth');
INSERT INTO cs_config_attr_value (value) VALUES ('alphabeth2');
INSERT INTO cs_config_attr_value (value) VALUES ('alphabeth3');

INSERT INTO cs_config_attr_jt                (dom_id, key_id, val_id) VALUES       (1, 1, 1);
INSERT INTO cs_config_attr_jt         (ug_id, dom_id, key_id, val_id) VALUES    (1, 1, 1, 2);
INSERT INTO cs_config_attr_jt (usr_id, ug_id, dom_id, key_id, val_id) VALUES (1, 1, 1, 1, 3);

COMMIT;