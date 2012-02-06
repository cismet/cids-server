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
public interface Shutdownable {

    //~ Methods ----------------------------------------------------------------

    /**
     * This method shall be used as a cleanup method if the runtime of the application is about to end. Do whatever
     * cleanup is needed here.
     *
     * @throws  ServerExitError  if any error occurs during cleanup
     */
    void shutdown() throws ServerExitError;

    /**
     * Indicates whether shutdown has already been called or not.
     *
     * @return  true if shutdown was already called, false otherwise
     */
    boolean isDown();
}
