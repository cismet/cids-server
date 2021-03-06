/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 thorsten
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cids.dynamics;

import Sirius.server.newuser.User;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface CustomBeanPermissionProvider extends CidsBeanStore {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    boolean getCustomWritePermissionDecisionforUser(final User u);

    /**
     * DOCUMENT ME!
     *
     * @param   u                  DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean getCustomWritePermissionDecisionforUser(final User u, final ConnectionContext connectionContext);
    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    boolean getCustomReadPermissionDecisionforUser(final User u);

    /**
     * DOCUMENT ME!
     *
     * @param   u                  DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean getCustomReadPermissionDecisionforUser(final User u, final ConnectionContext connectionContext);
}
