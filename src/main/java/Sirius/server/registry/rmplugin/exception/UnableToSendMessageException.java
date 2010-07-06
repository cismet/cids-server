/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UnableToDeregister.java
 *
 * Created on 24. November 2006, 15:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.registry.rmplugin.exception;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public class UnableToSendMessageException extends Exception {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -8381145764583871507L;

    //~ Instance fields --------------------------------------------------------

    private int totalSended = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UnableToDeregister.
     */
    public UnableToSendMessageException() {
        super();
    }

    /**
     * Creates a new UnableToSendMessageException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public UnableToSendMessageException(final String message) {
        super(message);
    }

    /**
     * Creates a new UnableToSendMessageException object.
     *
     * @param  message      DOCUMENT ME!
     * @param  totalSended  DOCUMENT ME!
     */
    public UnableToSendMessageException(final String message, final int totalSended) {
        super(message);
        this.totalSended = totalSended;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getTotalSended() {
        return totalSended;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  totalSended  DOCUMENT ME!
     */
    public void setTotalSended(final int totalSended) {
        this.totalSended = totalSended;
    }
}
