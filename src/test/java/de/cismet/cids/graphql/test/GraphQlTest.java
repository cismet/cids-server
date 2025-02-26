/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.graphql.test;

import Sirius.server.localserver.attribute.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cismet.cids.server.actions.graphql.GraphQlPermissionEvaluator;
import java.io.InputStream;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class GraphQlTest {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassAttributeTest object.
     */
    public GraphQlTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        ClassAttributeTest.configureLog4J();
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
     */
//    @Test
//    public void testQueries() {
//        try {
//            InputStream in = GraphQlTest.class.getResourceAsStream("de/cismet.cids.graphql.test.GraphQlTestCases.json");
//
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode node = mapper.readTree(in);
//            JsonNode cases = node.get("cases");
//            Iterator<JsonNode> it = cases.elements();
//            
//            while (it.hasNext()) {
//                JsonNode test = it.next();
//                
//                String query = test.get("query").asText();
//                String expectedResult = test.get("expectedResult").asText();
//                
//                GraphQlPermissionEvaluator evaluator = new GraphQlPermissionEvaluator(ms, user, cc);
//            }
//
//            assertNotNull("class attr option map shall never be null", attr.getOptions());
//            assertTrue("options not empty", attr.getOptions().isEmpty());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
