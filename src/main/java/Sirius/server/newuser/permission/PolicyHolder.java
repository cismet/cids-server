/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.newuser.permission;

import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.io.Serializable;

import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class PolicyHolder extends Shutdown implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(PolicyHolder.class);

    //~ Instance fields --------------------------------------------------------

    private Map<String, Map<Permission, Boolean>> policyHolder = new HashMap<String, Map<Permission, Boolean>>();
    private Map<Integer, String> idMapper = new HashMap<Integer, String>();
    private Map<String, Integer> idReverseMapper = new HashMap<String, Integer>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PolicyHolder object.
     *
     * @param   conPool  DOCUMENT ME!
     * @param   dialect  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public PolicyHolder(final DBConnectionPool conPool, final String dialect) throws ServerExitError {
        final DBConnection con = conPool.getDBConnection();
        try {
            final ResultSet serverPolicies = con.getConnection()
                        .createStatement()
                        .executeQuery(SQLTools.getStatements(
                                Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                            .getPolicyHolderServerPoliciesStmt());

            if (serverPolicies == null) {
                LOG.error(
                    "<LS> ERROR :: Serverpolicies could not be loaded. Fatal Error. Program exits"); // NOI18N
                throw new ServerExitError(
                    "Serverpolicies could not be loaded. Fatal Error. Program exits");               // NOI18N
            }

            while (serverPolicies.next()) {
                final String policyName = serverPolicies.getString("name");              // NOI18N
                final int policyId = serverPolicies.getInt("policyid");                  // NOI18N
                final int permId = serverPolicies.getInt("permissionid");                // NOI18N
                final String permissionKey = serverPolicies.getString("key");            // NOI18N
                final boolean defaultvalue = serverPolicies.getBoolean("default_value"); // NOI18N
                final Permission p = new Permission(permId, permissionKey);
                if (!policyHolder.containsKey(policyName)) {
                    final HashMap<Permission, Boolean> policyMap = new HashMap<Permission, Boolean>();
                    policyHolder.put(policyName, policyMap);
                }
                policyHolder.get(policyName).put(p, defaultvalue);
                idMapper.put(policyId, policyName);
                idReverseMapper.put(policyName, policyId);
            }

            LOG.info("Serverpolicies: " + policyHolder); // NOI18N

            addShutdown(new AbstractShutdownable() {

                    @Override
                    protected void internalShutdown() throws ServerExitError {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("shutting down PolicyHolder"); // NOI18N
                        }

                        policyHolder.clear();
                        idMapper.clear();
                        idReverseMapper.clear();
                    }
                });
        } catch (Exception e) {
            // Safetyfirst
            LOG.error("Error while analysing server policies", e);                 // NOI18N
            throw new ServerExitError("Error while analysing server policies", e); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   policyId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Policy getServerPolicy(final int policyId) {
        if (idMapper.containsKey(policyId)) {
            return getServerPolicy(idMapper.get(policyId), policyId);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Policy getServerPolicy(final String policyName) {
        return getServerPolicy(policyName, idReverseMapper.get(policyName));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policyName  DOCUMENT ME!
     * @param   policyId    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Policy getServerPolicy(final String policyName, final int policyId) {
        return new Policy(policyHolder.get(policyName), policyId, policyName);
    }
}
