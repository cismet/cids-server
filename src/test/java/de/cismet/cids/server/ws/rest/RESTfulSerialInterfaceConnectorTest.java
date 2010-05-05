/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.ServerExit;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.newuser.User;
import Sirius.server.property.ServerProperties;
import Sirius.server.registry.Registry;
import Sirius.server.search.SearchOption;
import Sirius.server.search.SearchResult;
import Sirius.util.image.Image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import java.util.Properties;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class RESTfulSerialInterfaceConnectorTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final String ROOT_RESOURCE = "http://localhost:8011/callserver/binary/"; // NOI18N
    private static final String SERVER_CONFIG =
            "src/test/resources/Sirius/server/localserver/object/runtime.properties"; // NOI18N
    private static final String STARTMODE = "notSimple"; // NOI18N

    private static RESTfulSerialInterfaceConnector connector;
    private static Registry registry;
    private static StartProxy proxy;
    private static DomainServerImpl server;

    private User admin;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistenceManagerTest object.
     */
    public RESTfulSerialInterfaceConnectorTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @BeforeClass
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
        RESTfulSerialInterface.up(8011);
        final PropertiesWrapper pw = new PropertiesWrapper(SERVER_CONFIG);
        pw.setStartMode(STARTMODE);
        server = new DomainServerImpl(pw);
        connector = new RESTfulSerialInterfaceConnector(ROOT_RESOURCE);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Throwable {
        try {
            RESTfulSerialInterface.down();
            server.shutdown();
            proxy.shutdown();
            registry.shutdown();
        } catch (final ServerExit serverExit) {
            // success...
        }
    }

    /**
     * DOCUMENT ME!
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

    // TODO: why does this test fail when run with the other tests but succeeds otherwise ?
    @Test
    public void testChangePassword() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sb");
        assertNotNull(user);
        boolean changed = connector.changePassword(user, "sb", "sbs");
        assertTrue(changed);
        user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sbs");
        assertNotNull(user);
        changed = connector.changePassword(user, "sbs", "sb");
        assertTrue(changed);
        user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sb");
        assertNotNull(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetDomains() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final String[] domains = connector.getDomains();
        assertNotNull(domains);
        for (final String domain : domains) {
            System.out.println("domain: " + domain);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetUserGroupNames() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Vector ugNames = connector.getUserGroupNames();
        assertNotNull(ugNames);
        for (final Object o : ugNames) {
            final String[] ug = (String[])o;
            System.out.println("ug: " + ug[0] + "@" + ug[1]);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetUserGroupNames_String_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Vector ugNames = connector.getUserGroupNames("admin", "WUNDA_BLAU");
        assertNotNull(ugNames);
        for (final Object o : ugNames) {
            final String[] ug = (String[])o;
            System.out.println("ug: " + ug[0] + "@" + ug[1]);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetUser() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sb");
        assertNotNull(user);
        System.out.println("user: " + user);
    }


//    @Test
//    public void testGetDefaultIcons() throws Exception {
//        System.out.println("\nTEST: " + getCurrentMethodName());
//        final Image[] icons = connector.getDefaultIcons();
//        assertNotNull(icons);
//        for(final Image icon : icons){
//            System.out.println("icon: " + icon);
//        }
//    }

    @Test
    public void testGetDefaultIcons_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Image[] icons = connector.getDefaultIcons("WUNDA_BLAU");
        assertNotNull(icons);
        for(final Image icon : icons){
            System.out.println("icon: " + icon);
        }

    }

    @Test
    public void testSearch() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final SearchResult result = connector.search(admin, new String[]{"1", "2", "3"}, new SearchOption[0]);
        assertNotNull(result);
        System.out.println("searchresult: " + result);
    }

    @Test
    public void testGetSearchOptions() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sb");
        final HashMap result = connector.getSearchOptions(user, "WUNDA_BLAU");
        assertNotNull(result);
        System.out.println("getsearchoptions: " + result);
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
