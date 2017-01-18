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
package de.cismet.cids.utils.serverresources;

import lombok.Getter;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class ServerResource implements Serializable {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Type {

        //~ Enum constants -----------------------------------------------------

        JASPER_REPORT, TEXT, BINARY
    }

    //~ Instance fields --------------------------------------------------------

    @Getter private final String path;
    @Getter private final Type type;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerResource object.
     *
     * @param  path  DOCUMENT ME!
     * @param  type  DOCUMENT ME!
     */
    public ServerResource(final String path, final Type type) {
        this.path = path;
        this.type = type;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return "[" + type.toString() + "] " + path;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerResource other = (ServerResource)obj;
        if ((this.path == null) ? (other.path != null) : (!this.path.equals(other.path))) {
            return false;
        }
        return this.type == other.type;
    }
}
