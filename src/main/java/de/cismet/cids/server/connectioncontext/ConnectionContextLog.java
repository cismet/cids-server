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

import lombok.Getter;
import lombok.Setter;

import java.text.DateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;
import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class ConnectionContextLog {

    //~ Static fields/initializers ---------------------------------------------

    private static final String LOG_SERVER_FORMAT = "[%s %s@%s] %s(%s) => %s %s";
    private static final String LOG_CLIENT_FORMAT = "[%s %s@%s] %s(%s) =(%s)> %s %s";

    //~ Instance fields --------------------------------------------------------

    private final Date timestamp;
    private final User user;
    private final String contextName;
    private final ConnectionContext.Category category;
    private final String methodName;
    private final Object[] methodParams;
    private final HashSet<Integer> objectIds = new HashSet<>();
    private final HashSet<String> classNames = new HashSet<>();
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
            final Object[] methodParams) {        
        this.timestamp = timestamp;
        this.user = user;
        this.methodName = methodName;
        this.methodParams = methodParams;
        this.category = (connectionContext != null) ? connectionContext.getCategory() : null;

        final Map<String, Object> infoFields = (connectionContext != null) ? connectionContext.getInfoFields() : new HashMap<String, Object>();

        // name
        if (infoFields.containsKey(AbstractConnectionContext.FIELD__CONTEXT_NAME)) {
            final String contextName = (String)infoFields.get(AbstractConnectionContext.FIELD__CONTEXT_NAME);
            this.contextName = contextName;
        } else {
            this.contextName = null;
        }

        // ip
        if (infoFields.containsKey(AbstractMetaObjectConnectionContext.FIELD__CLIENT_IP)) {
            this.originIp = (String)infoFields.get(AbstractConnectionContext.FIELD__CLIENT_IP);
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

    @Override
    public String toString() {
        if (originIp == null) {
            return String.format(
                    LOG_SERVER_FORMAT,
                    DateFormat.getDateTimeInstance().format(timestamp),
                    (user != null) ? user.getName() : null,
                    (user != null) ? user.getDomain() : null,
                    (category != null) ? category.name() : null,
                    (contextName != null) ? contextName : null,
                    methodName,
                    (methodParams != null) ? Arrays.toString(methodParams) : null);
        } else {
            return String.format(
                    LOG_CLIENT_FORMAT,
                    DateFormat.getDateTimeInstance().format(timestamp),
                    (user != null) ? user.getName() : null,
                    (user != null) ? user.getDomain() : null,
                    (category != null) ? category.name() : null,
                    (contextName != null) ? contextName : null,
                    originIp,
                    methodName,
                    (methodParams != null) ? Arrays.toString(methodParams) : null);
        }
    }
}
