package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.impls.domainserver.OfflineMetaClassCacheService;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.cismet.cids.utils.MetaClassCacheService;
import java.lang.reflect.Proxy;
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
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObject.equals(metaObject) ",
                    metaObject.equals(metaObject));

            final MetaObject metaObjectReference
                    = MetaObjectReference.getInstance(metaObject, metaObject.getReferencingObjectAttribute());

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is a MetaObject",
                    MetaObject.class.isAssignableFrom(metaObjectReference.getClass()));

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is an instance of MetaObject",
                    metaObjectReference instanceof MetaObject);

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is a MetaObject",
                    MetaObject.class.isAssignableFrom(metaObjectReference.getClass()));

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference is an instance of java.lang.reflect.Proxy",
                    metaObjectReference instanceof Proxy);

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObject.equals(metaObject) ",
                    metaObjectReference.equals(metaObject));

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference.equals(metaObjectReference) ",
                    metaObjectReference.equals(metaObjectReference));

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObjectReference.equals(metaObject) ",
                    metaObjectReference.equals(metaObject));

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): metaObject.equals(metaObjectReference) ",
                    metaObject.equals(metaObjectReference));

            Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
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
    public static void checkMetaObjectIntegrity(final MetaObject metaObject) {
        checkMetaObjectIntegrity(metaObject, new ArrayList<String>());
    }

    private static void checkMetaObjectIntegrity(final MetaObject metaObject, final List<String> objectHierarchy) {

        objectHierarchy.add(metaObject.getClassKey());
        final String hierarchyPath = getHierarchyPath(objectHierarchy);
        final MetaClass metaClass = metaObject.getMetaClass();
        final String metaClassName;

        if (metaObject.getClassID() >= 0) {
            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaObject.getMetaClass() + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): MetaClass is not null {" + hierarchyPath + "}",
                    metaObject.getMetaClass());

            metaClassName = metaClass.getTableName();
            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getClassID() matches getMetaClass().getID() {" + hierarchyPath + "}",
                    metaClass.getID(),
                    metaObject.getClassID());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getClassKey() matches getMetaClass().getKey() {" + hierarchyPath + "}",
                    metaClass.getKey().toString(),
                    metaObject.getClassKey());

            // FIXME #174: key in hashmap should be metaClass.getKey()! ------------
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAllClasses() contains class key '"
                    + metaObject.getClassKey() + "' {" + hierarchyPath + "}",
                    metaObject.getAllClasses().containsKey(metaClass.getDomain() + metaClass.getId()));

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAllClasses() contains correct MetaClass '"
                    + metaObject.getClassKey() + "' instance {" + hierarchyPath + "}",
                    metaClass,
                    metaObject.getAllClasses().get(metaClass.getDomain() + metaClass.getId()));
        } else {
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaObject.getMetaClass() + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): is Dummy since ClassId is negative {" + hierarchyPath + "}",
                    metaObject.isDummy());

            Assert.assertNull("MetaObject '" + metaObject.getName() + "|"
                    + metaObject.getMetaClass() + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): MetaClass of virtual dummy object is null {" + hierarchyPath + "}",
                    metaObject.getMetaClass());

            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaObject.getMetaClass() + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getReferencingObjectAttribute of virtual dummy object is not null {" + hierarchyPath + "}",
                    metaObject.getReferencingObjectAttribute());

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaObject.getMetaClass() + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getReferencingObjectAttribute of virtual dummy object is VirtualOneToManyAttribute {" + hierarchyPath + "}",
                    metaObject.getReferencingObjectAttribute().isVirtualOneToManyAttribute());

            Assert.assertNull("MetaObject '" + metaObject.getName() + "|"
                    + metaObject.getMetaClass() + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): MetaClass of virtual dummy object is null {" + hierarchyPath + "}",
                    metaObject.getMetaClass());

            metaClassName = metaObject.getClassKey();
        }

        // ---------------------------------------------------------------------
        Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttribs() is not empty {" + hierarchyPath + "}",
                metaObject.getAttributes().isEmpty());

        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
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

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributes() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") key {" + hierarchyPath + "}",
                    metaObject.getAttributes().containsKey(objectAttribute.getKey()));

            Assert.assertSame("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributes() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    objectAttribute,
                    metaObject.getAttributes().get(objectAttribute.getKey()));

            // getAttribute expects the name of the attribute, not the key! #174
            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttribute() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") key {" + hierarchyPath + "}",
                    metaObject.getAttribute(objectAttribute.getName()));

            Assert.assertSame("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttribute() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    objectAttribute,
                    metaObject.getAttribute(objectAttribute.getName()));
            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByFieldName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") key {" + hierarchyPath + "}",
                    metaObject.getAttributeByFieldName(objectAttribute.getMai().getFieldName()));

            Assert.assertSame("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByFieldName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    objectAttribute,
                    metaObject.getAttributeByFieldName(objectAttribute.getMai().getFieldName()));

            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") names {" + hierarchyPath + "}",
                    metaObject.getAttributeByName(objectAttribute.getName(), Integer.MAX_VALUE));

            Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") name {" + hierarchyPath + "}",
                    metaObject.getAttributeByName(objectAttribute.getName(), Integer.MAX_VALUE).isEmpty());

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributeByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    metaObject.getAttributeByName(objectAttribute.getName(), Integer.MAX_VALUE).contains(objectAttribute));

            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") names {" + hierarchyPath + "}",
                    metaObject.getAttributesByName(Arrays.asList(new String[]{objectAttribute.getName()})));

            Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") name {" + hierarchyPath + "}",
                    metaObject.getAttributesByName(Arrays.asList(new String[]{objectAttribute.getName()})).isEmpty());

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName() contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    metaObject.getAttributesByName(Arrays.asList(new String[]{objectAttribute.getName()})).contains(objectAttribute));

            // check value -----------------------------------------------------
            if (value != null) {
                Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributesByType() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") names {" + hierarchyPath + "}",
                        metaObject.getAttributesByType(value.getClass(), 0));

                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributesByType() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") name {" + hierarchyPath + "}",
                        metaObject.getAttributesByType(value.getClass(), 0).isEmpty());

                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributesByType() contains attribute '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                        metaObject.getAttributesByType(value.getClass(), 0).contains(objectAttribute));

                if (Sirius.server.localserver.object.Object.class.isAssignableFrom(value.getClass())) {
                    Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") references Object {" + hierarchyPath + "}",
                            objectAttribute.referencesObject());
                }

                if (objectAttribute.referencesObject()) {
                    Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") is Sirius Object {" + hierarchyPath + "}",
                            Sirius.server.localserver.object.Object.class.isAssignableFrom(value.getClass()));

                    Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") is MetaObject {" + hierarchyPath + "}",
                            MetaObject.class.isAssignableFrom(value.getClass()));

                    final MetaObject valueMetaObject = (MetaObject) value;
                    final ObjectAttribute referencingObjectAttribute = valueMetaObject.getReferencingObjectAttribute();
                    Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ")' MetaObject ReferencingObjectAttribute is not null {" + hierarchyPath + "}",
                            referencingObjectAttribute);

                    Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] '" + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ") MetaObject ReferencingObjectAttribute '"
                            + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey() + ") matches attribute's key {" + hierarchyPath + "}",
                            objectAttribute.getKey(),
                            valueMetaObject.getReferencingObjectAttribute().getKey());

                    Assert.assertSame("MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ")' MetaObject ReferencingObjectAttribute matches attribute instance {" + hierarchyPath + "}",
                            objectAttribute,
                            valueMetaObject.getReferencingObjectAttribute());

                    // FIXME: check negative ids
