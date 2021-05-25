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

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

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
public class DaqCacheRefreshService implements Runnable, DomainServerStartupHook {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DaqCacheRefreshService.class);
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int EXECUTION_TOLERANCE = 100;
    private static final double FACTOR_TO_INCREASE_INTERVAL = 1.1;

    //~ Instance fields --------------------------------------------------------

    private final Map<String, Long> lastRefresh = new HashMap<String, Long>();
    private final Map<String, Long> lastRefreshTimes = new HashMap<String, Long>();
    private final Set<String> currentlyRunningRefreshs = new TreeSet<String>();
    private ExecutorService executor;
    private int maxParallelThreads = 10;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CacheRefreshService object.
     */
    public DaqCacheRefreshService() {
    }

    //~ Methods ----------------------------------------------------------------

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

            // recreate views, if necessary
            for (final DaqCacheRefreshServiceViewConfiguration viewConfig : config.getViewConfigurations()) {
                final String name = viewConfig.getViewName();
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
        if (currentlyRunningRefreshs.contains(viewConfig.getViewName())) {
            LOG.warn("The view " + viewConfig.getViewName()
                        + " should be refreshed before the last refresh was completed");
            return false;
        } else {
            currentlyRunningRefreshs.add(viewConfig.getViewName());
            executor.submit(new ViewRefresher(viewConfig.getViewName(), metaService));

            return true;
        }
    }

    /**
     * Reads the refresh configuration.
     *
     * @return  The configuration object or null, if the configuration file does not exist
     */
    private DaqCacheRefreshServiceConfiguration readConfig() {
        DaqCacheRefreshServiceConfiguration config = new DaqCacheRefreshServiceConfiguration();

        try {
            config = ServerResourcesLoader.getInstance()
                        .loadJson(GeneralServerResources.CACHE_REFRESH_JSON.getValue(),
                                DaqCacheRefreshServiceConfiguration.class);
        } catch (Exception e) {
            LOG.error("Error while loading CacheRefreshService", e);

            return null;
        }

        // check if the last refresh time was longer than the refresh interval
        for (final DaqCacheRefreshServiceViewConfiguration viewConfig : config.getViewConfigurations()) {
            final Long lastRun = lastRefreshTimes.get(viewConfig.getViewName());

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
        new Thread(this).start();
    }

    @Override
    public String getDomain() {
        return DomainServerImpl.getServerProperties().getServerName();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ViewRefresher implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private final String viewName;
        private final DomainServerImpl metaService;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ViewRefresher object.
         *
         * @param  viewName     DOCUMENT ME!
         * @param  metaService  DOCUMENT ME!
         */
        public ViewRefresher(final String viewName, final DomainServerImpl metaService) {
            this.viewName = viewName;
            this.metaService = metaService;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            final long startRun = System.currentTimeMillis();
            Connection con = null;

            try {
                con = metaService.getConnectionPool().getConnection(true);
                final PreparedStatement stat = con.prepareStatement("select cs_recreateCachedDaqView(?)");
                stat.setString(1, viewName);
                stat.execute();
            } catch (SQLException e) {
                LOG.error("Error while refreshig cached table.", e);
            } finally {
                if (con != null) {
                    metaService.getConnectionPool().releaseDbConnection(con);
                }
            }

            final long timeToRun = System.currentTimeMillis() - startRun;
            lastRefreshTimes.put(viewName, timeToRun);
            currentlyRunningRefreshs.remove(viewName);
        }
    }
}
