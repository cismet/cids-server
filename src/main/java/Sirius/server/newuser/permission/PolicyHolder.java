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

import Sirius.server.ServerExitError;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;

import java.io.Serializable;

import java.sql.ResultSet;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class PolicyHolder implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private HashMap<String, HashMap<Permission, Boolean>> policyHolder =
        new HashMap<String, HashMap<Permission, Boolean>>();
    private HashMap<Integer, String> idMapper = new HashMap<Integer, String>();
    private HashMap<String, Integer> idReverseMapper = new HashMap<String, Integer>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PolicyHolder object.
     *
     * @param   conPool  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public PolicyHolder(DBConnectionPool conPool) throws ServerExitError {
        DBConnection con = conPool.getConnection();
        try {
            ResultSet serverPolicies = con.getConnection().createStatement()
                        .executeQuery(
                            "SELECT "   // NOI18N
                            + "cs_policy.id as policyid, "   // NOI18N
                            + "cs_policy.name,"   // NOI18N
                            + "cs_permission.id as permissionid,"   // NOI18N
                            + "cs_permission.key,"   // NOI18N
                            + "default_value "   // NOI18N
                            + "FROM "   // NOI18N
                            + "cs_permission,"   // NOI18N
                            + "cs_policy,"   // NOI18N
                            + "cs_policy_rule "   // NOI18N
                            + "WHERE "   // NOI18N
                            + "cs_policy_rule.permission= cs_permission.id "   // NOI18N
                            + "and cs_policy_rule.policy= cs_policy.id ");   // NOI18N
            if (serverPolicies == null) {
                log.error(
                    "<LS> ERROR :: Serverpolicies could not be loaded. Fatal Error. Program exits");   // NOI18N
                throw new ServerExitError(
                    "Serverpolicies could not be loaded. Fatal Error. Program exits");   // NOI18N
            }

            while (serverPolicies.next()) {
                String policyName = serverPolicies.getString("name");   // NOI18N
                int policyId = serverPolicies.getInt("policyid");   // NOI18N
                int permId = serverPolicies.getInt("permissionid");   // NOI18N
                String permissionKey = serverPolicies.getString("key");   // NOI18N
                boolean defaultvalue = serverPolicies.getBoolean("default_value");   // NOI18N
                Permission p = new Permission(permId, permissionKey);
                if (!policyHolder.containsKey(policyName)) {
                    HashMap<Permission, Boolean> policyMap = new HashMap<Permission, Boolean>();
                    policyHolder.put(policyName, policyMap);
                }
                policyHolder.get(policyName).put(p, defaultvalue);
                idMapper.put(policyId, policyName);
                idReverseMapper.put(policyName, policyId);
            }

            log.info("Serverpolicies: " + policyHolder);   // NOI18N
        } catch (Exception e) {
            // Safetyfirst
            log.error("Error while analysing server policies", e);   // NOI18N
            throw new ServerExitError("Error while analysing server policies", e);   // NOI18N
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
    public Policy getServerPolicy(int policyId) {
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
    public Policy getServerPolicy(String policyName) {
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
    private Policy getServerPolicy(String policyName, int policyId) {
        return new Policy(policyHolder.get(policyName), policyId, policyName);
    }
}
