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

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    private static final String QUERY = "SELECT * FROM daq.";
    private static final transient Logger LOG = Logger.getLogger(DataAquisitionAction.class);
    private static final ConnectionContext cc = ConnectionContext.create(
            ConnectionContext.Category.ACTION,
            "DataAquisition");
    private static final String QUOTE_IDENTIFIER = "select quote_ident(?)";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        daqView, cachingHint
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
        return "DataAquisition";
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String daqView = null;
        String cachingHint = null;

        for (final ServerActionParameter sap : params) {
            if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.daqView.toString())) {
                daqView = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.cachingHint.toString())) {
                cachingHint = (String)sap.getValue();
            }
        }

        try {
            if (daqView == null) {
                return "{exception: 'No view specified'}";
            }

            final DomainServerImpl domainServer = (DomainServerImpl)ms;

            final String allowedViews = domainServer.getConfigAttr(user, "allowedViewsForDataAquisition", cc);

            final StringTokenizer st = new StringTokenizer(allowedViews, "\n");
            boolean isAllowed = false;

            while (st.hasMoreTokens()) {
                if (st.nextToken().equals(daqView)) {
                    isAllowed = true;
                }
            }

            if (!isAllowed) {
                return "{exception: 'Not allowed'}";
            }

            final Connection con = domainServer.getConnectionPool().getConnection();
            daqView = quoteIdentifier(con, daqView);
            final ArrayList<ArrayList> result = ms.performCustomSearch(QUERY + daqView, cc);

            if ((result != null) && (result.size() > 0)) {
                if ((result.get(0) != null) && (result.get(0).size() > 0)) {
                    return result.get(0).get(0);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while extracting the data sources", e);
            return "{exception: '" + e.getMessage() + " '}";
        }

        return "{exception: 'No data'}";
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
