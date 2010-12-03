/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.history;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class HistoryServerTest {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HistoryServerTest object.
     */
    public HistoryServerTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * DOCUMENT ME!
     */
    @Before
    public void setUp() {
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
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetHistory() throws Exception {
        System.out.println("TEST " + getCurrentMethodName());
        System.err.println("!!!!!!!!!!!!!!!------------------- IMPLEMENT -------------------!!!!!!!!!!!!!!!");
        assertTrue(true);
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testEnqueueEntry() {
        System.out.println("TEST " + getCurrentMethodName());
        System.err.println("!!!!!!!!!!!!!!!------------------- IMPLEMENT -------------------!!!!!!!!!!!!!!!");
        assertTrue(true);
    }
}
