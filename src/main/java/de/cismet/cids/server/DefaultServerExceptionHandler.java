/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server;

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DefaultServerExceptionHandler implements Thread.UncaughtExceptionHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DefaultServerExceptionHandler.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public void uncaughtException(final Thread thread, final Throwable error) {
        LOG.error("uncaught exception in thread: " + thread, error);
    }
}
