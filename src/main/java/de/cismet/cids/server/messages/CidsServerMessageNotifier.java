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
package de.cismet.cids.server.messages;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CidsServerMessageNotifier {

    //~ Static fields/initializers ---------------------------------------------

    private static CidsServerMessageNotifier INSTANCE = null;

    //~ Instance fields --------------------------------------------------------

    private final CidsServerMessageManagerListenerHandler listenerHandler =
        new CidsServerMessageManagerListenerHandler();
    private CidsServerMessageManager manager;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsServerMessageNotifier object.
     */
    private CidsServerMessageNotifier() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CidsServerMessageNotifier getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CidsServerMessageNotifier();
        }
        return INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  manager  DOCUMENT ME!
     */
    public void setManager(final CidsServerMessageManager manager) {
        if (this.manager != null) {
            this.manager.removeCidsServerMessageManagerListener(listenerHandler);
        }
        this.manager = manager;
        if (manager != null) {
            manager.addCidsServerMessageManagerListener(listenerHandler);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class CidsServerMessageManagerListenerHandler implements CidsServerMessageManagerListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void messagePublished(final CidsServerMessageManagerListenerEvent event) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                           // choose Tools | Templates.
        }
    }
}
