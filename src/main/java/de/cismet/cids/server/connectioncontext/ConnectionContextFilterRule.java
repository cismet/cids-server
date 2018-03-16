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

import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import de.cismet.connectioncontext.ConnectionContext;

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
public class ConnectionContextFilterRule {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PROPERTY__USER_LOGIN = "userLogin";
    private static final String PROPERTY__USER_LOGINS = "userLogins";
    private static final String PROPERTY__GROUP_NAME = "groupName";
    private static final String PROPERTY__GROUP_NAMES = "groupNames";
    private static final String PROPERTY__CLASS_NAME = "className";
    private static final String PROPERTY__CLASS_NAMES = "classNames";
    private static final String PROPERTY__OBJECT = "object";
    private static final String PROPERTY__OBJECTS = "objects";
    private static final String PROPERTY__MODE = "mode";
    private static final String PROPERTY__MODES = "modes";
    private static final String PROPERTY__CATEGORY = "category";
    private static final String PROPERTY__CATEGORIES = "categories";
    private static final String PROPERTY__CONTEXT_NAME = "contextName";
    private static final String PROPERTY__CONTEXT_NAMES = "contextNames";
    private static final String PROPERTY__ORIGIN_IP = "originIp";
    private static final String PROPERTY__ORIGIN_IPS = "originIps";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Rule {

        //~ Enum constants -----------------------------------------------------

        INCLUDE, EXCLUDE
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Mode {

        //~ Enum constants -----------------------------------------------------

        READ, WRITE, BOTH
    }

    //~ Instance fields --------------------------------------------------------

    @JsonProperty private Rule rule = Rule.INCLUDE;
    @JsonProperty(PROPERTY__USER_LOGIN)
    private String userLogin;
    @JsonProperty(PROPERTY__USER_LOGINS)
    private Set<String> userLogins;
    @JsonProperty(PROPERTY__GROUP_NAME)
    private String groupName;
    @JsonProperty(PROPERTY__GROUP_NAMES)
    private Set<String> groupNames;
    @JsonProperty(PROPERTY__CLASS_NAME)
    private String className;
    @JsonProperty(PROPERTY__CLASS_NAMES)
    private Set<String> classNames;
    @JsonProperty(PROPERTY__OBJECT)
    private ConnectionContextFilterRuleObjectDescriptor object;
    @JsonProperty(PROPERTY__OBJECTS)
    private Set<ConnectionContextFilterRuleObjectDescriptor> objects;
    @JsonProperty(PROPERTY__MODE)
    private Mode mode = Mode.BOTH;
    @JsonProperty(PROPERTY__MODES)
    private Set<Mode> modes;
    @JsonProperty(PROPERTY__CATEGORY)
    private ConnectionContext.Category category;
    @JsonProperty(PROPERTY__CATEGORIES)
    private Set<ConnectionContext.Category> categories;
    @JsonProperty(PROPERTY__CONTEXT_NAME)
    private String contextName;
    @JsonProperty(PROPERTY__CONTEXT_NAMES)
    private Set<String> contextNames;
    @JsonProperty(PROPERTY__ORIGIN_IP)
    private String originIp;
    @JsonProperty(PROPERTY__ORIGIN_IPS)
    private Set<String> originIps;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getCombinedUserLogins() {
        return new CombinedSet<>(getUserLogin(), getUserLogins());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getCombinedGroupNames() {
        return new CombinedSet<>(getGroupName(), getGroupNames());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getCombinedClassNames() {
        return new CombinedSet<>(getClassName(), getClassNames());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<ConnectionContextFilterRuleObjectDescriptor> getCombinedObjects() {
        return new CombinedSet<>(getObject(), getObjects());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<ConnectionContextFilterRule.Mode> getCombinedModes() {
        return new CombinedSet<>(getMode(), getModes());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<ConnectionContext.Category> getCombinedCategories() {
        return new CombinedSet<>(getCategory(), getCategories());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getCombinedContextNames() {
        return new CombinedSet<>(getContextName(), getContextNames());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getCombinedOriginIps() {
        return new CombinedSet<>(getOriginIp(), getOriginIps());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean accepts(final ConnectionContextLog contextLog) {
        return false
                    || acceptsUserLogin(contextLog)
                    || acceptsGroupName(contextLog)
                    || acceptsClassName(contextLog)
                    || acceptsObject(contextLog)
                    || acceptsMode(contextLog)
                    || acceptsCategory(contextLog)
                    || acceptsContextName(contextLog)
                    || acceptsOriginIp(contextLog);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsUserLogin(final ConnectionContextLog contextLog) {
        final User user = contextLog.getUser();
        final String userLogin = (user != null) ? user.getName() : "";

        return acceptsCombinedAccordingToRule(userLogin, getCombinedUserLogins());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsGroupName(final ConnectionContextLog contextLog) {
        final User user = contextLog.getUser();
        final UserGroup userGroup = (user != null) ? user.getUserGroup() : null;
        final String groupName = (userGroup != null) ? userGroup.getName() : "";

        return acceptsCombinedAccordingToRule(groupName, getCombinedGroupNames());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsClassName(final ConnectionContextLog contextLog) {
        // accepts(log.getConnectionContext()., getCombinedClassNames())
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsObject(final ConnectionContextLog contextLog) {
        // accepts(log.getConnectionContext()., getCombinedObjects())
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsMode(final ConnectionContextLog contextLog) {
        // accepts(log.get, getCombinedModes()) ||
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsCategory(final ConnectionContextLog contextLog) {
        final ConnectionContext connectionContext = contextLog.getConnectionContext();
        return acceptsCombinedAccordingToRule(connectionContext.getCategory(), getCombinedCategories());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsContextName(final ConnectionContextLog contextLog) {
        final ConnectionContext connectionContext = contextLog.getConnectionContext();
        return acceptsCombinedAccordingToRule(connectionContext.getContent(), getCombinedContextNames());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean acceptsOriginIp(final ConnectionContextLog contextLog) {
        final ConnectionContext connectionContext = contextLog.getConnectionContext();
        return acceptsCombinedAccordingToRule(connectionContext.getOrigin().toString(), getCombinedOriginIps());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   object  DOCUMENT ME!
     * @param   set     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private boolean acceptsCombinedAccordingToRule(final Object object, final Set set) {
        switch (getRule()) {
            case INCLUDE: {
                return !set.isEmpty() && set.contains(object);
            }
            case EXCLUDE: {
                return set.isEmpty() || !set.contains(object);
            }
            default: {
                throw new IllegalArgumentException("rule has to be either INCLUDE or EXCLUDE");
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class CombinedSet<T> extends HashSet<T> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CombinedSet object.
         *
         * @param  single    DOCUMENT ME!
         * @param  multiple  DOCUMENT ME!
         */
        public CombinedSet(final T single, final Set<T> multiple) {
            if (single != null) {
                add(single);
            }
            if (multiple != null) {
                addAll(multiple);
            }
        }
    }
}
