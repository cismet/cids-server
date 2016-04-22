package de.cismet.cids.dynamics;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.cismet.cids.json.IntraObjectCacheJsonParams;
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
    public void testDeserializeCidsBean(final String cidsBeanJson) throws Exception {
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            //LOGGER.debug("testDeserializeCidsBean: " + cidsBean.getPrimaryKeyValue());

            // does not work: JSON in test resource is formatted
            // ->  testDeserializeAndCompareCidsBean
            //Assert.assertEquals(cidsBean.toJSONString(true), cidsBeanJson);
        } catch (AssertionError ae) {
            LOGGER.error("testDeserializeCidsBean failed with: " + ae.getMessage());
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
    public void testDeserializeAndCompareCidsBean(final String cidsBeanJson) throws Exception {
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            LOGGER.debug("testDeserializeAndCompareCidsBean: " + cidsBean.getPrimaryKeyValue());

            
            final IntraObjectCacheJsonParams params = new IntraObjectCacheJsonParams();
            params.setCacheDuplicates(false);
            params.setOmitNull(true);

            Assert.assertEquals("serialized and deserialies strings do match",
                    cidsBeanJson,
                    cidsBean.toJSONString(params));

        } catch (AssertionError ae) {
            LOGGER.error("testDeserializeCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    @Ignore
    public void testDeserializeCidsBeanMetaObjectStatus(final String cidsBeanJson) throws Exception {

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
