/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ClassCache {

    //~ Instance fields --------------------------------------------------------

    /** contains all cached objects. */
    protected ClassMap classes;
    protected HashMap<String, Sirius.server.localserver._class.Class> classesByTableName;
    protected HashMap classAttribs;
    protected PolicyHolder policyHolder;
    /**
     * conatains this local servers class and object icons load with the method loadIcons which is called in the
     * constructor.
     */
    protected IntMapsImage icons = new IntMapsImage(20, 0.7f);
    protected ServerProperties properties;

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * -----------------------------------
     *
     * @param   conPool       DOCUMENT ME!
     * @param   properties    DOCUMENT ME!
     * @param   policyHolder  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public ClassCache(DBConnectionPool conPool, ServerProperties properties, PolicyHolder policyHolder)
        throws Throwable {
        this.properties = properties;

        this.policyHolder = policyHolder;

        loadIcons(conPool);

        classes = new ClassMap(20); // allocation of the hashtable

        classesByTableName = new HashMap<String, Sirius.server.localserver._class.Class>();

        classAttribs = new HashMap(200);

        DBConnection con = conPool.getConnection();
        try {
            ResultSet classTable = con.submitQuery("get_all_classes", new Object[0]);   // NOI18N // getAllClasses

            if (classTable == null) {
                logger.error(
                    "<LS> ERROR :: Fatal Error: classes could not be loaded. Program exits");   // NOI18N
                throw new ServerExitError(
                    "Fatal Error: classes could not be loaded. Program exits");   // NOI18N
            }

            while (classTable.next()) // add all objects to the hashtable
            {
                Image object_i = null;
                Image class_i = null;

                try {
                    object_i = icons.getImageValue(classTable.getInt("object_icon_id"));   // NOI18N
                } catch (Exception e) {
                    logger.error("<LS> ERROR ::  !!Setting objectIcon  to default!!", e);   // NOI18N

                    object_i = new Image();

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }

                try {
                    class_i = icons.getImageValue(classTable.getInt("class_icon_id"));   // NOI18N
                } catch (Exception e) {
                    logger.error("<LS> ERROR :: !!Setting classIcon to default!!!!", e);   // NOI18N

                    class_i = new Image();

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }

                String toStringQualifier = classTable.getString("tostringqualifier");   // NOI18N
                String className = classTable.getString("name").trim();   // NOI18N
                Object policyTester = classTable.getObject("policy");   // NOI18N
                Policy policy = null;
                if (policyTester == null) {
                    policy = policyHolder.getServerPolicy(properties.getServerPolicy());
                } else {
                    int policyId = classTable.getInt("policy");   // NOI18N
                    policy = policyHolder.getServerPolicy(policyId);
                }

                Object attrPolicyTester = classTable.getObject("attribute_policy");   // NOI18N
                Policy attributePolicy = null;
                if (attrPolicyTester == null) {
                    attributePolicy = policyHolder.getServerPolicy(properties.getAttributePolicy());
                } else {
                    int policyId = classTable.getInt("attribute_policy");   // NOI18N
                    attributePolicy = policyHolder.getServerPolicy(policyId);
                }
                if (attributePolicy == null) {
                    attributePolicy = policyHolder.getServerPolicy(properties.getServerPolicy());
                }

                Class tmp = new Class(
                        classTable.getInt("id"),   // NOI18N
                        className,
                        classTable.getString("descr"),   // NOI18N
                        class_i,
                        object_i,
                        classTable.getString("table_name"),   // NOI18N
                        classTable.getString("primary_key_field"),   // NOI18N
                        toStringQualifier,
                        policy,
                        attributePolicy);
                if (logger.isDebugEnabled()) {
                    logger.debug("to string for Class :" + className + " :: " + toStringQualifier);   // NOI18N
                }
                // Hell
                tmp.setEditor(classTable.getString("editorqualifier"));   // NOI18N
                tmp.setRenderer(classTable.getString("RendererQualifier"));   // NOI18N
                try {
                    boolean arrayElementLink = classTable.getBoolean("array_link");   // NOI18N
                    tmp.setArrayElementLink(arrayElementLink);
                    if (logger.isDebugEnabled()) {
                        logger.debug("isArrayElementLink set to :" + arrayElementLink);   // NOI18N
                    }
                } catch (Exception e) {
                    logger.error("Error at arrayElementLink probably old DB version", e);   // NOI18N
                }

                classes.add(tmp.getID(), tmp);
                classesByTableName.put(tmp.getTableName().toLowerCase(), tmp);
            } // end while

            classTable.close();
            classes.rehash();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
        }

        addClassPermissions(conPool);

        addAttributes(conPool);

        addMethodIDs(conPool);

        addMemberInfos(conPool);

        // addClassSearchMasks(conPool);
    } // end of constructor

    //~ Methods ----------------------------------------------------------------

    /**
     * -----------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public final int size() {
        return classes.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public final Class getClass(int id) throws Exception {
        return classes.getClass(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public final Class getClassByTableName(String tableName) throws Exception {
        return classesByTableName.get(tableName);
    }
    /**
     * -----------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public final Vector getAllClasses() {
        return classes.getAll();
    }
    /**
     * -----------------------------------------------------------------------------
     *
     * @param   ug  DOCUMENT ME!
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public final Class getClass(UserGroup ug, int id) throws Exception {
        Class c = classes.getClass(id);

        if ((c != null) && c.getPermissions().hasPermission(ug.getKey(), PermissionHolder.READPERMISSION)) {
            return c;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug         DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public final Class getClassNyTableName(UserGroup ug, String tableName) throws Exception {
        Class c = getClassByTableName(tableName);

        if ((c != null) && c.getPermissions().hasPermission(ug.getKey(), PermissionHolder.READPERMISSION)) {
            return c;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Hashtable getClassHashMap() {
        return classes;
    }
    /**
     * -----------------------------------------------------------------------------
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public final Vector getAllClasses(UserGroup ug) throws Exception {
        Vector all = getAllClasses();
        Vector cs = new Vector(all.size());

        for (int i = 0; i < all.size(); i++) {
            Class c = (Class)all.get(i);
            if (c.getPermissions().hasReadPermission(ug)) {
                cs.addElement(c);
            }
        }

        cs.trimToSize();
        return cs;
    }

    // -----------------------------------------------------------------------------
    /**
     * Only to be called by the constructor.
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addAttributes(DBConnectionPool conPool) {
        DBConnection con = conPool.getConnection();
        try {
            ResultSet attribTable = con.submitQuery("get_all_class_attributes", new Object[0]);   // NOI18N

            int id = 0;
            int classID = 0;

            int typeID = 0;

            while (attribTable.next()) {
                String name = null;
                java.lang.Object value = null;
                ClassAttribute attrib = null;

                id = attribTable.getInt("id");   // NOI18N
                classID = attribTable.getInt("class_id");   // NOI18N
                name = attribTable.getString("attr_key");   // NOI18N
                typeID = attribTable.getInt("type_id");   // NOI18N
                value = attribTable.getString("attr_value");   // NOI18N

                attrib = new ClassAttribute(id + "", classID, name, typeID, classes.getClass(classID).getPolicy());   // NOI18N 
                attrib.setValue(value);
                classes.getClass(attrib.getClassID()).addAttribute(attrib);
                classAttribs.put(new Integer(attrib.getID()), attrib);
            }

            attribTable.close();

            // addAttributePermissions(conPool);
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: classcache  get_all_class_attributes", e);   // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addMemberInfos(DBConnectionPool conPool) {
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
                sql.replaceAll("\\?", "1=2");   // NOI18N
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
            // logger.fatal(classfieldtypes);

            ResultSet rs = con.submitQuery("get_attribute_info", new Object[0]);   // NOI18N

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
                String toString; // toString
                String defaultValue;
                String fromString;
                // String renderer;

                // Hell
                String complexEditor;
                boolean extensionAttribute = false;

                id = rs.getInt("id");   // NOI18N
                name = rs.getString("name");   // NOI18N
                classId = rs.getInt("class_id");   // NOI18N
                typeId = rs.getInt("type_id");   // NOI18N
                position = rs.getInt("pos");   // NOI18N

                fieldName = rs.getString("field_name");   // NOI18N
                if (fieldName != null) {
                    fieldName = fieldName.trim();
                }

                arrayKey = rs.getString("array_key");   // NOI18N
                if (arrayKey != null) {
                    arrayKey = arrayKey.trim();
                }

                toString = rs.getString("toStringString");   // NOI18N
                if (toString != null) {
                    toString = toString.trim();
                }

                // editor xxx
                editor = rs.getString("editor_class");   // NOI18N
                if (editor != null) {
                    editor = editor.trim();
                }

                complexEditor = rs.getString("complexeditorclass");   // NOI18N
                if (complexEditor != null) {
                    complexEditor = complexEditor.trim();
                }

//                 renderer=rs.getString("renderer");
//                if(renderer!=null)
//                    renderer=renderer.trim();

                defaultValue = rs.getString("default_value");   // NOI18N
                if (defaultValue != null) {
                    defaultValue = defaultValue.trim();
                }

                fromString = rs.getString("from_string_class");   // NOI18N
                if (fromString != null) {
                    fromString = fromString.trim();
                }

                foreignKey = rs.getBoolean("foreign_key");   // NOI18N

                substitute = rs.getBoolean("substitute");   // NOI18N

                visible = rs.getBoolean("visible");   // NOI18N

                optional = rs.getBoolean("optional");   // NOI18N

                // permission = DBConnection.stringToBool(rs.getString("permission"));
                indexed = rs.getBoolean("indexed");   // NOI18N

                foreignKeyClassId = rs.getInt("foreign_key_references_to");   // NOI18N

                array = rs.getBoolean("isarray");   // NOI18N

                try {
                    extensionAttribute = rs.getBoolean("extension_attr");   // NOI18N
                } catch (Exception skip) {
                }

                // xxx array stuff to be added
                mai = new MemberAttributeInfo(
                        id,
                        classId,
                        typeId,
                        name,
                        fieldName,
                        foreignKey,
                        substitute,
                        foreignKeyClassId,
                        visible,
                        indexed,
                        array,
                        arrayKey,
                        fromString,
                        toString,
                        position);

                mai.setOptional(optional);

                mai.setEditor(editor);
                mai.setComplexEditor(complexEditor);

                mai.setJavaclassname(classfieldtypes.get(classId).get(fieldName.toLowerCase()));
                if (
                    (mai.getJavaclassname() != null)
                            && mai.getJavaclassname().equals(org.postgis.PGgeometry.class.getName())) {
                    mai.setJavaclassname(com.vividsolutions.jts.geom.Geometry.class.getName());
                }
                mai.setExtensionAttribute(extensionAttribute);
                if (mai.isExtensionAttribute()) {
                    mai.setJavaclassname(java.lang.Object.class.getCanonicalName());
                }
                // mai.setRenderer(renderer);

                // int cId =rs.getInt("class_id");

                Sirius.server.localserver._class.Class c = classes.getClass(classId);
                // classes.getClass(cId);

                if (c != null) {
                    c.addMemberAttributeInfo(mai);
                } else {
                    logger.warn("Wrong addMemberInfos entry for class::" + classId);   // NOI18N
                }
            }

            rs.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: addMemberinfos", e);   // NOI18N
        }
    }

    /**
     * Only to be called by the constructor.
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addMethodIDs(DBConnectionPool conPool) {
        DBConnection con = conPool.getConnection();
        try {
            ResultSet methodTable = con.submitQuery("get_all_class_method_ids", new Object[0]);   // NOI18N

            while (methodTable.next()) {
                int classId = methodTable.getInt("class_id");   // NOI18N
                Sirius.server.localserver._class.Class c = classes.getClass(classId);

                int methodId = methodTable.getInt("method_id");   // NOI18N

                if (c != null) {
                    c.addMethodID(methodId);
                } else {
                    logger.warn(
                        "Wrong entry in classes/methods table: Class" + classId + " Method :"   // NOI18N
                        + methodId);
                }
            }

            methodTable.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: get_all_class_method_ids", e);   // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conPool  DOCUMENT ME!
     */
    protected final void loadIcons(DBConnectionPool conPool) {
        String iconDirectory;
        String separator;
        Image tmpImage;

        try {
            iconDirectory = properties.getIconDirectory();
        } catch (Exception e) {
            logger.error(
                "<LS> ERROR ::  Keyvalue ICONDIRECTORY in ConfigFile is missing\n<LS> ERROR ::  set ICONDIRECTORY to . ",   // NOI18N
                e);
            iconDirectory = ".";   // NOI18N
        }
        try {
            // separator=properties.getFileseparator();
            separator = System.getProperty("file.separator");   // NOI18N
        } catch (Exception e) {
            logger.error(
                "<LS> ERROR ::  KeyValue SEPARATOR in ConfigFile is missing\n<LS> ERROR ::  set DEFAULTSEPARATOR = \\",   // NOI18N
                e);
            separator = "\\";   // NOI18N
        }

        DBConnection con = conPool.getConnection();
        try {
            ResultSet imgTable = con.submitQuery("get_all_images", new Object[0]);   // NOI18N

            while (imgTable.next()) {
                // icons.add(imgTable.getInt("id"),new
                // Image(properties.getIconDirectory()+"\\"+imgTable.getString("data").trim()));
                tmpImage = new Image(iconDirectory + separator + imgTable.getString("file_name").trim());   // NOI18N
                icons.add(imgTable.getInt("id"), tmpImage);   // NOI18N
            }

            imgTable.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: get_all_icons", e);   // NOI18N
        }
    }
    /**
     * --------------------------------------------------------------------------
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addClassPermissions(DBConnectionPool conPool) {
        DBConnection con = conPool.getConnection();
        try {
            ResultSet permTable = con.submitQuery("get_all_class_permissions", new Object[0]);   // NOI18N

            String lsName = properties.getServerName();

            while (permTable.next()) {
                int ug_id = permTable.getInt("ug_id");   // NOI18N
                String ug_name = permTable.getString("ug_name");   // NOI18N
                String lsHome = permTable.getString("domainname").trim();   // NOI18N
                int permId = permTable.getInt("permission");   // NOI18N
                String permKey = permTable.getString("key");   // NOI18N
                // String permPolicy = permTable.getString("policy");

                if (lsHome.equalsIgnoreCase("local")) {   // NOI18N
                    lsHome = new String(lsName);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("==permId set ======! " + permId);   // NOI18N
                }

                classes.getClass(permTable.getInt("class_id")).getPermissions()   // NOI18N
                        .addPermission(new UserGroup(ug_id, ug_name, lsHome), new Permission(permId, permKey));
            }

            permTable.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: addClassPermissions", e);   // NOI18N 
        }
    }
    /**
     * -----------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public ServerProperties getProperties() {
        return properties;
    }
} // end of class
