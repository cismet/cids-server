BEGIN;

DROP TABLE cs_config_attr_jt;
DROP SEQUENCE cs_config_attr_jt_sequence;

DROP TABLE cs_config_attr_value;
DROP SEQUENCE cs_config_attr_value_sequence;

DROP TABLE cs_config_attr_key;
DROP SEQUENCE cs_config_attr_key_sequence;

DROP TABLE cs_config_attr_type;
DROP SEQUENCE cs_config_attr_type_sequence;

COMMIT;

BEGIN;

CREATE SEQUENCE cs_config_attr_key_sequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE TABLE cs_config_attr_key (
    id INTEGER PRIMARY KEY DEFAULT nextval('cs_config_attr_key_sequence'),
    key VARCHAR(200) NOT NUll );

CREATE SEQUENCE cs_config_attr_value_sequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE TABLE cs_config_attr_value (
    id INTEGER PRIMARY KEY DEFAULT nextval('cs_config_attr_value_sequence'),
    value TEXT );

CREATE SEQUENCE cs_config_attr_type_sequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE TABLE cs_config_attr_type (
    id INTEGER PRIMARY KEY DEFAULT nextval('cs_config_attr_type_sequence'),
    type char(1) NOT NULL,
    descr varchar(200) );

CREATE SEQUENCE cs_config_attr_jt_sequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE TABLE cs_config_attr_jt (
    id INTEGER PRIMARY KEY DEFAULT nextval('cs_config_attr_jt_sequence'),
    usr_id INTEGER,
    ug_id INTEGER,
    dom_id INTEGER NOT NULL,
    key_id INTEGER NOT NULL,
    val_id INTEGER NOT NULL,
  -- type is only for editing purposes, determines which editor is suited best
    type_id INTEGER,
    FOREIGN KEY (usr_id) REFERENCES cs_usr,
    FOREIGN KEY (ug_id) REFERENCES cs_ug,
    FOREIGN KEY (dom_id) REFERENCES cs_domain,
    FOREIGN KEY (key_id) REFERENCES cs_config_attr_key,
    FOREIGN KEY (val_id) REFERENCES cs_config_attr_value,
    FOREIGN KEY (type_id) REFERENCES cs_config_attr_type,
    -- NULL != NULL in this case so don't fully rely on that
    UNIQUE ( usr_id, ug_id, dom_id, key_id) );

COMMIT;
-- Regarding 'cs_config_attr_jt':
--   type_id: is only for editing purposes, determines which editor is suited best
--   UNIQUE: NULL != NULL in this case so don't fully rely on that

-- insert the known types
BEGIN;

INSERT INTO cs_config_attr_type (type, descr) VALUES ('C', 'regular configuration attribute, a simple string value');
INSERT INTO cs_config_attr_type (type, descr) VALUES ('A', 'action tag configuration attribute, value of no relevance');
INSERT INTO cs_config_attr_type (type, descr) VALUES ('X', 'XML configuration attribute, XML content wrapped by some root element');

COMMIT;