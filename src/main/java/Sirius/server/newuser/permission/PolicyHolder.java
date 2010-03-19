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
                            "SELECT "
                            + "cs_policy.id as policyid, "
                            + "cs_policy.name,"
                            + "cs_permission.id as permissionid,"
                            + "cs_permission.key,"
                            + "default_value "
                            + "FROM "
                            + "cs_permission,"
                            + "cs_policy,"
                            + "cs_policy_rule "
                            + "WHERE "
                            + "cs_policy_rule.permission= cs_permission.id "
                            + "and cs_policy_rule.policy= cs_policy.id ");
            if (serverPolicies == null) {
                log.error(
                    "<LS> ERROR :: Serverpolicies konten nicht geladen werden fataler Fehler das Programm beendet sich");
                throw new ServerExitError(
                    "Serverpolicies konten nicht geladen werden fataler Fehler das Programm beendet sich");
            }

            while (serverPolicies.next()) {
                String policyName = serverPolicies.getString("name");
                int policyId = serverPolicies.getInt("policyid");
                int permId = serverPolicies.getInt("permissionid");
                String permissionKey = serverPolicies.getString("key");
                boolean defaultvalue = serverPolicies.getBoolean("default_value");
                Permission p = new Permission(permId, permissionKey);
                if (!policyHolder.containsKey(policyName)) {
                    HashMap<Permission, Boolean> policyMap = new HashMap<Permission, Boolean>();
                    policyHolder.put(policyName, policyMap);
                }
                policyHolder.get(policyName).put(p, defaultvalue);
                idMapper.put(policyId, policyName);
                idReverseMapper.put(policyName, policyId);
            }

            log.info("Serverpolicies: " + policyHolder);
        } catch (Exception e) {
            // Safetyfirst
            log.error("Fehler beim Analysieren der Serverpolicies", e);
            throw new ServerExitError("Fehler beim Analysieren der Serverpolicies", e);
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
