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
package de.cismet.cids.server.messages;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CidsServerMessageManagerImpl implements CidsServerMessageManager {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            CidsServerMessageManagerImpl.class);
    private static CidsServerMessageManagerImpl INSTANCE;

    //~ Instance fields --------------------------------------------------------

    private final Map<String, LinkedList<CidsServerMessage>> messagesPerCategoryMap =
        new HashMap<String, LinkedList<CidsServerMessage>>();

    private final Collection<CidsServerMessageManagerListener> listeners =
        new LinkedList<CidsServerMessageManagerListener>();

    private final Map<CidsServerMessage, Set> userKeyMap = new HashMap<CidsServerMessage, Set>();
    private final Map<CidsServerMessage, Set> userGroupKeyMap = new HashMap<CidsServerMessage, Set>();
    private final CidsServerMessageManagerListenerHandler listenerHandler =
        new CidsServerMessageManagerListenerHandler();

    private int messageId = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsServerMessageHandler object.
     */
    private CidsServerMessageManagerImpl() {
        messageId = (int)((System.currentTimeMillis() / 1000) % Integer.MAX_VALUE);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CidsServerMessageManagerImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CidsServerMessageManagerImpl();
        }
        return INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  category  DOCUMENT ME!
     * @param  object    DOCUMENT ME!
     */
    @Override
    public void publishMessage(final String category, final Object object) {
        CidsServerMessageManagerImpl.this.publishMessage(category, object, null, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  category                          DOCUMENT ME!
     * @param  object                            DOCUMENT ME!
     * @param  keys                              DOCUMENT ME!
     * @param  trueForUserKeysFalseForGroupKeys  DOCUMENT ME!
     */
    @Override
    public void publishMessage(final String category,
            final Object object,
            final Set keys,
            final boolean trueForUserKeysFalseForGroupKeys) {
        if (trueForUserKeysFalseForGroupKeys) {
            CidsServerMessageManagerImpl.this.publishMessage(category, object, null, keys);
        } else {
            CidsServerMessageManagerImpl.this.publishMessage(category, object, keys, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  category       DOCUMENT ME!
     * @param  object         DOCUMENT ME!
     * @param  userGroupKeys  DOCUMENT ME!
     * @param  userKeys       DOCUMENT ME!
     */
    @Override
    public void publishMessage(final String category,
            final Object object,
            final Set userGroupKeys,
            final Set userKeys) {
        final int newMessageId;

        synchronized (this) {
            newMessageId = ++messageId;
        }

        final CidsServerMessage message = new CidsServerMessage(newMessageId, object, category, new Date());

        if ((userKeys != null) && !userKeys.isEmpty()) {
            userKeyMap.put(message, new HashSet(userKeys));
        }

        if ((userGroupKeys != null) && !userGroupKeys.isEmpty()) {
            userGroupKeyMap.put(message, new HashSet(userGroupKeys));
        }

        putMessage(category, message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  category  DOCUMENT ME!
     * @param  message   DOCUMENT ME!
     */
    private void putMessage(final String category, final CidsServerMessage message) {
        final LinkedList<CidsServerMessage> messages;
        if (!messagesPerCategoryMap.containsKey(category)) {
            messages = new LinkedList<CidsServerMessage>();
            messagesPerCategoryMap.put(category, messages);
        } else {
            messages = messagesPerCategoryMap.get(category);
        }
        messages.add(message);

        listenerHandler.messagePublished(new CidsServerMessageManagerListenerEvent(
                CidsServerMessageManagerListenerEvent.MESSAGE_PUBLISHED,
                message,
                this));
    }

    @Override
    public boolean addCidsServerMessageManagerListener(final CidsServerMessageManagerListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeCidsServerMessageManagerListener(final CidsServerMessageManagerListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public Collection<CidsServerMessage> getLastMessages() {
        return getLastMessages(null, null);
    }

    @Override
    public Collection<CidsServerMessage> getLastMessages(final User user) {
        return getLastMessages(user, null);
    }

    @Override
    public Collection<CidsServerMessage> getLastMessages(final User user, final Integer biggerThen) {
        final Collection<CidsServerMessage> messages = new ArrayList<CidsServerMessage>(messagesPerCategoryMap.keySet()
                        .size());
        for (final String category : messagesPerCategoryMap.keySet()) {
            final CidsServerMessage message = getLastMessage(category, user, biggerThen);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   category  DOCUMENT ME!
     * @param   user      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsServerMessage getLastMessage(final String category, final User user) {
        return getLastMessage(category, user, null);
    }

    @Override
    public CidsServerMessage getLastMessage(final String category) {
        return getLastMessage(category, null, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   category    DOCUMENT ME!
     * @param   user        DOCUMENT ME!
     * @param   biggerThen  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsServerMessage getLastMessage(final String category, final User user, final Integer biggerThen) {
        final LinkedList<CidsServerMessage> messages = (LinkedList<CidsServerMessage>)messagesPerCategoryMap.get(
                category);
        final Iterator<CidsServerMessage> itBackwards = messages.descendingIterator();

        boolean abort = false;
        while (itBackwards.hasNext() && !abort) {
            final CidsServerMessage message = itBackwards.next();

            if ((biggerThen != null) && (message.getId() <= biggerThen)) {
                abort = true;
                continue;
            }

            if (user == null) {
                return message;
            }

            // is user matching ?
            if (userKeyMap.containsKey(message) && (userKeyMap.get(message) != null)
                        && userKeyMap.get(message).contains(user.getKey())) {
                return message;
            }

            // is group matching ?
            if (userGroupKeyMap.containsKey(message)) {
                final Set userGroupKeys = userGroupKeyMap.get(message);
                if (userGroupKeys != null) {
                    if (userGroupKeys.contains(user.getUserGroup().getKey())) {
                        // single group
                        return message;
                    } else {
                        // all groups
                        if (user.getPotentialUserGroups() != null) {
                            for (final UserGroup userGroup : user.getPotentialUserGroups()) {
                                if (userGroupKeys.contains(userGroup.getKey())) {
                                    return message;
                                }
                            }
                        }
                    }
                }
            }

            if (category != null) {
                boolean csmChecked = false;
                try {
                    csmChecked = DomainServerImpl.getServerInstance().hasConfigAttr(user, "csm://" + category);
                } catch (final RemoteException ex) {
                    LOG.warn(ex, ex);
                }
                if (csmChecked) {
                    return message;
                }
            }
        }

        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class CidsServerMessageManagerListenerHandler implements CidsServerMessageManagerListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void messagePublished(final CidsServerMessageManagerListenerEvent event) {
            for (final CidsServerMessageManagerListener listener : listeners) {
                listener.messagePublished(event);
            }
        }
    }
}
