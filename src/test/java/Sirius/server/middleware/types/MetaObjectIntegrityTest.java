package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.impls.domainserver.OfflineMetaClassCacheService;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.cismet.cids.integrationtests.LegacyRESTfulInterfaceTest;
import de.cismet.cids.integrationtests.RESTfulInterfaceTest;
import de.cismet.cids.integrationtests.TestEnvironment;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import de.cismet.cids.utils.MetaClassCacheService;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@RunWith(DataProviderRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetaObjectIntegrityTest {

    private final static Logger LOGGER = Logger.getLogger(MetaObjectIntegrityTest.class);
    private final static String DOMAIN = "CIDS_REF";

    @BeforeClass
    public static void setUpClass() throws Exception {
        final Properties log4jProperties = new Properties();
        log4jProperties.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        log4jProperties.put("log4j.appender.Remote.remoteHost", "localhost");
        log4jProperties.put("log4j.appender.Remote.port", "4445");
        log4jProperties.put("log4j.appender.Remote.locationInfo", "true");
        log4jProperties.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(log4jProperties);

        try {
            final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="DATA PROVIDERS ----------------------------------------------------------">
    @DataProvider
    public final static String[] getMetaClassTableNames() throws Exception {
        final Collection<MetaClass> metaClasses
                = OfflineMetaClassCacheService.getInstance().getAllClasses(
                        DOMAIN).values();

        final String[] metaClassTableNames = new String[metaClasses.size()];
        int i = 0;
        for (MetaClass metaClass : metaClasses) {
            metaClassTableNames[i] = metaClass.getTableName();
            i++;
        }
        return metaClassTableNames;

    }
    // </editor-fold>

    @Test
    @UseDataProvider("getMetaClassTableNames")
    public void test01MetaObjectLocalInstanceIntegrity(final String tableName) throws Exception {
        LOGGER.debug("testing MetaObjectLocalInstanceIntegrity(" + tableName + ")");
        try {

            Assert.assertNotNull("MetaClassCache initialized",
                    OfflineMetaClassCacheService.getInstance());
            Assert.assertNotNull("MetaClassCache for domain '" + DOMAIN + "' initialized",
                    OfflineMetaClassCacheService.getInstance().getAllClasses(DOMAIN));
            Assert.assertFalse("MetaClassCache for domain '" + DOMAIN + "' is not empty",
                    OfflineMetaClassCacheService.getInstance().getAllClasses(DOMAIN).isEmpty());
            final MetaClass metaClass = OfflineMetaClassCacheService.getInstance().getMetaClass(DOMAIN, tableName);
            Assert.assertNotNull("meta class '" + tableName + "' from meta class cache not null", metaClass);

            final MetaObject metaObjectFromClass = metaClass.getEmptyInstance();
            Assert.assertNotNull("new meta object of meta class '" + tableName + "' from meta class not null",
                    metaObjectFromClass);

            checkMetaObjectIntegrity(metaObjectFromClass);

            LOGGER.info("MetaObjectLocalInstanceIntegrity(" + tableName + ") test passed!");

        } catch (AssertionError ae) {
            LOGGER.error("MetaObjectLocalInstanceIntegrity(" + tableName + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during MetaObjectLocalInstanceIntegrity(" + tableName + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getMetaClassTableNames")
    public void test02MetaObjectReferenceLocalInstanceIntegrity(final String tableName) throws Exception {
        LOGGER.debug("testing MetaObjectLocalInstanceProxyIntegrity(" + tableName + ")");
        try {

            Assert.assertNotNull("MetaClassCache initialized",
                    OfflineMetaClassCacheService.getInstance());
            Assert.assertNotNull("MetaClassCache for domain '" + DOMAIN + "' initialized",
                    OfflineMetaClassCacheService.getInstance().getAllClasses(DOMAIN));
            Assert.assertFalse("MetaClassCache for domain '" + DOMAIN + "' is not empty",
                    OfflineMetaClassCacheService.getInstance().getAllClasses(DOMAIN).isEmpty());
            final MetaClass metaClass = OfflineMetaClassCacheService.getInstance().getMetaClass(DOMAIN, tableName);
            Assert.assertNotNull("meta class '" + tableName + "' from meta class cache not null", metaClass);

            final MetaObject metaObject = metaClass.getEmptyInstance();
            Assert.assertNotNull("new meta object of meta class '" + tableName + "' from meta class not null",
                    metaObject);

            checkMetaObjectIntegrity(metaObject);

            final String metaClassName = metaClass.getTableName();
            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObject.equals(metaObject) ",
                    metaObject.equals(metaObject));

            final MetaObject metaObjectReference
                    = MetaObjectReference.getInstance(metaObject, metaObject.getReferencingObjectAttribute());

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is a MetaObject",
                    MetaObject.class.isAssignableFrom(metaObjectReference.getClass()));

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is an instance of MetaObject",
                    metaObjectReference instanceof MetaObject);

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is a MetaObject",
                    MetaObject.class.isAssignableFrom(metaObjectReference.getClass()));

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is an instance of java.lang.reflect.Proxy",
                    metaObjectReference instanceof Proxy);

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObject.equals(metaObject) ",
                    metaObjectReference.equals(metaObject));

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference.equals(metaObjectReference) ",
                    metaObjectReference.equals(metaObjectReference));

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference.equals(metaObject) ",
                    metaObjectReference.equals(metaObject));

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObject.equals(metaObjectReference) ",
                    metaObject.equals(metaObjectReference));

            Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): other instance of metaObject does not equals(metaObjectReference) ",
                    metaObject.getMetaClass().getEmptyInstance().equals(metaObjectReference));

            checkMetaObjectIntegrity(metaObjectReference);

            LOGGER.info("MetaObjectLocalInstanceProxyIntegrity(" + tableName + ") test passed!");

        } catch (AssertionError ae) {
            LOGGER.error("MetaObjectLocalInstanceProxyIntegrity(" + tableName + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during MetaObjectLocalInstanceProxyIntegrity(" + tableName + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HELPER METHODS ----------------------------------------------------------">
    /**
     * Check the integrity of the supplied MetaObject
     *
     * @param metaObject
     */
    public static void checkMetaObjectIntegrity(final MetaObject metaObject) {
        checkMetaObjectIntegrity(metaObject, true);
    }

    /**
     * Check the integrity of the supplied MetaObject
     *
     * @param metaObject
     * @param checkBackReference set to false intaObjectCacheed objects till
     * #175 is fixed
     */
    public static void checkMetaObjectIntegrity(final MetaObject metaObject,
            final boolean checkBackReference) {
        checkMetaObjectIntegrity(metaObject, checkBackReference, new ArrayList<String>());
    }

    /**
     * Check the integrity of the supplied MetaObject.
     *
     * @param metaObject
     * @param checkBackReference set to false intaObjectCacheed objects till
     * #175 is fixed
     * @param objectHierarchy
     */
    private static void checkMetaObjectIntegrity(final MetaObject metaObject,
            final boolean checkBackReference,
            final List<String> objectHierarchy
    ) {

        objectHierarchy.add(metaObject.getClassKey());
        final String hierarchyPath = getHierarchyPath(objectHierarchy);
        final MetaClass metaClass = metaObject.getMetaClass();
        final String metaClassName = metaClass.getTableName();

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaObject.getMetaClass() + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): MetaClass is not null {" + hierarchyPath + "}",
                metaObject.getMetaClass());

        Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaObject.getMetaClass() + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): Class ID is not negative {" + hierarchyPath + "}",
                metaObject.getClassID() < 0);

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getClassKey() matches getMetaClass().getKey() {" + hierarchyPath + "}",
                metaClass.getKey().toString(),
                metaObject.getClassKey());

        // FIXME #174: key in hashmap should be metaClass.getKey()! ------------
        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAllClasses() contains class key '"
                + metaObject.getClassKey() + "' {" + hierarchyPath + "}",
                metaObject.getAllClasses().containsKey(metaClass.getDomain() + metaClass.getId()));

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAllClasses() contains correct MetaClass '"
                + metaObject.getClassKey() + "' instance {" + hierarchyPath + "}",
                metaClass,
                metaObject.getAllClasses().get(metaClass.getDomain() + metaClass.getId()));

        // ---------------------------------------------------------------------
        Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttribs() is not empty {" + hierarchyPath + "}",
                metaObject.getAttributes().isEmpty());

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttribs() matches getAttributes() {" + hierarchyPath + "}",
                metaObject.getAttributes().size(),
                metaObject.getAttribs().length);

        final ObjectAttribute[] objectAttributes = metaObject.getAttribs();
        final String[] metaObjectAttributeNames = new String[metaObject.getAttributes().size()];
        int i = 0;

        // check ObjectAttributes ----------------------------------------------
        for (final ObjectAttribute objectAttribute : objectAttributes) {
            final Object value = objectAttribute.getValue();
            metaObjectAttributeNames[i] = objectAttribute.getMai().getName();

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributes() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") key {" + hierarchyPath + "}",
                    metaObject.getAttributes().containsKey(objectAttribute.getKey()));

            Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributes() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    objectAttribute,
                    metaObject.getAttributes().get(objectAttribute.getKey()));

            // getAttribute expects the name of the attribute, not the key! #174
            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttribute() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") key {" + hierarchyPath + "}",
                    metaObject.getAttribute(objectAttribute.getName()));

            if (metaObject.isDummy()) {
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributeByFieldName() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") all array instances {" + hierarchyPath + "}",
                        metaObject.getAttributes().size(),
                        metaObject.getAttributeByName(objectAttribute.getMai().getName(), Integer.MAX_VALUE).size());
            }

            // serveral attributes may have the same name!