//                    if (objectAttribute.isVirtualOneToManyAttribute()) {
//                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
//                                + metaClassName + "' (" + metaObject.getId()
//                                + "@" + metaObject.getClassKey() + ")'s virtual array object attribute "
//                                + "[" + i + "] '" + objectAttribute.getName() + "' (" + objectAttribute.getKey()
//                                + ")'s  ClassKey matches valueMetaObject's ClassKey",
//                                "-" + valueMetaObject.getClassKey(),
//                                objectAttribute.getClassKey());
//                    } else {
                    Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                            + metaClassName + "' (" + metaObject.getId()
                            + "@" + metaObject.getClassKey() + ")'s object attribute "
                            + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                            + ")'s  ClassKey matches valueMetaObject's ClassKey {" + hierarchyPath + "}",
                            valueMetaObject.getClassKey(),
                            objectAttribute.getClassKey());
                    //}

                    // TODO: check if object and attribute editors / renderers are the same
                    // =========================================================
                    if (valueMetaObject.getComplexEditor() != null) {
                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ") getComplexEditor() matches valueMetaObject.getComplexEditor() {" + hierarchyPath + "}",
                                valueMetaObject.getComplexEditor(),
                                objectAttribute.getComplexEditor());
                    }

                    if (valueMetaObject.getRenderer() != null) {
                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ") getRenderer() matches  valueMetaObject.getRenderer() {" + hierarchyPath + "}",
                                valueMetaObject.getRenderer(),
                                objectAttribute.getRenderer());
                    }

                    if (valueMetaObject.getSimpleEditor() != null) {
                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
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
                    if (arrayKeyFieldName != null && !arrayKeyFieldName.isEmpty()) {
                        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                                + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ")' valueMetaObject is dummy meta object {" + hierarchyPath + "}",
                                valueMetaObject.isDummy());
                    }

                    if (objectAttribute.isArray() || objectAttribute.isVirtualOneToManyAttribute()) {
                        checkArrayAttributes(metaObject, objectAttribute);
                    }

                    // recursively check integrity
                    checkMetaObjectIntegrity(valueMetaObject, new ArrayList<String>(objectHierarchy));
                }
            }

            // end check value -----------------------------------------------------
            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ")' ParentObject is not null {" + hierarchyPath + "}",
                    objectAttribute.getParentObject());
            Assert.assertSame("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ")' matches metaObject instance {" + hierarchyPath + "}",
                    metaObject,
                    objectAttribute.getParentObject());
            if (!objectAttribute.isArray()
                    && !objectAttribute.isVirtualOneToManyAttribute()
                    && !objectAttribute.isSubstitute()
                    && !objectAttribute.getMai().isExtensionAttribute()) {

                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + "): getAttributes() order matches '"
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") position defined in metaClass {" + hierarchyPath + "}",
                        objectAttribute.getMai().getPosition(),
                        i);
            }

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getClassID() matches  metaObject.getClassID() {" + hierarchyPath + "}",
                    metaObject.getClassID(),
                    objectAttribute.getClassID());

            final MemberAttributeInfo memberAttributeInfo = objectAttribute.getMai();
            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ")'s MemberAttributeInfo is not null  {" + hierarchyPath + "}",
                    memberAttributeInfo);

            if (metaClass != null) {
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s MemberAttributeInfo key found in metaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
                        metaClass.getMemberAttributeInfos().containsKey(memberAttributeInfo.getKey()));

                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s MemberAttributeInfo found in metaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
                        ((MemberAttributeInfo) metaClass.getMemberAttributeInfos().get(memberAttributeInfo.getKey())).getKey(),
                        memberAttributeInfo.getKey());

                Assert.assertSame("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s MemberAttributeInfo instance found in metaClass.getMemberAttributeInfos() {" + hierarchyPath + "}",
                        metaClass.getMemberAttributeInfos().get(memberAttributeInfo.getKey()),
                        memberAttributeInfo);
            }

            if (memberAttributeInfo.getArrayKeyFieldName() != null
                    && !memberAttributeInfo.getArrayKeyFieldName().isEmpty()) {
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")' is array since ArrayKeyFieldName '"
                        + memberAttributeInfo.getArrayKeyFieldName() + "' is set {" + hierarchyPath + "}",
                        objectAttribute.isArray());
            }

            if (objectAttribute.isArray() && !objectAttribute.isVirtualOneToManyAttribute()) {
                Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ")'s  ArrayKeyFieldName is not null {" + hierarchyPath + "}",
                        memberAttributeInfo.getArrayKeyFieldName());
            }

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getClassID() matches  memberAttributeInfo.getClassId() {" + hierarchyPath + "}",
                    memberAttributeInfo.getClassId(),
                    objectAttribute.getClassID());

            if (memberAttributeInfo.getComplexEditor() != null) {
                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") getComplexEditor() matches  memberAttributeInfo.getComplexEditor() {" + hierarchyPath + "}",
                        memberAttributeInfo.getComplexEditor(),
                        objectAttribute.getComplexEditor());
            }

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getSimpleEditor() matches  memberAttributeInfo.getEditor() {" + hierarchyPath + "}",
                    memberAttributeInfo.getEditor(),
                    objectAttribute.getSimpleEditor());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getRenderer() matches  memberAttributeInfo.getRenderer() {" + hierarchyPath + "}",
                    memberAttributeInfo.getRenderer(),
                    objectAttribute.getRenderer());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") getTypeId() matches  memberAttributeInfo.getTypeId() {" + hierarchyPath + "}",
                    memberAttributeInfo.getTypeId(),
                    objectAttribute.getTypeId());
