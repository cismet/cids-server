/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions.graphql;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

import java.util.Iterator;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GraphQlTestCases {

    //~ Instance fields --------------------------------------------------------

    private final MetaService ms;
    private final User user;
    private final ConnectionContext cc;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GraphQlTestCases object.
     *
     * @param  ms    DOCUMENT ME!
     * @param  user  DOCUMENT ME!
     * @param  cc    DOCUMENT ME!
     */
    public GraphQlTestCases(final MetaService ms, final User user, final ConnectionContext cc) {
        this.ms = ms;
        this.user = user;
        this.cc = cc;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void startTests() {
        try {
            final InputStream in = GraphQlTestCases.class.getResourceAsStream(
                    "/de/cismet/cids/graphql/test/GraphQlTestCases.json");

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode node = mapper.readTree(in);
            final JsonNode cases = node.get("cases");
            final Iterator<JsonNode> it = cases.elements();

            while (it.hasNext()) {
                final JsonNode test = it.next();

                final String query = test.get("query").asText();
                final String expectedResult = test.get("expectedResult").asText();

                final GraphQlPermissionEvaluator evaluator = new GraphQlPermissionEvaluator(ms, user, cc);
                final String result = evaluator.evaluate(query);

                if (result.equals(expectedResult)) {
                    System.out.println("test successful");
                } else {
                    System.out.println("test failed: " + query);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
