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
package de.cismet.cids.nodepermissions;

import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCustomNodePermissionProvider implements CustomNodePermissionProvider {

    //~ Instance fields --------------------------------------------------------

    private MetaObjectNode mon;

    //~ Methods ----------------------------------------------------------------

    @Override
    public MetaObjectNode getObjectNode() {
        return mon;
    }

    @Override
    public void setObjectNode(final MetaObjectNode mon) {
        this.mon = mon;
    }

    @Override
    public boolean getCustomWritePermissionDecisionforUser(final User u) {
        return true;
    }
}
