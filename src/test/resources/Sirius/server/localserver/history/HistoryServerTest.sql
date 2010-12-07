BEGIN;

DROP TABLE cs_history;

COMMIT;

BEGIN;

CREATE TABLE cs_history (
    class_id    INTEGER     NOT NULL,
    object_id   INTEGER     NOT NULL,
 -- because of anonymous usage null must be allowed
    usr_id      INTEGER             ,
 -- because of anonymous usage null must be allowed
    ug_id       INTEGER             , 
    valid_from  TIMESTAMP   NOT NULL,
    json_data   TEXT        NOT NULL,

    FOREIGN KEY (class_id)  REFERENCES cs_class,
    FOREIGN KEY (usr_id)    REFERENCES cs_usr,
    FOREIGN KEY (ug_id)     REFERENCES cs_ug,

    PRIMARY KEY (class_id, object_id, valid_from)
);

INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES (1, 1, 1, 1, '2010-12-04 15:12:49.553', 'test1');
INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES (1, 1, 1, 1, '2010-12-04 15:13:46.023', 'test2');
INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES (1, 1, 1, 1, '2010-12-04 15:14:27.873', 'test3');

COMMIT;