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
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.sql.PreparableStatement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import java.sql.Types;

import java.util.ArrayList;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class DeleteObjectAction implements ServerAction, MetaServiceStore, UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DeleteObjectAction.class);
    private static final ConnectionContext CC = ConnectionContext.create(
            ConnectionContext.Category.ACTION,
            "DeleteObjectAction");

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        className, data
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
        return "DeleteObject";
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String className = null;
        String value = null;
        // The cache will be used when md5 is set. The caller can set md5 to "cached" for example, if the cache
        // should be used, but the checksum should not influence the result
        final String md5 = null;

        for (final ServerActionParameter sap : params) {
            if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.className.toString())) {
                className = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.data.toString())) {
                value = (String)sap.getValue();
            }
        }

        try {
            if ((className == null) || (value == null)) {
                LOG.error("Error in DeleteObjectAction: Not all parameters specified");
                return "{\"Exception\": \"parameters className and data must be set.\"}";
            }

            // Determine table/view to use and check permissions
            final DomainServerImpl domainServer = (DomainServerImpl)ms;
            final MetaClass[] classes = domainServer.getClasses(user, CC);
            final MetaClass clazz = getClassByTableName(classes, className);
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode rootNode = mapper.readTree(value);

            if (clazz == null) {
                return "{\"Exception\": \"Class with table name " + className + " not found.\"}";
            }

            Integer nodeId = ((rootNode.get("id") != null) ? rootNode.get("id").asInt() : null);
            final String uuid = ((rootNode.get("uuid") != null) ? rootNode.get("uuid").asText() : null);

            if ((nodeId != null) && (nodeId > 0)) {
                final MetaObject mo = ms.getMetaObject(user, nodeId, clazz.getID(), CC);

                if (mo != null) {
                    domainServer.deleteMetaObject(user, mo, CC);

                    return "{\"deleted\": true}";
                } else {
                    return "{\"Exception\": \"Object not found.\"}";
                }
            } else if (uuid != null) {
                final PreparableStatement ps = new PreparableStatement("select id from " + className
                                + " where uuid = ?",
                        new int[] { Types.VARCHAR });
                ps.setObjects(uuid);
                final ArrayList<ArrayList> list = ms.performCustomSearch(ps, CC);

                MetaObject mo = null;

                if ((list != null) && (list.size() > 0) && (list.get(0).size() > 0)
                            && (list.get(0).get(0) != null)) {
                    nodeId = (Integer)list.get(0).get(0);
                    mo = ms.getMetaObject(user, nodeId, getClassByTableName(classes, className).getID(), CC);
                }

                if (mo != null) {
                    domainServer.deleteMetaObject(user, mo, CC);

                    return "{\"deleted\": true}";
                } else {
                    return "{\"Exception\": \"Object not found.\"}";
                }
            }
        } catch (JsonProcessingException e) {
            LOG.error("Error while extracting the data sources", e);
            return "{\"Exception\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            LOG.error("Error while deleting object", e);
            return "{\"Exception\": \"" + e.getMessage() + "\"}";
        }

        return "{\"deleted\": false}";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes    DOCUMENT ME!
     * @param   tablename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private MetaClass getClassByTableName(final MetaClass[] classes, final String tablename) {
        for (final MetaClass clazz : classes) {
            if (clazz.getTableName().equalsIgnoreCase(tablename)) {
                return clazz;
            }
        }

        return null;
    }
}
