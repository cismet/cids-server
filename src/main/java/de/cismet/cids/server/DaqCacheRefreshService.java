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
package de.cismet.cids.server;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.DomainServerStartupHook;
import Sirius.server.property.ServerProperties;
import Sirius.server.property.ServerPropertiesHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

import de.cismet.cids.server.actions.DataAquisitionAction;

import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.commons.concurrency.CismetExecutors;

/**
 * Refreshs the daq cache.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = DomainServerStartupHook.class)
public class DaqCacheRefreshService implements Runnable, DomainServerStartupHook, ServerPropertiesHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DaqCacheRefreshService.class);
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int EXECUTION_TOLERANCE = 100;
    private static final double FACTOR_TO_INCREASE_INTERVAL = 1.1;
    private static final Integer INVALID_JSON_STATUS = 502;
    private static final Integer ERROR_WHILE_FILLING_STATUS = 500;
    private static final Integer OK_STATUS = 200;
    private static final String DAQ_SCHEMA = "daq";
    private static final String MATERIALIZE_SCHEMA = "materialize";

    //~ Instance fields --------------------------------------------------------

    private final Map<String, Long> lastRefresh = new HashMap<>();
    private final Map<String, Long> lastRefreshTimes = new HashMap<>();
    private final Set<String> currentlyRunningRefreshs = new TreeSet<>();
    private ExecutorService executor;
    private int maxParallelThreads = 10;
    private ServerProperties properties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CacheRefreshService object.
     */
    public DaqCacheRefreshService() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setServerProperties(final ServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("CacheRefreshService");
        DomainServerImpl metaService = null;

        while (metaService == null) {
            metaService = DomainServerImpl.getServerInstance();
            try {
                Thread.sleep(MILLIS_PER_SECOND);
            } catch (InterruptedException ex) {
                // nothimng to do
            }
        }

        while (true) {
            long sleepTime = 20 * MILLIS_PER_SECOND;
            final DaqCacheRefreshServiceConfiguration config = readConfig();

            if (config == null) {
                // The configuration file does not exist, so this service is not required
                return;
            }

            if (executor == null) {
                // if the executor was created once, changes in the config file will not affect it.

                if (config.getMaxParallelThreads() != null) {
                    maxParallelThreads = config.getMaxParallelThreads();
                }

                executor = CismetExecutors.newFixedThreadPool(
                        maxParallelThreads,
                        CismetConcurrency.getInstance("CacheRefreshService").createThreadFactory(
                            "CacheRefreshService"));
            }

            if (config.getReconsideringTimer() != null) {
                sleepTime = config.getReconsideringTimer() * MILLIS_PER_SECOND;
            }

            final LocalTime startTime = ((config.getStartTime() != null) ? LocalTime.parse(config.getStartTime())
                                                                         : null);
            final LocalTime endTime = ((config.getEndTime() != null) ? LocalTime.parse(config.getEndTime()) : null);
            final LocalTime currentLocalTime = LocalTime.now();

            if ((startTime != null) && (endTime != null) && startTime.isAfter(endTime)) {
                LOG.error("DaqCacheRefreshService: start time is after end time. So the times will be ignored");
            }

            if ((startTime == null) || (endTime == null) || startTime.isAfter(endTime)
                        || (startTime.isBefore(currentLocalTime) && endTime.isAfter(currentLocalTime))) {
                // recreate views, if necessary
                for (final DaqCacheRefreshServiceViewConfiguration viewConfig : config.getViewConfigurations()) {
                    final String name = getViewNameWithSchema(viewConfig);
                    final Integer refreshInterval = viewConfig.getTimeInSeconds();
                    final Long lastRefreshTime = lastRefresh.get(name);
                    final long currentTime = new Date().getTime();

                    if (lastRefreshTime == null) {
                        // the view was not refreshed, yet
                        final boolean refreshInProgress = refreshView(viewConfig, metaService);

                        if (refreshInProgress) {
                            lastRefresh.put(name, currentTime);
                        }
                    } else {
                        final long timeForNextRun = lastRefreshTime + (refreshInterval * MILLIS_PER_SECOND);

                        if ((timeForNextRun < currentTime)
                                    || (Math.abs(timeForNextRun - currentTime) < EXECUTION_TOLERANCE)) {
                            final boolean refreshInProgress = refreshView(viewConfig, metaService);

                            if (refreshInProgress) {
                                lastRefresh.put(name, currentTime);
                            }
                        } else {
                            if ((timeForNextRun - currentTime) < sleepTime) {
                                sleepTime = timeForNextRun - currentTime;
                            }
                        }
                    }

                    if ((refreshInterval * MILLIS_PER_SECOND) < sleepTime) {
                        sleepTime = refreshInterval * MILLIS_PER_SECOND;
                    }
                }
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Create a new Thread to refresh the view.
     *
     * @param   viewConfig   DOCUMENT ME!
     * @param   metaService  DOCUMENT ME!
     *
     * @return  true, iff the refresh thread was added to the executor
     */
    private boolean refreshView(final DaqCacheRefreshServiceViewConfiguration viewConfig,
            final DomainServerImpl metaService) {
        if (currentlyRunningRefreshs.contains(getViewNameWithSchema(viewConfig))) {
            LOG.warn("The view " + getViewNameWithSchema(viewConfig)
                        + " should be refreshed before the last refresh was completed");
            return false;
        } else {
            currentlyRunningRefreshs.add(getViewNameWithSchema(viewConfig));
            executor.submit(new ViewRefresher(viewConfig, metaService));

            return true;
        }
    }

    /**
     * Reads the refresh configuration.
     *
     * @return  The configuration object or null, if the configuration file does not exist
     */
    private DaqCacheRefreshServiceConfiguration readConfig() {
        final DaqCacheRefreshServiceConfiguration config;

        try {
            config = ServerResourcesLoader.getInstance()
                        .loadJson(GeneralServerResources.CACHE_REFRESH_JSON.getValue(),
                                DaqCacheRefreshServiceConfiguration.class);
        } catch (Exception e) {
            return null;
        }

        // check if the last refresh time was longer than the refresh interval
        for (final DaqCacheRefreshServiceViewConfiguration viewConfig : config.getViewConfigurations()) {
            final Long lastRun = lastRefreshTimes.get(getViewNameWithSchema(viewConfig));

            if (lastRun != null) {
                if (lastRun > (viewConfig.getTimeInSeconds() * MILLIS_PER_SECOND)) {
                    int newRefreshTime = (int)((lastRun / MILLIS_PER_SECOND) * FACTOR_TO_INCREASE_INTERVAL);

                    if (newRefreshTime == viewConfig.getTimeInSeconds()) {
                        ++newRefreshTime;
                    }

                    viewConfig.setTimeInSeconds(newRefreshTime);

                    LOG.warn("The refresh interval of the view " + viewConfig.getViewName()
                                + " is shorter than the last refresh time. Increase the refresh time to "
                                + newRefreshTime + " seconds.");
                }
            }
        }

        return config;
    }

    @Override
    public void domainServerStarted() {
        if ((properties != null)
                    && (ServerProperties.DEPLOY_ENV__PRODUCTION.equalsIgnoreCase(properties.getDeployEnv())
                        || ServerProperties.DEPLOY_ENV__DEVELOPMENT.equalsIgnoreCase(properties.getDeployEnv()))) {
            new Thread(this).start();
        } else {
            LOG.info("No production mode. So the DaqCacheRefreshService will not be started");
        }
    }

    @Override
    public String getDomain() {
        return DomainServerImpl.getServerProperties().getServerName();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   viewConfig  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getViewNameWithSchema(final DaqCacheRefreshServiceViewConfiguration viewConfig) {
        String viewName;

        if ((viewConfig.isIsDaqView() == null) || viewConfig.isIsDaqView()) {
            viewName = toStringOrDefault(viewConfig.getSchemaName(), MATERIALIZE_SCHEMA) + "."
                        + viewConfig.getViewName();
        } else {
            viewName = toStringOrDefault(viewConfig.getSchemaName(), DAQ_SCHEMA) + "." + viewConfig.getViewName();
        }

        return viewName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   str  DOCUMENT ME!
     * @param   def  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toStringOrDefault(final Object str, final String def) {
        if (str == null) {
            return def;
        } else {
            return String.valueOf(str);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Refreshs the given View.
     *
     * @version  $Revision$, $Date$
     */
    private class ViewRefresher implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private final DaqCacheRefreshServiceViewConfiguration viewConfig;
        private final DomainServerImpl metaService;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ViewRefresher object.
         *
         * @param  viewConfig   viewName DOCUMENT ME!
         * @param  metaService  DOCUMENT ME!
         */
        public ViewRefresher(final DaqCacheRefreshServiceViewConfiguration viewConfig,
                final DomainServerImpl metaService) {
            this.viewConfig = viewConfig;
            this.metaService = metaService;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            final long startRun = System.currentTimeMillis();

            if ((viewConfig.isIsDaqView() == null) || viewConfig.isIsDaqView()) {
                refreshJsonView();
            } else {
                refreshView();
            }

            final long timeToRun = System.currentTimeMillis() - startRun;
            lastRefreshTimes.put(getViewNameWithSchema(viewConfig), timeToRun);
            currentlyRunningRefreshs.remove(getViewNameWithSchema(viewConfig));
        }

        /**
         * DOCUMENT ME!
         */
        private void refreshView() {
            Connection con = null;
            final String viewName = getViewNameWithSchema(viewConfig);

            try {
                try {
                    con = metaService.getConnectionPool().getConnection(true);
                    final PreparedStatement stat = con.prepareStatement("select cs_recreateCachedView(?,?)");
                    stat.setString(1, toStringOrDefault(viewConfig.getSchemaName(), MATERIALIZE_SCHEMA));
                    stat.setString(2, viewConfig.getViewName());
                    stat.execute();

                    // execute attached sql file
                    if ((viewConfig.getIndexFile() != null) && !viewConfig.getIndexFile().trim().equals("")) {
                        final File f = new File(viewConfig.getIndexFile());

                        if (f.exists()) {
                            final StringBuilder commands = new StringBuilder();

                            try(final FileReader fr = new FileReader(f);
                                        final BufferedReader br = new BufferedReader(fr)) {
                                String tmp;

                                while ((tmp = br.readLine()) != null) {
                                    commands.append(tmp).append("\n");
                                }
                            }

                            final String commandStr = commands.toString();

                            if (!commandStr.trim().equals("")) {
                                Statement st = null;
                                try {
                                    st = con.createStatement();
                                    st.execute(commandStr);
                                } finally {
                                    if (st != null) {
                                        st.close();
                                    }
                                }
                            }
                        } else {
                            LOG.error("DaqCacheRefreshService: index file " + viewConfig.getIndexFile()
                                        + " does not exist");
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Error while refreshing cached table " + viewName, e);
                }
            } catch (Exception e) {
                LOG.error("Error while refreshing cached table " + viewName, e);
            } finally {
                if (con != null) {
                    metaService.getConnectionPool().releaseDbConnection(con);
                }
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void refreshJsonView() {
            Connection con = null;
            boolean recreationSuccessful = false;
            final String viewName = viewConfig.getViewName();
            final String schemaName = toStringOrDefault(viewConfig.getSchemaName(), DAQ_SCHEMA);
//            String viewName = toStringOrDefault(viewConfig.getSchemaName(), "materialized") + "." + viewConfig.getViewName();

            try {
                con = metaService.getConnectionPool().getConnection(true);

                try {
                    final PreparedStatement stat = con.prepareStatement("select cs_recreateCachedDaqView(?, ?)");
                    stat.setString(1, toStringOrDefault(viewConfig.getSchemaName(), DAQ_SCHEMA));
                    stat.setString(2, viewConfig.getViewName());
                    stat.execute();
                    recreationSuccessful = true;
                } catch (Exception e) {
                    LOG.error("Error while refreshing cached table. Set status field.", e);

                    try {
                        if (con == null) {
                            con = metaService.getConnectionPool().getConnection(true);
                        }
                        final String checkedViewName = DataAquisitionAction.quoteIdentifier(con, viewName);
                        final PreparedStatement stat = con.prepareStatement("insert into " + schemaName + "."
                                        + checkedViewName
                                        + "_cached (md5, json, time, version, status) (select md5, json, now(), version, ? from "
                                        + schemaName + "."
                                        + checkedViewName + "_cached where status = '200')");
                        stat.setString(1, ERROR_WHILE_FILLING_STATUS + ", " + e.getMessage());
                        stat.execute();
                    } catch (Exception ex) {
                        LOG.error("Error while set status field to error.", ex);
                    }
                }

                final String checkedViewName = DataAquisitionAction.quoteIdentifier(con, viewName);

                if (recreationSuccessful) {
                    // a new dataset was created from the view

                    // check created json
                    final PreparedStatement queryStat = con.prepareStatement("select json, md5, time, version from "
                                    + schemaName + "."
                                    + checkedViewName
                                    + "_cached where status is null");
                    final ResultSet rs = queryStat.executeQuery();

                    while (rs.next()) {
                        final String json = rs.getString(1);
                        final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

                        try {
                            // test, if json is invalid
                            final JsonNode node = mapper.readTree(json);

                            // json can be parsed so set status to 200
                            setStatus(con, schemaName, checkedViewName, OK_STATUS.toString());
                        } catch (Exception e) {
                            // Cannot parse json. So set status to INVALID_JSON_STATUS
                            setStatus(con, schemaName, checkedViewName, INVALID_JSON_STATUS.toString());
                        }
                    }
                }

                deleteAllExceptOnePerStatus(con, schemaName, checkedViewName);
            } catch (Exception e) {
                LOG.error("Error while refreshing cached table.", e);
            } finally {
                if (con != null) {
                    metaService.getConnectionPool().releaseDbConnection(con);
                }
            }
        }

        /**
         * Change the status of the ds with the status null to the given value.
         *
         * @param   con              the db connection
         * @param   schemaName       DOCUMENT ME!
         * @param   checkedViewName  the name of the view
         * @param   status           the new status
         *
         * @throws  SQLException  DOCUMENT ME!
         */
        private void setStatus(final Connection con,
                final String schemaName,
                final String checkedViewName,
                final String status) throws SQLException {
            final PreparedStatement queryStat = con.prepareStatement("update " + schemaName + "." + checkedViewName
                            + "_cached set status = ? where status is null");
            queryStat.setString(1, status);
            queryStat.execute();
        }

        /**
         * Delete all data sets within the given cached table except one per status.
         *
         * @param   con              the db connection
         * @param   schemaName       DOCUMENT ME!
         * @param   checkedViewName  the name of the view
         *
         * @throws  SQLException  DOCUMENT ME!
         */
        private void deleteAllExceptOnePerStatus(final Connection con,
                final String schemaName,
                final String checkedViewName) throws SQLException {
            final PreparedStatement queryStat = con.prepareStatement("delete from " + schemaName + "." + checkedViewName
                            + "_cached where time not in (select max(time) from " + schemaName + "." + checkedViewName
                            + "_cached group by substring(rpad(coalesce(status, ''), 3, 'x') for 3))");
            queryStat.execute();
        }
    }
}
