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
package de.cismet.cids.server.connectioncontext;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConnectionContextLoggerConfig {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PROPERTY__NAME = "name";
    private static final String PROPERTY__TYPE = "type";
    private static final String PROPERTY__CONFIG = "config";

    //~ Instance fields --------------------------------------------------------

    @JsonProperty(PROPERTY__NAME)
    private final String name;
    @JsonProperty(PROPERTY__TYPE)
    private final String type;
    @JsonProperty(PROPERTY__CONFIG)
    private final Object config;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextFilterRuleSet object.
     *
     * @param  name    DOCUMENT ME!
     * @param  type    DOCUMENT ME!
     * @param  config  loggerName DOCUMENT ME!
     */
    @JsonCreator
    public ConnectionContextLoggerConfig(@JsonProperty(PROPERTY__NAME) final String name,
            @JsonProperty(PROPERTY__TYPE) final String type,
            @JsonProperty(PROPERTY__CONFIG) final Object config) {
        this.name = name;
        this.type = type;
        this.config = config;
    }
}