//            Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
//                    + metaClassName + "' (" + metaObject.getId()
//                    + "@" + metaObject.getClassKey() + "): getAttribute() contains attribute '"
//                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
//                    objectAttribute,
//                    metaObject.getAttribute(objectAttribute.getName()));
            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByFieldName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") key {" + hierarchyPath + "}",
                    metaObject.getAttributeByFieldName(objectAttribute.getMai().getFieldName()));

            // dummy objects -> all attributes wit same (field) name!
            if (!metaObject.isDummy()) {
                Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributeByFieldName() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                        objectAttribute,
                        metaObject.getAttributeByFieldName(objectAttribute.getMai().getFieldName()));
            }

            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") names {" + hierarchyPath + "}",
                    metaObject.getAttributeByName(objectAttribute.getName(), Integer.MAX_VALUE));

            Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") name {" + hierarchyPath + "}",
                    metaObject.getAttributeByName(objectAttribute.getName(), Integer.MAX_VALUE).isEmpty());

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    metaObject.getAttributeByName(objectAttribute.getName(), Integer.MAX_VALUE).contains(objectAttribute));

            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") names {" + hierarchyPath + "}",
                    metaObject.getAttributesByName(Arrays.asList(new String[]{objectAttribute.getName()})));

            Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") name {" + hierarchyPath + "}",
                    metaObject.getAttributesByName(Arrays.asList(new String[]{objectAttribute.getName()})).isEmpty());

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    metaObject.getAttributesByName(Arrays.asList(new String[]{objectAttribute.getName()})).contains(objectAttribute));

            // check value -----------------------------------------------------
            if (value != null) {
                Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributesByType() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") names {" + hierarchyPath + "}",
                        metaObject.getAttributesByType(value.getClass(), 0));

                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributesByType() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") name {" + hierarchyPath + "}",
                        metaObject.getAttributesByType(value.getClass(), 0).isEmpty());

                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributesByType() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                        metaObject.getAttributesByType(value.getClass(), 0).contains(objectAttribute));

                if (Sirius.server.localserver.object.Object.class.isAssignableFrom(value.getClass())) {
                    Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") references Object {" + hierarchyPath + "}",
                            objectAttribute.referencesObject());
                }

                if (objectAttribute.referencesObject()) {
                    Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") is Sirius Object {" + hierarchyPath + "}",
                            Sirius.server.localserver.object.Object.class.isAssignableFrom(value.getClass()));

                    Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") is MetaObject {" + hierarchyPath + "}",
                            MetaObject.class.isAssignableFrom(value.getClass()));

                    final MetaObject valueMetaObject = (MetaObject) value;

                    if (checkBackReference) {
                        final ObjectAttribute referencingObjectAttribute = valueMetaObject.getReferencingObjectAttribute();
                        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s object attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ")' MetaObject ReferencingObjectAttribute is not null {" + hierarchyPath + "}",
                                referencingObjectAttribute);

                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s object attribute "
                                + "[" + i + "] '" + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ") MetaObject ReferencingObjectAttribute '"
                                + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey() + ") matches attribute's key {" + hierarchyPath + "}",
                                objectAttribute.getKey(),
                                valueMetaObject.getReferencingObjectAttribute().getKey());

                        Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s object attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ")' MetaObject ReferencingObjectAttribute matches attribute instance {" + hierarchyPath + "}",
                                objectAttribute,
                                referencingObjectAttribute);
                    }

                    if (objectAttribute.isVirtualOneToManyAttribute()) {
                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s virtual array object attribute "
                                + "[" + i + "] '" + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ")'s  ClassKey matches valueMetaObject's ClassKey",
                                "-" + valueMetaObject.getClassKey(),
                                objectAttribute.getClassKey());
                    } else {
                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s object attribute "
                                + "[" + i + "] '" + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ")'s  ClassKey matches valueMetaObject's ClassKey {" + hierarchyPath + "}",
                                valueMetaObject.getClassKey(),
                                objectAttribute.getClassKey());
                    }

                    // TODO: check if object and attribute editors / renderers are the same
                    // =========================================================
                    if (valueMetaObject.getComplexEditor() != null) {
                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ") getComplexEditor() matches valueMetaObject.getComplexEditor() {" + hierarchyPath + "}",
                                valueMetaObject.getComplexEditor(),
                                objectAttribute.getComplexEditor());
                    }

                    if (valueMetaObject.getRenderer() != null) {
                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ") getRenderer() matches  valueMetaObject.getRenderer() {" + hierarchyPath + "}",
                                valueMetaObject.getRenderer(),
                                objectAttribute.getRenderer());
                    }

                    if (valueMetaObject.getSimpleEditor() != null) {
                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ") getSimpleEditor() matches  valueMetaObject.getSimpleEditor() {" + hierarchyPath + "}",
                                valueMetaObject.getSimpleEditor(),
                                objectAttribute.getSimpleEditor());
                    }
                    // =========================================================

                    // check n-m array
                    final String arrayKeyFieldName = objectAttribute.getMai().getArrayKeyFieldName();

                    //  OMFG! intermediate array object OR dummy array object!
                    if (arrayKeyFieldName != null && !arrayKeyFieldName.isEmpty()) {
                        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                                + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ")'with arrayKeyFieldName '"
                                + arrayKeyFieldName + "' is Array Attribute {" + hierarchyPath + "}",
                                objectAttribute.isArray());

                        // current attribute is attribute of the 'master' object: class IDs do match!
                        // value must be dummy!
                        if (objectAttribute.getClassID() == objectAttribute.getParentObject().getClassID()) {
                            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                    + metaClassName + "' (" + metaObject.getId()
                                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                                    + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                    + ")' valueMetaObject '" + valueMetaObject.getName()
                                    + "' (" + valueMetaObject.getKey() + ") with arrayKeyFieldName '"
                                    + arrayKeyFieldName + "' is dummy meta object {" + hierarchyPath + "}",
                                    valueMetaObject.isDummy());
                        } else {
                            // THIS IS MADNESS: current attribute is attribute of the 'dummy' object: class IDs do NOT match!
                            // value must not be dummy but intermediate object (no 'real' dummy)!
                            Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                    + metaClassName + "' (" + metaObject.getId()
                                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                                    + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                    + ")' valueMetaObject '" + valueMetaObject.getName()
                                    + "' (" + valueMetaObject.getKey() + ") with arrayKeyFieldName '"
                                    + arrayKeyFieldName + "' is no dummy meta object but intermediate array element object{"
                                    + hierarchyPath + "}",
                                    valueMetaObject.isDummy());

                            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                    + metaClassName + "' (" + metaObject.getId()
                                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                                    + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                    + ")' valueMetaObject '" + valueMetaObject.getName()
                                    + "' (" + valueMetaObject.getKey() + ") with arrayKeyFieldName '"
                                    + arrayKeyFieldName + "' is intermediate object {" + hierarchyPath + "}",
                                    objectAttribute.getParentObject().getReferencingObjectAttribute().getClassID(),
                                    objectAttribute.getClassID());

                        }
                    }

                    if (valueMetaObject.isDummy()) {
                        checkArrayAttributes(metaObject, 
                                objectAttribute, 
                                checkBackReference,
                                new ArrayList<String>(objectHierarchy));
                    }

                    // recursively check integrity
                    checkMetaObjectIntegrity(valueMetaObject,
                            checkBackReference,
                            new ArrayList<String>(objectHierarchy));
                }
            }

            // end check value -----------------------------------------------------
            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ")' ParentObject is not null {" + hierarchyPath + "}",
                    objectAttribute.getParentObject());
            Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ")' ParentObject matches metaObject instance {" + hierarchyPath + "}",
                    metaObject,
                    objectAttribute.getParentObject());

            // array attributes of dummy object 'borrowed' from parent object!!! :o(
            if (metaObject.isDummy()) {

                // TERROR: objectID of attributes of dummy objects does not refer to the object ID
                // of the dummy object but to the object ids of the value objects!
                if (objectAttribute.getValue() != null && MetaObject.class.isAssignableFrom(objectAttribute.getClass())) {
                    Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ")' ParentObject's (dummy) ParentObject (master) id matches getObjectID() {" + hierarchyPath + "}",
                            ((MetaObject) objectAttribute.getValue()).getID(),
                            objectAttribute.getObjectID());
                }
            } else {
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")' ParentObject's id matches getObjectID() {" + hierarchyPath + "}",
                        metaObject.getID(),
                        objectAttribute.getObjectID());
            }

            if (!objectAttribute.isArray()
                    && !objectAttribute.isVirtualOneToManyAttribute()
                    && !objectAttribute.isSubstitute()
                    && !objectAttribute.getMai().isExtensionAttribute()) {

                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributes() order matches '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") position defined in metaClass {" + hierarchyPath + "}",
                        objectAttribute.getMai().getPosition(),
                        i);
            }

            final MemberAttributeInfo memberAttributeInfo = objectAttribute.getMai();
            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ")'s MemberAttributeInfo is not null  {" + hierarchyPath + "}",
                    memberAttributeInfo);

            // OMG!!!
            // array attributes of dummy objects are not from dummy class
            // but from parent ("master") class! 
            // SPIELHALLE/KATEGORIEN[]->SPIELHALLE_KATEGORIEN/KATEGORIEN[]
            // same kategorien attribute type but different classes !!!!
            if ((objectAttribute.isVirtualOneToManyAttribute() || objectAttribute.isArray())
                    && metaObject.isDummy()) {

                // for KATEGORIEN[] in SPIELHALLE, ForeignKey refers to SPIELHALLE_KATEGORIEN (dummy object)
                // DUMMY metaObject/attribute don't have the same classId!
                if (!objectAttribute.isVirtualOneToManyAttribute()) { //n-m
                    Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ") getClassID() matches  metaObject.getClassID() {" + hierarchyPath + "}",
                            metaObject.getClassID(),
                            objectAttribute.getMai().getForeignKeyClassId());
                } else { //1-n
                    // negative class id in MAI but not in object
                    Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ") getClassID() matches  metaObject.getClassID() {" + hierarchyPath + "}",
                            (-1 * metaObject.getClassID()),
                            objectAttribute.getMai().getForeignKeyClassId());
                }

                final MetaClass parentMetaClass = ((MetaObject) metaObject.getReferencingObjectAttribute()
                        .getParentObject()).getMetaClass();

                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s MemberAttributeInfo key found in parentMetaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
                        parentMetaClass.getMemberAttributeInfos().containsKey(memberAttributeInfo.getKey()));

                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s MemberAttributeInfo found in parentMetaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
                        ((MemberAttributeInfo) parentMetaClass.getMemberAttributeInfos().get(memberAttributeInfo.getKey())).getKey(),
                        memberAttributeInfo.getKey());

                RESTfulInterfaceTest.compareMemberAttributeInfos(
                        ((MemberAttributeInfo) parentMetaClass.getMemberAttributeInfos().get(memberAttributeInfo.getKey())),
                        memberAttributeInfo, metaObject.getID());

            } else {
                // metaObject/attribute have the same classId
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") getClassID() matches  metaObject.getClassID() {" + hierarchyPath + "}",
                        metaObject.getClassID(),
                        objectAttribute.getClassID());

                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s MemberAttributeInfo key found in metaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
                        metaClass.getMemberAttributeInfos().containsKey(memberAttributeInfo.getKey()));

                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s MemberAttributeInfo found in metaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
                        ((MemberAttributeInfo) metaClass.getMemberAttributeInfos().get(memberAttributeInfo.getKey())).getKey(),
                        memberAttributeInfo.getKey());

                RESTfulInterfaceTest.compareMemberAttributeInfos(
                        ((MemberAttributeInfo) metaClass.getMemberAttributeInfos().get(memberAttributeInfo.getKey())),
                        memberAttributeInfo, metaObject.getID());
            }

            // DISABLED: Not the *same instance* of MAI in Class and Object!
