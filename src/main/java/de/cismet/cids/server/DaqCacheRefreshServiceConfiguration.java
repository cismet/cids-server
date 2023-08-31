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
package de.cismet.cids.server;

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
public class DaqCacheRefreshServiceConfiguration {

    //~ Instance fields --------------------------------------------------------

    private DaqCacheRefreshServiceViewConfiguration[] viewConfigurations;
    private Integer reconsideringTimer;
    private Integer maxParallelThreads;
    private String startTime;
    private String endTime;
}
