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
package de.cismet.cids.server.actions;

import Sirius.server.newuser.User;

import java.util.HashSet;
import java.util.Set;

import de.cismet.cids.server.messages.CidsServerMessageManagerImpl;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class PublishCidsServerMessageAction implements ServerAction, UserAwareServerAction, ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            PublishCidsServerMessageAction.class);

    public static final String TASK_NAME = "addCidsServerMessage";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum ParameterType {

        //~ Enum constants -----------------------------------------------------

        CATEGORY, USER, USERGROUP, RENOTIFY
    }

    //~ Instance fields --------------------------------------------------------

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    private User user;

    //~ Methods ----------------------------------------------------------------

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   body    DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final Object message = body;
        String category = null;
        boolean renotify = false;

        final Set userGroupKeys = new HashSet();
        final Set userKeys = new HashSet();
        for (final ServerActionParameter sap : params) {
            if (sap != null) {
                if (sap.getKey().equals(ParameterType.CATEGORY.toString())) {
                    category = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.RENOTIFY.toString())) {
                    renotify = Boolean.TRUE.equals((Boolean)sap.getValue());
                } else if (sap.getKey().equals(ParameterType.USERGROUP.toString())) {
                    userGroupKeys.add(sap.getValue());
                } else if (sap.getKey().equals(ParameterType.USER.toString())) {
                    userKeys.add(sap.getValue());
                }
            }
        }

        CidsServerMessageManagerImpl.getInstance()
                .publishMessage(category, message, renotify, userGroupKeys, userKeys, getConnectionContext());

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getTaskName() {
        return TASK_NAME;
    }
}