//            Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
//                    + metaClassName + "' (" + metaObject.getId()
//                    + "@" + metaObject.getClassKey() + ")'s attribute "
//                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
//                    + ")'s MemberAttributeInfo instance found in metaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
//                    metaClass.getMemberAttributeInfos().get(memberAttributeInfo.getKey()),
//                    memberAttributeInfo);
            if (memberAttributeInfo.getArrayKeyFieldName() != null
                    && !memberAttributeInfo.getArrayKeyFieldName().isEmpty()) {
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")' is array since ArrayKeyFieldName '"
                        + memberAttributeInfo.getArrayKeyFieldName() + "' is set {" + hierarchyPath + "}",
                        objectAttribute.isArray());
            }

            if (objectAttribute.isArray() && !objectAttribute.isVirtualOneToManyAttribute()) {
                Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s  ArrayKeyFieldName is not null {" + hierarchyPath + "}",
                        memberAttributeInfo.getArrayKeyFieldName());
            }

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getClassID() matches  memberAttributeInfo.getClassId() {" + hierarchyPath + "}",
                    memberAttributeInfo.getClassId(),
                    objectAttribute.getClassID());

            if (memberAttributeInfo.getComplexEditor() != null) {
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") getComplexEditor() matches  memberAttributeInfo.getComplexEditor() {" + hierarchyPath + "}",
                        memberAttributeInfo.getComplexEditor(),
                        objectAttribute.getComplexEditor());
            }

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getSimpleEditor() matches  memberAttributeInfo.getEditor() {" + hierarchyPath + "}",
                    memberAttributeInfo.getEditor(),
                    objectAttribute.getSimpleEditor());

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getRenderer() matches  memberAttributeInfo.getRenderer() {" + hierarchyPath + "}",
                    memberAttributeInfo.getRenderer(),
                    objectAttribute.getRenderer());

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getTypeId() matches  memberAttributeInfo.getTypeId() {" + hierarchyPath + "}",
                    memberAttributeInfo.getTypeId(),
                    objectAttribute.getTypeId());
