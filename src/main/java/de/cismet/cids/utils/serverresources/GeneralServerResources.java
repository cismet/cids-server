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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public enum GeneralServerResources {

    //~ Enum constants ---------------------------------------------------------

    CACHE_REFRESH_JSON(new TextServerResource("/daq_cache/refresh.json")),
    OFFLINE_ACTION_JSON(new TextServerResource("/action_execution/configuration.json")),
    GRAPHQL_PROPERTIES(new TextServerResource("/graphQl/configuration.properties")),
    CONFIF_ATTR_REDIRECTING_JSON(new JsonServerResource("/configAttr/redirecting.json")),
    CONFIG_UNCAUGHT_CLIENT_EXCEPTION_JSON(new JsonServerResource("/uce/uncaught_client_exception.json"));

    //~ Instance fields --------------------------------------------------------

    @Getter private final ServerResource value;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Props object.
     *
     * @param  value  DOCUMENT ME!
     */
    private GeneralServerResources(final ServerResource value) {
        this.value = value;
    }
}
