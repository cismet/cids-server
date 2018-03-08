package de.cismet.cids.dynamics;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import de.cismet.cids.utils.MetaClassCacheService;
import de.cismet.connectioncontext.ConnectionContext;
import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.openide.util.Lookup;
import org.junit.Assert;

/**
 * Simple new CidsBean instance <-> MetaObject interaction tests.
 *
 * @See CidsBeanSerialisationTest
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@RunWith(PowerMockRunner.class)
//@RunWith(DataProviderRunner.class)
//@PowerMockRunnerDelegate(DataProviderRunner.class)
//@PrepareForTest(IdGenerator.class)
public class CidsBeanTest {

    private final static Logger LOGGER = Logger.getLogger(CidsBeanTest.class);
    private static MetaClass SPIELHALLE_META_CLASS;
    private static MetaClass KATEGORIE_META_CLASS;
    private static MetaClass BETREIBER_META_CLASS;

    private CidsBean cidsBean = null;
    private MetaObject metaObject = null;
    private MetaObject metaObjectSpy = null;
    private CidsBean referenceCidsBean = null;
    private MetaObject referenceMetaObject = null;

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
            final ConnectionContext connectionContext = ConnectionContext.createDummy();
            final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
            SPIELHALLE_META_CLASS = classCacheService.getMetaClass("CIDS", "SPH_SPIELHALLE", connectionContext);
            BETREIBER_META_CLASS = classCacheService.getMetaClass("CIDS", "SPH_BETREIBER", connectionContext);
            KATEGORIE_META_CLASS = classCacheService.getMetaClass("CIDS", "SPH_KATEGORIE", connectionContext);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    /**
     * DOCUMENT ME!
     */
    @Before
    public void setUp() {
        metaObject = SPIELHALLE_META_CLASS.getEmptyInstance();
        //System.out.println(metaObject.getStatus());
        cidsBean = metaObject.getBean();
        metaObjectSpy = Mockito.spy(metaObject);
        cidsBean.setMetaObject(metaObjectSpy);

        referenceMetaObject = (MetaObject) SPIELHALLE_META_CLASS.getEmptyInstance();
        referenceCidsBean = referenceMetaObject.getBean();
    }

    /**
     * DOCUMENT ME!
     */
    @After
    public void tearDown() {
        cidsBean = null;
        metaObjectSpy = null;
        referenceCidsBean = null;
        Mockito.validateMockitoUsage();
    }

