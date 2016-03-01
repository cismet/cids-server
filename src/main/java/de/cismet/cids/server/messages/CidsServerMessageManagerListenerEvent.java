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

import lombok.Getter;

import java.util.EventObject;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
public class CidsServerMessageManagerListenerEvent extends EventObject {

    //~ Static fields/initializers ---------------------------------------------

    public static final int MESSAGE_PUBLISHED = 1;

    //~ Instance fields --------------------------------------------------------

    private final int type;
    private final CidsServerMessage message;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsServerMessageManagerListenerEvent object.
     *
     * @param  type     DOCUMENT ME!
     * @param  message  DOCUMENT ME!
     * @param  source   DOCUMENT ME!
     */
    public CidsServerMessageManagerListenerEvent(final int type,
            final CidsServerMessage message,
            final CidsServerMessageManagerImpl source) {
        super(source);
        this.type = type;
        this.message = message;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsServerMessageManagerImpl getSource() {
        return (CidsServerMessageManagerImpl)super.getSource();
    }
}
