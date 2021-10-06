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

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class ConnectionContextLog {

    //~ Static fields/initializers ---------------------------------------------

    private static final String DEFAULT_FORMAT_STRING =
        "[${TIME} ${USER_NAME}@${USER_DOMAIN}] ${CATEGORY}(${CONTEXT_NAME}) =(${CLIENT_IP})> ${METHOD_NAME}(${METHOD_PARAMS})";

    //~ Instance fields --------------------------------------------------------

    private final Date timestamp;
    private final User user;
    private final String contextName;
    private final ConnectionContext.Category category;
    private final String methodName;
    private final Map<String, Object> methodParams;
    private final HashSet<Integer> objectIds = new HashSet<>();
    private final HashSet<String> classNames = new HashSet<>();
    private String configAttr;
    private String task;
    private String search;
    private final Exception stacktraceException;
    private final String originIp;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextLog object.
     *
     * @param  timestamp          DOCUMENT ME!
     * @param  user               DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     * @param  methodName         DOCUMENT ME!
     * @param  methodParams       DOCUMENT ME!
     */
    public ConnectionContextLog(final Date timestamp,
            final User user,
            final ConnectionContext connectionContext,
            final String methodName,
            final Map<String, Object> methodParams) {
        this.timestamp = timestamp;
        this.user = user;
        this.methodName = methodName;
        this.methodParams = methodParams;
        this.category = (connectionContext != null) ? connectionContext.getCategory() : null;

        final Map<String, Object> infoFields = (connectionContext != null) ? connectionContext.getInfoFields()
                                                                           : new HashMap<String, Object>();

        // name
        if (infoFields.containsKey(AbstractConnectionContext.FIELD__CONTEXT_NAME)) {
            final String contextName = (String)infoFields.get(AbstractConnectionContext.FIELD__CONTEXT_NAME);
            this.contextName = contextName;
        } else {
            this.contextName = null;
        }

        // ip
        if (infoFields.containsKey(AbstractMetaObjectConnectionContext.FIELD__CLIENT_IP)) {
            final Collection<String> ips = (Collection)infoFields.get(AbstractConnectionContext.FIELD__CLIENT_IP);
            if ((ips != null) && !ips.isEmpty()) {
                this.originIp = ips.iterator().next();
            } else {
                this.originIp = null;
            }
        } else {
            this.originIp = null;
        }

        // objects
        if (infoFields.containsKey(AbstractMetaObjectConnectionContext.FIELD__OBJECT_ID)) {
            this.objectIds.add((Integer)infoFields.get(AbstractMetaObjectConnectionContext.FIELD__OBJECT_ID));
        }
        if (infoFields.containsKey(AbstractMetaObjectConnectionContext.FIELD__OBJECT_IDS)) {
            this.objectIds.addAll((Set)infoFields.get(AbstractMetaObjectConnectionContext.FIELD__OBJECT_IDS));
        }

        // classes
        if (infoFields.containsKey(AbstractMetaClassConnectionContext.FIELD__CLASS_NAME)) {
            this.classNames.add((String)infoFields.get(AbstractMetaObjectConnectionContext.FIELD__CLASS_NAME));
        }
        if (infoFields.containsKey(AbstractMetaClassConnectionContext.FIELD__CLASS_NAMES)) {
            this.classNames.addAll((Set)infoFields.get(AbstractMetaObjectConnectionContext.FIELD__CLASS_NAMES));
        }

        // stacktrace
        if (infoFields.containsKey(AbstractMetaClassConnectionContext.FIELD__STACKTRACE_EXCEPTION)) {
            this.stacktraceException = (Exception)infoFields.get(AbstractConnectionContext.FIELD__STACKTRACE_EXCEPTION);
        } else {
            this.stacktraceException = null;
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   metaClasses        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog createForMetaClasses(
            final MetaClass[] metaClasses,
            final ConnectionContext connectionContext,
            final User user,
            final String methodName,
            final Map<String, Object> params) {
        final ConnectionContextLog contextLog = create(connectionContext, user, methodName, params);
        if ((contextLog != null) && (metaClasses != null)) {
            final List<String> classNames = new ArrayList<>(metaClasses.length);
            for (final MetaClass metaClass : metaClasses) {
                classNames.add((metaClass != null) ? metaClass.getTableName().toLowerCase() : null);
            }
            contextLog.getClassNames().addAll(classNames);
        }
        return contextLog;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass          DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog createForMetaClass(
            final MetaClass metaClass,
            final ConnectionContext connectionContext,
            final User user,
            final String methodName,
            final Map<String, Object> params) {
        return createForMetaClasses(new MetaClass[] { metaClass }, connectionContext, user, methodName, params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObjects        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog createForMetaObjects(
            final MetaObject[] metaObjects,
            final ConnectionContext connectionContext,
            final User user,
            final String methodName,
            final Map<String, Object> params) {
        final ConnectionContextLog contextLog = create(connectionContext, user, methodName, params);
        if ((contextLog != null) && (metaObjects != null)) {
            final List<Integer> objectIds = new ArrayList<>(metaObjects.length);
            for (final MetaObject metaObject : metaObjects) {
                objectIds.add((metaObject != null) ? metaObject.getId() : null);
            }
            contextLog.getObjectIds().addAll(objectIds);
        }
        return contextLog;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject         DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog createForMetaObject(
            final MetaObject metaObject,
            final ConnectionContext connectionContext,
            final User user,
            final String methodName,
            final Map<String, Object> params) {
        return createForMetaObjects(new MetaObject[] { metaObject }, connectionContext, user, methodName, params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog create(final ConnectionContext connectionContext,
            final User user,
            final String methodName) {
        return create(connectionContext, user, methodName, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   configAttr         DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog createForConfigAttr(final ConnectionContext connectionContext,
            final User user,
            final String configAttr,
            final String methodName,
            final Map<String, Object> params) {
        final ConnectionContextLog contextLog = new ConnectionContextLog(new Date(),
                user,
                connectionContext,
                methodName,
                params);
        contextLog.setConfigAttr(configAttr);
        return contextLog;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   search             DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog createForSearch(final ConnectionContext connectionContext,
            final User user,
            final String search,
            final String methodName,
            final Map<String, Object> params) {
        final ConnectionContextLog contextLog = new ConnectionContextLog(new Date(),
                user,
                connectionContext,
                methodName,
                params);
        contextLog.setSearch(search);
        return contextLog;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   task               DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog createForTask(final ConnectionContext connectionContext,
            final User user,
            final String task,
            final String methodName,
            final Map<String, Object> params) {
        final ConnectionContextLog contextLog = new ConnectionContextLog(new Date(),
                user,
                connectionContext,
                methodName,
                params);
        contextLog.setTask(task);
        return contextLog;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   methodName         DOCUMENT ME!
     * @param   params             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLog create(final ConnectionContext connectionContext,
            final User user,
            final String methodName,
            final Map<String, Object> params) {
        final ConnectionContextLog contextLog = new ConnectionContextLog(new Date(),
                user,
                connectionContext,
                methodName,
                params);
        return contextLog;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   formatString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toString(String formatString) {
        if (formatString == null) {
            formatString = DEFAULT_FORMAT_STRING;
        }

        final String time = (getTimestamp() != null) ? DateFormat.getDateTimeInstance().format(getTimestamp()) : null;
        final String userName = (getUser() != null) ? getUser().getName() : null;
        final String userDomain = (getUser() != null) ? getUser().getDomain() : null;
        final String category = (getCategory() != null) ? getCategory().name() : null;
        final String contextName = getContextName();
        final String clientIp = getOriginIp();
        final String methodName = getMethodName();

        final String methodParams;
        if (getMethodParams() != null) {
            final String separator = ",";
            final String keyValueSeparator = ":";

            final StringBuffer buffer = new StringBuffer();

            final Iterator<Map.Entry<String, Object>> entryIterator = getMethodParams().entrySet().iterator();

            while (entryIterator.hasNext()) {
                final Map.Entry<String, Object> entry = entryIterator.next();

                final String key = entry.getKey();
                final Object value = entry.getValue();
                buffer.append(key).append(keyValueSeparator).append(value);

                if (entryIterator.hasNext()) {
                    buffer.append(separator);
                }
            }
            methodParams = buffer.toString();
        } else {
            methodParams = "";
        }

        return formatString.replaceAll(Pattern.quote("${TIME}"), Matcher.quoteReplacement(time))
                    .replaceAll(Pattern.quote("${USER_NAME}"), Matcher.quoteReplacement(userName))
                    .replaceAll(Pattern.quote("${USER_DOMAIN}"), Matcher.quoteReplacement(userDomain))
                    .replaceAll(Pattern.quote("${CATEGORY}"), Matcher.quoteReplacement(category))
                    .replaceAll(Pattern.quote("${CONTEXT_NAME}"), Matcher.quoteReplacement(contextName))
                    .replaceAll(Pattern.quote("${CLIENT_IP}"), Matcher.quoteReplacement(clientIp))
                    .replaceAll(Pattern.quote("${METHOD_NAME}"), Matcher.quoteReplacement(methodName))
                    .replaceAll(Pattern.quote("${METHOD_PARAMS}"), Matcher.quoteReplacement(methodParams));
    }

    @Override
    public String toString() {
        return toString(DEFAULT_FORMAT_STRING);
    }
}
