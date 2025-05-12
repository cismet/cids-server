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
package de.cismet.cids.server.actions;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface AsyncActionProgressListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  percentage  DOCUMENT ME!
     */
    void setProgress(int percentage);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getProgress();
    /**
     * DOCUMENT ME!
     *
     * @param  result  DOCUMENT ME!
     */
    void setResult(Object result);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getResult();
    /**
     * DOCUMENT ME!
     */
    void dispose();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Date getResultDate();
}