// FIXME: #174   
//            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
//                    + metaClassName + "' (" + metaObject.getId()
//                    + "@" + metaObject.getClassKey() + ")'s attribute "
//                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
//                    + ") getJavaType() matches  memberAttributeInfo.getJavaclassname()",
//                    memberAttributeInfo.getJavaclassname(),
//                    objectAttribute.getJavaType());

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") isArray() matches  memberAttributeInfo.isArray() {" + hierarchyPath + "}",
                    memberAttributeInfo.isArray(),
                    objectAttribute.isArray());

            if (memberAttributeInfo.isExtensionAttribute()
                    || memberAttributeInfo.getForeignKeyClassId() < 0) {

                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s extension attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") must be a virtual attribute {" + hierarchyPath + "}",
                        memberAttributeInfo.isVirtual());

                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s extension attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") getJavaType must be java.lang.Object {" + hierarchyPath + "}",
                        java.lang.Object.class.getCanonicalName(),
                        memberAttributeInfo.getJavaclassname());

                if (memberAttributeInfo.isExtensionAttribute()) {
                    Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s extension attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ") recognized in MetaClass {" + hierarchyPath + "}",
                            metaClass.hasExtensionAttributes());

                }
            }

            if (objectAttribute.isArray()) {
                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no virtual attribute {" + hierarchyPath + "}",
                        objectAttribute.isVirtualOneToManyAttribute());
                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no virtual attribute {" + hierarchyPath + "}",
                        memberAttributeInfo.isVirtual());
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") isForeignKey {" + hierarchyPath + "}",
                        memberAttributeInfo.isForeignKey());
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") referencesObject {" + hierarchyPath + "}",
                        objectAttribute.referencesObject());
            }

            if (objectAttribute.isVirtualOneToManyAttribute()) {
                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no virtual attribute {" + hierarchyPath + "}",
                        objectAttribute.isArray());
                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no array attribute {" + hierarchyPath + "}",
                        memberAttributeInfo.isArray());
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") isForeignKey {" + hierarchyPath + "}",
                        memberAttributeInfo.isForeignKey());
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") referencesObject {" + hierarchyPath + "}",
                        objectAttribute.referencesObject());
            }

            if (metaObject.isDummy() && (objectAttribute.isArray() || objectAttribute.isVirtualOneToManyAttribute())) {
                Assert.assertFalse("Dummy MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") isOptional() isOptional() must not be false {" + hierarchyPath + "}",
                        objectAttribute.isOptional());
            } else {
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") isOptional() matches  memberAttributeInfo.isOptional() {" + hierarchyPath + "}",
                        memberAttributeInfo.isOptional(),
                        objectAttribute.isOptional());
            }

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") isSubstitute() matches  memberAttributeInfo.isSubstitute() {" + hierarchyPath + "}",
                    memberAttributeInfo.isSubstitute(),
                    objectAttribute.isSubstitute());

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") isVisible() matches  memberAttributeInfo.isVisible() {" + hierarchyPath + "}",
                    memberAttributeInfo.isVisible(),
                    objectAttribute.isVisible());

            i++;
        }
        // end check attributes --------------------------------------------

        final Collection attributeCollection
                = metaObject.getAttributesByName(Arrays.asList(metaObjectAttributeNames));

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttributesByName returns attributes {" + hierarchyPath + "}",
                attributeCollection);

        Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttributesByName returns attribute {" + hierarchyPath + "}s",
                attributeCollection.isEmpty());

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttributesByName matches attributes objectAttributes size {" + hierarchyPath + "}",
                objectAttributes.length,
                attributeCollection.size());

        for (final ObjectAttribute objectAttribute : objectAttributes) {
            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    attributeCollection.contains(objectAttribute));
        }

        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): equals(metaObject)  {" + hierarchyPath + "}",
                metaObject.equals(metaObject));

        // FIXME: DISABLED due to Wild status guesses in CIDS BEAN #174
        // See CidsBeanJsonDeserializer.java#L301
