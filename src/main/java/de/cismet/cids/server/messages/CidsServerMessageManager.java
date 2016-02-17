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

import Sirius.server.newuser.User;

import java.io.Serializable;

import java.util.Collection;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface CidsServerMessageManager {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  object    DOCUMENT ME!
     * @param  category  DOCUMENT ME!
     */
    void publishMessage(final Object object, final String category);

    /**
     * DOCUMENT ME!
     *
     * @param  object                          DOCUMENT ME!
     * @param  category                        DOCUMENT ME!
     * @param  ids                             DOCUMENT ME!
     * @param  trueForUserIdsFalseForGroupIds  DOCUMENT ME!
     */
    void publishMessage(final Object object,
            final String category,
            final Set<Integer> ids,
            final boolean trueForUserIdsFalseForGroupIds);

    /**
     * DOCUMENT ME!
     *
     * @param  object        DOCUMENT ME!
     * @param  category      DOCUMENT ME!
     * @param  userGroupIds  DOCUMENT ME!
     * @param  userIds       DOCUMENT ME!
     */
    void publishMessage(final Object object,
            final String category,
            final Set<Integer> userGroupIds,
            final Set<Integer> userIds);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<CidsServerMessage> getLastMessages();

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<CidsServerMessage> getLastMessages(final User user);

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   biggerThen  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<CidsServerMessage> getLastMessages(final User user, final Integer biggerThen);

    /**
     * DOCUMENT ME!
     *
     * @param   category  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsServerMessage getLastMessage(final String category);

    /**
     * DOCUMENT ME!
     *
     * @param   category  DOCUMENT ME!
     * @param   user      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsServerMessage getLastMessage(final String category, final User user);

    /**
     * DOCUMENT ME!
     *
     * @param   category    DOCUMENT ME!
     * @param   user        DOCUMENT ME!
     * @param   biggerThen  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsServerMessage getLastMessage(final String category, final User user, final Integer biggerThen);

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean addCidsServerMessageManagerListener(final CidsServerMessageManagerListener listener);

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean removeCidsServerMessageManagerListener(final CidsServerMessageManagerListener listener);
}
