package de.cismet.cids.dynamics;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import com.tngtech.java.junit.dataprovider.DataProvider;
import static org.mockito.Mockito.*;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.cismet.cids.utils.MetaClassCacheService;
import java.awt.EventQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.openide.util.Lookup;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.junit.Assert;

/**
 *
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
    private MetaObject metaObjectSpy = null;

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
            SPIELHALLE_META_CLASS = classCacheService.getMetaClass("CIDS", "SPH_SPIELHALLE");
            BETREIBER_META_CLASS = classCacheService.getMetaClass("CIDS", "SPH_BETREIBER");
            KATEGORIE_META_CLASS = classCacheService.getMetaClass("CIDS", "SPH_KATEGORIE");
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
        final DefaultMetaObject metaObject = (DefaultMetaObject) SPIELHALLE_META_CLASS.getEmptyInstance();
        cidsBean = metaObject.getBean();
        metaObjectSpy = Mockito.spy(metaObject);
        cidsBean.setMetaObject(metaObjectSpy);
    }

    /**
     * DOCUMENT ME!
     */
    @After
    public void tearDown() {
        cidsBean = null;
        metaObjectSpy = null;
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
    public void testGetCidsBeanInfo() throws Exception {

        try {
            final CidsBeanInfo beanInfo = cidsBean.getCidsBeanInfo();
            LOGGER.info("testGetCidsBeanInfo: " + beanInfo);

            // Mockito Spy Object: verify that a certain method has been called
            Mockito.verify(metaObjectSpy, Mockito.atLeastOnce()).getMetaClass();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testGetPrimaryKeyProperty() throws Exception {

        try {
            LOGGER.info("testGetPrimaryKeyProperty: " + cidsBean.getPrimaryKeyValue());
            // Mockito Spy Object: verify that a certain method has never been called
            Mockito.verify(metaObjectSpy, Mockito.never()).getAttributeByFieldName(Mockito.anyString());

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testSetPrimaryKeyProperty() throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            final Semaphore semaphore = new Semaphore(1);
            final int newId = 666;
            cidsBean.setProperty(SPIELHALLE_META_CLASS.getPrimaryKey().toLowerCase(), newId);
            LOGGER.info("testSetPrimaryKeyProperty: " + newId);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName(Mockito.anyString());
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertEquals(cidsBean.getCidsBeanInfo().getObjectKey(), metaObjectSpy.getPrimaryKey().getValue().toString());
                        LOGGER.debug("testSetPrimaryKeyProperty passed: " + cidsBean.getCidsBeanInfo().getObjectKey());
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }

    @Test
    public void testSetStringProperty() throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            final Semaphore semaphore = new Semaphore(1);
            final String name = "Spielhölle";
            cidsBean.setProperty("name", name);
            LOGGER.info("testSetStringProperty: " + name);

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertEquals(cidsBean.getProperty("name"), metaObjectSpy.getAttributeByFieldName("name").getValue().toString());
                        LOGGER.debug("testSetStringProperty passed: " + cidsBean.getProperty("name"));
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
            cidsBean.setProperty("name", name);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("name");
                        // verify that modified status is not set again!
                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(Mockito.anyInt());

                        Assert.assertEquals(cidsBean.getProperty("name"), metaObjectSpy.getAttributeByFieldName("name").getValue().toString());
                        LOGGER.debug("testSetStringProperty passed: " + cidsBean.getProperty("name"));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }
    
    @Test
    public void testSetCidsBeanProperty() throws Throwable {

        final LinkedList<Throwable> throwablesFromThread = new LinkedList<Throwable>();

        try {
            final Semaphore semaphore = new Semaphore(1);
            final CidsBean betreiberBean = BETREIBER_META_CLASS.getEmptyInstance().getBean();
            final String name = "Mike Hansen";

            cidsBean.setProperty("betreiber", betreiberBean);
            LOGGER.info("testSetCidsBeanProperty: " + betreiberBean.getMOString());

            // wait for property change event!
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).getAttributeByFieldName("betreiber");
                        Mockito.verify(metaObjectSpy, Mockito.times(1)).setStatus(MetaObject.MODIFIED);

                        Assert.assertTrue(metaObjectSpy.getStatus() == MetaObject.MODIFIED);
                        Assert.assertNotNull(metaObjectSpy.getAttributeByFieldName("betreiber").getValue());
                        Assert.assertTrue(MetaObject.class.isAssignableFrom(metaObjectSpy.getAttributeByFieldName("betreiber").getValue().getClass()));
                        Assert.assertEquals(betreiberBean.toJSONString(true), 
                                ((MetaObject)metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().toJSONString(true));
                        LOGGER.debug("testSetCidsBeanProperty passed");
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
            //betreiberBean.setProperty("name", name);
            ((CidsBean)cidsBean.getProperty("betreiber")).setProperty("name", name);
            
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Mockito Spy Object: verify that a certain method has been called
                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(Mockito.anyInt());
                       
                        Assert.assertTrue(metaObjectSpy.getStatus() == MetaObject.NO_STATUS);
                        Assert.assertEquals(betreiberBean.getProperty("name"), 
                                ((MetaObject)metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));
                        LOGGER.debug("testSetCidsBeanProperty passed: " + betreiberBean.getProperty("name"));
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
            betreiberBean.setProperty("name", name);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                       // Mockito Spy Object: verify that a certain method has not been called
                        Mockito.verify(metaObjectSpy, Mockito.never()).setStatus(MetaObject.MODIFIED);

                        Assert.assertEquals(betreiberBean.getProperty("name"), 
                                ((MetaObject)metaObjectSpy.getAttributeByFieldName("betreiber").getValue()).getBean().getProperty("name"));
                        LOGGER.debug("testSetCidsBeanProperty passed: " + betreiberBean.getProperty("name"));
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                        throwablesFromThread.add(t);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            semaphore.acquire();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            if (!throwablesFromThread.isEmpty()) {
                throw (throwablesFromThread.getLast());
            }
        }
    }
}
