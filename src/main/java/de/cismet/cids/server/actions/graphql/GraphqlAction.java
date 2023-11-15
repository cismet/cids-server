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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

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
    private static final ConnectionContext CC = ConnectionContext.create(
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

        QUERY, VARIABLES, ZIPPED, CHUNKED
    }

    //~ Instance fields --------------------------------------------------------

    private Properties config = null;
    private MetaService ms;
    private User user;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GraphqlAction object.
     */
    public GraphqlAction() {
        try {
            config = ServerResourcesLoader.getInstance()
                        .loadProperties(GeneralServerResources.GRAPHQL_PROPERTIES.getValue());
        } catch (Exception e) {
            LOG.info("Cannot load graphQl resources. The graphQl action cannot be used.");
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
        // reload the configuration
        try {
            config = ServerResourcesLoader.getInstance()
                        .loadProperties(GeneralServerResources.GRAPHQL_PROPERTIES.getValue());
        } catch (Exception e) {
            LOG.info("Cannot reload graphQl resources. The graphQl action cannot be used.");
        }

        if (config == null) {
            LOG.error("The graphQl action cannot be used, because the resources could not be loaded.");

            return null;
        }
        String query = null;
        String variables = "";
        boolean zipped = false;
        boolean chunked = false;
        final String hasuraSecret = config.getProperty("hasura.secret", null);

        for (final ServerActionParameter sap : params) {
            if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.QUERY.toString())) {
                query = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.VARIABLES.toString())) {
                variables = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.ZIPPED.toString())) {
                zipped = ((sap.getValue() != null) && ((String)sap.getValue()).equalsIgnoreCase("true"));
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.CHUNKED.toString())) {
                chunked = ((sap.getValue() != null) && ((String)sap.getValue()).equalsIgnoreCase("true"));
            }
        }

        final GraphQlPermissionEvaluator evaluator = new GraphQlPermissionEvaluator(ms, user, CC);
        final String tablesWithoutCheck = config.getProperty("tables.without.permission.check", null);

        if (tablesWithoutCheck != null) {
            evaluator.setTablesWithoutPermissionCheck(Arrays.asList(tablesWithoutCheck.split(",")));
        }

        try {
            query = evaluator.evaluate(query);

            // prepare request
            final URL url = new URL(config.getProperty("graphql.url"));
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            if (zipped && !chunked) {
                connection.setRequestProperty("Accept-Encoding", "gzip");
            }

            if (hasuraSecret != null) {
                connection.setRequestProperty("x-hasura-admin-secret", hasuraSecret);
            }

            final OutputStream os = connection.getOutputStream();

            // use the ObjectMapper to ensure that valid json is created
            final GraphQlQuery graphQlQuery = new GraphQlQuery();
            graphQlQuery.setQuery(query);
            graphQlQuery.setVariables(((variables == null) ? null : new ObjectMapper().readTree(variables)));

            final String requestAsString = new ObjectMapper().writeValueAsString(graphQlQuery);
            final byte[] inputAsBytes = requestAsString.getBytes("utf-8");

            os.write(inputAsBytes, 0, inputAsBytes.length);

            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("send graphql request (expect gzip: %s): %s", zipped, requestAsString));
            }

            // receive response
            if (!chunked) {
                final InputStream response = connection.getInputStream();
                final String contentType = connection.getHeaderField("Content-Encoding");

                if (zipped && (contentType != null) && contentType.contains("gzip")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("received graphql result in gzip format");
                    }
                    final BufferedInputStream br = new BufferedInputStream(response);
                    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    final byte[] array = new byte[1024];
                    int size;

                    while ((size = br.read(array)) != -1) {
                        bout.write(array, 0, size);
                    }

                    br.close();
                    return bout.toByteArray();
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("received graphql result in text format");
                    }
                    final BufferedReader br = new BufferedReader(new InputStreamReader(response, "utf-8"));
                    final StringBuilder completeResponse = new StringBuilder();
                    String tmpLine;

                    while ((tmpLine = br.readLine()) != null) {
                        completeResponse.append(tmpLine);
                    }

                    return completeResponse.toString();
                }
            } else {
                final InputStream response = connection.getInputStream();
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode rootNode = mapper.readTree(response);
                final JsonNode data = rootNode.get("data");
                boolean canBeParsed = data != null;

                if (canBeParsed) {
                    final Iterator<String> fieldNames = data.fieldNames();
                    canBeParsed = false;

                    while (fieldNames.hasNext()) {
                        final JsonNode node = data.get(fieldNames.next());

                        if (!node.isArray()) {
                            canBeParsed = false;
                            break;
                        } else {
                            canBeParsed = true;
                        }
                    }
                }

                if (canBeParsed) {
                    StringBuilder sb = null;
                    final Iterator<String> fieldNames = data.fieldNames();

                    while (fieldNames.hasNext()) {
                        final String name = fieldNames.next();
                        final JsonNode node = data.get(name);

                        if (sb == null) {
                            sb = new StringBuilder("{\"dataz\": {");
                        } else {
                            sb.append(",");
                        }

                        sb.append(arrayToChunks(name, node));
                    }

                    if (sb == null) {
                        // cannot happen
                        return rootNode.toString();
                    } else {
                        sb.append("}}");
                    }

                    return sb.toString();
                } else {
                    return rootNode.toString();
                }
            }
        } catch (IOException ex) {
            LOG.error(ex, ex);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String arrayToChunks(final String name, final JsonNode node) {
        if (node.isArray()) {
            final StringBuilder sb = new StringBuilder("\"" + name + "\": [");
            boolean firstPacket = true;
            int packets = 20;
            final int size = node.size();
            int chunk = size / packets;

            if (chunk < 200) {
                chunk = node.size();
                packets = 1;
            }

            for (int packet = 0; packet < packets; ++packet) {
                final StringBuilder inner = new StringBuilder("[");
                boolean first = true;

                for (int i = 0; i < ((packet == (packets - 1)) ? (node.size() - (packet * chunk)) : chunk); ++i) {
                    if (!first) {
                        inner.append(",");
                    } else {
                        first = false;
                    }
                    final int index = (packet * chunk) + i;
                    final JsonNode current = node.get(index);
                    inner.append(current.toString());
                }
                inner.append("]");

                if (!firstPacket) {
                    sb.append(",");
                } else {
                    firstPacket = false;
                }
                sb.append("\"");
                sb.append(toCompressedBase64(inner.toString()));
                sb.append("\"");
            }
            sb.append("], \"").append(name).append("_length\": ").append(size);

            return sb.toString();
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toCompressedBase64(final String value) {
        try {
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            final Writer out = new OutputStreamWriter(new GZIPOutputStream(byteOut), "UTF-8");
            out.write(value);
            out.close();
            final byte[] compressedBytes = byteOut.toByteArray();
            final byte[] base64 = Base64.encodeBase64(compressedBytes);

            return new String(base64);
        } catch (Exception e) {
            LOG.error("Error during compression", e);

            return value;
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