//        if (metaObject.getStatus() == MetaObject.NEW) {
//            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
//                    + metaClassName + "' (" + metaObject.getId()
//                    + "@" + metaObject.getClassKey() + "): Status is NEW if id is -1 {" + hierarchyPath + "}: "
//                    + metaObject.getStatusDebugString(),
//                    -1,
//                    metaObject.getId());
//        }
        if (metaObject.getStatus() == MetaObject.TEMPLATE) {
            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): Status is NEW if id is -1 {" + hierarchyPath + "}",
                    -1,
                    metaObject.getId());
        }

        if (metaObject.getId() == -1) {
            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): Status is NEW if id is -1 {" + hierarchyPath + "}",
                    MetaObject.NEW,
                    metaObject.getStatus());
        }

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getComplexEditor() matches metaClass.getComplexEditor() {" + hierarchyPath + "}",
                metaClass.getComplexEditor(),
                metaObject.getComplexEditor());

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getDomain() matches metaClass.getDomain() {" + hierarchyPath + "}",
                metaClass.getDomain(),
                metaObject.getDomain());

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getEditor() matches metaClass.getEditor() {" + hierarchyPath + "}",
                metaClass.getEditor(),
                metaObject.getEditor());

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getGroup() matches metaClass.getGroup() {" + hierarchyPath + "}",
                metaClass.getGroup(),
                metaObject.getGroup());

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getID matches getId() {" + hierarchyPath + "}",
                metaObject.getID(),
                metaObject.getId());

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getKey() set {" + hierarchyPath + "}",
                metaObject.getKey());

        // no primary key in dummy objects ....
        if (!metaObject.isDummy()) {
            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getPrimaryKey() set {" + hierarchyPath + "}",
                    metaObject.getPrimaryKey());

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getPrimaryKey() isPrimaryKey  {" + hierarchyPath + "}",
                    metaObject.getPrimaryKey().isPrimaryKey());

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getPrimaryKey() isPrimaryKey  {" + hierarchyPath + "}",
                    metaObject.getPrimaryKey().isPrimaryKey());

            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getPrimaryKey() matches metaClass.getPrimaryKey() {" + hierarchyPath + "}",
                    metaClass.getPrimaryKey(),
                    ((ObjectAttribute) metaObject.getPrimaryKey()).getMai().getFieldName());

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' primary key attribute '"
                    + metaObject.getPrimaryKey().getName() + "' (" + metaObject.getPrimaryKey().getKey()
                    + ")' in getAttributes() {" + hierarchyPath + "}",
                    metaObject.getAttributes().containsValue(metaObject.getPrimaryKey()));
        }

        // getReferencingObjectAttribute ---------------------------------------
        if (metaObject.getReferencingObjectAttribute() != null) {
            final ObjectAttribute referencingObjectAttribute = metaObject.getReferencingObjectAttribute();

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' references Object {" + hierarchyPath + "}",
                    referencingObjectAttribute.referencesObject());

            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' isForeignKey {" + hierarchyPath + "}",
                    referencingObjectAttribute.getMai().isForeignKey());

            if (metaObject.isDummy()) {
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                        + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                        + ")' must be array if value is a dummy meta object {" + hierarchyPath + "}",
                        referencingObjectAttribute.isArray() || referencingObjectAttribute.isVirtualOneToManyAttribute());
            }

            if (referencingObjectAttribute.getMai().getForeignKeyClassId() < 0) {
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                        + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                        + ")' negative ForeignKeyClassId matches metaObject.getClassID() of virtual array dummy object {" + hierarchyPath + "}",
                        (- 1 * metaObject.getClassID()),
                        referencingObjectAttribute.getMai().getForeignKeyClassId());
            } else {
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                        + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                        + ")' ForeignKeyClassId matches metaObject.getClassID() {" + hierarchyPath + "}",
                        metaObject.getClassID(),
                        referencingObjectAttribute.getMai().getForeignKeyClassId());
            }

            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' object not null {" + hierarchyPath + "}",
                    referencingObjectAttribute.getValue());

            Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' object matches meta object instance  {" + hierarchyPath + "}",
                    metaObject,
                    referencingObjectAttribute.getValue());
        }

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getRenderer matches metaClass.getRenderer() {" + hierarchyPath + "}",
                metaClass.getRenderer(),
                metaObject.getRenderer());

        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getRenderer matches metaClass.getSimpleEditor() {" + hierarchyPath + "}",
                metaClass.getSimpleEditor(),
                metaObject.getSimpleEditor());

        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getStatus is valid status {" + hierarchyPath + "}",
                (metaObject.getStatus() == MetaObject.MODIFIED
                || metaObject.getStatus() == MetaObject.NEW
                || metaObject.getStatus() == MetaObject.NO_STATUS
                || metaObject.getStatus() == MetaObject.TEMPLATE
                || metaObject.getStatus() == MetaObject.TO_DELETE));

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getStatusDebugString is not null {" + hierarchyPath + "}",
                metaObject.getStatusDebugString());
    }

    protected static void checkArrayAttributes(
            final MetaObject metaObject,
            final ObjectAttribute arrayAttribute,
            final boolean checkBackReference,
            final List<String> objectHierarchy) throws AssertionError {

        String hierarchyPath = getHierarchyPath(objectHierarchy);
        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s meta class is not null {" + hierarchyPath + "}",
                metaObject.getMetaClass());

        Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + " Array Master MetaObject '" + metaObject.getName() + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ") is no dummy meta object {" + hierarchyPath + "}",
                metaObject.isDummy());

        final String metaClassName = metaObject.getMetaClass().getTableName();

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName()
                + "' attribute available in meta object '" + metaObject.getName() + "' {" + hierarchyPath + "}",
                arrayAttribute);

        // WARNING: isArray is not set on 1-n array attributes!
        if (arrayAttribute.isArray()) { // n-m
            // ForeignKeyClassId is the class id of the intermediata array class (SPIELHALLE_KATEGORIE)
            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                    + arrayAttribute.getName() + "' is n-m array: "
                    + "arrayField.getMai().getForeignKeyClassId() >= 0 = true {" + hierarchyPath + "}",
                    arrayAttribute.getMai().getForeignKeyClassId() >= 0);
            // ForeignKeyClassId is the negative! class id of the parent (master) object class (SPIELHALLE *-1)
            Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                    + arrayAttribute.getName()
                    + "' is no Virtual One To Many Attribute (n-m array) {" + hierarchyPath + "}",
                    arrayAttribute.isVirtualOneToManyAttribute());
        } else { // 1-n
            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                    + arrayAttribute.getName() + "' is 1-n array: "
                    + "arrayField.getMai().getForeignKeyClassId() < 0 = true {" + hierarchyPath + "}",
                    arrayAttribute.getMai().getForeignKeyClassId() < 0);
            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                    + arrayAttribute.getName() + "' is Virtual One To Many Attribute (1-n array) {" + hierarchyPath + "}",
                    arrayAttribute.isVirtualOneToManyAttribute());
        }

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                + arrayAttribute.getName() + "' attribute in meta object '"
                + metaObject.getName() + "' is not null {" + hierarchyPath + "}",
                arrayAttribute.getValue());
        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                + arrayAttribute.getName() + "' attribute in meta object '"
                + metaObject.getName() + "' is MetaObject {" + hierarchyPath + "}",
                MetaObject.class.isAssignableFrom(arrayAttribute.getValue().getClass()));

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName()
                + "' array Container Dummy Object is not null {" + hierarchyPath + "}",
                arrayAttribute.getValue());

        // dumm dummy object: value of the immediate array attribute
        // n-m: instance of intermediate array class (SPH_SPIELHALLE)
        // 1-n: instance of the master class (SPIELHALLE)
        final MetaObject arrayContainerDummyObject = (MetaObject) arrayAttribute.getValue();

        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName()
                + "' array Container Dummy Object's class key is not null {" + hierarchyPath + "}",
                arrayContainerDummyObject.getClassKey());
        objectHierarchy.add(arrayContainerDummyObject.getClassKey());
        hierarchyPath = getHierarchyPath(objectHierarchy);

        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName()
                + "' attribute in meta object '" + metaObject.getName() + "' ("
                + metaObject.getKey() + ") is array Container Dummy Object ("
                + arrayContainerDummyObject.getKey() + ") {" + hierarchyPath + "}",
                arrayContainerDummyObject.isDummy());

        final ObjectAttribute[] arrayContainerDummyObjectAttributes = arrayContainerDummyObject.getAttribs();
        int i = 0;

        for (final ObjectAttribute arrayContainerDummyObjectAttribute : arrayContainerDummyObjectAttributes) {
            i++;
            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                    + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #"
                    + i + " in meta object '" + metaObject.getName() + "' is not null {" + hierarchyPath + "}",
                    arrayContainerDummyObjectAttribute.getValue());
            Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                    + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #"
                    + i + " in meta object '" + metaObject.getName() + "' is MetaObject {" + hierarchyPath + "}",
                    MetaObject.class.isAssignableFrom(arrayContainerDummyObjectAttribute.getValue().getClass()));
            Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                    + arrayAttribute.getName() + "'s array attribute #" + i + "' parent object points to dummy object"
                    + "arrayContainerDummyObjectAttribute. {" + hierarchyPath + "}",
                    arrayContainerDummyObject,
                    arrayContainerDummyObjectAttribute.getParentObject());
            Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                    + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #"
                    + i + " in meta object '" + metaObject.getName() + "' is not optional {" + hierarchyPath + "}",
                    arrayContainerDummyObjectAttribute.isOptional());

            // n-m: additional intermediate object of same type as array dummy object!
            // 1-n: the real array element entry 
            final MetaObject intermediateArrayElementObject = (MetaObject) arrayContainerDummyObjectAttribute.getValue();
            objectHierarchy.add(intermediateArrayElementObject.getClassKey());
            hierarchyPath = getHierarchyPath(objectHierarchy);

            // the intermediate or element object is no dummy object!
            // it is a real instance of the intermediate array element class
            Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName()
                    + "' dummy array object's attribute '" + arrayContainerDummyObjectAttribute.getName()
                    + "'s value is no dummy object {" + hierarchyPath + "}",
                    intermediateArrayElementObject.isDummy());

            Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName()
                    + "' dummy array object's attribute '" + arrayContainerDummyObjectAttribute.getName()
                    + "'s value references correct object attribute {" + hierarchyPath + "}",
                    arrayContainerDummyObjectAttribute,
                    intermediateArrayElementObject.getReferencingObjectAttribute());

            // n-m: process intermediate objects
            if (arrayContainerDummyObjectAttribute.isArray()) {
                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                        + arrayAttribute.getName() + "' is n-m array: "
                        + "arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute() = false {" + hierarchyPath + "}",
                        arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute());
                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                        + arrayAttribute.getName() + "' is n-m array: "
                        + "arrayField.getMai().isVirtual() = false {" + hierarchyPath + "}",
                        arrayAttribute.getMai().isVirtual());
                Assert.assertFalse((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                        + arrayAttribute.getName() + "' is n-m array: "
                        + "arrayField.getMai().getForeignKeyClassId() < 0 = false {" + hierarchyPath + "}",
                        arrayAttribute.getMai().getForeignKeyClassId() < 0);
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute intermediate Array Element Object #"
                        + i + " is instance of class " + arrayAttribute.getMai().getForeignKeyClassId() + " {" + hierarchyPath + "}",
                        arrayAttribute.getMai().getForeignKeyClassId(),
                        intermediateArrayElementObject.getMetaClass().getID());

                // process array list (may be empty!)
                final ObjectAttribute[] intermediateArrayElementObjectAttributes = intermediateArrayElementObject.getAttribs();
                for (final ObjectAttribute intermediateArrayElementObjectAttribute : intermediateArrayElementObjectAttributes) {
                    Assert.assertSame((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                            + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #"
                            + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName()
                            + "' in meta object '" + metaObject.getName() + "'' parent reference correctly set {" + hierarchyPath + "}",
                            intermediateArrayElementObject,
                            intermediateArrayElementObjectAttribute.getParentObject());

                    // the attribute holding the real array element
                    if (intermediateArrayElementObjectAttribute.referencesObject()) {
                        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                                + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #"
                                + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName()
                                + "' in meta object '" + metaObject.getName() + "' is not null {" + hierarchyPath + "}",
                                intermediateArrayElementObjectAttribute.getValue());
                        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute value of '"
                                + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #"
                                + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName()
                                + "' in meta object '" + metaObject.getName() + "' is MetaObject {" + hierarchyPath + "}",
                                MetaObject.class.isAssignableFrom(intermediateArrayElementObjectAttribute.getValue().getClass()));

                        final MetaObject arrayElementObject = (MetaObject) intermediateArrayElementObjectAttribute.getValue();
                        objectHierarchy.add(arrayElementObject.getClassKey());
                        hierarchyPath = getHierarchyPath(objectHierarchy);

                        if (checkBackReference) {
                            final ObjectAttribute referencingObjectAttribute
                                    = arrayElementObject.getReferencingObjectAttribute();

                            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                    + metaClassName + "' (" + metaObject.getId()
                                    + "@" + metaObject.getClassKey() + ")'s array attribute Referencing Object Attribute of array element '"
                                    + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName()
                                    + "' in meta object '" + metaObject.getName() + " set {" + hierarchyPath + "}",
                                    referencingObjectAttribute);
                            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                    + metaClassName + "' (" + metaObject.getId()
                                    + "@" + metaObject.getClassKey() + ")'s array attribute Referencing Object Attribute of array element '"
                                    + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName()
                                    + "' in meta object '" + metaObject.getName() + " correctly set {" + hierarchyPath + "}",
                                    referencingObjectAttribute,
                                    intermediateArrayElementObjectAttribute);
                            Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                    + metaClassName + "' (" + metaObject.getId()
                                    + "@" + metaObject.getClassKey() + ")'s array attribute Parent Object of Referencing Object Attribute of array element '"
                                    + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName() + "' in meta object '"
                                    + metaObject.getName() + " set {" + hierarchyPath + "}",
                                    referencingObjectAttribute.getParentObject());
                            Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                    + metaClassName + "' (" + metaObject.getId()
                                    + "@" + metaObject.getClassKey() + ")'s array attribute Parent Object of Referencing Object Attribute of array element '"
                                    + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName()
                                    + "' in meta object '" + metaObject.getName() + " correctly set {" + hierarchyPath + "}",
                                    referencingObjectAttribute.getParentObject().getKey(),
                                    intermediateArrayElementObject.getKey());
                        }

                        objectHierarchy.remove(objectHierarchy.size() - 1);
                        // back reference to "master" class
                    } else if (!intermediateArrayElementObjectAttribute.isPrimaryKey()) {

                        // ArrayKeyFieldName set
                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName()
                                + "' ArrayKeyFieldName matches intermediate Array Element Object Attribute FieldName in intermediate Array Element Object #"
                                + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '"
                                + metaObject.getName() + "' is MetaObject {" + hierarchyPath + "}",
                                arrayContainerDummyObjectAttribute.getMai().getArrayKeyFieldName().toLowerCase(),
                                intermediateArrayElementObjectAttribute.getMai().getFieldName().toLowerCase());

                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName()
                                + "' ArrayKeyFieldName matches intermediate Array Element Object Attribute FieldName in intermediate Array Element Object #"
                                + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '"
                                + metaObject.getName() + "' is MetaObject {" + hierarchyPath + "}",
                                arrayAttribute.getMai().getArrayKeyFieldName().toLowerCase(),
                                intermediateArrayElementObjectAttribute.getMai().getFieldName().toLowerCase());

                        Assert.assertNotNull((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName()
                                + "' ArrayKeyField '" + intermediateArrayElementObjectAttribute.getMai().getFieldName()
                                + "' value is not null in intermediate Array Element Object #" + i + "'s attribute '"
                                + intermediateArrayElementObjectAttribute.getName() + "' in meta object '"
                                + metaObject.getName() + "' is MetaObject {" + hierarchyPath + "}",
                                intermediateArrayElementObjectAttribute.getValue());
                        Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName() + "' ArrayKeyField '"
                                + intermediateArrayElementObjectAttribute.getMai().getFieldName()
                                + "' value is Integer in intermediate Array Element Object #" + i + "'s attribute '"
                                + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + metaObject.getName()
                                + "' is MetaObject {" + hierarchyPath + "}",
                                Integer.class.isAssignableFrom(intermediateArrayElementObjectAttribute.getValue().getClass()));
                        Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute[" + i + "] '"
                                + arrayAttribute.getName() + "' ArrayKeyField '"
                                + intermediateArrayElementObjectAttribute.getMai().getFieldName()
                                + "'s value matches parent object id '"
                                + metaObject.getId() + "' {" + hierarchyPath + "}",
                                metaObject.getID(),
                                ((Integer) intermediateArrayElementObjectAttribute.getValue()).intValue());
                    }
                }
            } else { // 1-n
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                        + arrayAttribute.getName() + "' is 1-n array: "
                        + "arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute() = true {" + hierarchyPath + "}",
                        arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute());
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                        + arrayAttribute.getName() + "' is 1-n array: "
                        + "arrayField.getMai().isVirtual() = true {" + hierarchyPath + "}",
                        arrayAttribute.getMai().isVirtual());
                Assert.assertTrue((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                        + arrayAttribute.getName() + "' is 1-n array: "
                        + "arrayField.getMai().getForeignKeyClassId() < 0 = true {" + hierarchyPath + "}",
                        arrayAttribute.getMai().getForeignKeyClassId() < 0);
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object ' {" + hierarchyPath + "}"
                        + arrayAttribute.getName()
                        + "' is 1-n array: getForeignKeyClassId is negative {" + hierarchyPath + "}",
                        arrayContainerDummyObjectAttribute.getMai().getForeignKeyClassId(),
                        -1 * arrayContainerDummyObject.getClassID());
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '"
                        + arrayAttribute.getName()
                        + "' is 1-n array: arrayAttribute.getClassKey() is negative {" + hierarchyPath + "}",
                        0,
                        arrayAttribute.getClassKey().indexOf('-'));

                final MetaObject arrayElementObject = intermediateArrayElementObject;
                objectHierarchy.add(arrayElementObject.getClassKey());
                hierarchyPath = getHierarchyPath(objectHierarchy);

                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute Referencing Object Attribute of 1-n  array element '"
                        + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName()
                        + "' in meta object '" + metaObject.getName() + " correctly set {" + hierarchyPath + "}",
                        arrayElementObject.getReferencingObjectAttribute(),
                        arrayContainerDummyObjectAttribute);
                Assert.assertEquals((metaObject.isDummy() ? "DUMMY " : "") + "MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute Parent Object of Referencing Object Attribute of 1-n array element '"
                        + arrayElementObject.getName() + "' in array attribute '"
                        + arrayAttribute.getName() + "' in meta object '"
                        + metaObject.getName() + " correctly set {" + hierarchyPath + "}",
                        arrayElementObject.getReferencingObjectAttribute().getParentObject().getKey(),
                        arrayContainerDummyObject.getKey());
                objectHierarchy.remove(objectHierarchy.size() - 1);
            }
            objectHierarchy.remove(objectHierarchy.size() - 1);
        }
    }

    public static HashMap<MetaClass, List<MetaObject>> getAllInstancesByClass(final MetaObject metaObject) {
        final HashMap<MetaClass, List<MetaObject>> allInstances = new HashMap<MetaClass, List<MetaObject>>();
        final List<MetaObject> instances = new ArrayList<MetaObject>();

        instances.add(metaObject);
        allInstances.put(metaObject.getMetaClass(), instances);
        allInstancesByClass(metaObject, allInstances);

        return allInstances;
    }

    protected static void allInstancesByClass(
            final MetaObject metaObject,
            final HashMap<MetaClass, List<MetaObject>> allInstances) {
        //f

        for (final ObjectAttribute objectAttribute : metaObject.getAttributes().values()) {
            if (objectAttribute.referencesObject()
                    && objectAttribute.getValue() != null
                    && MetaObject.class.isAssignableFrom(objectAttribute.getValue().getClass())) {

                final MetaObject childMetaObject = (MetaObject) objectAttribute.getValue();
                final MetaClass childMetaClass = childMetaObject.getMetaClass();
                final List<MetaObject> instances;
                if (allInstances.containsKey(childMetaClass)) {
                    instances = allInstances.get(childMetaClass);
                } else {
                    instances = new ArrayList<MetaObject>();
                    allInstances.put(childMetaClass, instances);
                }

                instances.add(childMetaObject);
                allInstancesByClass(childMetaObject, allInstances);
            }
        }
    }

    public static HashMap<Object, List<MetaObject>> getAllInstancesByKey(final MetaObject metaObject) {
        final HashMap<Object, List<MetaObject>> allInstances = new HashMap<Object, List<MetaObject>>();
        final List<MetaObject> instances = new ArrayList<MetaObject>();

        instances.add(metaObject);
        allInstances.put(metaObject.getKey(), instances);
        allInstancesByKey(metaObject, allInstances);

        return allInstances;
    }

    protected static void allInstancesByKey(
            final MetaObject metaObject,
            final HashMap<Object, List<MetaObject>> allInstances) {

        for (final ObjectAttribute objectAttribute : metaObject.getAttributes().values()) {
            if (objectAttribute.referencesObject()
                    && objectAttribute.getValue() != null
                    && MetaObject.class.isAssignableFrom(objectAttribute.getValue().getClass())) {

                final MetaObject childMetaObject = (MetaObject) objectAttribute.getValue();
                final List<MetaObject> instances;
                if (allInstances.containsKey(childMetaObject.getKey())) {
                    instances = allInstances.get(childMetaObject.getKey());
                } else {
                    instances = new ArrayList<MetaObject>();
                    allInstances.put(childMetaObject.getKey(), instances);
                }

                instances.add(childMetaObject);
                allInstancesByKey(childMetaObject, allInstances);
            }
        }
    }

    private static String getHierarchyPath(final List<String> objectHierarchy) {
        StringBuilder sb = new StringBuilder();
        final Iterator<String> iterator = objectHierarchy.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append('/');
            }
        }

        return sb.toString();
    }

    public static void compareInstances(
            final MetaObject metaObject,
            final boolean intraObjectCacheEnlabled) {

        // compare instances of the same meta object (same object key)
        final HashMap<Object, List<MetaObject>> allInstances
                = MetaObjectIntegrityTest.getAllInstancesByKey(metaObject);
        for (final Object key : allInstances.keySet()) {
            final List<MetaObject> instances = allInstances.get(key);

            if (instances.size() > 1) {
                MetaObject cachedObject1 = null;
                for (final MetaObject cachedObject2 : instances) {

                    // ignore array dummy and intermediate objects 
                    if (cachedObject2.isDummy()
                            || (cachedObject2.getReferencingObjectAttribute() != null
                            && cachedObject2.getReferencingObjectAttribute().isArray())) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.warn("ignoring parent meta object '"
                                    + metaObject.getName() + "' (" + metaObject.getKey() + ") in "
                                    + instances.size() + " cached instances of meta object '"
                                    + cachedObject2.getName() + "' (" + cachedObject2.getKey()
                                    + "): is dummy (" + cachedObject2.isDummy()
                                    + ") or intermdiate array helper object (" + cachedObject2.getReferencingObjectAttribute().isArray() + ")");
                        }
                        continue;
                    }

                    // WORKAROUND for spielhalle/betreiber/spielhalle[] -> self-cycle
                    if (cachedObject2.getKey().equals(metaObject.getKey())) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.warn("ignoring parent meta object "
                                    + metaObject.getName() + "' (" + metaObject.getKey() + ") in "
                                    + instances.size() + " cached instances of meta object '"
                                    + cachedObject2.getName() + "' (" + cachedObject2.getKey() + ")");
                            LOGGER.debug(metaObject.getDebugString());
                        }
                        break;
                    }

                    // regardless of object hierarchy
                    if (cachedObject1 == null) {
                        // compare in next loop
                        cachedObject1 = cachedObject2;
                    } else {
                        LOGGER.debug("checking " + instances.size() + " cached instances of meta object '"
                                + cachedObject2.getName() + "' (" + cachedObject2.getKey() + ") in parent meta object "
                                + metaObject.getName() + "' (" + metaObject.getKey() + ")");
                        if (intraObjectCacheEnlabled) {
                            Assert.assertSame(instances.size() + " cached instances of meta object '"
                                    + cachedObject2.getName() + "' (" + cachedObject2.getKey() + ") in parent meta object "
                                    + metaObject.getName() + "' (" + metaObject.getKey()
                                    + ") are the same instances since IntraObjectCache is enabled",
                                    cachedObject1, cachedObject2);
                        } else {
                            Assert.assertNotSame(instances.size() + " cached instances of meta object '"
                                    + cachedObject2.getName() + "' (" + cachedObject2.getKey() + ") in parent meta object "
                                    + metaObject.getName() + "' (" + metaObject.getKey()
                                    + ") are not the same instances since IntraObjectCache is disabled",
                                    cachedObject1, cachedObject2);
                        }

                        // important: set referencing oa check to false!
                        LegacyRESTfulInterfaceTest.compareMetaObjects(cachedObject1,
                                cachedObject2, true, !intraObjectCacheEnlabled, false, false);
                    }
                }
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="MAIN METHOD -------------------------------------------------------------">
    public static final void main(String args[]) {

        try {

            final Properties log4jProperties = new Properties();
            log4jProperties.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
            log4jProperties.put("log4j.appender.Remote.remoteHost", "localhost");
            log4jProperties.put("log4j.appender.Remote.port", "4445");
            log4jProperties.put("log4j.appender.Remote.locationInfo", "true");
            log4jProperties.put("log4j.rootLogger", "ALL,Remote");
            org.apache.log4j.PropertyConfigurator.configure(log4jProperties);

            final Properties PROPERTIES = TestEnvironment.getProperties();
            final RESTfulSerialInterfaceConnector connector
                    = new RESTfulSerialInterfaceConnector("http://localhost:9986/callserver/binary");

            final User user = connector.getUser(PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    PROPERTIES.getProperty("userDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("username", "admin"),
                    PROPERTIES.getProperty("password", "cismet"));

            MetaObject metaObject = connector.getMetaObject(user, 1, 7, "CIDS_REF");

            checkMetaObjectIntegrity(metaObject);
//            checkArrayAttributes(metaObject,
//                    metaObject.getAttributeByFieldName("kategorien"),
//                    new ArrayList<String>(Arrays.asList(new String[]{metaObject.getClassKey()})));

//            metaObject = connector.getMetaObject(user, 1, 8, "CIDS_REF");
//            checkArrayAttributes(metaObject,
//                    metaObject.getAttributeByFieldName("spielhallen"),
//                    new ArrayList<String>(Arrays.asList(new String[]{metaObject.getClassKey()})));
            LOGGER.info("MetaObjectIntegrityTest passed!");

        } catch (AssertionError ae) {
            LOGGER.error(ae.getMessage(), ae);
            System.exit(1);
        } catch (Exception ex) {
            LOGGER.fatal(ex.getMessage(), ex);
            System.exit(1);
        }

    }
    // </editor-fold>

}
