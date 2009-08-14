package Sirius.server.localserver._class;

import java.sql.*;
import Sirius.server.sql.*;
import Sirius.util.*;
import Sirius.server.localserver.attribute.*;
import Sirius.util.image.*;
import java.util.*;
import Sirius.server.newuser.*;
import Sirius.server.newuser.permission.*;
import Sirius.server.*;
import Sirius.server.property.*;

public class ClassCache {

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /** contains all cached objects */
    protected ClassMap classes;
    protected HashMap<String, Sirius.server.localserver._class.Class> classesByTableName;
    protected HashMap classAttribs;
    protected PolicyHolder policyHolder;
    /** conatains this local servers class and object icons load with the method loadIcons which is called in the constructor*/
    protected IntMapsImage icons = new IntMapsImage(20, 0.7f);
    protected ServerProperties properties;

    //-----------------------------------
    public ClassCache(DBConnectionPool conPool, ServerProperties properties, PolicyHolder policyHolder) throws Throwable {
        this.properties = properties;

        this.policyHolder = policyHolder;

        loadIcons(conPool);

        classes = new ClassMap(20);  // allocation of the hashtable


        classesByTableName = new HashMap<String, Sirius.server.localserver._class.Class>();

        classAttribs = new HashMap(200);

        DBConnection con = conPool.getConnection();
        try {
            ResultSet classTable = con.submitQuery("get_all_classes", new Object[0]); // getAllClasses

            if (classTable == null) {

                logger.error("<LS> ERROR :: Klassen konten nicht geladen werden fataler Fehler das Programm beendet sich");
                throw new ServerExitError("Klassen konten nicht geladen werden fataler Fehler das Programm beendet sich");

            }

            while (classTable.next())//add all objects to the hashtable
            {


                Image object_i = null;
                Image class_i = null;

                try {

                    object_i = icons.getImageValue(classTable.getInt("object_icon_id"));

                } catch (Exception e) {

                    logger.error("<LS> ERROR ::  !!Setting objectIcon  to default!!", e);

                    object_i = new Image();

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }

                try {

                    class_i = icons.getImageValue(classTable.getInt("class_icon_id"));


                } catch (Exception e) {
                    logger.error("<LS> ERROR :: !!Setting classIcon to default!!!!", e);

                    class_i = new Image();

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }

                String toStringQualifier = classTable.getString("tostringqualifier");
                String className = classTable.getString("name").trim();
                Object policyTester = classTable.getObject("policy");
                Policy policy = null;
                if (policyTester == null) {
                    policy = policyHolder.getServerPolicy(properties.getServerPolicy());
                } else {
                    int policyId = classTable.getInt("policy");
                    policy = policyHolder.getServerPolicy(policyId);
                }


                Object attrPolicyTester = classTable.getObject("attribute_policy");
                Policy attributePolicy = null;
                if (attrPolicyTester == null) {
                    attributePolicy = policyHolder.getServerPolicy(properties.getAttributePolicy());
                } else {
                    int policyId = classTable.getInt("attribute_policy");
                    attributePolicy = policyHolder.getServerPolicy(policyId);
                }
                if (attributePolicy == null) {
                    attributePolicy = policyHolder.getServerPolicy(properties.getServerPolicy());
                }

                Class tmp = new Class(classTable.getInt("id"), className, classTable.getString("descr"), class_i, object_i, classTable.getString("table_name"), classTable.getString("primary_key_field"), toStringQualifier, policy, attributePolicy);

                logger.debug("to string for Class :" + className + " :: " + toStringQualifier);
                //Hell
                tmp.setEditor(classTable.getString("editorqualifier"));
                tmp.setRenderer(classTable.getString("RendererQualifier"));
                try {
                    boolean arrayElementLink = classTable.getBoolean("array_link");
                    tmp.setArrayElementLink(arrayElementLink);
                    logger.debug("isArrayElementLink set to :" + arrayElementLink);
                } catch (Exception e) {
                    logger.error("Fehler bei arrayElementLink wahrscheinlich alte DB Version", e);
                }


                classes.add(tmp.getID(), tmp);
                classesByTableName.put(tmp.getTableName().toLowerCase(), tmp);


            }// end while

            classTable.close();
            classes.rehash();

        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);

        }

        addClassPermissions(conPool);

        addAttributes(conPool);

        addMethodIDs(conPool);

        addMemberInfos(conPool);

