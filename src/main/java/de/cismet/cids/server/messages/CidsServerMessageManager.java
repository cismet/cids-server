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

import java.util.Collection;
import java.util.Map;
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
     * @param  category  DOCUMENT ME!
     * @param  object    DOCUMENT ME!
     * @param  renotify  DOCUMENT ME!
     */
    void publishMessage(final String category, final Object object, final boolean renotify);

    /**
     * DOCUMENT ME!
     *
     * @param  category                         DOCUMENT ME!
     * @param  object                           DOCUMENT ME!
     * @param  renotify                         DOCUMENT ME!
     * @param  ids                              DOCUMENT ME!
     * @param  trueForUserKeysFalseForGroupIds  DOCUMENT ME!
     */
    void publishMessage(final String category,
            final Object object,
            final boolean renotify,
            final Set ids,
            final boolean trueForUserKeysFalseForGroupIds);

    /**
     * DOCUMENT ME!
     *
     * @param  category       DOCUMENT ME!
     * @param  object         DOCUMENT ME!
     * @param  renotify       DOCUMENT ME!
     * @param  userGroupKeys  DOCUMENT ME!
     * @param  userKeys       DOCUMENT ME!
     */
    void publishMessage(final String category,
            final Object object,
            final boolean renotify,
            final Set userGroupKeys,
            final Set userKeys);

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   biggerThen  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<CidsServerMessage> getLastMessages(final User user, final Map<String, Integer> biggerThen);

    /**
     * DOCUMENT ME!
     *
     * @param   category    DOCUMENT ME!
     * @param   user        DOCUMENT ME!
     * @param   biggerThen  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsServerMessage getLastMessage(final String category, final User user, final int biggerThen);

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