//    @DataProvider
//    public final static Object[] getCidsBeans() throws Exception {
//        System.out.println("getCidsBeans");
//        return new CidsBean[]{PowerMockito.mock(CidsBean.class)};
//    }
//
//    @DataProvider
//    public final static Object[] getMetaObjects() throws Exception {
//
//        final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
//        final MetaClass metaClass = classCacheService.getMetaClass("CIDS", "SPH_SPIELHALLE");
//
//        final Sirius.server.localserver.object.Object legacyObject = new Sirius.server.localserver.object.DefaultObject(
//                -1,
//                metaClass.getId());
//
//        legacyObject.setStatus(Sirius.server.localserver.object.Object.NEW);
//
//        final Iterator iter = metaClass.getMemberAttributeInfos().values().iterator();
//
//        while (iter.hasNext()) {
//            final MemberAttributeInfo mai = (MemberAttributeInfo) iter.next();
//
//            final ObjectAttribute oAttr;
//            oAttr = new ObjectAttribute(mai, -1, null, metaClass.getAttributePolicy());
//
//            oAttr.setVisible(mai.isVisible());
//            oAttr.setSubstitute(mai.isSubstitute());
//            oAttr.setReferencesObject(mai.isForeignKey());
//
//            oAttr.setIsPrimaryKey(mai.getFieldName().equalsIgnoreCase(metaClass.getPrimaryKey()));
//            if (oAttr.isPrimaryKey()) {
//                oAttr.setValue(-1);
//            }
//            oAttr.setOptional(mai.isOptional());
//
//            oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + metaClass.getDomain()); // NOI18N
//            legacyObject.addAttribute(oAttr);
//        }
//
//        final MetaObject metaObjectSpy = Mockito.spy(new DefaultMetaObject(legacyObject, metaClass.getDomain()));
//        return new Object[]{metaObjectSpy};
//    }
//    @Test
//    @UseDataProvider("getCidsBeans")
//    public void testCidsBeans(final CidsBean cidsBean) {
//        System.out.println("testCidsBeans");
//        LOGGER.debug(cidsBean.getMOString());
//    }
    @Test
    //@UseDataProvider("getMetaObjects")
    public void test01getCidsBeanInfo() throws Exception {

        try {
            final CidsBeanInfo beanInfo = cidsBean.getCidsBeanInfo();
            LOGGER.debug("testGetCidsBeanInfo: " + beanInfo);

            // Mockito Spy Object: verify that a certain method has been called
            Mockito.verify(metaObjectSpy, Mockito.atLeastOnce()).getMetaClass();

            Assert.assertSame("referenceCidsBean.getMetaObject() matches referenceMetaObject",
                    referenceMetaObject,
                    referenceCidsBean.getMetaObject());
            Assert.assertSame("cidsBean.getMetaObject() matches metaObjectSpy",
                    metaObjectSpy,
                    cidsBean.getMetaObject());
            LOGGER.info("testGetCidsBeanInfo passed!");
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw e;
        }
    }

    @Test
    public void test02getPrimaryKeyProperty() throws Exception {

        try {
            LOGGER.debug("testGetPrimaryKeyProperty: " + cidsBean.getPrimaryKeyValue());
            // Mockito Spy Object: verify that a certain method has never been called
            Mockito.verify(metaObjectSpy, Mockito.never()).getAttributeByFieldName(Mockito.anyString());
            LOGGER.info("testGetPrimaryKeyProperty '" + cidsBean.getPrimaryKeyValue() + "' passed!");

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw e;
        }
    }

    @Test
    public void test03setPrimaryKeyProperty() throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            final Semaphore semaphore = new Semaphore(1);
            final int newId = 666;
            referenceCidsBean.setProperty(SPIELHALLE_META_CLASS.getPrimaryKey().toLowerCase(), newId);
            cidsBean.setProperty(SPIELHALLE_META_CLASS.getPrimaryKey().toLowerCase(), newId);
            LOGGER.debug("testSetPrimaryKeyProperty: " + newId);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        Assert.assertEquals("Reference CidsBean and Reference MetaObject PKs do match",
                                referenceCidsBean.getCidsBeanInfo().getObjectKey(), referenceMetaObject.getPrimaryKey().getValue().toString());

                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName(Mockito.anyString());
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertEquals("CidsBean and MetaObject PKs do match",
                                cidsBean.getCidsBeanInfo().getObjectKey(), metaObjectSpy.getPrimaryKey().getValue().toString());
                        LOGGER.info("testSetPrimaryKeyProperty passed: " + cidsBean.getCidsBeanInfo().getObjectKey());
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw e;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }

    @Test
    public void test04setStringProperty() throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            final Semaphore semaphore = new Semaphore(1);
            final String name = "Spielhölle";
            referenceCidsBean.setProperty("name", name);
            cidsBean.setProperty("name", name);
            LOGGER.debug("testSetStringProperty: " + name);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        Assert.assertEquals("Reference CidsBean and Reference MetaObject names do match after changing property",
                                referenceCidsBean.getProperty("name"),
                                referenceMetaObject.getAttributeByFieldName("name").getValue().toString());

// Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertNotNull("MetaObject name attribute available",
                                metaObjectSpy.getAttributeByFieldName("name"));
                        Assert.assertNotNull("MetaObject name attribute value not null",
                                metaObjectSpy.getAttributeByFieldName("name").getValue());
                        Assert.assertEquals("CidsBean and MetaObject names do match after changing property",
                                cidsBean.getProperty("name"),
                                metaObjectSpy.getAttributeByFieldName("name").getValue().toString());

                        LOGGER.info("testSetStringProperty passed: " + cidsBean.getProperty("name"));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);

                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Mockito.reset(metaObjectSpy);
            referenceCidsBean.setProperty("name", name);
            cidsBean.setProperty("name", name);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        Assert.assertEquals("Reference CidsBean and Reference MetaObject names do match after changing property again",
                                referenceCidsBean.getProperty("name"),
                                referenceMetaObject.getAttributeByFieldName("name").getValue().toString());

                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        // verify that modified status is not set again!
                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(Mockito.anyInt());

                        Assert.assertEquals("CidsBean and MetaObject names do match after changing property again",
                                cidsBean.getProperty("name"),
                                metaObjectSpy.getAttributeByFieldName("name").getValue().toString());

                        LOGGER.info("testSetStringProperty passed: " + cidsBean.getProperty("name"));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Mockito.reset(metaObjectSpy);

            final String newName = "Lucky Casino";
            cidsBean.setProperty("name", newName);
            referenceCidsBean.setProperty("name", newName);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        Assert.assertEquals("Reference MetaObject property successfully updated",
                                newName,
                                referenceMetaObject.getAttributeByFieldName("name").getValue().toString());

                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        // verify that modified status is not set again!
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertEquals("CidsBean property successfully updated",
                                newName,
                                cidsBean.getProperty("name"));

                        Assert.assertNotNull("MetaObject name attribute available",
                                metaObjectSpy.getAttributeByFieldName("name"));
                        Assert.assertNotNull("MetaObject name attribute value not null",
                                metaObjectSpy.getAttributeByFieldName("name").getValue());
                        Assert.assertEquals("MetaObject property successfully updated",
                                newName,
                                metaObjectSpy.getAttributeByFieldName("name").getValue().toString());

                        Assert.assertEquals("CidsBean and MetaObject names do match after changing property again",
                                cidsBean.getProperty("name"),
                                metaObjectSpy.getAttributeByFieldName("name").getValue().toString());

                        LOGGER.info("testSetStringProperty passed: " + cidsBean.getProperty("name"));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw e;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }

    @Test
    public void test05setCidsBeanProperty() throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            final Semaphore semaphore = new Semaphore(1);
            final CidsBean betreiberBean = BETREIBER_META_CLASS.getEmptyInstance().getBean();
            final CidsBean referenceBetreiberBean = BETREIBER_META_CLASS.getEmptyInstance().getBean();
            final String name = "Mike Hansen";
            cidsBean.setProperty("betreiber", betreiberBean);
            referenceCidsBean.setProperty("betreiber", referenceBetreiberBean);
            LOGGER.debug("testSetCidsBeanProperty: " + betreiberBean.getMOString());

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        Assert.assertTrue("Reference MetaObject Attribute is MetaObject",
                                MetaObject.class.isAssignableFrom(referenceMetaObject.getAttributeByFieldName("betreiber").getValue().getClass()));

                        Assert.assertEquals("updated Reference CidsBean and Reference MetaObject Properties do match",
                                referenceBetreiberBean.toJSONString(true),
                                ((MetaObject) referenceMetaObject.getAttributeByFieldName("betreiber").getValue()).getBean().toJSONString(true));

                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("betreiber");
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        //System.out.println(metaObjectSpy.getStatus());
                        Assert.assertNotNull("CidsBean property correctly set: " + metaObjectSpy.getAttributeByFieldName("betreiber"),
                                metaObjectSpy.getAttributeByFieldName("betreiber"));
                        Assert.assertNotNull("CidsBean property correctly set: " + metaObjectSpy.getAttributeByFieldName("betreiber").getValue(),
                                metaObjectSpy.getAttributeByFieldName("betreiber").getValue());

                        Assert.assertTrue("MetaObject Attribute is MetaObject",
                                MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("betreiber").getValue().getClass()));

                        Assert.assertEquals("updated CidsBean and MetaObject Properties do match",
                                betreiberBean.toJSONString(true),
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().toJSONString(true));

                        LOGGER.info("testSetCidsBeanProperty passed");
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Mockito.reset(metaObjectSpy);
            final MetaObject subMetaObjectSpy = Mockito.spy(betreiberBean.getMetaObject());
            betreiberBean.setMetaObject(subMetaObjectSpy);
            ((CidsBean) cidsBean.getProperty("betreiber")).setProperty("name", name);
            ((CidsBean) referenceCidsBean.getProperty("betreiber")).setProperty("name", name);

            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        Assert.assertTrue("Status of new Reference MetaObject is set to NEW",
                                ((MetaObject) referenceMetaObject.getAttributeByFieldName("betreiber").getValue()).getStatus()
                                == MetaObject.NEW);

                        Assert.assertEquals("new Reference CidsBean and Reference MetaObject Properties do match",
                                betreiberBean.getProperty("name"),
                                ((MetaObject) referenceMetaObject.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));

                        Assert.assertEquals("updated Reference CidsBean and Reference MetaObject Properties do match",
                                betreiberBean.getProperty("name"),
                                referenceBetreiberBean.getMetaObject().getAttributeByFieldName("name").getValue());

                        // Mockito Spy Object: verify that a certain method has been called
                        //Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(Mockito.anyInt());
                        Mockito.verify(subMetaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        Mockito.verify(subMetaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        //System.out.println(((MetaObject)metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getStatus() );
                        Assert.assertNotNull("CidsBean property correctly set: " + metaObjectSpy.getAttributeByFieldName("betreiber"),
                                metaObjectSpy.getAttributeByFieldName("betreiber"));
                        Assert.assertNotNull("CidsBean property correctly set: " + metaObjectSpy.getAttributeByFieldName("betreiber").getValue(),
                                metaObjectSpy.getAttributeByFieldName("betreiber").getValue());

                        Assert.assertTrue("Status of new MetaObject is set to NEW",
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getStatus()
                                == MetaObject.NEW);

                        Assert.assertEquals("new CidsBean and MetaObject Properties do match",
                                betreiberBean.getProperty("name"),
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));

                        Assert.assertEquals("updated CidsBean and MetaObject Properties do match",
                                betreiberBean.getProperty("name"),
                                subMetaObjectSpy.getAttributeByFieldName("name").getValue());

                        LOGGER.info("testSetCidsBeanProperty passed: " + betreiberBean.getProperty("name"));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

            Mockito.reset(metaObjectSpy);
            betreiberBean.setProperty("name", name);
            referenceBetreiberBean.setProperty("name", name);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        Assert.assertEquals("updated CidsBean and MetaObject Properties do match after updating them again",
                                referenceBetreiberBean.getProperty("name"),
                                ((MetaObject) referenceMetaObject.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));

                        // Mockito Spy Object: verify that a certain method has not been called
                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(MetaObject.MODIFIED);

                        Assert.assertEquals("updated CidsBean and MetaObject Properties do match after updating them again",
                                betreiberBean.getProperty("name"),
                                ((MetaObject) metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));

                        LOGGER.info("testSetCidsBeanProperty passed: " + betreiberBean.getProperty("name"));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw e;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }

    @Test
    public void test06setCidsBeanNtoMArrayProperty() throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            final Semaphore semaphore = new Semaphore(1);
            final CidsBean kategorieBean = SPIELHALLE_META_CLASS.getEmptyInstance().getBean();
            final CidsBean referenceKategorieBean = SPIELHALLE_META_CLASS.getEmptyInstance().getBean();

            final MetaObject kategorieMetaObject = kategorieBean.getMetaObject();
            final MetaObject referenceKategorieMetaObject = referenceKategorieBean.getMetaObject();

            final String name = "UnitTestCategory";
            kategorieBean.setProperty("name", name);
            referenceKategorieBean.setProperty("name", name);

            Assert.assertNull("new referenceKategorieMetaObject not yet assigned to spiehalle object",
                    referenceKategorieMetaObject.getReferencingObjectAttribute());

            Assert.assertNull("new kategorieMetaObject not yet assigned to spiehalle object",
                    kategorieMetaObject.getReferencingObjectAttribute());

            cidsBean.getBeanCollectionProperty("kategorien").add(kategorieBean);
            referenceCidsBean.getBeanCollectionProperty("kategorien").add(referenceKategorieBean);

            LOGGER.debug("testSetCidsBeanNtoMArrayProperty: " + kategorieBean.getMOString());

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {

                        Assert.assertSame("new referenceKategorieMetaObject properly assigned to CidsBean array",
                                referenceKategorieMetaObject,
                                referenceCidsBean.getBeanCollectionProperty("kategorien").get(
                                referenceCidsBean.getBeanCollectionProperty("kategorien").size() - 1).getMetaObject());
                        Assert.assertNotNull("new referenceKategorieMetaObject assigned to spiehalle object",
                                referenceKategorieMetaObject.getReferencingObjectAttribute());
                        Assert.assertNotNull("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object correctly set",
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject());
                        Assert.assertNotNull("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute correctly set",
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute());
                        Assert.assertTrue("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute is array attribute",
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().isArray());
                        Assert.assertNotNull("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute parent object correctly set",
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject());
                        Assert.assertTrue("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute parent object is Dummy",
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().isDummy());
                        Assert.assertNotNull("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute dummy object ReferencingObjectAttribute correctly set",
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute());
                        Assert.assertNotNull("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute dummy object ReferencingObjectAttribute object correctly set",
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject());
                        Assert.assertSame("new referenceKategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute dummy object ReferencingObjectAttribute object correctly set to spielhalle object",
                                referenceMetaObject,
                                referenceKategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject());

                        // Mockito Spy Object: verify that a certain method has been called
                        //Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("kategorie");
                        //Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);
                        Assert.assertSame("new kategorieMetaObject properly assigned to CidsBean array",
                                kategorieMetaObject,
                                cidsBean.getBeanCollectionProperty("kategorien").get(
                                cidsBean.getBeanCollectionProperty("kategorien").size() - 1).getMetaObject());
                        Assert.assertNotNull("new kategorieMetaObject assigned to spiehalle object",
                                kategorieMetaObject.getReferencingObjectAttribute());
                        Assert.assertNotNull("new kategorieMetaObject ReferencingObjectAttribute intermediate array object correctly set",
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject());
                        Assert.assertNotNull("new kategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute correctly set",
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute());
                        Assert.assertTrue("new kategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute is array attribute",
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().isArray());
                        Assert.assertNotNull("new kategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute parent object correctly set",
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject());
                        Assert.assertTrue("new kategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute parent object is Dummy",
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().isDummy());
                        Assert.assertNotNull("new kategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute dummy object ReferencingObjectAttribute correctly set",
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute());
                        Assert.assertNotNull("new kategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute dummy object ReferencingObjectAttribute object correctly set",
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject());

                        // test against origion meta object and not Mockity metaObjectSpy since 
                        // Mockity doesn't update all reference to parent MO!!!!!
                        Assert.assertSame("new kategorieMetaObject ReferencingObjectAttribute intermediate array object ReferencingObjectAttribute dummy object ReferencingObjectAttribute object correctly set to spielhalle object",
                                metaObject,
                                kategorieMetaObject.getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject().getReferencingObjectAttribute().getParentObject());

                        LOGGER.info("testSetCidsBeanNtoMArrayProperty passed!");
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        LOGGER.debug(referenceMetaObject.getDebugString());
                        LOGGER.debug(metaObjectSpy.getDebugString());
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug(referenceMetaObject.getDebugString());
            LOGGER.debug(metaObjectSpy.getDebugString());
            throw e;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }
}
