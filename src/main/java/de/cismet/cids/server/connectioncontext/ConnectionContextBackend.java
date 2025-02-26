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

import Sirius.server.ServerExitError;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.io.File;
import java.io.IOException;

import java.lang.management.ManagementFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.ObjectName;

import javax.servlet.http.HttpServletRequest;

import de.cismet.connectioncontext.AbstractConnectionContext;

import de.cismet.connectioncontext.AbstractConnectionContext.Category;

import de.cismet.connectioncontext.ConnectionContext;

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

    private final Map<String, ConnectionContextLogger> contextLoggerDummies = new HashMap<>();
    private final Map<String, ConnectionContextLogger> contextLoggers = new HashMap<>();

    private boolean enabled = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextLogger object.
     */
    private ConnectionContextBackend() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public void registerMBean() {
        try {
            ManagementFactory.getPlatformMBeanServer()
                    .registerMBean(
                        new ConnectionContextManagement(),
                        new ObjectName("de.cismet.cids.server.connectioncontext:type=ConnectionContextMBean"));
        } catch (final Exception ex) {
            final String message = "could not register connection context MBean"; // NOI18N
            LOG.error(message, ex);
            throw new ServerExitError(message, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  configFilePath  DOCUMENT ME!
     */
    public void loadConfig(final String configFilePath) {
        contextLoggerDummies.clear();
        for (final ConnectionContextLogger connectionContextLogger
                    : (Collection<ConnectionContextLogger>)Lookup.getDefault().lookupAll(
                        ConnectionContextLogger.class)) {
            final String type = connectionContextLogger.getType();
            contextLoggerDummies.put(type, connectionContextLogger);
        }

        if (configFilePath != null) {
            final File json = new File(configFilePath);

            contextLoggers.clear();
            try {
                final ConnectionContextConfig config = OBJECT_MAPPER.readValue(json, ConnectionContextConfig.class);
                final Collection<ConnectionContextLoggerConfig> loggerConfigs = config.getLoggers();
                for (final ConnectionContextLoggerConfig loggerConfig : loggerConfigs) {
                    if (loggerConfig != null) {
                        final String loggerName = loggerConfig.getName();
                        if (loggerName != null) {
                            final String loggerType = loggerConfig.getType();
                            if (contextLoggerDummies.containsKey(loggerType)) {
                                final ConnectionContextLogger loggerDummy = contextLoggerDummies.get(loggerType);
                                final ConnectionContextLogger logger = loggerDummy.createNewLogger(
                                        loggerName,
                                        loggerConfig.getConfig());
                                contextLoggers.put(loggerName, logger);
                            }
                        }
                    }
                }

                final Collection<ConnectionContextFilterRuleSet> ruleSets = config.getRuleSets();
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
                LOG.error("The connection context rulesets couldn't be loaded", ex);
            }
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
     * @param  contextLog  DOCUMENT ME!
     */
    public void log(final ConnectionContextLog contextLog) {
        new Thread(new LogRunner(contextLog)).start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hsr                DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConnectionContext addOriginToConnectionContext(final HttpServletRequest hsr,
            final ConnectionContext connectionContext) {
        final ConnectionContext notNullConnectionContext;
        if (connectionContext == null) {
            LOG.warn("Connection-Context is null, creating new one.", new Exception());
            notNullConnectionContext = ConnectionContext.createDeprecated();
        } else {
            notNullConnectionContext = connectionContext;
        }

        if (isEnabled()) {
            Collection<String> ips = (Collection)notNullConnectionContext.getInfoFields()
                        .get(AbstractConnectionContext.FIELD__CLIENT_IP);
            if (ips == null) {
                ips = new ArrayList<>();
            }
            ips.add(hsr.getLocalAddr());

            notNullConnectionContext.getInfoFields().put(AbstractConnectionContext.FIELD__CLIENT_IP, ips);
        }
        return notNullConnectionContext;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
//        final Properties p = new Properties();
//        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
//        p.put("log4j.appender.Remote.remoteHost", "localhost");
//        p.put("log4j.appender.Remote.port", Integer.toString(4445));
//        p.put("log4j.appender.Remote.locationInfo", "true");
//        p.put("log4j.rootLogger", "DEBUG,Remote");
//        org.apache.log4j.PropertyConfigurator.configure(p);

        final ConnectionContextBackend backend = getInstance();
        backend.loadConfig("/home/jruiz/testRuleSets.json");
        final ConnectionContext connectionContext = ConnectionContext.create(Category.RENDERER, "test");
        connectionContext.getInfoFields().put(AbstractMetaObjectConnectionContext.FIELD__CLASS_NAME, "treppe");
        connectionContext.getInfoFields().put(AbstractMetaObjectConnectionContext.FIELD__OBJECT_ID, 527);
        final ConnectionContextLog contextLog = ConnectionContextLog.create(
                connectionContext,
                new User(0, "testUser", "testDomain"),
                "main");
        backend.log(contextLog);
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

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class LogRunner implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private final ConnectionContextLog contextLog;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LogRunner object.
         *
         * @param  contextLog  DOCUMENT ME!
         */
        public LogRunner(final ConnectionContextLog contextLog) {
            this.contextLog = contextLog;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            for (final ConnectionContextLogger contextLogger : contextLoggers.values()) {
                if (contextLogger != null) {
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("giving connection context log to logger: " + contextLogger.getType());
                        }
                        contextLogger.log(contextLog);
                    } catch (final Exception ex) {
                        LOG.warn("exception while logging context with logger " + contextLogger.getType(), ex);
                    }
                }
            }
        }
    }
}
