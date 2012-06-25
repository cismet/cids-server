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
        
        internalShutdown();
        
        down = true;
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
