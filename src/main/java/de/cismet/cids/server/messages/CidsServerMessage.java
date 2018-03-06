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

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

import java.sql.Connection;

import java.util.Date;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@AllArgsConstructor
public class CidsServerMessage implements Serializable, ConnectionContextProvider {

    //~ Instance fields --------------------------------------------------------

    private final Integer id;
    private final Object content;
    private final boolean renotify;
    private final String category;
    private final Date timestamp;
    private final ConnectionContext connectionContext;

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean equals(final Object obj) {
        return id.equals(obj);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
