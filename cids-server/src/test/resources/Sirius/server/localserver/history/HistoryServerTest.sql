DROP TABLE cs_history;
DROP TABLE test1;
DROP TABLE test2;
DROP TABLE test3;
DROP TABLE test4;
DROP TABLE test5;
DROP TABLE test6;
DROP SEQUENCE test1_seq;
DROP SEQUENCE test2_seq;
DROP SEQUENCE test3_seq;
DROP SEQUENCE test4_seq;
DROP SEQUENCE test5_seq;
DROP SEQUENCE test6_seq;
DELETE FROM cs_class_attr WHERE class_id = (select id from cs_class where name like 'test2');
DELETE FROM cs_class_attr WHERE class_id = (select id from cs_class where name like 'test3');
DELETE FROM cs_class_attr WHERE class_id = (select id from cs_class where name like 'test4');
DELETE FROM cs_class_attr WHERE class_id = (select id from cs_class where name like 'test6');
DELETE FROM cs_attr where class_id = (select id from cs_class where name like 'test1');
DELETE FROM cs_attr where class_id = (select id from cs_class where name like 'test2');
DELETE FROM cs_attr where class_id = (select id from cs_class where name like 'test3');
DELETE FROM cs_attr where class_id = (select id from cs_class where name like 'test4');
DELETE FROM cs_attr where class_id = (select id from cs_class where name like 'test5');
DELETE FROM cs_attr where class_id = (select id from cs_class where name like 'test6');
DELETE FROM cs_class WHERE name LIKE 'test1';
DELETE FROM cs_class WHERE name LIKE 'test2';
DELETE FROM cs_class WHERE name LIKE 'test3';
DELETE FROM cs_class WHERE name LIKE 'test4';
DELETE FROM cs_class WHERE name LIKE 'test5';
DELETE FROM cs_class WHERE name LIKE 'test6';

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

INSERT INTO cs_class(name, class_icon_id, object_icon_id, table_name, primary_key_field, indexed, array_link)         VALUES ('test1', 1, 1, 'test1', 'ID', false, false);
INSERT INTO cs_class(name, class_icon_id, object_icon_id, table_name, primary_key_field, indexed, array_link)         VALUES ('test2', 1, 1, 'test2', 'ID', false, false);
INSERT INTO cs_class(name, class_icon_id, object_icon_id, table_name, primary_key_field, indexed, array_link, policy) VALUES ('test3', 1, 1, 'test3', 'ID', false, false, (select id from cs_policy where name like 'SECURE'));
INSERT INTO cs_class(name, class_icon_id, object_icon_id, table_name, primary_key_field, indexed, array_link)         VALUES ('test4', 1, 1, 'test4', 'ID', false, false);
INSERT INTO cs_class(name, class_icon_id, object_icon_id, table_name, primary_key_field, indexed, array_link)         VALUES ('test5', 1, 1, 'test5', 'ID', false, false);
INSERT INTO cs_class(name, class_icon_id, object_icon_id, table_name, primary_key_field, indexed, array_link, policy) VALUES ('test6', 1, 1, 'test6', 'ID', false, false, (select id from cs_policy where name like 'WIKI'));

INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test1'), (select id from cs_type where name ilike 'integer'), 'id', 'id', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test1'), (select id from cs_type where name ilike 'varchar'), 'name', 'name', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test2'), (select id from cs_type where name ilike 'integer'), 'id', 'id', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test2'), (select id from cs_type where name ilike 'varchar'), 'name', 'name', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test3'), (select id from cs_type where name ilike 'integer'), 'id', 'id', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test3'), (select id from cs_type where name ilike 'varchar'), 'name', 'name', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test4'), (select id from cs_type where name ilike 'integer'), 'id', 'id', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test4'), (select id from cs_type where name ilike 'varchar'), 'name', 'name', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test5'), (select id from cs_type where name ilike 'integer'), 'id', 'id', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test5'), (select id from cs_type where name ilike 'varchar'), 'name', 'name', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test6'), (select id from cs_type where name ilike 'integer'), 'id', 'id', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test6'), (select id from cs_type where name ilike 'varchar'), 'name', 'name', false, false, false, false, false, false,  false);

INSERT INTO cs_class_attr(class_id, type_id, attr_key, attr_value) VALUES ((select id from cs_class where name like 'test2'), (select id from cs_type where name ilike 'varchar'), 'history_enabled', null);
INSERT INTO cs_class_attr(class_id, type_id, attr_key, attr_value) VALUES ((select id from cs_class where name like 'test3'), (select id from cs_type where name ilike 'varchar'), 'history_enabled', 'anonymous=true');
INSERT INTO cs_class_attr(class_id, type_id, attr_key, attr_value) VALUES ((select id from cs_class where name like 'test4'), (select id from cs_type where name ilike 'varchar'), 'history_enabled', '');
INSERT INTO cs_class_attr(class_id, type_id, attr_key, attr_value) VALUES ((select id from cs_class where name like 'test6'), (select id from cs_type where name ilike 'varchar'), 'history_enabled', 'anonymous=true');

CREATE SEQUENCE test1_seq MINVALUE 1 START 1;
CREATE TABLE test1( id INTEGER PRIMARY KEY DEFAULT nextval('test1_seq'), name VARCHAR NOT NULL );
CREATE SEQUENCE test2_seq MINVALUE 1 START 1;
CREATE TABLE test2( id INTEGER PRIMARY KEY DEFAULT nextval('test2_seq'), name VARCHAR NOT NULL );
CREATE SEQUENCE test3_seq MINVALUE 1 START 1;
CREATE TABLE test3( name VARCHAR NOT NULL, id INTEGER PRIMARY KEY DEFAULT nextval('test3_seq') );
CREATE SEQUENCE test4_seq MINVALUE 1 START 1;
CREATE TABLE test4( name VARCHAR NOT NULL, id INTEGER PRIMARY KEY DEFAULT nextval('test4_seq') );
CREATE SEQUENCE test5_seq MINVALUE 1 START 1;
CREATE TABLE test5( name VARCHAR NOT NULL, id INTEGER PRIMARY KEY DEFAULT nextval('test5_seq') );
CREATE SEQUENCE test6_seq MINVALUE 1 START 1;
CREATE TABLE test6( name VARCHAR NOT NULL, id INTEGER PRIMARY KEY DEFAULT nextval('test6_seq') );

INSERT INTO test1 (name) VALUES ('newTest1');
INSERT INTO test2 (name) VALUES ('newTest2');
INSERT INTO test3 (name) VALUES ('newTest3');
INSERT INTO test4 (name) VALUES ('newTest4');
INSERT INTO test5 (name) VALUES ('newTest5');
INSERT INTO test6 (name) VALUES ('newTest6');

INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES ((select id from cs_class where name like 'test1'), 1, null, null, '2010-12-04 15:12:49.553', 'test11');
INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES ((select id from cs_class where name like 'test2'), 1, null, null, '2010-12-04 15:13:46.023', 'test21');
INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES ((select id from cs_class where name like 'test2'), 1, null, null, '2010-12-04 15:13:46.024', 'test22');
INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES ((select id from cs_class where name like 'test2'), 1, null, null, '2010-12-04 15:13:46.025', 'test22');
INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES ((select id from cs_class where name like 'test3'), 1, null, null, '2010-12-04 15:14:27.873', 'test31');
INSERT INTO cs_history (class_id, object_id, usr_id, ug_id, valid_from, json_data) VALUES ((select id from cs_class where name like 'test6'), 1, null, null, '2010-12-04 15:14:27.873', 'test61');
