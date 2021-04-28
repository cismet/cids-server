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
package de.cismet.cids.server.actions.graphql.exceptions;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class InvalidJoinException extends Exception {

    //~ Instance fields --------------------------------------------------------

    private String joinField = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new InvalidJoinException object.
     *
     * @param  joinField  DOCUMENT ME!
     */
    public InvalidJoinException(final String joinField) {
        this.joinField = joinField;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the joinField
     */
    public String getJoinField() {
        return joinField;
    }
}
