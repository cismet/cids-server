/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.nodepermissions;

import Sirius.server.newuser.User;

/**
 *
 * @author thorsten
 */
public interface CustomNodePermissionProvider extends ObjectNodeStore{
      /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    // don't know if this is needed (to be discussed)
    boolean getCustomWritePermissionDecisionforUser(final User u);
    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean getCustomReadPermissionDecisionforUser(final User u);
}
