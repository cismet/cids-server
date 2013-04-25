/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions;

import Sirius.server.middleware.interfaces.domainserver.UserStore;

/**
 *
 * @author daniel
 */
public interface UserAwareServerAction extends ServerAction, UserStore {


}
