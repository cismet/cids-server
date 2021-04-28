/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions.graphql;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class GraphqlAction implements ServerAction, MetaServiceStore, UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(GraphqlAction.class);
    private static final String jsonResult = "{\n"
                + "  \"data\": {\n"
                + "    \"abzweigdose\": [\n"
                + "      {\n"
                + "        \"id\": 2,\n"
                + "        \"dokumenteArray\": [],\n"
                + "        \"geom\": {\n"
                + "          \"geo_field\": {\n"
                + "            \"type\": \"Point\",\n"
                + "            \"crs\": {\n"
                + "              \"type\": \"name\",\n"
                + "              \"properties\": {\n"
                + "                \"name\": \"urn:ogc:def:crs:EPSG::25832\"\n"
                + "              }\n"
                + "            },\n"
                + "            \"coordinates\": [\n"
                + "              370391.833132958,\n"
                + "              5677158.32765719\n"
                + "            ]\n"
                + "          }\n"
                + "        }\n"
                + "      },\n"
                + "      {\n"
                + "        \"id\": 3,\n"
                + "        \"dokumenteArray\": [],\n"
                + "        \"geom\": {\n"
                + "          \"geo_field\": {\n"
                + "            \"type\": \"Point\",\n"
                + "            \"crs\": {\n"
                + "              \"type\": \"name\",\n"
                + "              \"properties\": {\n"
                + "                \"name\": \"urn:ogc:def:crs:EPSG::25832\"\n"
                + "              }\n"
                + "            },\n"
                + "            \"coordinates\": [\n"
                + "              370391.842388162,\n"
                + "              5677150.77888103\n"
                + "            ]\n"
                + "          }\n"
                + "        }\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "}";

    private static final ConnectionContext cc = ConnectionContext.create(
            ConnectionContext.Category.ACTION,
            "GraphQlAction");

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        QUERY, VARIABLES
    }

    //~ Instance fields --------------------------------------------------------

    private MetaService ms;
    private User user;

    private final String request = "{"
                + "    \"query\": \"query  {"
                + "  abzweigdose {"
                + "    id"
                + "    dokumenteArray {"
                + "      dms_url {"
                + "        name"
                + "        url {"
                + "          object_name"
                + "          url_base {"
                + "            path"
                + "            prot_prefix"
                + "            server"
                + "          }"
                + "        }"
                + "      }"
                + "    }"
                + "    geom {"
                + "      geo_field"
                + "    }"
                + "  }"
                + "}\",\n"
                + "    \"variables\": null\n"
                + "}";
    private final String queryTemplate = "{\"query\": \"%1s\", \"variables\": {\"%2s}";

    //~ Methods ----------------------------------------------------------------

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public String getTaskName() {
        return "graphQl";
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String query = null;
        String variables = "null";

        for (final ServerActionParameter sap : params) {
//            System.out.println(sap);

            if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.QUERY.toString())) {
                query = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.VARIABLES.toString())) {
                variables = (String)sap.getValue();
            }
        }
//        final GraphQlTestCases testCases = new GraphQlTestCases(ms, user, cc);
//        testCases.startTests();

        final GraphQlPermissionEvaluator evaluator = new GraphQlPermissionEvaluator(ms, user, cc);
        query = evaluator.evaluate(query);
//        System.out.println("query: " + query);

        try {
            // prepare request
            final URL url = new URL("http://localhost:8090/v1/graphql");
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            final OutputStream os = connection.getOutputStream();
            final byte[] inputAsBytes = String.format(queryTemplate, query, variables).getBytes("utf-8");
            os.write(inputAsBytes, 0, inputAsBytes.length);

            // receive response
            final InputStream response = connection.getInputStream();

            final BufferedReader br = new BufferedReader(new InputStreamReader(response, "utf-8"));
            final StringBuilder completeResponse = new StringBuilder();
            String tmpLine;

            while ((tmpLine = br.readLine()) != null) {
                completeResponse.append(tmpLine);
            }

            return completeResponse.toString();
        } catch (IOException ex) {
            LOG.error(ex, ex);
            return null;
        }
    }

    @Override
    public MetaService getMetaService() {
        return ms;
    }

    @Override
    public void setMetaService(final MetaService service) {
        ms = service;
    }
}
