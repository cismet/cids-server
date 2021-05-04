/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.util;

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/

import lombok.Getter;
import lombok.Setter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class MapImageFactoryConfiguration {

    //~ Instance fields --------------------------------------------------------

    private String srs;
    private Double bbX1;
    private Double bbX2;
    private Double bbY1;
    private Double bbY2;
    private Integer height;
    private Integer width;
    private String mapUrl;
    private Integer mapDpi;
}