// FIXME: #174   
//            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
//                    + metaClassName + "' (" + metaObject.getId()
//                    + "@" + metaObject.getClassKey() + ")'s attribute "
//                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
//                    + ") getJavaType() matches  memberAttributeInfo.getJavaclassname()",
//                    memberAttributeInfo.getJavaclassname(),
//                    objectAttribute.getJavaType());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") isArray() matches  memberAttributeInfo.isArray() {" + hierarchyPath + "}",
                    memberAttributeInfo.isArray(),
                    objectAttribute.isArray());

            if (memberAttributeInfo.isExtensionAttribute()
                    || memberAttributeInfo.getForeignKeyClassId() < 0) {

                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s extension attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") must be a virtual attribute {" + hierarchyPath + "}",
                        memberAttributeInfo.isVirtual());

                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s extension attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") getJavaType must be java.lang.Object {" + hierarchyPath + "}",
                        java.lang.Object.class.getCanonicalName(),
                        memberAttributeInfo.getJavaclassname());

                if (metaClass != null) {
                    if (memberAttributeInfo.isExtensionAttribute()) {
                        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s extension attribute "
                                + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                                + ") recognized in MetaClass {" + hierarchyPath + "}",
                                metaClass.hasExtensionAttributes());
                    }
                }
            }

            if (objectAttribute.isArray()) {
                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no virtual attribute {" + hierarchyPath + "}",
                        objectAttribute.isVirtualOneToManyAttribute());
                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no virtual attribute {" + hierarchyPath + "}",
                        memberAttributeInfo.isVirtual());
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") isForeignKey {" + hierarchyPath + "}",
                        memberAttributeInfo.isForeignKey());
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") referencesObject {" + hierarchyPath + "}",
                        objectAttribute.referencesObject());
            }

            if (objectAttribute.isVirtualOneToManyAttribute()) {
                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no virtual attribute {" + hierarchyPath + "}",
                        objectAttribute.isArray());
                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") is no array attribute {" + hierarchyPath + "}",
                        memberAttributeInfo.isArray());
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") isForeignKey {" + hierarchyPath + "}",
                        memberAttributeInfo.isForeignKey());
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s virtual attribute "
                        + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                        + ") referencesObject {" + hierarchyPath + "}",
                        objectAttribute.referencesObject());
            }

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") isOptional() matches  memberAttributeInfo.isOptional() {" + hierarchyPath + "}",
                    memberAttributeInfo.isOptional(),
                    objectAttribute.isOptional());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s attribute "
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey()
                    + ") isSubstitute() matches  memberAttributeInfo.isSubstitute() {" + hierarchyPath + "}",
                    memberAttributeInfo.isSubstitute(),
                    objectAttribute.isSubstitute());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
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

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttributesByName returns attributes {" + hierarchyPath + "}",
                attributeCollection);

        Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttributesByName returns attribute {" + hierarchyPath + "}s",
                attributeCollection.isEmpty());

        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getAttributesByName matches attributes objectAttributes size {" + hierarchyPath + "}",
                objectAttributes.length,
                attributeCollection.size());

        for (final ObjectAttribute objectAttribute : objectAttributes) {
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getAttributesByName contains attribute '"
                    + "[" + i + "] " + objectAttribute.getName() + "' (" + objectAttribute.getKey() + ") instance {" + hierarchyPath + "}",
                    attributeCollection.contains(objectAttribute));
        }

        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): equals(metaObject)  {" + hierarchyPath + "}",
                metaObject.equals(metaObject));

        if (metaObject.getStatus() == MetaObject.NEW) {
            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): Status is NEW if id is -1 {" + hierarchyPath + "}",
                    -1,
                    metaObject.getId());
        }

        if (metaObject.getStatus() == MetaObject.TEMPLATE) {
            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): Status is NEW if id is -1 {" + hierarchyPath + "}",
                    -1,
                    metaObject.getId());
        }

        if (metaObject.getId() == -1) {
            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): Status is NEW if id is -1 {" + hierarchyPath + "}",
                    MetaObject.NEW,
                    metaObject.getStatus());
        }

        if (metaClass != null) {

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getComplexEditor() matches metaClass.getComplexEditor() {" + hierarchyPath + "}",
                    metaClass.getComplexEditor(),
                    metaObject.getComplexEditor());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getDomain() matches metaClass.getDomain() {" + hierarchyPath + "}",
                    metaClass.getDomain(),
                    metaObject.getDomain());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getEditor() matches metaClass.getEditor() {" + hierarchyPath + "}",
                    metaClass.getEditor(),
                    metaObject.getEditor());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getGroup() matches metaClass.getGroup() {" + hierarchyPath + "}",
                    metaClass.getGroup(),
                    metaObject.getGroup());
        }

        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getID matches getId() {" + hierarchyPath + "}",
                metaObject.getID(),
                metaObject.getId());

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getKey() set {" + hierarchyPath + "}",
                metaObject.getKey());

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getPrimaryKey() set {" + hierarchyPath + "}",
                metaObject.getPrimaryKey());

        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getPrimaryKey() isPrimaryKey  {" + hierarchyPath + "}",
                metaObject.getPrimaryKey().isPrimaryKey());

        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getPrimaryKey() isPrimaryKey  {" + hierarchyPath + "}",
                metaObject.getPrimaryKey().isPrimaryKey());

        if (metaClass != null) {
            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getPrimaryKey() matches metaClass.getPrimaryKey() {" + hierarchyPath + "}",
                    metaClass.getPrimaryKey(),
                    ((ObjectAttribute) metaObject.getPrimaryKey()).getMai().getFieldName());
        }

        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")' primary key attribute '"
                + metaObject.getPrimaryKey().getName() + "' (" + metaObject.getPrimaryKey().getKey()
                + ")' in getAttributes() {" + hierarchyPath + "}",
                metaObject.getAttributes().containsValue(metaObject.getPrimaryKey()));

        // getReferencingObjectAttribute ---------------------------------------
        if (metaObject.getReferencingObjectAttribute() != null) {
            final ObjectAttribute referencingObjectAttribute = metaObject.getReferencingObjectAttribute();

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' references Object {" + hierarchyPath + "}",
                    referencingObjectAttribute.referencesObject());

            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' isForeignKey {" + hierarchyPath + "}",
                    referencingObjectAttribute.getMai().isForeignKey());

            if (metaObject.isDummy()) {
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                        + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                        + ")' must be array if value is a dummy meta object {" + hierarchyPath + "}",
                        referencingObjectAttribute.isArray() || referencingObjectAttribute.isVirtualOneToManyAttribute());
            }

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' ForeignKeyClassId matches metaObject.getClassID() {" + hierarchyPath + "}",
                    metaObject.getClassID(),
                    referencingObjectAttribute.getMai().getForeignKeyClassId());

            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' object not null {" + hierarchyPath + "}",
                    referencingObjectAttribute.getValue());

            Assert.assertSame("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")' referencing object attribute '"
                    + referencingObjectAttribute.getName() + "' (" + referencingObjectAttribute.getKey()
                    + ")' object matches meta object instance  {" + hierarchyPath + "}",
                    metaObject,
                    referencingObjectAttribute.getValue());
        }

        if (metaClass != null) {
            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getRenderer matches metaClass.getRenderer() {" + hierarchyPath + "}",
                    metaClass.getRenderer(),
                    metaObject.getRenderer());

            Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + "): getRenderer matches metaClass.getSimpleEditor() {" + hierarchyPath + "}",
                    metaClass.getSimpleEditor(),
                    metaObject.getSimpleEditor());
        }

        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getStatus is valid status {" + hierarchyPath + "}",
                (metaObject.getStatus() == MetaObject.MODIFIED
                || metaObject.getStatus() == MetaObject.NEW
                || metaObject.getStatus() == MetaObject.NO_STATUS
                || metaObject.getStatus() == MetaObject.TEMPLATE
                || metaObject.getStatus() == MetaObject.TO_DELETE));

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + "): getStatusDebugString is not null {" + hierarchyPath + "}",
                metaObject.getStatusDebugString());
    }

    protected static void checkArrayAttributes(final MetaObject metaObject,
            final ObjectAttribute arrayAttribute) throws AssertionError {

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s meta class is not null",
                metaObject.getMetaClass());

        final String metaClassName = metaObject.getMetaClass().getTableName();

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName() + "' attribute available in meta object '" + metaObject.getName() + "'",
                arrayAttribute);

        // WARNING: isArray is not set on 1-n array attributes!
        if (arrayAttribute.isArray()) {
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is n-m array: "
                    + "arrayField.getMai().getForeignKeyClassId() >= 0 = true",
                    arrayAttribute.getMai().getForeignKeyClassId() >= 0);
            Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName()
                    + "' is no Virtual One To Many Attribute (n-m array)",
                    arrayAttribute.isVirtualOneToManyAttribute());
        } else {
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is 1-n array: "
                    + "arrayField.getMai().getForeignKeyClassId() < 0 = true",
                    arrayAttribute.getMai().getForeignKeyClassId() < 0);
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName()
                    + "' is Virtual One To Many Attribute (1-n array)",
                    arrayAttribute.isVirtualOneToManyAttribute());
        }

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName() + "' is not null",
                arrayAttribute.getValue());
        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName() + "' is MetaObject",
                MetaObject.class.isAssignableFrom(arrayAttribute.getValue().getClass()));

        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName() + "'' array Container Dummy Object is not null",
                arrayAttribute.getValue());

        final MetaObject arrayContainerDummyObject = (MetaObject) arrayAttribute.getValue();

        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                + metaClassName + "' (" + metaObject.getId()
                + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName() + "' is array Container Dummy Object",
                arrayContainerDummyObject.isDummy());

        if (arrayAttribute.isArray()) {
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName() + "' array Container Dummy Object's Class ID is not negative",
                    arrayContainerDummyObject.getClassID() >= 0);
        } else {
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName() + "' array Container Dummy Object's Class ID is negative",
                    arrayContainerDummyObject.getClassID() < 0);
            Assert.assertNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute in meta object '" + metaObject.getName() + "' array Container Dummy Object's MetaClass is null",
                    arrayContainerDummyObject.getClassID() < 0);
        }

        final ObjectAttribute[] arrayContainerDummyObjectAttributes = arrayContainerDummyObject.getAttribs();
        int i = 0;

        for (final ObjectAttribute arrayContainerDummyObjectAttribute : arrayContainerDummyObjectAttributes) {
            i++;
            Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #" + i + " in meta object '" + metaObject.getName() + "' is not null",
                    arrayContainerDummyObjectAttribute.getValue());
            Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                    + metaClassName + "' (" + metaObject.getId()
                    + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #" + i + " in meta object '" + metaObject.getName() + "' is MetaObject",
                    MetaObject.class.isAssignableFrom(arrayContainerDummyObjectAttribute.getValue().getClass()));
            final MetaObject intermediateArrayElementObject = (MetaObject) arrayContainerDummyObjectAttribute.getValue();

            // n-m: process intermediate objects
            if (arrayContainerDummyObjectAttribute.isArray()) {
                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is n-m array: "
                        + "arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute() = false",
                        arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute());
                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is n-m array: "
                        + "arrayField.getMai().isVirtual() = false",
                        arrayAttribute.getMai().isVirtual());
                Assert.assertFalse("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is n-m array: "
                        + "arrayField.getMai().getForeignKeyClassId() < 0 = false",
                        arrayAttribute.getMai().getForeignKeyClassId() < 0);
                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute intermediate Array Element Object #" + i + " is instance of class " + arrayAttribute.getMai().getForeignKeyClassId(),
                        arrayAttribute.getMai().getForeignKeyClassId(),
                        intermediateArrayElementObject.getMetaClass().getID());

                // process array list (may be empty!)
                final ObjectAttribute[] intermediateArrayElementObjectAttributes = intermediateArrayElementObject.getAttribs();
                for (final ObjectAttribute intermediateArrayElementObjectAttribute : intermediateArrayElementObjectAttributes) {
                    if (intermediateArrayElementObjectAttribute.referencesObject()) {
                        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + metaObject.getName() + "' is not null",
                                intermediateArrayElementObjectAttribute.getValue());
                        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute value of '" + arrayAttribute.getName() + "' attribute's intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + metaObject.getName() + "' is MetaObject",
                                MetaObject.class.isAssignableFrom(intermediateArrayElementObjectAttribute.getValue().getClass()));

                        final MetaObject arrayElementObject = (MetaObject) intermediateArrayElementObjectAttribute.getValue();
                        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute Referencing Object Attribute of array element '" + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName() + "' in meta object '" + metaObject.getName() + " set",
                                arrayElementObject.getReferencingObjectAttribute());
                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute Referencing Object Attribute of array element '" + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName() + "' in meta object '" + metaObject.getName() + " correctly set",
                                arrayElementObject.getReferencingObjectAttribute(),
                                intermediateArrayElementObjectAttribute);
                        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute Parent Object of Referencing Object Attribute of array element '" + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName() + "' in meta object '" + metaObject.getName() + " set",
                                arrayElementObject.getReferencingObjectAttribute().getParentObject());
                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute Parent Object of Referencing Object Attribute of array element '" + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName() + "' in meta object '" + metaObject.getName() + " correctly set",
                                arrayElementObject.getReferencingObjectAttribute().getParentObject().getKey(),
                                intermediateArrayElementObject.getKey());

                    } else if (!intermediateArrayElementObjectAttribute.isPrimaryKey()) {
                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName() + "' ArrayKeyFieldName matches intermediate Array Element Object Attribute FieldName in intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + metaObject.getName() + "' is MetaObject",
                                arrayContainerDummyObjectAttribute.getMai().getArrayKeyFieldName().toLowerCase(),
                                intermediateArrayElementObjectAttribute.getMai().getFieldName().toLowerCase());
                        Assert.assertNotNull("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName() + "' ArrayKeyField '" + intermediateArrayElementObjectAttribute.getMai().getFieldName() + "' value is not null in intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + metaObject.getName() + "' is MetaObject",
                                intermediateArrayElementObjectAttribute.getValue());
                        Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute '" + arrayAttribute.getName() + "' ArrayKeyField '"
                                + intermediateArrayElementObjectAttribute.getMai().getFieldName()
                                + "' value is Integer in intermediate Array Element Object #" + i + "'s attribute '"
                                + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + metaObject.getName()
                                + "' is MetaObject",
                                Integer.class.isAssignableFrom(intermediateArrayElementObjectAttribute.getValue().getClass()));
                        Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                                + metaClassName + "' (" + metaObject.getId()
                                + "@" + metaObject.getClassKey() + ")'s array attribute[" + i + "] '" + arrayAttribute.getName() + "' ArrayKeyField '"
                                + intermediateArrayElementObjectAttribute.getMai().getFieldName() + "'s value matches parent object id '"
                                + metaObject.getId() + "'",
                                metaObject.getID(),
                                ((Integer) intermediateArrayElementObjectAttribute.getValue()).intValue());
                    }
                }
            } else { // 1-n
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is 1-n array: "
                        + "arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute() = true",
                        arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute());
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is 1-n array: "
                        + "arrayField.getMai().isVirtual() = true",
                        arrayAttribute.getMai().isVirtual());
                Assert.assertTrue("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName() + "' is 1-n array: "
                        + "arrayField.getMai().getForeignKeyClassId() < 0 = true",
                        arrayAttribute.getMai().getForeignKeyClassId() < 0);
                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute dummy array object '" + arrayAttribute.getName()
                        + "' is 1-n array: arrayAttribute.getClassKey() is negative",
                        0,
                        arrayAttribute.getClassKey().indexOf('-'));

                final MetaObject arrayElementObject = intermediateArrayElementObject;
                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute Referencing Object Attribute of 1-n  array element '" + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName() + "' in meta object '" + metaObject.getName() + " correctly set",
                        arrayElementObject.getReferencingObjectAttribute(),
                        arrayContainerDummyObjectAttribute);
                Assert.assertEquals("MetaObject '" + metaObject.getName() + "|"
                        + metaClassName + "' (" + metaObject.getId()
                        + "@" + metaObject.getClassKey() + ")'s array attribute Parent Object of Referencing Object Attribute of 1-n array element '" + arrayElementObject.getName() + "' in array attribute '" + arrayAttribute.getName() + "' in meta object '" + metaObject.getName() + " correctly set",
                        arrayElementObject.getReferencingObjectAttribute().getParentObject().getKey(),
                        arrayContainerDummyObject.getKey());
            }
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

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="MAIN METHOD -------------------------------------------------------------">
    public static final void main(String args[]) {

        assert false : "assertion error";
        System.out.println("Still running?!");
        // java -enableassertions
        // -> Exception in thread "main" java.lang.AssertionError: assertion error
    }
    // </editor-fold>

}
