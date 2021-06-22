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
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.newuser.User;
import Sirius.server.sql.PreparableStatement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import java.util.ArrayList;
import java.util.StringTokenizer;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class DataAquisitionAction implements ServerAction, MetaServiceStore, UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final String QUERY = "SELECT json, md5(json), now(), null FROM daq.";
    private static final String QUERY_WITH_MD5 =
        "SELECT case when md5 <> ? then json else null::text end, md5, time, version, status FROM daq.%1s";
    private static final transient Logger LOG = Logger.getLogger(DataAquisitionAction.class);
    private static final ConnectionContext cc = ConnectionContext.create(
            ConnectionContext.Category.ACTION,
            "DataAquisition");
    private static final String QUOTE_IDENTIFIER = "select quote_ident(?)";
    private static final String CONF_ATTR_PREFIX = "daq";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        daqKey, cachingHint, md5
    }

    //~ Instance fields --------------------------------------------------------

    private MetaService ms;
    private User user;

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
    public void setMetaService(final MetaService ms) {
        this.ms = ms;
    }

    @Override
    public MetaService getMetaService() {
        return this.ms;
    }

    @Override
    public String getTaskName() {
        return "dataAquisition";
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String daqKey = null;
        String cachingHint = null;
        // The cache will be used when md5 is set. The caller can set md5 to "cached" for example, if the cache
        // should be used, but the checksum should not influence the result
        String md5 = null;
        final DataAquisitionResponse response = new DataAquisitionResponse();

        for (final ServerActionParameter sap : params) {
            if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.daqKey.toString())) {
                daqKey = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.cachingHint.toString())) {
                cachingHint = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.md5.toString())) {
                md5 = (String)sap.getValue();
            }
        }

        try {
            if (daqKey == null) {
                response.setStatus(404);
                LOG.error("Error in DataQuisitionAction: No view specified");
                try {
                    return new ObjectMapper().writeValueAsString(response);
                } catch (JsonProcessingException e) {
                    LOG.error("Error while processing json", e);
                    return response;
                }
            }

            // Determine table/view to use and check permissions
            final DomainServerImpl domainServer = (DomainServerImpl)ms;
            String view = domainServer.getConfigAttr(user, CONF_ATTR_PREFIX + upperFirstLetter(daqKey), cc);

            if ((view == null) || view.equals("")) {
                boolean isAllowed = false;
                final String allowedViews = domainServer.getConfigAttr(user, "allowedViewsForDataAquisition", cc);
                view = daqKey;

                if (allowedViews != null) {
                    final StringTokenizer st = new StringTokenizer(allowedViews, "\n");

                    while (st.hasMoreTokens()) {
                        if (st.nextToken().equals(view)) {
                            isAllowed = true;
                        }
                    }
                }

                if (!isAllowed) {
                    response.setStatus(401);
                    return new ObjectMapper().writeValueAsString(response);
                }
            }

            final Connection con = domainServer.getConnectionPool().getConnection();

            if (md5 != null) {
                // use the cached view
                view = quoteIdentifier(con, view + "_cached");
            } else {
                view = quoteIdentifier(con, view);
            }

            if (md5 != null) {
                // check for md5
                final String query = String.format(QUERY_WITH_MD5, view);
                final PreparableStatement ps = new PreparableStatement(query, new int[] { Types.VARCHAR });
                ps.setObjects(md5);

                final ArrayList<ArrayList> result = ms.performCustomSearch(ps, cc);

                if ((result != null) && (result.size() > 0)) {
                    if ((result.get(0) != null) && (result.get(0).size() > 0)) {
                        if ((getValueAsString(result.get(0).get(1)) != null)
                                    && getValueAsString(result.get(0).get(1)).equals(md5)) {
                            response.setTime(getValueAsString(result.get(0).get(2)));
                            if ((result.get(0).get(4) != null)
                                        && getValueAsString(result.get(0).get(4)).equals("200")) {
                                response.setStatus(304);
                            } else {
                                response.setStatus(206);
                            }
                        } else {
                            response.setContent(getValueAsString(result.get(0).get(0)));
                            response.setMd5(getValueAsString(result.get(0).get(1)));
                            response.setTime(getValueAsString(result.get(0).get(2)));
                            response.setVersion(getValueAsString(result.get(0).get(3)));

                            if ((result.get(0).get(4) != null)
                                        && getValueAsString(result.get(0).get(4)).equals("200")) {
                                response.setStatus(200);
                            } else {
                                response.setStatus(206);
                            }
                        }
                    }
                }
            } else {
                final ArrayList<ArrayList> result = ms.performCustomSearch(QUERY + view, cc);
                if ((result != null) && (result.size() > 0)) {
                    if ((result.get(0) != null) && (result.get(0).size() > 0)) {
                        response.setContent(getValueAsString(result.get(0).get(0)));
                        response.setMd5(getValueAsString(result.get(0).get(1)));
                        response.setTime(getValueAsString(result.get(0).get(2)));
                        response.setVersion(getValueAsString(result.get(0).get(3)));
                        response.setStatus(200);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while extracting the data sources", e);
            response.setStatus(500);
        }

        if (response.getStatus() == null) {
            response.setStatus(404);
        }

        try {
            return new ObjectMapper().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOG.error("Error while processing json", e);
            return response;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getValueAsString(final Object value) {
        if (value == null) {
            return null;
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   word  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String upperFirstLetter(final String word) {
        if (Character.isLowerCase(word.charAt(0))) {
            return Character.toUpperCase(word.charAt(0)) + word.substring(1);
        } else {
            return word;
        }
    }

    /**
     * Quotes the given identifier.
     *
     * @param   con         a db connection
     * @param   identifier  an identifier
     *
     * @return  the quoted identifier
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static String quoteIdentifier(final Connection con, final String identifier) throws Exception {
        String result = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(QUOTE_IDENTIFIER);
            ps.setString(1, identifier);
            rs = ps.executeQuery();

            if (rs.next()) {
                result = rs.getString(1);
            }

            return result;
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }
}
