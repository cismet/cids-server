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
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cismet.connectioncontext.ConnectionContext;

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

    private final Map<String, LinkedList<CidsServerMessage>> messagesPerCategoryMap = new HashMap<>();

    private final Collection<CidsServerMessageManagerListener> listeners = new LinkedList<>();

    private final Map<CidsServerMessage, Set> userKeyMap = new HashMap<>();
    private final Map<CidsServerMessage, Set> userGroupKeyMap = new HashMap<>();
    private final CidsServerMessageManagerListenerHandler listenerHandler =
        new CidsServerMessageManagerListenerHandler();

    private final Map<String, Long> userActivityTimeMap = new HashMap<>();

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

    @Override
    @Deprecated
    public void publishMessage(final String category,
            final Object object,
            final boolean renotify) {
        publishMessage(category, object, renotify, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  category           DOCUMENT ME!
     * @param  object             DOCUMENT ME!
     * @param  renotify           DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    @Override
    public void publishMessage(final String category,
            final Object object,
            final boolean renotify,
            final ConnectionContext connectionContext) {
        publishMessage(category, object, renotify, null, null, connectionContext);
    }

    @Override
    public void publishMessage(final String category,
            final Object object,
            final boolean renotify,
            final Set keys,
            final boolean trueForUserKeysFalseForGroupKeys) {
        publishMessage(
            category,
            object,
            renotify,
            keys,
            trueForUserKeysFalseForGroupKeys,
            ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  category                          DOCUMENT ME!
     * @param  object                            DOCUMENT ME!
     * @param  renotify                          DOCUMENT ME!
     * @param  keys                              DOCUMENT ME!
     * @param  trueForUserKeysFalseForGroupKeys  DOCUMENT ME!
     * @param  connectionContext                 DOCUMENT ME!
     */
    @Override
    public void publishMessage(final String category,
            final Object object,
            final boolean renotify,
            final Set keys,
            final boolean trueForUserKeysFalseForGroupKeys,
            final ConnectionContext connectionContext) {
        if (trueForUserKeysFalseForGroupKeys) {
            publishMessage(category, object, renotify, null, keys, connectionContext);
        } else {
            publishMessage(category, object, renotify, keys, null, connectionContext);
        }
    }

    @Override
    public void publishMessage(final String category,
            final Object object,
            final boolean renotify,
            final Set userGroupKeys,
            final Set userKeys) {
        publishMessage(category, object, renotify, userGroupKeys, userKeys, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  category           DOCUMENT ME!
     * @param  object             DOCUMENT ME!
     * @param  renotify           DOCUMENT ME!
     * @param  userGroupKeys      DOCUMENT ME!
     * @param  userKeys           DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    @Override
    public void publishMessage(final String category,
            final Object object,
            final boolean renotify,
            final Set userGroupKeys,
            final Set userKeys,
            final ConnectionContext connectionContext) {
        final int newMessageId;

        synchronized (this) {
            newMessageId = ++messageId;
        }

        final CidsServerMessage message = new CidsServerMessage(
                newMessageId,
                object,
                renotify,
                category,
                new Date(),
                connectionContext);

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
            messages = new LinkedList<>();
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
    @Deprecated
    public List<CidsServerMessage> getMessages(final User user,
            final Map<String, Integer> biggerThenPerCategory) {
        return getMessages(user, biggerThenPerCategory, ConnectionContext.createDeprecated());
    }

    @Override
    public List<CidsServerMessage> getMessages(final User user,
            final Map<String, Integer> biggerThenPerCategory,
            final ConnectionContext connectionContext) {
        final List<CidsServerMessage> messages = new ArrayList<>(messagesPerCategoryMap.keySet().size());

        final int defaultBiggerThen = -1;
        for (final String category : messagesPerCategoryMap.keySet()) {
            final Integer biggerThen = biggerThenPerCategory.get(category);
            final boolean lastMessageOnly = !category.endsWith("*");
            if (lastMessageOnly) {
                final CidsServerMessage message = getLastMessage(
                        category,
                        user,
                        (biggerThen != null) ? biggerThen : defaultBiggerThen,
                        connectionContext);
                if (message != null) {
                    messages.add(message);
                }
            } else {
                messages.addAll(
                    getAllMessages(
                        category,
                        user,
                        (biggerThen != null) ? biggerThen : defaultBiggerThen,
                        connectionContext));
            }
        }
        return messages;
    }

    @Override
    public CidsServerMessage getLastMessage(final String category,
            final User user,
            final int biggerThen) {
        return getLastMessage(category, user, biggerThen, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   category           DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   biggerThen         DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsServerMessage getLastMessage(final String category,
            final User user,
            final int biggerThen,
            final ConnectionContext connectionContext) {
        final List<CidsServerMessage> lastMessages = getMessages(category, user, biggerThen, 1, connectionContext);
        if (lastMessages.isEmpty()) {
            return null;
        } else {
            return lastMessages.iterator().next();
        }
    }

    @Override
    @Deprecated
    public List<CidsServerMessage> getAllMessages(final String category,
            final User user,
            final int biggerThen) {
        return getAllMessages(category, user, biggerThen, ConnectionContext.createDeprecated());
    }

    @Override
    public List<CidsServerMessage> getAllMessages(final String category,
            final User user,
            final int biggerThen,
            final ConnectionContext connectionContext) {
        return getMessages(category, user, biggerThen, -1, connectionContext);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   category           DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   biggerThen         DOCUMENT ME!
     * @param   numOfMessages      DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<CidsServerMessage> getMessages(final String category,
            final User user,
            final int biggerThen,
            final int numOfMessages,
            final ConnectionContext connectionContext) {
        final List<CidsServerMessage> messages = new ArrayList<>();

        final LinkedList<CidsServerMessage> categoryMessages = (LinkedList<CidsServerMessage>)
            messagesPerCategoryMap.get(
                category);
        final Iterator<CidsServerMessage> itBackwards = categoryMessages.descendingIterator();

        while (itBackwards.hasNext() && ((numOfMessages < 0) || (messages.size() < numOfMessages))) {
            final CidsServerMessage message = itBackwards.next();

            if (!message.isRenotify() && (message.getId() <= biggerThen)) {
                break;
            }

            if (user == null) {
                messages.add(message);
                // is user matching ?
            } else if (userKeyMap.containsKey(message) && (userKeyMap.get(message) != null)
                        && userKeyMap.get(message).contains(user.getKey())) {
                messages.add(message);
                // is group matching ?
            } else if (userGroupKeyMap.containsKey(message)) {
                final Set userGroupKeys = userGroupKeyMap.get(message);
                if (userGroupKeys != null) {
                    if (userGroupKeys.contains(user.getUserGroup().getKey())) {
                        // single group
                        messages.add(message);
                    } else {
                        // all groups
                        if (user.getPotentialUserGroups() != null) {
                            for (final UserGroup userGroup : user.getPotentialUserGroups()) {
                                if (userGroupKeys.contains(userGroup.getKey())) {
                                    messages.add(message);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (category != null) {
                boolean csmChecked = false;
                try {
                    csmChecked = DomainServerImpl.getServerInstance()
                                .hasConfigAttr(
                                        user,
                                        "csm://"
                                        + category,
                                        connectionContext);
                } catch (final RemoteException ex) {
                    LOG.warn(ex, ex);
                }
                if (csmChecked) {
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  userKey               DOCUMENT ME!
     * @param  nextExpectedActivity  DOCUMENT ME!
     */
    public void logUserActivity(final String userKey, final Long nextExpectedActivity) {
        userActivityTimeMap.put(userKey, nextExpectedActivity);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getActiveUsers() {
        final Set<String> activeUsers = new HashSet<>();
        final long currentTimeInMs = System.currentTimeMillis();
        for (final String userKey : userActivityTimeMap.keySet()) {
            final Long expectedTs = userActivityTimeMap.get(userKey);
            if ((expectedTs != null) && (expectedTs > currentTimeInMs)) {
                activeUsers.add(userKey);
            }
        }
        return activeUsers;
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
