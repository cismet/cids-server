package de.cismet.cids.dynamics;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static de.cismet.cids.dynamics.AbstractCidsBeanDeserialisationTest.LOGGER;
import de.cismet.cids.utils.MetaClassCacheService;
import de.cismet.connectioncontext.ConnectionContext;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openide.util.Lookup;

/**
 * Existing CidsBean instances <-> MetaObject serialisation and interaction
 * tests.
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
public class CidsBeanSerialisationTest extends AbstractCidsBeanDeserialisationTest {

    protected final static ArrayList<CidsBean> CIDS_BEANS = new ArrayList<CidsBean>();
    private final ConnectionContext connectionContext = ConnectionContext.createDummy();

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
    public void test01SerializeCidsBeanDeduplication(CidsBean cidsBean) throws Exception {
        try {

            LOGGER.debug("testSerializeCidsBean: " + cidsBean.getPrimaryKeyValue());
            final String cidsBeanJson = cidsBean.toJSONString(true);
            final CidsBean serializedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            Assert.assertEquals("JSON of re-serialized CidsBean matches source JSON (deduplicate=true)",
                    serializedCidsBean.toJSONString(true), cidsBeanJson);

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
    public void test02SerializeCidsBean(CidsBean cidsBean) throws Exception {
        try {
            LOGGER.debug("testSerializeCidsBean: " + cidsBean.getPrimaryKeyValue());
            final String cidsBeanJson = cidsBean.toJSONString(false);
            final CidsBean serializedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            Assert.assertEquals("JSON of re-serialized CidsBean matches source JSON (deduplicate=false)",
                    serializedCidsBean.toJSONString(false), cidsBeanJson);

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
    public void test03SerializeUpdatedCidsBeanId(CidsBean cidsBean) throws Throwable {

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

                        Assert.assertTrue("MetaObject status is modified after changing direct property in CidsBean",
                                metaObjectSpy.getStatus() == MetaObject.MODIFIED);

                        Assert.assertTrue("MetaObject attribute status is modified after changing direct property in CidsBean",
                                metaObjectSpy.getPrimaryKey().isChanged());

                        Assert.assertEquals("changed MetaObject attribute value is equal to changed property in CidsBean",
                                updatedCidsBean.getCidsBeanInfo().getObjectKey(),
                                metaObjectSpy.getPrimaryKey().getValue().toString());

                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Assert.assertNotEquals("updated CidsBean is different from original CidsBean",
                    updatedCidsBean.toJSONString(true), cidsBeanJson);

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
    public void test04SerializeUpdatedCidsBeanObject(CidsBean cidsBean) throws Throwable {

        Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {

            LOGGER.debug("testSerializeUpdatedCidsBeanObject: " + cidsBean.getPrimaryKeyValue());
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

                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(Mockito.anyInt());

                        Assert.assertTrue("MetaObject Status not modified after sub-object modification",
                                metaObjectSpy.getStatus() == MetaObject.NO_STATUS);

                        Assert.assertNotNull("sub object does exist",
                                metaObjectSpy.getAttributeByFieldName("betreiber").getValue());

                        Assert.assertTrue("sub object is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("betreiber").getValue().getClass()));

                        Assert.assertEquals("updated sub object JSON does match in CidsBean and MetaObject",
                                ((CidsBean) updatedCidsBean.getProperty("betreiber")).toJSONString(true),
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().toJSONString(true));

                        Assert.assertEquals("updated property in sub object does match in CidsBean and MetaObject",
                                ((CidsBean) updatedCidsBean.getProperty("betreiber")).getProperty("name"),
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));

                        Assert.assertEquals("MetaObject Status modified in sub object after changing property",
                                MetaObject.MODIFIED,
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getStatus());

                        LOGGER.debug("testSerializeUpdatedCidsBeanObject passed: "
                                + ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name")
                                + " (" + ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getStatus() + ")");

                    } catch (AssertionError ae) {
                        LOGGER.error("testSerializeUpdatedCidsBeanObject failed with: " + ae.getMessage());
                        throwablesFromThread.add(ae);
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Assert.assertNotEquals("updated CidsBean is different from original CidsBean",
                    updatedCidsBean.toJSONString(true), cidsBeanJson);

        } catch (AssertionError ae) {
            LOGGER.error("testSerializeUpdatedCidsBeanObject failed with: " + ae.getMessage());
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

    /**
     * Test skipped till #175 is fixed
     *
     * @param cidsBean
     * @throws Throwable
     */
    @Ignore
    @Test
    @UseDataProvider("getCidsBeans")
    public void test05SerializeUpdatedArrayProperty(CidsBean cidsBean) throws Throwable {

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
                        Assert.assertTrue("MetaObject Status not modified after array property modification",
                                metaObjectSpy.getStatus() == MetaObject.NO_STATUS);

                        //Assert.assertNotNull(metaObjectSpy.getAttributeByFieldName("kategorien[0]").getValue());
                        Assert.assertTrue("Array Dummy Object is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertEquals("updated array object JSON does match in CidsBean and MetaObject",
                                ((CidsBean) updatedCidsBean.getProperty("kategorien[0]")).toJSONString(true),
                                ((CidsBean) ((MetaObject) ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs()[0].getValue()).getBean().getProperty("kategorie")).toJSONString(true));

                        Assert.assertEquals("updated property in array object does match in CidsBean and MetaObject",
                                ((CidsBean) updatedCidsBean.getProperty("kategorien[0]")).getProperty("name"),
                                ((CidsBean) ((MetaObject) ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs()[0].getValue()).getBean().getProperty("kategorie")).getProperty("name"));

                        Assert.assertEquals("MetaObject Status modified in array element after changing property",
                                MetaObject.MODIFIED,
                                ((CidsBean) ((MetaObject) ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs()[0].getValue()).getBean().getProperty("kategorie")).getMetaObject().getStatus());

                        LOGGER.debug("testSerializeUpdatedCidsBeanObject passed: "
                                + ((CidsBean) updatedCidsBean.getProperty("kategorien[0]")).getProperty("name")
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

            Assert.assertNotEquals("updated CidsBean is different from original CidsBean",
                    updatedCidsBean.toJSONString(true), cidsBeanJson);

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

    /**
     * Test skipped till #175 is fixed
     *
     * @param cidsBean
     * @throws Throwable
     */
    @Ignore
    @Test
    @UseDataProvider("getCidsBeans")
    public void test06SerializeAddArrayElement(CidsBean cidsBean) throws Throwable {

        Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            LOGGER.debug("testSerializeAddArrayElement: " + cidsBean.getPrimaryKeyValue());

            final String cidsBeanJson = cidsBean.toJSONString(true);

            final CidsBean updatedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            final MetaObject metaObjectSpy = Mockito.spy(updatedCidsBean.getMetaObject());
            updatedCidsBean.setMetaObject(metaObjectSpy);

            final Semaphore semaphore = new Semaphore(1);

            final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
            final CidsBean arrayEntryBean = classCacheService.getMetaClass("CIDS", "SPH_KATEGORIE", connectionContext).getEmptyInstance().getBean();
            arrayEntryBean.setProperty("name", "Climbing for Dollars");
            final int arrayElements = ((Collection) updatedCidsBean.getProperty("kategorien")).size();
            updatedCidsBean.addCollectionElement("kategorien", arrayEntryBean);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        // involed 2 times: collection listener and property listener
                        Mockito.verify(metaObjectSpy, Mockito.times(2)).getAttributeByFieldName("kategorien");

                        Assert.assertTrue("Dunmmy Array Object is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertTrue("CidsBean Array Property is Collection",
                                Collection.class.isAssignableFrom(updatedCidsBean.getProperty("kategorien").getClass()));

                        final Collection arrayCollection = ((Collection) updatedCidsBean.getProperty("kategorien"));
                        Assert.assertTrue("CidsBean Collection size increased after adding element",
                                arrayCollection.size() > arrayElements);

                        Assert.assertEquals("CidsBean Collection entry JSON matches",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                arrayEntryBean.toJSONString(true));

                        final ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
                        Assert.assertTrue("MetaObject array size increased after adding element",
                                arrayArray.length > arrayElements);

                        Assert.assertEquals("MetaObject array sizea and CidsBean Collection size do match after adding element",
                                arrayArray.length, arrayCollection.size());

                        Assert.assertEquals("CidsBean and MetaObject array entries are equal",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                ((CidsBean) ((CidsBean) ((MetaObject) arrayArray[arrayArray.length - 1].getValue()).getBean()).getProperty("kategorie")).toJSONString(true));

                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Mockito.reset(metaObjectSpy);
            final String name = "Klettern für Dollars";
            final MetaObject subMetaObjectSpy = Mockito.spy(arrayEntryBean.getMetaObject());
            arrayEntryBean.setMetaObject(subMetaObjectSpy);
            updatedCidsBean.setProperty("kategorien[" + (updatedCidsBean.getBeanCollectionProperty("kategorien").size() - 1) + "].name", name);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(Mockito.anyInt());
                        Mockito.verify(subMetaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        Mockito.verify(subMetaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertTrue("Dunmmy Array Object is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertTrue("CidsBean Array Property is Collection",
                                Collection.class.isAssignableFrom(updatedCidsBean.getProperty("kategorien").getClass()));

                        final Collection arrayCollection = ((Collection) updatedCidsBean.getProperty("kategorien"));
                        Assert.assertTrue("CidsBean Collection size increased after adding element",
                                arrayCollection.size() > arrayElements);

                        Assert.assertEquals("CidsBean Collection entry JSON matches",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                arrayEntryBean.toJSONString(true));

                        final ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();

                        Assert.assertEquals("CidsBean and MetaObject array entries are equal",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                ((CidsBean) ((CidsBean) ((MetaObject) arrayArray[arrayArray.length - 1].getValue()).getBean()).getProperty("kategorie")).toJSONString(true));

                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Assert.assertNotEquals("updated CidsBean is different from original CidsBean",
                    updatedCidsBean.toJSONString(true), cidsBeanJson);

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

    /**
     * Simple remove array element test without Mockito Spy Test skipped till
     * #175 is fixed
     *
     * @param cidsBean
     * @throws Throwable
     */
    @Ignore
    @Test
    @UseDataProvider("getCidsBeans")
    public void test07SerializeRemoveArrayElementNoSpy(CidsBean cidsBean) throws Throwable {

        Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));
        try {
            LOGGER.debug("testSerializeRemoveArrayElement: " + cidsBean.getPrimaryKeyValue());

            final String cidsBeanJson = cidsBean.toJSONString(true);

            final CidsBean updatedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            final MetaObject metaObjectSpy = updatedCidsBean.getMetaObject();
            updatedCidsBean.setMetaObject(metaObjectSpy);

            int arrayElements = ((Collection) updatedCidsBean.getProperty("kategorien")).size();
            ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
            Assert.assertEquals("MetaObject array size machtes Bean Collection size",
                    arrayArray.length, arrayElements);

            final CidsBean removedCidsBean = updatedCidsBean.getBeanCollectionProperty("kategorien").remove(0);

            // Problem in CidsBean.listElementsRemoved: See #174
            // ReferencingObjectAttribute of cached MetaObjects from intraObjectCacheEnabled-CidsBeanJsonDeserializer 
            // point to the wrong parent ObjectAttribute (kategorie[] vs hauptkategorie)
            Assert.assertNotNull("CidsBEan successfully removed from collection",
                    removedCidsBean);
            Assert.assertEquals("Bean Collection size decreased after removal",
                    arrayElements - 1, ((Collection) updatedCidsBean.getProperty("kategorien")).size());

            arrayElements = ((Collection) updatedCidsBean.getProperty("kategorien")).size();
            arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
            Assert.assertEquals("MetaObject array size machtes Bean Collection size after removal",
                    arrayArray.length, arrayElements);

        } catch (AssertionError ae) {
            LOGGER.error("testSerializeUpdatedCidsBean failed with: " + ae.getMessage(), ae);
            throw ae;

        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test skipped till #175 is fixed
     *
     * @param cidsBean
     * @throws Throwable
     */
    @Ignore
    @Test
    @UseDataProvider("getCidsBeans")
    public void test08SerializeRemoveAndAddArrayElement(CidsBean cidsBean) throws Throwable {

        Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            LOGGER.debug("testSerializeRemoveAndAddArrayElement: " + cidsBean.getPrimaryKeyValue());

            final String cidsBeanJson = cidsBean.toJSONString(true);

            final CidsBean updatedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            //final MetaObject metaObjectSpy = Mockito.spy(updatedCidsBean.getMetaObject());
            final MetaObject metaObjectSpy = updatedCidsBean.getMetaObject();
            //updatedCidsBean.setMetaObject(metaObjectSpy);

            //final Semaphore semaphore = new Semaphore(1);
            final int arrayElements = ((Collection) updatedCidsBean.getProperty("kategorien")).size();

            final CidsBean removedCidsBean = updatedCidsBean.getBeanCollectionProperty("kategorien").remove(0);

            Assert.assertNotNull("CidsBEan successfully removed from collection",
                    removedCidsBean);

            final ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
            Assert.assertTrue("MetaObject Dummy Array Element size smaller after removing an element",
                    arrayArray.length < arrayElements);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        // involed 2 times: collection listener and property listener
                        Mockito.verify(metaObjectSpy, Mockito.times(2)).getAttributeByFieldName("kategorien");

                        Assert.assertTrue("Dunmmy Array Object is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertTrue("CidsBean Array Property is Collection",
                                Collection.class.isAssignableFrom(updatedCidsBean.getProperty("kategorien").getClass()));

                        final Collection arrayCollection = updatedCidsBean.getBeanCollectionProperty("kategorien");
                        Assert.assertTrue("Array size of bean collection smaller after removing an element",
                                arrayCollection.size() < arrayElements);

                        final ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
                        Assert.assertTrue("MetaObject Dummy Array Element size smaller after removing an element",
                                arrayArray.length < arrayElements);

                        Assert.assertEquals("Array size of bean collection property and MetaObject dummy array object matches",
                                arrayArray.length, arrayCollection.size());

                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        //semaphore.release();
                    }
                }
            });
            //semaphore.acquire();

            Mockito.reset(metaObjectSpy);
            final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
            final CidsBean arrayEntryBean = classCacheService.getMetaClass("CIDS", "SPH_KATEGORIE", connectionContext).getEmptyInstance().getBean();
            arrayEntryBean.setProperty("name", "Climbing for Dollars");
            updatedCidsBean.addCollectionElement("kategorien", arrayEntryBean);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        // involed 2 times: collection listener and property listener
                        Mockito.verify(metaObjectSpy, Mockito.times(2)).getAttributeByFieldName("kategorien");

                        Assert.assertTrue("Dunmmy Array Object is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertTrue("Array Property is Collection",
                                Collection.class.isAssignableFrom(updatedCidsBean.getProperty("kategorien").getClass()));

                        final Collection arrayCollection = ((Collection) updatedCidsBean.getProperty("kategorien"));
                        Assert.assertTrue("Array size matches again after adding a new element",
                                arrayCollection.size() == arrayElements);

                        Assert.assertEquals("CidsBean and MetaObject array entries are equal",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                arrayEntryBean.toJSONString(true));

                        final ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
                        Assert.assertTrue("Array size of bean collection property and MetaObject dummy array object matches",
                                arrayArray.length == arrayElements);

                        Assert.assertEquals("MetaObject dummy array object size matches again after adding a new element",
                                arrayArray.length, arrayCollection.size());

                        Assert.assertEquals("CidsBean and MetaObject array entries are equal",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                ((CidsBean) ((CidsBean) ((MetaObject) arrayArray[arrayArray.length - 1].getValue()).getBean()).getProperty("kategorie")).toJSONString(true));

                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        //semaphore.release();
                    }
                }
            });
            //semaphore.acquire();

            Mockito.reset(metaObjectSpy);
            final String name = "Klettern für Dollars";
            final MetaObject subMetaObjectSpy = Mockito.spy(arrayEntryBean.getMetaObject());
            arrayEntryBean.setMetaObject(subMetaObjectSpy);
            updatedCidsBean.setProperty("kategorien[" + (updatedCidsBean.getBeanCollectionProperty("kategorien").size() - 1) + "].name", name);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(Mockito.anyInt());
                        Mockito.verify(subMetaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        Mockito.verify(subMetaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertTrue("Dunmmy Array Object is MetaObject", MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertTrue("Array Property is Collection", Collection.class.isAssignableFrom(updatedCidsBean.getProperty("kategorien").getClass()));

                        final Collection arrayCollection = ((Collection) updatedCidsBean.getProperty("kategorien"));
                        Assert.assertTrue("Array size still matches", arrayCollection.size() == arrayElements);

                        Assert.assertEquals("CidsBean Collection entry JSON matches",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                arrayEntryBean.toJSONString(true));

                        final ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
                        Assert.assertEquals("CidsBean and MetaObject array entries JSON are equal",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[arrayCollection.size() - 1].toJSONString(true),
                                ((CidsBean) ((CidsBean) ((MetaObject) arrayArray[arrayArray.length - 1].getValue()).getBean()).getProperty("kategorie")).toJSONString(true));

                    } catch (AssertionError ae) {
                        LOGGER.error("testSerializeRemoveArrayElement failed with: " + ae.getMessage());
                        throwablesFromThread.add(ae);
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        //semaphore.release();
                    }
                }
            });
            //semaphore.acquire();

            Assert.assertNotEquals("updated CidsBean is different from original CidsBean",
                    updatedCidsBean.toJSONString(true), cidsBeanJson);

        } catch (AssertionError ae) {
            LOGGER.error("testSerializeRemoveAndAddArrayElement failed with: " + ae.getMessage());
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

    /**
     *
     * Test skipped till #175 is fixed
     *
     * @param cidsBean
     * @throws Throwable
     */
    @Ignore
    @Test
    @UseDataProvider("getCidsBeans")
    public void test09SerializeReplaceArrayElement(CidsBean cidsBean) throws Throwable {

        Assume.assumeTrue(cidsBean.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            LOGGER.debug("testSerializeReplaceArrayElement: " + cidsBean.getPrimaryKeyValue());

            final String cidsBeanJson = cidsBean.toJSONString(true);

            final CidsBean updatedCidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);

            final MetaObject metaObjectSpy = Mockito.spy(updatedCidsBean.getMetaObject());
            updatedCidsBean.setMetaObject(metaObjectSpy);

            // WHY NEW?! -> Possible Problem in CidsJsonDeserializer?
//            Assert.assertEquals("Status of Dummay Array MetaObject is modified",
//                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getStatus(),
//                                MetaObject.NEW);
            final Semaphore semaphore = new Semaphore(1);
            final int arrayElements = ((Collection) updatedCidsBean.getProperty("kategorien")).size();

            final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
            final CidsBean arrayEntryBean = classCacheService.getMetaClass("CIDS", "SPH_KATEGORIE", connectionContext).getEmptyInstance().getBean();
            arrayEntryBean.setProperty("name", "Climbing for Dollars");

            //FIXME: This does not work -> listElementReplaced not implemented in CidsBean
            //updatedCidsBean.getBeanCollectionProperty("kategorien").set(0, arrayEntryBean);
            updatedCidsBean.getBeanCollectionProperty("kategorien").remove(0);
            updatedCidsBean.getBeanCollectionProperty("kategorien").add(0, arrayEntryBean);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        Assert.assertTrue("Dunmmy Array Object is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("kategorien").getValue().getClass()));

                        Assert.assertTrue("Array Property is Collection",
                                Collection.class.isAssignableFrom(updatedCidsBean.getProperty("kategorien").getClass()));

                        final Collection arrayCollection = ((Collection) updatedCidsBean.getProperty("kategorien"));
                        Assert.assertTrue("Array size matches after replacing an element",
                                arrayCollection.size() == arrayElements);

                        Assert.assertEquals("CidsBean array entry successfully replaced",
                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[0].toJSONString(true),
                                arrayEntryBean.toJSONString(true));

//                        Assert.assertEquals("Status of Dummay Array MetaObject is modified",
//                                MetaObject.NEW,
//                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getStatus());
                        final ObjectAttribute[] arrayArray = ((MetaObject) metaObjectSpy.getAttributeByFieldName("kategorien").getValue()).getAttribs();
                        Assert.assertTrue("Array size of bean collection property and MetaObject dummy array object matches",
                                arrayArray.length == arrayElements);

                        Assert.assertEquals("MetaObject dummy array object size matches again after adding a new element",
                                arrayArray.length, arrayCollection.size());

                        // FIXME: Position not preserved!
//                        Assert.assertEquals("CidsBean and MetaObject array entries are equal",
//                                ((CidsBean[]) arrayCollection.toArray(new CidsBean[arrayCollection.size()]))[0].toJSONString(true),
//                                ((CidsBean) ((CidsBean) ((MetaObject) arrayArray[0].getValue()).getBean()).getProperty("kategorie")).toJSONString(true));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Assert.assertNotEquals("updated CidsBean is different from original CidsBean",
                    updatedCidsBean.toJSONString(true), cidsBeanJson);

        } catch (AssertionError ae) {
            LOGGER.error("testSerializeRemoveArrayElement failed with: " + ae.getMessage());
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
