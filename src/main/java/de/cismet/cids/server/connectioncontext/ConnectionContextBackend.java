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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cismet.connectioncontext.AbstractConnectionContext;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.cismet.connectioncontext.AbstractConnectionContext.Category;

import de.cismet.connectioncontext.ConnectionContext;
import javax.servlet.http.HttpServletRequest;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ConnectionContextBackend {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ConnectionContextBackend.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //~ Instance fields --------------------------------------------------------

    private final Map<String, ConnectionContextLogger> contextLoggers = new HashMap<>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextLogger object.
     */
    private ConnectionContextBackend() {
        for (final ConnectionContextLogger connectionContextLogger
                    : (Collection<ConnectionContextLogger>)Lookup.getDefault().lookupAll(ConnectionContextLogger.class)) {
            final String name = connectionContextLogger.getName();
            contextLoggers.put(name, connectionContextLogger);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void loadRuleSets() {
        final File json = new File(
                "/home/jruiz/git_cismet/040-cids-server/src/main/java/de/cismet/cids/server/connectioncontext/testRuleSet.json");

        for (final ConnectionContextLogger connectionContextLogger : contextLoggers.values()) {
            connectionContextLogger.getFilterRuleSets().clear();
        }

        try {
            final List<ConnectionContextFilterRuleSet> ruleSets = OBJECT_MAPPER.readValue(
                    json,
                    new TypeReference<List<ConnectionContextFilterRuleSet>>() {
                    });
            for (final ConnectionContextFilterRuleSet ruleSet : ruleSets) {
                final String loggerName = ruleSet.getLoggerName();
                if (contextLoggers.containsKey(loggerName)) {
                    final ConnectionContextLogger connectionContextLogger = contextLoggers.get(loggerName);
                    if (connectionContextLogger != null) {
                        connectionContextLogger.getFilterRuleSets().add(ruleSet);
                    } else {
                        LOG.error("ConnectionContextLogger " + loggerName
                                    + " is null. Something went wrong while initialization !");
                    }
                } else {
                    LOG.warn("ConnectionContextLogger " + loggerName + " not found !");
                }
            }
        } catch (final IOException ex) {
            LOG.error("the RuleSets couldn't be loaded", ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextBackend getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  connectionContext  DOCUMENT ME!
     * @param  user               DOCUMENT ME!
     * @param  methodName         DOCUMENT ME!
     * @param  params             DOCUMENT ME!
     */
    public void log(final ConnectionContext connectionContext,
            final User user,
            final String methodName,
            final Object... params) {      
        final ConnectionContextLog contextLog = new ConnectionContextLog(new Date(),
                user,
                connectionContext,
                methodName,
                params);
        for (final ConnectionContextLogger contextLogger : contextLoggers.values()) {
            if (contextLogger != null) {
                try {
                    contextLogger.log(contextLog);
                } catch (final Exception ex) {
                    LOG.warn("exception while logging context with logger " + contextLogger.getName(), ex);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hsr                DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConnectionContext addOriginToConnectionContext(final HttpServletRequest hsr, final ConnectionContext connectionContext) {
        final ConnectionContext notNullConnectionContext;
        if (connectionContext == null) {
            notNullConnectionContext = ConnectionContext.createDeprecated();
        } else {
            notNullConnectionContext = connectionContext;
        }
        
        notNullConnectionContext.getInfoFields().put(AbstractConnectionContext.FIELD__CLIENT_IP, hsr.getLocalAddr());
        return notNullConnectionContext;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", Integer.toString(4445));
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "DEBUG,Remote");
        org.apache.log4j.PropertyConfigurator.configure(p);

        final ConnectionContextBackend backend = getInstance();
        backend.loadRuleSets();
        final ConnectionContext connectionContext = ConnectionContext.create(Category.RENDERER, "test");
        connectionContext.getInfoFields().put(AbstractMetaObjectConnectionContext.FIELD__CLASS_NAME, "treppe");
        connectionContext.getInfoFields().put(AbstractMetaObjectConnectionContext.FIELD__OBJECT_ID, 17);
        backend.log(connectionContext, null, "test");
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final ConnectionContextBackend INSTANCE = new ConnectionContextBackend();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
