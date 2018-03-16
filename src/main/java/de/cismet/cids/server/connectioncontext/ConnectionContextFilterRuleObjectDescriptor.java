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
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@RequiredArgsConstructor
public class ConnectionContextFilterRuleObjectDescriptor {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PROPERTY__CLASS_NAME = "className";
    private static final String PROPERTY__OBJECT_ID = "objectId";
    private static final String PROPERTY__OBJECT_IDS = "objectIds";

    //~ Instance fields --------------------------------------------------------

    @JsonProperty private final String className;
    @JsonProperty private Integer objectId;
    @JsonProperty private final Set<Integer> objectIds = new HashSet<>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ObjectDescriptor object.
     *
     * @param  className  DOCUMENT ME!
     * @param  objectId   DOCUMENT ME!
     * @param  objectIds  DOCUMENT ME!
     */
    @JsonCreator
    public ConnectionContextFilterRuleObjectDescriptor(@JsonProperty(PROPERTY__CLASS_NAME) final String className,
            @JsonProperty(PROPERTY__OBJECT_ID) final Integer objectId,
            @JsonProperty(PROPERTY__OBJECT_IDS) final Collection<Integer> objectIds) {
        this.className = className;
        this.objectId = objectId;
        if (objectIds != null) {
            this.objectIds.addAll(objectIds);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConnectionContextFilterRuleObjectDescriptor setObjectId(final Integer objectId) {
        this.objectId = objectId;
        return this;
    }
}
