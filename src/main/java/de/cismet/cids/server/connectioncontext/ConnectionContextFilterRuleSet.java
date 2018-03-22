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

import java.util.Map;
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
public class ConnectionContextFilterRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PROPERTY__NAME = "name";
    private static final String PROPERTY__LOGGER_NAME = "loggerName";
    private static final String PROPERTY__LOGGER_PARAMS = "loggerParams";
    private static final String PROPERTY__RULES = "rules";

    //~ Instance fields --------------------------------------------------------

    @JsonProperty(PROPERTY__NAME)
    private final String name;
    @JsonProperty(PROPERTY__LOGGER_NAME)
    private final String loggerName;
    @JsonProperty(PROPERTY__LOGGER_PARAMS)
    private Map<String, Object> loggerParams;
    @JsonProperty(PROPERTY__RULES)
    private Set<ConnectionContextFilterRule> rules;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextFilterRuleSet object.
     *
     * @param  name        DOCUMENT ME!
     * @param  loggerName  DOCUMENT ME!
     */
    @JsonCreator
    public ConnectionContextFilterRuleSet(@JsonProperty(PROPERTY__NAME) final String name,
            @JsonProperty(PROPERTY__LOGGER_NAME) final String loggerName) {
        this.name = name;
        this.loggerName = loggerName;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   log  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSatisfied(final ConnectionContextLog log) {
        boolean accept = true;
        for (final ConnectionContextFilterRule filterRule : getRules()) {
            if (!filterRule.isSatisfied(log)) {
                accept = false;
                break;
            }
        }
        return accept;
    }
}
