package de.cismet.cids.dynamics;

import Sirius.server.middleware.types.MetaObject;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
public class CidsBeanSerialisationTest extends AbstractCidsBeanDeserialisationTest {

    protected final static ArrayList<CidsBean> CIDS_BEANS = new ArrayList<CidsBean>();

    @DataProvider
    public final static Object[][] getCidsBeans() throws Exception {
        if (CIDS_BEANS.isEmpty()) {
            if (CIDS_BEANS_JSON.isEmpty()) {
                AbstractCidsBeanDeserialisationTest.initCidsBeansJson();
            }

            for (String cidsBeanJson : CIDS_BEANS_JSON) {
                final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
                CIDS_BEANS.add(cidsBean);
            }
        }

        final Object[][] cidsBeans = new Object[CIDS_BEANS.size()][1];
        int i = 0;
        for (final CidsBean cidsBean : CIDS_BEANS) {
            cidsBeans[i][0] = cidsBean;
            i++;
        }

        LOGGER.debug(cidsBeans.length + " cids beans processed");

        return cidsBeans;
    }

    @Test
    @UseDataProvider("getCidsBeans")
    public void testSerializeCidsBean(CidsBean cidsBean) throws Exception {
        try {

            LOGGER.debug("testSerializeCidsBean: " + cidsBean.getPrimaryKeyValue());
            final String cidsBeanJson = cidsBean.toJSONString(true);
            final CidsBean serializedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            Assert.assertEquals(serializedCidsBean.toJSONString(true), cidsBeanJson);
        } catch (AssertionError ae) {
            LOGGER.error("testSerializeCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getCidsBeans")
    public void testSerializeUpdatedCidsBeanId(CidsBean cidsBean) throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {

            LOGGER.debug("testSerializeUpdatedCidsBean: " + cidsBean.getPrimaryKeyValue());
            final String cidsBeanJson = cidsBean.toJSONString(true);

            final CidsBean updatedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            final MetaObject metaObjectSpy = Mockito.spy(updatedCidsBean.getMetaObject());
            updatedCidsBean.setMetaObject(metaObjectSpy);

            final Semaphore semaphore = new Semaphore(1);
            final int newId = 666;
            updatedCidsBean.setProperty(updatedCidsBean.getPrimaryKeyFieldname().toLowerCase(), newId);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName(Mockito.anyString());
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);
                        Assert.assertTrue(metaObjectSpy.getStatus() == MetaObject.MODIFIED);
                        Assert.assertEquals(updatedCidsBean.getCidsBeanInfo().getObjectKey(), metaObjectSpy.getPrimaryKey().getValue().toString());
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Assert.assertNotEquals(updatedCidsBean.toJSONString(true), cidsBeanJson);

        } catch (AssertionError ae) {
            LOGGER.error("testSerializeCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }

    @Test
    @UseDataProvider("getCidsBeans")
    public void testSerializeUpdatedCidsBeanObject(CidsBean cidsBean) throws Throwable {

        Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {

            LOGGER.debug("testSerializeUpdatedCidsBean: " + cidsBean.getPrimaryKeyValue());
            final String cidsBeanJson = cidsBean.toJSONString(true);

            final CidsBean updatedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            final MetaObject metaObjectSpy = Mockito.spy(updatedCidsBean.getMetaObject());
            updatedCidsBean.setMetaObject(metaObjectSpy);

            final Semaphore semaphore = new Semaphore(1);
            final String name = "Mike Hansen";

            LOGGER.info("testSerializeUpdatedCidsBeanObject: " + name);
            updatedCidsBean.setProperty("betreiber.name", name);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        //Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("betreiber");
                        //Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);
                        Assert.assertTrue(metaObjectSpy.getStatus() == MetaObject.NO_STATUS);

                        Assert.assertNotNull(metaObjectSpy.getAttributeByFieldName("betreiber").getValue());

                        Assert.assertTrue(MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("betreiber").getValue().getClass()));

                        Assert.assertEquals(((CidsBean) updatedCidsBean.getProperty("betreiber")).toJSONString(true),
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().toJSONString(true));

                        Assert.assertEquals(((CidsBean) updatedCidsBean.getProperty("betreiber")).getProperty("name"),
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));

                        Assert.assertEquals(MetaObject.MODIFIED,
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getStatus());

                        LOGGER.debug("testSerializeUpdatedCidsBeanObject passed: "
                                + ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name")
                                + " (" + ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getStatus() + ")");

                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Assert.assertNotEquals(updatedCidsBean.toJSONString(true), cidsBeanJson);

        } catch (AssertionError ae) {
            LOGGER.error("testSerializeUpdatedCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }
    
    @Test
    @UseDataProvider("getCidsBeans")
    public void testSerializeUpdatedArrayProperty(CidsBean cidsBean) throws Throwable {

        Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {

            LOGGER.debug("testSerializeUpdatedArrayProperty: " + cidsBean.getPrimaryKeyValue());
            final String cidsBeanJson = cidsBean.toJSONString(true);

            final CidsBean updatedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            final MetaObject metaObjectSpy = Mockito.spy(updatedCidsBean.getMetaObject());
            updatedCidsBean.setMetaObject(metaObjectSpy);

            final Semaphore semaphore = new Semaphore(1);
            final String name = "Tetris";

            LOGGER.info("testSerializeUpdatedArrayProperty: " + name);
            updatedCidsBean.setProperty("kategorien[0].name", name);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        //Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("betreiber");
                        //Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);
                        Assert.assertTrue(metaObjectSpy.getStatus() == MetaObject.NO_STATUS);

                        //Assert.assertNotNull(metaObjectSpy.getAttributeByFieldName("kategorien[0]").getValue());

                        Assert.assertTrue(MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertEquals(((CidsBean) updatedCidsBean.getProperty("kategorien[0]")).toJSONString(true),
                                ((CidsBean)((MetaObject)((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs()[0].getValue()).getBean().getProperty("kategorie")).toJSONString(true));

                        Assert.assertEquals(((CidsBean) updatedCidsBean.getProperty("kategorien[0]")).getProperty("name"),
                                ((CidsBean)((MetaObject)((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs()[0].getValue()).getBean().getProperty("kategorie")).getProperty("name"));

                        Assert.assertEquals(MetaObject.MODIFIED,
                                ((CidsBean)((MetaObject)((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs()[0].getValue()).getBean().getProperty("kategorie")).getMetaObject().getStatus());

                        LOGGER.debug("testSerializeUpdatedCidsBeanObject passed: "
                                + ((CidsBean)updatedCidsBean.getProperty("kategorien[0]")).getProperty("name")
                                + " (" + ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getStatus() + ")");

                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Assert.assertNotEquals(updatedCidsBean.toJSONString(true), cidsBeanJson);

        } catch (AssertionError ae) {
            LOGGER.error("testSerializeUpdatedCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }
}
