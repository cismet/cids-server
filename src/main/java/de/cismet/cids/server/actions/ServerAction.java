/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ServerAction {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   body    json DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    Object execute(final Object body, final ServerActionParameter... params);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getTaskName();
}
