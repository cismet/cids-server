insert into cs_cat_node (name) values ('test1');
insert into cs_cat_node (name) values ('test2');
insert into cs_cat_node (name) values ('test3');


INSERT INTO cs_class(name, class_icon_id, object_icon_id, table_name, primary_key_field, indexed, array_link, policy) VALUES ('test7', 1, 1, 'test7', 'ID', false, false, (select id from cs_policy where name like 'WIKI'));
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test7'), (select id from cs_type where name ilike 'integer'), 'id', 'id', false, false, false, false, false, false,  false);
INSERT INTO cs_attr(class_id, type_id, name, field_name, foreign_key, substitute, visible, indexed, isarray, optional, extension_attr) VALUES ((select id from cs_class where name like 'test7'), (select id from cs_type where name ilike 'varchar'), 'name', 'name', false, false, false, false, false, false,  false);
CREATE SEQUENCE test7_seq MINVALUE 1 START 1;
CREATE TABLE test7( name VARCHAR NOT NULL, id INTEGER PRIMARY KEY DEFAULT nextval('test7_seq') );
INSERT INTO test7 (name) VALUES ('newTest7');