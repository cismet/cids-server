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
package de.cismet.cids.server.search;

import java.io.Serializable;

import java.util.ArrayList;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface QueryPostProcessor extends Serializable {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   result  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ArrayList<ArrayList> postProcess(ArrayList<ArrayList> result);
}
