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

import java.util.Collection;
import java.util.Map;

import de.cismet.cids.server.messages.CidsServerMessage;
import de.cismet.cids.server.messages.CidsServerMessageManagerImpl;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class CheckCidsServerMessageAction implements ServerAction, UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            CheckCidsServerMessageAction.class);

    public static final String TASK_NAME = "checkCidsServerMessage";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        LAST_MESSAGE_IDS, INTERVALL
    }

    //~ Instance fields --------------------------------------------------------

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
        final Long currentTimeInMs = System.currentTimeMillis();
        Map<String, Integer> lastMessageIdPerCategory = null;
        Integer intervall = null;

        for (final ServerActionParameter sap : params) {
            if (sap != null) {
                if (sap.getKey().equals(Parameter.LAST_MESSAGE_IDS.toString())) {
                    lastMessageIdPerCategory = (Map)sap.getValue();
                } else if (sap.getKey().equals(Parameter.INTERVALL.toString())) {
                    intervall = (Integer)sap.getValue();
                }
            }
        }

        if (intervall != null) {
            CidsServerMessageManagerImpl.getInstance()
                    .logUserActivity(getUser().getName(), currentTimeInMs + (intervall * 2));
        }

        final Collection<CidsServerMessage> messages = CidsServerMessageManagerImpl.getInstance()
                    .getMessages(getUser(), lastMessageIdPerCategory);

        return messages;
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
