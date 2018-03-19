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

import java.util.Collection;

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

    private static final String PROPERTY__EXCLUDE = "exclude";
    private static final String PROPERTY__USER_LOGIN = "userLogin";
    private static final String PROPERTY__GROUP_NAME = "groupName";
    private static final String PROPERTY__CLASS_NAME = "className";
    private static final String PROPERTY__OBJECT_ID = "objectId";
    private static final String PROPERTY__MODE = "mode";
    private static final String PROPERTY__CATEGORY = "category";
    private static final String PROPERTY__CONTEXT_NAME = "contextName";
    private static final String PROPERTY__ORIGIN_IP = "originIp";

    //~ Enums ------------------------------------------------------------------

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

    @JsonProperty(PROPERTY__EXCLUDE)
    private boolean exclude = false;
    private String userLogin;
    @JsonProperty(PROPERTY__GROUP_NAME)
    private String groupName;
    @JsonProperty(PROPERTY__CLASS_NAME)
    private String className;
    @JsonProperty(PROPERTY__OBJECT_ID)
    private String objectId;
    @JsonProperty(PROPERTY__MODE)
    private Mode mode;
    @JsonProperty(PROPERTY__CATEGORY)
    private String category;
    @JsonProperty(PROPERTY__CONTEXT_NAME)
    private String contextName;
    @JsonProperty(PROPERTY__ORIGIN_IP)
    private String originIp;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSatisfied(final ConnectionContextLog contextLog) {
        final boolean exclude = isExclude();

        return true
                    && isUserLoginSatisfied(contextLog, exclude)
                    && isGroupNameSatisfied(contextLog, exclude)
                    && isClassNameSatisfied(contextLog, exclude)
                    && isObjectIdSatisfied(contextLog, exclude)
                    && isModeSatisfied(contextLog, exclude)
                    && isCategorySatisfied(contextLog, exclude)
                    && isContextNameSatisfied(contextLog, exclude)
                    && isOriginIpSatisfied(contextLog, exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isUserLoginSatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getUserLogin() == null) {
            return true;
        }
        final User user = contextLog.getUser();
        final String userLogin = (user != null) ? user.getName() : "";

        return isSatisfied(userLogin, getUserLogin(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isGroupNameSatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getGroupName() == null) {
            return true;
        }
        final User user = contextLog.getUser();
        final UserGroup userGroup = (user != null) ? user.getUserGroup() : null;
        final String groupName = (userGroup != null) ? userGroup.getName() : "";

        return isSatisfied(groupName, getGroupName(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isClassNameSatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getClassName() == null) {
            return true;
        }
        return isSatisfied(contextLog.getClassNames(), getClassName(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isObjectIdSatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getObjectId() == null) {
            return true;
        }
        return isSatisfied(contextLog.getObjectIds(), getObjectId(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isModeSatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getMode() == null) {
            return true;
        }
        return isSatisfied(null, getMode().name(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isCategorySatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getCategory() == null) {
            return true;
        }
        return isSatisfied(contextLog.getCategory(), getCategory(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isContextNameSatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getContextName() == null) {
            return true;
        }
        return isSatisfied(contextLog.getContextName(), getContextName(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   contextLog  DOCUMENT ME!
     * @param   exclude     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isOriginIpSatisfied(final ConnectionContextLog contextLog, final boolean exclude) {
        if (getOriginIp() == null) {
            return true;
        }
        return isSatisfied(contextLog.getOriginIp(), getOriginIp(), exclude);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   object   DOCUMENT ME!
     * @param   filter   DOCUMENT ME!
     * @param   exclude  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean isSatisfied(final Object object, final String filter, final boolean exclude) {
        if (filter.isEmpty()) {
            return true;
        }
        if (object instanceof Collection) {
            final Collection collection = (Collection)object;
            if (exclude) {
                for (final Object objectOfCollection : collection) {
                    if (isMatching(objectOfCollection, filter)) {
                        return false;
                    }
                }
                return true;
            } else {
                for (final Object objectOfCollection : collection) {
                    if (isMatching(objectOfCollection, filter)) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            if (exclude) {
                return !isMatching(object, filter);
            } else {
                return isMatching(object, filter);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   object  DOCUMENT ME!
     * @param   filter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean isMatching(final Object object, final String filter) {
        if (filter == null) {
            return true;
        }
        if (object == null) {
            return true; // should return false except if filter = *
        }
        return filter.equalsIgnoreCase(object.toString());
    }
}
