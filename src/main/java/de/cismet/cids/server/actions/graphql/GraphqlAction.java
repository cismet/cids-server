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

import java.util.Arrays;
import java.util.Properties;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

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

    private Properties config = null;
    private MetaService ms;
    private User user;

    private final String queryTemplate = "{\"query\": \"%1s\", \"variables\": %2s}";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GraphqlAction object.
     */
    public GraphqlAction() {
        try {
            config = ServerResourcesLoader.getInstance()
                        .loadProperties(GeneralServerResources.GRAPHQL_PROPERTIES.getValue());
        } catch (Exception e) {
            LOG.error("Error while loading graphQl resources", e);
        }
    }

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
        String variables = "";
        final String hasuraSecret = config.getProperty("hasura.secret", null);

        for (final ServerActionParameter sap : params) {
            if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.QUERY.toString())) {
                query = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.VARIABLES.toString())) {
                variables = (String)sap.getValue();
            }
        }

        final GraphQlPermissionEvaluator evaluator = new GraphQlPermissionEvaluator(ms, user, cc);
        final String tablesWithoutCheck = config.getProperty("tables.without.permission.check", null);

        if (tablesWithoutCheck != null) {
            evaluator.setTablesWithoutPermissionCheck(Arrays.asList(tablesWithoutCheck.split(",")));
        }
        query = evaluator.evaluate(query);

        try {
            // prepare request
            final URL url = new URL(config.getProperty("graphql.url"));
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            if (hasuraSecret != null) {
                connection.setRequestProperty("x-hasura-admin-secret", hasuraSecret);
            }

            final OutputStream os = connection.getOutputStream();
            final byte[] inputAsBytes = String.format(queryTemplate, query, ((variables == null) ? "{}" : variables))
                        .getBytes("utf-8");
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
