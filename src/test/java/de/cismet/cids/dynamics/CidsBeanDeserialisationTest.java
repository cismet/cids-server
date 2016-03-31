package de.cismet.cids.dynamics;

import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.FixMethodOrder;
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
            LOGGER.debug("testDeserializeCidsBean: " + cidsBean.getPrimaryKeyValue());
            //Assert.assertEquals(cidsBean.toJSONString(true), cidsBeanJson);
        } catch (AssertionError ae) {
            LOGGER.error("testDeserializeCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
