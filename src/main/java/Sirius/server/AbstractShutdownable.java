/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractShutdownable implements Shutdownable {

    //~ Instance fields --------------------------------------------------------

    private transient boolean down = false;

    //~ Methods ----------------------------------------------------------------

    @Override
    public final synchronized void shutdown() throws ServerExitError {
        if (down) {
            return;
        }
        // this not only indicates that it has been shut down but also that a shutdown is currently in progess
        down = true;

        internalShutdown();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    protected abstract void internalShutdown() throws ServerExitError;

    @Override
    public final synchronized boolean isDown() {
        return down;
    }
}
