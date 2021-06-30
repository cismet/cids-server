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

import lombok.Getter;
import lombok.Setter;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class DataAquisitionResponse {

    //~ Instance fields --------------------------------------------------------

    String md5;
    String content;
    String version;
    String time;
    /**
     * Possible response codes.
     *
     * <ul>
     *   <li>200 OK</li>
     *   <li>298 The last refresh of the cached table produced invalid json. So the given result is possibly out of
     *     date</li>
     *   <li>299 The view cannot refresh the cached table. So the given result is possibly out of date</li>
     *   <li>304 there are no changes. The given md5 is still up to date</li>
     *   <li>401 The user has no permission to request the given view</li>
     *   <li>404 View not found</li>
     *   <li>500 any server error</li>
     * </ul>
     */
    Integer status;
}
