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
import lombok.Setter;

import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class DefaultServerResourcePropertiesHandler implements ServerResourcePropertiesHandler {

    //~ Instance fields --------------------------------------------------------

    @Getter @Setter private Properties properties;
}
