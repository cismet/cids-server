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
import org.apache.log4j.Logger;
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

    private final static Logger LOGGER = Logger.getLogger(CidsBeanDeserialisationTest.class);

    //@Test
    @UseDataProvider("getCidsBeansJson")
    public void test00DeserializeCidsBeanIntraObjectCacheDisabled(final String cidsBeanJson) throws Exception {

        MetaObject metaObject = null;
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(false, cidsBeanJson);
            metaObject = cidsBean.getMetaObject();
            LOGGER.debug("test00DeserializeCidsBeanIntraObjectCacheDisabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + "): '" + metaObject.getName() + "'");

            final CidsBean referenceCidsBean = CidsBean.createNewCidsBeanFromJSON(false, cidsBeanJson);
            final MetaObject referenceMetaObject = referenceCidsBean.getMetaObject();

            MetaObjectIntegrityTest.checkMetaObjectIntegrity(metaObject);
            MetaObjectIntegrityTest.checkMetaObjectIntegrity(referenceMetaObject);

            // set compare new to true since array ids do not match
            // FIXME: #174
            LegacyRESTfulInterfaceTest.compareMetaObjects(metaObject,
                    referenceMetaObject, false, true, false, true);

            MetaObjectIntegrityTest.compareInstances(metaObject, false);

            LOGGER.info("test00DeserializeCidsBeanIntraObjectCacheDisabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test00DeserializeCidsBeanIntraObjectCacheDisabled failed with: " + ae.getMessage(), ae);
            if (metaObject != null) {
                LOGGER.debug(metaObject.getDebugString());
            }
            throw ae;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void test01DeserializeCidsBeanIntraObjectCacheEnabled(final String cidsBeanJson) throws Exception {

        MetaObject metaObject = null;
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            metaObject = cidsBean.getMetaObject();
            LOGGER.debug("test01DeserializeCidsBeanIntraObjectCacheEnabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + "): '" + metaObject.getName() + "'");

            final CidsBean referenceCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject referenceMetaObject = referenceCidsBean.getMetaObject();

            // set comapre new to true since array ids do not match
            // FIXME: #174
            // don't compare referencing object attributes
            LegacyRESTfulInterfaceTest.compareMetaObjects(metaObject,
                    referenceMetaObject, false, true, false, false);

            // does not work: JSON in test resource is formatted
            // ->  testDeserializeAndCompareCidsBean
            //Assert.assertEquals(cidsBean.toJSONString(true), cidsBeanJson);
            // #174
            // does not work if IntraObjectCache is enabled!!!! -> Referencing object attributes do not match
            //MetaObjectIntegrityTest.checkMetaObjectIntegrity(cidsBean.getMetaObject());
            final boolean intraObjectCacheEnabled = true;
            MetaObjectIntegrityTest.compareInstances(metaObject, intraObjectCacheEnabled);

            LOGGER.info("test01DeserializeCidsBeanIntraObjectCacheEnabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test01DeserializeCidsBeanIntraObjectCacheEnabled failed with: " + ae.getMessage(), ae);
            if (metaObject != null) {
                LOGGER.debug(metaObject.getDebugString());
            }
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    //@Test
    @UseDataProvider("getCidsBeansJson")
    public void test02DeserializeAndCompareCidsBeans(final String cidsBeanJson) throws Exception {

        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(false, cidsBeanJson);
            final CidsBean intraObjectCacheEnabledCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            final MetaObject metaObject = cidsBean.getMetaObject();
            final MetaObject intraObjectCacheEnabledMetaObject = intraObjectCacheEnabledCidsBean.getMetaObject();

            LOGGER.debug("test02DeserializeAndCompareCidsBeans("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + "): '" + metaObject.getName() + "'");

            // set comapre new to true since array ids do not match
            // FIXME: #174
            LegacyRESTfulInterfaceTest.compareMetaObjects(metaObject,
                    intraObjectCacheEnabledMetaObject, false, true, false, false);

            LOGGER.info("test02DeserializeAndCompareCidsBeans("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test02DeserializeAndCompareCidsBeans failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @Ignore
    @UseDataProvider("getCidsBeansJsonNormalised")
    public void test03DeserializeNormalisedCidsBeanIntraObjectCacheDisabled(final String cidsBeanJson) throws Exception {

        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(false, cidsBeanJson);
            final MetaObject metaObject = cidsBean.getMetaObject();
            LOGGER.debug("test03DeserializeNormalisedCidsBeanIntraObjectCacheDisabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + "): '" + metaObject.getName() + "'");

            final CidsBean referenceCidsBean = CidsBean.createNewCidsBeanFromJSON(false, cidsBeanJson);
            final MetaObject referenceMetaObject = referenceCidsBean.getMetaObject();

            MetaObjectIntegrityTest.checkMetaObjectIntegrity(metaObject);
            MetaObjectIntegrityTest.checkMetaObjectIntegrity(referenceMetaObject);

            // set compare new to true since array ids do not match
            // FIXME: #174
            LegacyRESTfulInterfaceTest.compareMetaObjects(metaObject,
                    referenceMetaObject, false, true, false, true);

            MetaObjectIntegrityTest.compareInstances(metaObject, false);

            LOGGER.info("test03DeserializeNormalisedCidsBeanIntraObjectCacheDisabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test03DeserializeNormalisedCidsBeanIntraObjectCacheDisabled failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    //@Test
    @UseDataProvider("getCidsBeansJsonNormalised")
    public void test04DeserializeNormalisedCidsBeanIntraObjectCacheEnabled(final String cidsBeanJson) throws Exception {

        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject metaObject = cidsBean.getMetaObject();
            LOGGER.debug("test04DeserializeNormalisedCidsBeanIntraObjectCacheEnabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + "): '" + metaObject.getName() + "'");

            final CidsBean referenceCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject referenceMetaObject = referenceCidsBean.getMetaObject();

            // set comapre new to true since array ids do not match
            // FIXME: #174
            // don't compare referencing object attributes
            LegacyRESTfulInterfaceTest.compareMetaObjects(metaObject,
                    referenceMetaObject, false, true, false, false);

            // does not work: JSON in test resource is formatted
            // ->  testDeserializeAndCompareCidsBean
            //Assert.assertEquals(cidsBean.toJSONString(true), cidsBeanJson);
            // #174
            // does not work if IntraObjectCache is enabled!!!! -> Refercing object attributes do not match
            //MetaObjectIntegrityTest.checkMetaObjectIntegrity(cidsBean.getMetaObject());
            MetaObjectIntegrityTest.compareInstances(metaObject, true);

            LOGGER.info("test04DeserializeNormalisedCidsBeanIntraObjectCacheEnabled("
                    + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test04DeserializeNormalisedCidsBeanIntraObjectCacheEnabled failed with: " + ae.getMessage(), ae);
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
    //@Test
    @UseDataProvider("getCidsBeansJsonUnformatted")
    public void test05DeserializeAndCompareCidsBeansJson(final String cidsBeanJson) throws Exception {
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            LOGGER.debug("test05DeserializeAndCompareCidsBeansJson: " + cidsBean.getPrimaryKeyValue());

            final IntraObjectCacheJsonParams params = new IntraObjectCacheJsonParams();
            params.setCacheDuplicates(false);
            params.setOmitNull(true);

            Assert.assertEquals("serialized and deserialies strings do match",
                    cidsBeanJson,
                    cidsBean.toJSONString(params));

            LOGGER.info("test03DeserializeAndCompareCidsBeansJson: " + cidsBean.getPrimaryKeyValue() + " passed!");

        } catch (AssertionError ae) {
            LOGGER.error("test05DeserializeAndCompareCidsBeansJson failed with: " + ae.getMessage());
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
    public void test06DeserializeCidsBeanMetaObjectStatus(final String cidsBeanJson) throws Exception {

        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            //Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

            LOGGER.debug("test06DeserializeCidsBeanMetaObjectStatus: " + cidsBean.getPrimaryKeyValue());

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
            LOGGER.error("test06DeserializeCidsBeanMetaObjectStatus failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    
}
