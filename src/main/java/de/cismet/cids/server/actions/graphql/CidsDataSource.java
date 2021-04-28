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
package de.cismet.cids.server.actions.graphql;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsDataSource {

    //~ Instance fields --------------------------------------------------------

    private String name;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsDataSource object.
     *
     * @param  name  DOCUMENT ME!
     */
    public CidsDataSource(final String name) {
        this.name = name;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the name
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
}
