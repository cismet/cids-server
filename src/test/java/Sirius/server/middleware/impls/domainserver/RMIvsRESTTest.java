/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.domainserver;

import Sirius.server.ServerExit;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.middleware.interfaces.proxy.UserService;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.property.ServerProperties;
import Sirius.server.registry.Registry;
import de.cismet.cids.server.connectioncontext.ConnectionContext;
import de.cismet.cids.server.connectioncontext.ConnectionContextProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.rmi.Naming;
import java.rmi.RemoteException;

import java.util.Properties;

import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import de.cismet.cids.server.ws.rest.RESTfulService;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class RMIvsRESTTest implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final String ROOT_RESOURCE = "http://localhost:8011/callserver/binary/";                               // NOI18N
    private static final String SERVER_CONFIG =
        "src/test/resources/Sirius/server/localserver/object/runtime.properties";                                         // NOI18N
    private static final String STARTMODE = "notSimple";                                                                  // NOI18N

    private static RESTfulSerialInterfaceConnector connector;
    private static Registry registry;
    private static StartProxy proxy;
    private static DomainServerImpl server;

    //~ Instance fields --------------------------------------------------------

    private transient volatile long fastest;
    private transient volatile long slowest;
    private transient volatile long average;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    //@BeforeClass
    public static void setUpClass() throws Throwable {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(p);

        registry = new Sirius.server.registry.Registry(1099);
        proxy = StartProxy.getInstance(SERVER_CONFIG);
        final PropertiesWrapper pw = new PropertiesWrapper(SERVER_CONFIG);
        pw.setStartMode(STARTMODE);
        RESTfulService.up(pw);
        server = new DomainServerImpl(pw);
        connector = new RESTfulSerialInterfaceConnector(ROOT_RESOURCE);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    //@AfterClass
    public static void tearDownClass() throws Throwable {
        RESTfulService.down();
        try {
            server.shutdown();
        } catch (final ServerExit e) {
            // success
        }
        try {
            proxy.shutdown();
        } catch (final ServerExit e) {
            // success
        }
        try {
            registry.shutdown();
        } catch (final ServerExit serverExit) {
            // success...
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Before
    public void setUp() throws Exception {
//        admin = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sb");
    }

    /**
     * DOCUMENT ME!
     */
    @After
    public void tearDown() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception         DOCUMENT ME!
     * @throws  RuntimeException  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testGetMetaObjectRMI() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Object callserver = Naming.lookup("rmi://localhost/callServer");
        final User user = ((UserService)callserver).getUser(
                "WUNDA_BLAU",
                "Administratoren",
                "WUNDA_BLAU",
                "cismet",
                "sb", ConnectionContext.create(RMIvsRESTTest.class.getSimpleName()));
        final String domain = "WUNDA_BLAU";
        final int objectID = 3;
        final int classID = 106;

        fastest = Long.MAX_VALUE;
        slowest = Long.MIN_VALUE;
        average = 0;
        for (int i = 0; i < 1000; ++i) {
            final long before = System.currentTimeMillis();
            final MetaObject result = ((MetaService)callserver).getMetaObject(user, objectID, classID, domain, getConnectionContext());
            final long after = System.currentTimeMillis();

            if ((i % 50) == 0) {
                System.out.println(i + " MOs received");
            }

            final long duration = after - before;
            if (duration < fastest) {
                fastest = duration;
            } else if (duration > slowest) {
                slowest = duration;
            }

            average += duration;
        }

        System.out.println("RMI st fastest: " + fastest);
        System.out.println("RMI st slowest: " + slowest);
        System.out.println("RMI st average: " + (average / 1000));

        fastest = Long.MAX_VALUE;
        slowest = Long.MIN_VALUE;
        average = 0;

        final Runnable runner = new Runnable() {

                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 200; ++i) {
                            final long before = System.currentTimeMillis();
                            final MetaObject result = ((MetaService)callserver).getMetaObject(
                                    user,
                                    objectID,
                                    classID,
                                    domain, getConnectionContext());
                            final long after = System.currentTimeMillis();
                            final long duration = after - before;
                            if (duration < fastest) {
                                fastest = duration;
                            } else if (duration > slowest) {
                                slowest = duration;
                            }

                            average += duration;
                        }
                    } catch (final RemoteException ex) {
                        System.err.println("ex" + ex);
                        ex.printStackTrace();
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            };

        final Thread t1 = new Thread(runner);
        final Thread t2 = new Thread(runner);
        final Thread t3 = new Thread(runner);
        final Thread t4 = new Thread(runner);
        final Thread t5 = new Thread(runner);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();

        System.out.println("RMI mt fastest: " + fastest);
        System.out.println("RMI mt slowest: " + slowest);
        System.out.println("RMI mt average: " + (average / 1000));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception         DOCUMENT ME!
     * @throws  RuntimeException  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testGetMetaObjectREST() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet", getConnectionContext());
        final String domain = "WUNDA_BLAU";
        final int objectID = 3;
        final int classID = 106;

        fastest = Long.MAX_VALUE;
        slowest = Long.MIN_VALUE;
        average = 0;
        for (int i = 0; i < 1000; ++i) {
            final long before = System.currentTimeMillis();
            final MetaObject result = connector.getMetaObject(user, objectID, classID, domain, getConnectionContext());
            final long after = System.currentTimeMillis();

            if ((i % 50) == 0) {
                System.out.println(i + "MOs received");
            }

            final long duration = after - before;
            if (duration < fastest) {
                fastest = duration;
            } else if (duration > slowest) {
                slowest = duration;
            }

            average += duration;
        }

        System.out.println("REST fastest: " + fastest);
        System.out.println("REST slowest: " + slowest);
        System.out.println("REST average: " + (average / 1000));

        fastest = Long.MAX_VALUE;
        slowest = Long.MIN_VALUE;
        average = 0;

        final Runnable runner = new Runnable() {

                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 200; ++i) {
                            final long before = System.currentTimeMillis();
                            final MetaObject result = connector.getMetaObject(
                                    user,
                                    objectID,
                                    classID,
                                    domain, 
                                    getConnectionContext());
                            final long after = System.currentTimeMillis();
                            final long duration = after - before;
                            if (duration < fastest) {
                                fastest = duration;
                            } else if (duration > slowest) {
                                slowest = duration;
                            }

                            average += duration;
                        }
                    } catch (final RemoteException ex) {
                        System.err.println("ex" + ex);
                        ex.printStackTrace();
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            };

        final Thread t1 = new Thread(runner);
        final Thread t2 = new Thread(runner);
        final Thread t3 = new Thread(runner);
        final Thread t4 = new Thread(runner);
        final Thread t5 = new Thread(runner);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();

        System.out.println("REST mt fastest: " + fastest);
        System.out.println("REST mt slowest: " + slowest);
        System.out.println("REST mt average: " + (average / 1000));
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return ConnectionContext.create(RMIvsRESTTest.class.getSimpleName());
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class PropertiesWrapper extends ServerProperties {

        //~ Instance fields ----------------------------------------------------

        private transient String startMode;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PropertiesWrapper object.
         *
         * @param   configFile  DOCUMENT ME!
         *
         * @throws  FileNotFoundException  DOCUMENT ME!
         * @throws  IOException            DOCUMENT ME!
         */
        public PropertiesWrapper(final String configFile) throws FileNotFoundException, IOException {
            super(configFile);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public String getStartMode() {
            if (startMode == null) {
                return super.getStartMode();
            } else {
                return startMode;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  startMode  DOCUMENT ME!
         */
        public void setStartMode(final String startMode) {
            this.startMode = startMode;
        }
    }
}
