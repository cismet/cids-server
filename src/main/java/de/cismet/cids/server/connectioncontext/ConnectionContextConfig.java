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

import java.util.Collection;
import java.util.Set;

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
public class ConnectionContextConfig {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PROPERTY__LOGGER_CONFIGS = "loggers";
    private static final String PROPERTY__RULE_SETS = "ruleSets";

    //~ Instance fields --------------------------------------------------------

    @JsonProperty(PROPERTY__LOGGER_CONFIGS)
    private final Collection<ConnectionContextLoggerConfig> loggers;
    @JsonProperty(PROPERTY__RULE_SETS)
    private final Collection<ConnectionContextFilterRuleSet> ruleSets;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextFilterRuleSet object.
     *
     * @param  loggers   name DOCUMENT ME!
     * @param  ruleSets  loggerName DOCUMENT ME!
     */
    @JsonCreator
    public ConnectionContextConfig(
            @JsonProperty(PROPERTY__LOGGER_CONFIGS) final Collection<ConnectionContextLoggerConfig> loggers,
            @JsonProperty(PROPERTY__RULE_SETS) final Collection<ConnectionContextFilterRuleSet> ruleSets) {
        this.loggers = loggers;
        this.ruleSets = ruleSets;
    }
}
