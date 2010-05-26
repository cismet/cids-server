/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server;

import java.rmi.RMISecurityManager;

import java.security.Permission;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ServerSecurityManager extends RMISecurityManager {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void checkPermission(final Permission perm) {
        // do nothing
        // TODO: create appropriate security manager!
    }

    @Override
    public void checkPermission(final Permission perm, final Object context) {
        // do nothing TODO: create appropriate security manager!
    }
}