    //addClassSearchMasks(conPool);





    }// end of constructor

    //-----------------------------------------------------------------------------
    public final int size() {
        return classes.size();
    }

    final public Class getClass(int id) throws Exception {
        return classes.getClass(id);
    }

    final public Class getClassByTableName(String tableName) throws Exception {
        return classesByTableName.get(tableName);
    }

    //-----------------------------------------------------------------------------
    final public Vector getAllClasses() {
        return classes.getAll();
    }

    //-----------------------------------------------------------------------------
    final public Class getClass(UserGroup ug, int id) throws Exception {
        Class c = classes.getClass(id);

        if (c != null && c.getPermissions().hasPermission(ug.getKey(), PermissionHolder.READPERMISSION)) {
            return c;
        }


        return null;

    }

    final public Class getClassNyTableName(UserGroup ug,String tableName) throws Exception {
        Class c = getClassByTableName(tableName);

        if (c != null && c.getPermissions().hasPermission(ug.getKey(), PermissionHolder.READPERMISSION)) {
            return c;
        }


        return null;

    }

    public Hashtable getClassHashMap() {
        return classes;
    }

    //-----------------------------------------------------------------------------
    final public Vector getAllClasses(UserGroup ug) throws Exception {
        Vector all = getAllClasses();
        Vector cs = new Vector(all.size());

        for (int i = 0; i < all.size(); i++) {
            Class c = (Class) all.get(i);
            if (c.getPermissions().hasReadPermission(ug)) {
                cs.addElement(c);
            }
        }

        cs.trimToSize();
        return cs;

    }

    //-----------------------------------------------------------------------------
    /**Only to be called by the constructor*/
    final private void addAttributes(DBConnectionPool conPool) {
        DBConnection con = conPool.getConnection();
        try {

            ResultSet attribTable = con.submitQuery("get_all_class_attributes", new Object[0]);




            int id = 0;
            int classID = 0;


            int typeID = 0;


            while (attribTable.next()) {

                String name = null;
                java.lang.Object value = null;
                ClassAttribute attrib = null;

                id = attribTable.getInt("id");
                classID = attribTable.getInt("class_id");
                name = attribTable.getString("attr_key");
                typeID = attribTable.getInt("type_id");
                value = attribTable.getString("attr_value");



                attrib = new ClassAttribute(id + "", classID, name, typeID, classes.getClass(classID).getPolicy());
                attrib.setValue(value);
                classes.getClass(attrib.getClassID()).addAttribute(attrib);
                classAttribs.put(new Integer(attrib.getID()), attrib);
            }

            attribTable.close();

        //  addAttributePermissions(conPool);


        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: classcache  get_all_class_attributes", e);

        }

    }

    final private void addMemberInfos(DBConnectionPool conPool) {

        DBConnection con = conPool.getConnection();

        HashMap<Integer, HashMap<String, String>> classfieldtypes = new HashMap<Integer, HashMap<String, String>>();

        Vector<Sirius.server.localserver._class.Class> vc = getAllClasses();

        try {

            for (Sirius.server.localserver._class.Class c : vc) {
                HashMap<String, String> fieldtypes = classfieldtypes.get(c.getID());
                if (fieldtypes == null) {
                    fieldtypes = new HashMap<String, String>();
                    classfieldtypes.put(c.getID(), fieldtypes);
                }
                Statement s = con.getConnection().createStatement();
                String sql = c.getGetDefaultInstanceStmnt();
                sql.replaceAll("\\?", "1=2");
                ResultSet resultset = s.executeQuery(sql);
                ResultSetMetaData rsmd = resultset.getMetaData();

                for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                    String fieldname = rsmd.getColumnName(i);
                    String javaclassname = rsmd.getColumnClassName(i);
                    fieldtypes.put(fieldname.toLowerCase(), javaclassname);
                }
                resultset.close();
                s.close();
            }
            //logger.fatal(classfieldtypes);


            ResultSet rs = con.submitQuery("get_attribute_info", new Object[0]);

            MemberAttributeInfo mai = null;

            // konstruktor paramters
            int id;
            int classId;
            int typeId;

            boolean foreignKey;
            boolean substitute;
            int foreignKeyClassId;

            boolean visible = true;
            // boolean permission;
            boolean indexed = false;

            boolean array = false;

            boolean optional = false;

            int position = 0;





            while (rs.next()) {
                String name;
                String fieldName;
                String arrayKey;
                String editor;
                String toString;// toString
                String defaultValue;
                String fromString;
                //  String renderer;

                //Hell
                String complexEditor;


                id = rs.getInt("id");
                name = rs.getString("name");
                classId = rs.getInt("class_id");
                typeId = rs.getInt("type_id");
                position = rs.getInt("pos");

                fieldName = rs.getString("field_name");
                if (fieldName != null) {
                    fieldName = fieldName.trim();
                }

                arrayKey = rs.getString("array_key");
                if (arrayKey != null) {
                    arrayKey = arrayKey.trim();
                }

                toString = rs.getString("toStringString");
                if (toString != null) {
                    toString = toString.trim();
                }

                // editor xxx
                editor = rs.getString("editor_class");
                if (editor != null) {
                    editor = editor.trim();
                }

                complexEditor = rs.getString("complexeditorclass");
                if (complexEditor != null) {
                    complexEditor = complexEditor.trim();
                }

//                 renderer=rs.getString("renderer");
//                if(renderer!=null)
//                    renderer=renderer.trim();


                defaultValue = rs.getString("default_value");
                if (defaultValue != null) {
                    defaultValue = defaultValue.trim();
                }

                fromString = rs.getString("from_string_class");
                if (fromString != null) {
                    fromString = fromString.trim();
                }



                foreignKey = rs.getBoolean("foreign_key");

                substitute = rs.getBoolean("substitute");

                visible = rs.getBoolean("visible");

                optional = rs.getBoolean("optional");

                // permission = DBConnection.stringToBool(rs.getString("permission"));
                indexed = rs.getBoolean("indexed");

                foreignKeyClassId = rs.getInt("foreign_key_references_to");

                array = rs.getBoolean("isarray");




                //xxx array stuff to be added
                mai = new MemberAttributeInfo(id, classId, typeId, name, fieldName, foreignKey, substitute, foreignKeyClassId, visible, indexed, array, arrayKey, fromString, toString, position);

                mai.setOptional(optional);

                mai.setEditor(editor);
                mai.setComplexEditor(complexEditor);

                mai.setJavaclassname(classfieldtypes.get(classId).get(fieldName.toLowerCase()));
                if (mai.getJavaclassname()!=null && mai.getJavaclassname().equals(org.postgis.PGgeometry.class.getName())){
                    mai.setJavaclassname(com.vividsolutions.jts.geom.Geometry.class.getName());
                }

                //mai.setRenderer(renderer);

                //  int cId =rs.getInt("class_id");

                Sirius.server.localserver._class.Class c = classes.getClass(classId);
                // classes.getClass(cId);





                if (c != null) {
                    c.addMemberAttributeInfo(mai);
                } else {
                    logger.warn("Falscher Eintrag f\u00FCr addMemberInfos f\u00FCr Klasse::" + classId);
                }


            }

            rs.close();


        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: addMemberinfos", e);

        }



    }

    /**Only to be called by the constructor*/
    final private void addMethodIDs(DBConnectionPool conPool) {
        DBConnection con = conPool.getConnection();
        try {

            ResultSet methodTable = con.submitQuery("get_all_class_method_ids", new Object[0]);

            while (methodTable.next()) {

                int classId = methodTable.getInt("class_id");
                Sirius.server.localserver._class.Class c =
                        classes.getClass(classId);

                int methodId = methodTable.getInt("method_id");

                if (c != null) {
                    c.addMethodID(methodId);
                } else {
                    logger.warn("Eintrag in der Klassen/Methoden tabelle fehlerhaft Klasse" + classId + " Methode :" + methodId);
                }
            }

            methodTable.close();


        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: get_all_class_method_ids", e);

        }

    }

    final protected void loadIcons(DBConnectionPool conPool) {
        String iconDirectory, separator;
        Image tmpImage;


        try {
            iconDirectory = properties.getIconDirectory();
        } catch (Exception e) {
            logger.error("<LS> ERROR ::  Keyvalue ICONDIRECTORY in ConfigFile is missing\n<LS> ERROR ::  set ICONDIRECTORY to . ", e);
            iconDirectory = ".";
        }
        try {
            //separator=properties.getFileseparator();
            separator = System.getProperty("file.separator");
        } catch (Exception e) {
            logger.error("<LS> ERROR ::  KeyValue SEPARATOR in ConfigFile is missing\n<LS> ERROR ::  set DEFAULTSEPARATOR = \\", e);
            separator = "\\";
        }

        DBConnection con = conPool.getConnection();
        try {

            ResultSet imgTable = con.submitQuery("get_all_images", new Object[0]);


            while (imgTable.next()) {
                //icons.add(imgTable.getInt("id"),new Image(properties.getIconDirectory()+"\\"+imgTable.getString("data").trim()));
                tmpImage = new Image(iconDirectory + separator + imgTable.getString("file_name").trim());
                icons.add(imgTable.getInt("id"), tmpImage);
            }



            imgTable.close();


        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: get_all_icons", e);

        }


    }

    //--------------------------------------------------------------------------
    final private void addClassPermissions(DBConnectionPool conPool) {
        DBConnection con = conPool.getConnection();
        try {

            ResultSet permTable = con.submitQuery("get_all_class_permissions", new Object[0]);

            String lsName = properties.getServerName();

            while (permTable.next()) {
                int ug_id = permTable.getInt("ug_id");
                String ug_name = permTable.getString("ug_name");
                String lsHome = permTable.getString("domainname").trim();
                int permId = permTable.getInt("permission");
                String permKey = permTable.getString("key");
                //String permPolicy = permTable.getString("policy");

                if (lsHome.equalsIgnoreCase("local")) {
                    lsHome = new String(lsName);
                }

                logger.debug("==permId set ======! " + permId);

                classes.getClass(permTable.getInt("class_id")).getPermissions().addPermission(new UserGroup(ug_id, ug_name, lsHome), new Permission(permId, permKey));
            }

            permTable.close();


        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: addClassPermissions", e);

        }


    }

    //-----------------------------------------------------------------------------
    public ServerProperties getProperties() {
        return properties;
    }
}// end of class
