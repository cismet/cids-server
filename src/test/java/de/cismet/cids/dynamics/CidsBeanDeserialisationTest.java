package de.cismet.cids.dynamics;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectIntegrityTest;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.cismet.cids.integrationtests.LegacyRESTfulInterfaceTest;
import de.cismet.cids.json.IntraObjectCacheJsonParams;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@RunWith(PowerMockRunner.class)
@RunWith(DataProviderRunner.class)
//@PowerMockRunnerDelegate(DataProviderRunner.class)
//@PrepareForTest(IdGenerator.class)
public class CidsBeanDeserialisationTest extends AbstractCidsBeanDeserialisationTest {

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void test00DeserializeCidsBeanIntraObjectCacheDisabled(final String cidsBeanJson) throws Exception {

        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(false, cidsBeanJson);
            final MetaObject metaObject = cidsBean.getMetaObject();
            LOGGER.debug("test00DeserializeCidsBeanIntraObjectCacheDisabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + "): '" + metaObject.getName() + "'");

            MetaObjectIntegrityTest.checkMetaObjectIntegrity(metaObject);

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
                                && cachedObject2.getReferencingObjectAttribute().isArray())
                                || cachedObject2.getKey().equals(metaObject.getKey())) {
                            break;
                        }

                        // regardless of object hierarchy
                        if (cachedObject1 == null) {
                            LOGGER.debug("checking " + instances.size() + " cached instances of meta object '"
                                    + cachedObject2.getName() + "' (" + cachedObject2.getKey() + ") in parent meta object "
                                    + metaObject.getName() + "' (" + metaObject.getKey() + ")");
                            cachedObject1 = cachedObject2;
                        } else {
                            // important: set referecing oa check to false!
                            LegacyRESTfulInterfaceTest.compareMetaObjects(cachedObject1,
                                    cachedObject2, true, true, false, false);
                        }
                    }
                }
            }

            LOGGER.info("test00DeserializeCidsBeanIntraObjectCacheDisabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test00DeserializeCidsBeanIntraObjectCacheDisabled failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void test01DeserializeCidsBean(final String cidsBeanJson) throws Exception {

        LOGGER.debug("test01DeserializeCidsBean(" + cidsBeanJson.length() + ")");

        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject metaObject = cidsBean.getMetaObject();

            // does not work: JSON in test resource is formatted
            // ->  testDeserializeAndCompareCidsBean
            //Assert.assertEquals(cidsBean.toJSONString(true), cidsBeanJson);
            // #174
            // does not work if IntraObjectCache is enabled!!!!
            //MetaObjectIntegrityTest.checkMetaObjectIntegrity(cidsBean.getMetaObject());
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
                                && cachedObject2.getReferencingObjectAttribute().isArray())
                                || cachedObject2.getKey().equals(metaObject.getKey())) {
                            break;
                        }

                        if (cachedObject1 == null) {
                            LOGGER.debug("checking " + instances.size() + " cached instances of meta object '"
                                    + cachedObject2.getName() + "' (" + cachedObject2.getKey() + ") in parent meta object "
                                    + metaObject.getName() + "' (" + metaObject.getKey() + ")");
                            cachedObject1 = cachedObject2;
                        } else {
                            // important: set referecing oa check to false!
                            LegacyRESTfulInterfaceTest.compareMetaObjects(cachedObject1,
                                    cachedObject2, true, true, false, false);
                        }
                    }
                }
            }

            LOGGER.info("test01DeserializeCidsBean("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test01DeserializeCidsBean failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Uses UNFORMATTED_ENTITIES for testing
     *
     * @param cidsBeanJson
     * @throws Exception
     */
    @Test
    @UseDataProvider("getCidsBeansJsonUnformatted")
    public void test02DeserializeAndCompareCidsBean(final String cidsBeanJson) throws Exception {
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            LOGGER.debug("test02DeserializeAndCompareCidsBean: " + cidsBean.getPrimaryKeyValue());

            final IntraObjectCacheJsonParams params = new IntraObjectCacheJsonParams();
            params.setCacheDuplicates(false);
            params.setOmitNull(true);

            Assert.assertEquals("serialized and deserialies strings do match",
                    cidsBeanJson,
                    cidsBean.toJSONString(params));

            LOGGER.info("test02DeserializeAndCompareCidsBean: " + cidsBean.getPrimaryKeyValue() + " passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test02DeserializeAndCompareCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Disabled. See #165
     *
     * @param cidsBeanJson
     * @throws Exception
     */
    @Test
    @UseDataProvider("getCidsBeansJson")
    @Ignore
    public void test03DeserializeCidsBeanMetaObjectStatus(final String cidsBeanJson) throws Exception {

        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            //Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

            LOGGER.debug("testDeserializeCidsBeanMetaObjectStatus: " + cidsBean.getPrimaryKeyValue());

            // FIXME: TEST Fails!
            //            Assert.assertEquals("Status of Array MetaObject not set ",
            //                    MetaObject.NO_STATUS,
            //                    ((MetaObject) cidsBean.getMetaObject().getAttributeByFieldName("kategorien").getValue()).getStatus());
            final ObjectAttribute[] arrayArray
                    = ((MetaObject) cidsBean.getMetaObject().getAttributeByFieldName("kategorien").getValue()).getAttribs();

            // FIXME: TEST Fails!
            //            Assert.assertEquals("Status of first Dummy Array MetaObject entry not set",
            //                    MetaObject.NO_STATUS,
            //                    ((MetaObject) arrayArray[0].getValue()).getStatus());
            // FIXME: TEST Fails!
            //            Assert.assertEquals("Status of first real Array MetaObject entry not set",
            //                    MetaObject.NO_STATUS,
            //                    ((MetaObject) ((MetaObject) arrayArray[0].getValue()).getAttribute("kategorie")).getStatus());
        } catch (AssertionError ae) {
            LOGGER.error("testDeserializeCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
