/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver._class;

import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.Permission;
import Sirius.server.newuser.permission.Policy;
import Sirius.server.newuser.permission.PolicyHolder;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.ExceptionHandler;
import Sirius.server.sql.SQLTools;

import Sirius.util.image.Image;
import Sirius.util.image.IntMapsImage;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.cismet.cids.utils.ErrorUtils;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ClassCache extends Shutdown {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ClassCache.class);

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
    public ClassCache(final DBConnectionPool conPool,
            final ServerProperties properties,
            final PolicyHolder policyHolder) throws Throwable {
        this.properties = properties;

        this.policyHolder = policyHolder;

        loadIcons(conPool);

        classes = new ClassMap(20); // allocation of the hashtable

        classesByTableName = new HashMap<String, Sirius.server.localserver._class.Class>();

        classAttribs = new HashMap(200);

        final DBConnection con = conPool.getDBConnection();
        try {
            final ResultSet classTable = con.submitInternalQuery(DBConnection.DESC_GET_ALL_CLASSES, new Object[0]); // getAllClasses //NOI18N

            if (classTable == null) {
                LOG.error(
                    "<LS> ERROR :: Fatal Error: classes could not be loaded. Program exits"); // NOI18N
                throw new ServerExitError(
                    "Fatal Error: classes could not be loaded. Program exits");               // NOI18N
            }

            while (classTable.next()) // add all objects to the hashtable
            {
                Image object_i = null;
                Image class_i = null;

                try {
                    object_i = icons.getImageValue(classTable.getInt("object_icon_id")); // NOI18N
                } catch (Exception e) {
                    LOG.error("<LS> ERROR ::  !!Setting objectIcon  to default!!", e);   // NOI18N

                    object_i = new Image();

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }

                try {
                    class_i = icons.getImageValue(classTable.getInt("class_icon_id")); // NOI18N
                } catch (Exception e) {
                    LOG.error("<LS> ERROR :: !!Setting classIcon to default!!!!", e);  // NOI18N

                    class_i = new Image();

                    if (e instanceof java.sql.SQLException) {
                        throw e;
                    }
                }

                final String toStringQualifier = classTable.getString("tostringqualifier"); // NOI18N
                final String className = classTable.getString("name").trim();               // NOI18N
                final Object policyTester = classTable.getObject("policy");                 // NOI18N
                final boolean indexed = classTable.getBoolean("indexed");

                Policy policy = null;
                if (policyTester == null) {
                    policy = policyHolder.getServerPolicy(properties.getServerPolicy());
                } else {
                    final int policyId = classTable.getInt("policy"); // NOI18N
                    policy = policyHolder.getServerPolicy(policyId);
                }

                final Object attrPolicyTester = classTable.getObject("attribute_policy"); // NOI18N
                Policy attributePolicy = null;
                if (attrPolicyTester == null) {
                    attributePolicy = policyHolder.getServerPolicy(properties.getAttributePolicy());
                } else {
                    final int policyId = classTable.getInt("attribute_policy");           // NOI18N
                    attributePolicy = policyHolder.getServerPolicy(policyId);
                }
                if (attributePolicy == null) {
                    attributePolicy = policyHolder.getServerPolicy(properties.getServerPolicy());
                }

                final Class tmp = new Class(
                        classTable.getInt("id"),                               // NOI18N
                        className,
                        classTable.getString("descr"),                         // NOI18N
                        class_i,
                        object_i,
                        classTable.getString("table_name"),                    // NOI18N
                        classTable.getString("primary_key_field"),             // NOI18N
                        toStringQualifier,
                        policy,
                        attributePolicy,
                        indexed);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("to string for Class :" + className + " :: " + toStringQualifier); // NOI18N
                }
                // Hell
                tmp.setEditor(classTable.getString("editorqualifier"));                   // NOI18N
                tmp.setRenderer(classTable.getString("RendererQualifier"));               // NOI18N
                try {
                    final boolean arrayElementLink = classTable.getBoolean("array_link"); // NOI18N
                    tmp.setArrayElementLink(arrayElementLink);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("isArrayElementLink set to :" + arrayElementLink);      // NOI18N
                    }
                } catch (Exception e) {
                    LOG.error("Error at arrayElementLink probably old DB version", e);    // NOI18N
                }

                classes.add(tmp.getID(), tmp);
                classesByTableName.put(tmp.getTableName().toLowerCase(), tmp);
            }

            classTable.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
        }

        addClassPermissions(conPool);

        addAttributes(conPool);

        addMemberInfos(conPool);

        addShutdown(new AbstractShutdownable() {

                @Override
                protected void internalShutdown() throws ServerExitError {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("shutting down ClassCache"); // NOI18N
                    }

                    classes.clear();
                    classesByTableName.clear();
                    classAttribs.clear();
                    icons.clear();
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
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
     */
    public final Class getClass(final int id) {
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
    public final Class getClassByTableName(final String tableName) throws Exception {
        return classesByTableName.get(tableName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final List getAllClasses() {
        return classes.getAll();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u   DOCUMENT ME!
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final Class getClass(final User u, final int id) {
        final Class c = classes.getClass(id);

        if ((c != null) && c.getPermissions().hasReadPermission(u)) {
            return c;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u          DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public final Class getClassNyTableName(final User u, final String tableName) throws Exception {
        final Class c = getClassByTableName(tableName);

        if ((c != null) && c.getPermissions().hasReadPermission(u)) {
            return c;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap getClassHashMap() {
        return classes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final List getAllClasses(final User u) {
        final List all = getAllClasses();
        final List cs = new ArrayList(all.size());

        for (int i = 0; i < all.size(); i++) {
            final Class c = (Class)all.get(i);

            if (c.getPermissions().hasReadPermission(u)) {
                cs.add(c);
            }
        }

        return cs;
    }

    /**
     * Only to be called by the constructor.
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addAttributes(final DBConnectionPool conPool) {
        final DBConnection con = conPool.getDBConnection();
        try {
            final ResultSet attribTable = con.submitInternalQuery(
                    DBConnection.DESC_GET_ALL_CLASS_ATTRIBUTES,
                    new Object[0]);

            int id = 0;
            int classID = 0;

            int typeID = 0;

            while (attribTable.next()) {
                String name = null;
                java.lang.Object value = null;
                ClassAttribute attrib = null;

                id = attribTable.getInt("id");               // NOI18N
                classID = attribTable.getInt("class_id");    // NOI18N
                name = attribTable.getString("attr_key");    // NOI18N
                typeID = attribTable.getInt("type_id");      // NOI18N
                value = attribTable.getString("attr_value"); // NOI18N

                attrib = new ClassAttribute(id + "", classID, name, typeID, classes.getClass(classID).getPolicy()); // NOI18N
                attrib.setValue(value);
                classes.getClass(attrib.getClassID()).addAttribute(attrib);
                classAttribs.put(new Integer(attrib.getID()), attrib);
            }

            attribTable.close();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR :: classcache  get_all_class_attributes", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addMemberInfos(final DBConnectionPool conPool) {
        final DBConnection con = conPool.getDBConnection();

        final HashMap<Integer, HashMap<String, String>> classfieldtypes = new HashMap<>();

        try {
            final List<Sirius.server.localserver._class.Class> allClasses = getAllClasses();
            for (final Sirius.server.localserver._class.Class _class : allClasses) {
                HashMap<String, String> fieldtypes = classfieldtypes.get(_class.getID());
                if (fieldtypes == null) {
                    fieldtypes = new HashMap<>();
                    classfieldtypes.put(_class.getID(), fieldtypes);
                }
                try(final Statement statement = con.getConnection().createStatement();
                            final ResultSet resultset = statement.executeQuery(_class.getGetEmptyResultStmnt())) {
                    final ResultSetMetaData rsmd = resultset.getMetaData();

                    for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                        final String fieldname = rsmd.getColumnName(i);
                        final String javaclassname = rsmd.getColumnClassName(i);
                        if (SQLTools.getGeometryFactory(
                                        Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                                    .isGeometryColumn(
                                        rsmd.getColumnTypeName(i))) {
                            // we use the jts geometry class for geometry fields, however, the ObjectFactory has to do
                            // similar conversion
                            fieldtypes.put(fieldname.toLowerCase(),
                                com.vividsolutions.jts.geom.Geometry.class.getName());
                        } else if (Types.CLOB == rsmd.getColumnType(i)) {
                            // since clobs are not serialisable we "use" String with all it's limitations
                            // this has to be done similarly in ObjectFactory
                            fieldtypes.put(fieldname.toLowerCase(), String.class.getName());
                        } else {
                            fieldtypes.put(fieldname.toLowerCase(), javaclassname);
                        }
                    }
                }
            }

            try(final ResultSet rs = con.submitInternalQuery(DBConnection.DESC_GET_ATTRIBUTE_INFO, new Object[0])) {
                while (rs.next()) {
                    final int id = rs.getInt("id");            // NOI18N
                    final String name = rs.getString("name");  // NOI18N
                    final int classId = rs.getInt("class_id"); // NOI18N
                    final int typeId = rs.getInt("type_id");   // NOI18N
                    final int position = rs.getInt("pos");     // NOI18N

                    String fieldName = rs.getString("field_name"); // NOI18N
                    if (fieldName != null) {
                        fieldName = fieldName.trim();
                    }

                    String arrayKey = rs.getString("array_key"); // NOI18N
                    if (arrayKey != null) {
                        arrayKey = arrayKey.trim();
                    }

                    String toString = rs.getString("toStringString"); // NOI18N
                    if (toString != null) {
                        toString = toString.trim();
                    }

                    String editor = rs.getString("editor_class"); // NOI18N
                    if (editor != null) {
                        editor = editor.trim();
                    }

                    String complexEditor = rs.getString("complexeditorclass"); // NOI18N
                    if (complexEditor != null) {
                        complexEditor = complexEditor.trim();
                    }

                    String fromString = rs.getString("from_string_class"); // NOI18N
                    if (fromString != null) {
                        fromString = fromString.trim();
                    }

                    final boolean foreignKey = rs.getBoolean("foreign_key");              // NOI18N
                    final boolean substitute = rs.getBoolean("substitute");               // NOI18N
                    final boolean visible = rs.getBoolean("visible");                     // NOI18N
                    final boolean optional = rs.getBoolean("optional");                   // NOI18N
                    final boolean indexed = rs.getBoolean("indexed");                     // NOI18N
                    final int foreignKeyClassId = rs.getInt("foreign_key_references_to"); // NOI18N
                    final boolean array = rs.getBoolean("isarray");                       // NOI18N

                    boolean extensionAttribute = false;
                    try {
                        extensionAttribute = rs.getBoolean("extension_attr"); // NOI18N
                    } catch (Exception skip) {
                    }

                    // xxx array stuff to be added
                    final MemberAttributeInfo mai = new MemberAttributeInfo(
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

                    mai.setJavaclassname(classfieldtypes.get(classId).get(
                            (fieldName != null) ? fieldName.toLowerCase() : null));
                    mai.setExtensionAttribute(extensionAttribute);
                    if (mai.isExtensionAttribute()) {
                        mai.setJavaclassname(java.lang.Object.class.getCanonicalName());
                        mai.setVirtual(true);
                    }

                    if (foreignKeyClassId < 0) {
                        mai.setVirtual(true);
                        mai.setJavaclassname(java.lang.Object.class.getCanonicalName());
                    }

                    final Sirius.server.localserver._class.Class c = classes.getClass(classId);

                    if (c != null) {
                        c.addMemberAttributeInfo(mai);
                    } else {
                        LOG.warn("Wrong addMemberInfos entry for class::" + classId); // NOI18N
                    }
                }
            }
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
            LOG.fatal("<LS> ERROR :: addMemberinfos", e);                             // NOI18N

            ErrorUtils.createRUDFileAndExit(1, null, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conPool  DOCUMENT ME!
     */
    protected final void loadIcons(final DBConnectionPool conPool) {
        String iconDirectory;
        String separator;
        Image tmpImage;

        try {
            iconDirectory = properties.getIconDirectory();
        } catch (Exception e) {
            LOG.error(
                "<LS> ERROR ::  Keyvalue ICONDIRECTORY in ConfigFile is missing\n<LS> ERROR ::  set ICONDIRECTORY to . ", // NOI18N
                e);
            iconDirectory = "."; // NOI18N
        }
        try {
            separator = System.getProperty("file.separator"); // NOI18N
        } catch (Exception e) {
            LOG.error(
                "<LS> ERROR ::  KeyValue SEPARATOR in ConfigFile is missing\n<LS> ERROR ::  set DEFAULTSEPARATOR = \\", // NOI18N
                e);
            separator = "\\"; // NOI18N
        }

        final DBConnection con = conPool.getDBConnection();
        try {
            final ResultSet imgTable = con.submitInternalQuery(DBConnection.DESC_GET_ALL_IMAGES, new Object[0]);

            while (imgTable.next()) {
                tmpImage = new Image(iconDirectory + separator + imgTable.getString("file_name").trim()); // NOI18N
                icons.add(imgTable.getInt("id"), tmpImage);                                               // NOI18N
            }

            imgTable.close();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR :: get_all_icons", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addClassPermissions(final DBConnectionPool conPool) {
        final DBConnection con = conPool.getDBConnection();
        try {
            final ResultSet permTable = con.submitInternalQuery(DBConnection.DESC_GET_ALL_CLASS_PERMS, new Object[0]);

            final String lsName = properties.getServerName();

            while (permTable.next()) {
                final int ug_id = permTable.getInt("ug_id");              // NOI18N
                final String ug_name = permTable.getString("ug_name");    // NOI18N
                String lsHome = permTable.getString("domainname").trim(); // NOI18N
                final int permId = permTable.getInt("permission");        // NOI18N
                final String permKey = permTable.getString("key");

                if (lsHome.equalsIgnoreCase("local")) {          // NOI18N
                    lsHome = String.valueOf(lsName);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("==permId set ======! " + permId); // NOI18N
                }

                final Class clazz = classes.getClass(permTable.getInt("class_id"));                    // NOI18N
                if (clazz == null) {
                    LOG.warn("illegal class_id in cs_ug_class_perm: " + permTable.getInt("class_id")); // NOI18N
                } else {
                    final UserGroup ug = new UserGroup(ug_id, ug_name, lsHome);
                    final Permission perm = new Permission(permId, permKey);
                    clazz.getPermissions().addPermission(ug, perm);
                }
            }

            permTable.close();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR :: addClassPermissions", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerProperties getProperties() {
        return properties;
    }
}
