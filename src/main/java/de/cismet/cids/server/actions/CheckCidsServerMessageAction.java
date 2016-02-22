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
    public enum ParameterType {

        //~ Enum constants -----------------------------------------------------

        LAST_MESSAGE_ID
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
        Integer lastMessageId = null;

        for (final ServerActionParameter sap : params) {
            if (sap != null) {
                if (sap.getKey().equals(ParameterType.LAST_MESSAGE_ID.toString())) {
                    lastMessageId = (Integer)sap.getValue();
                }
            }
        }

        final Collection<CidsServerMessage> messages = CidsServerMessageManagerImpl.getInstance()
                    .getLastMessages(getUser(), lastMessageId);

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
