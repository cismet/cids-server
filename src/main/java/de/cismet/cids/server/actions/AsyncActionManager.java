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
package de.cismet.cids.server.actions;

import Sirius.server.middleware.interfaces.domainserver.ActionService;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import de.cismet.cids.utils.AsyncActionConfig;
import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AsyncActionManager {

    //~ Static fields/initializers ---------------------------------------------

    private static final int MAX_PARALLEL_THREADS = 10;
    private static final Logger LOG = Logger.getLogger(AsyncActionManager.class);

    //~ Instance fields --------------------------------------------------------

    private final Map<String, AsyncActionProgressListener> taskMap = Collections.synchronizedMap(new HashMap<>());
    private final String folderNameForResults;
    private final ExecutorService executor;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AsyncActionManager object.
     */
    private AsyncActionManager() {
        String tmp = "/tmp/";

        try {
            final AsyncActionConfig config = ServerResourcesLoader.getInstance()
                        .loadJson(GeneralServerResources.CONFIG_ASYNC_ACTION_JSON.getValue(),
                            AsyncActionConfig.class);
            tmp = config.getTmpFilePath();
        } catch (Exception e) {
            LOG.error("Cannot read AsyncActionManager configuration", e);
        }
        folderNameForResults = tmp;
        executor = CismetExecutors.newFixedThreadPool(
                MAX_PARALLEL_THREADS,
                CismetConcurrency.getInstance("AsyncActionThread").createThreadFactory("AsyncActionThread"));
        final Timer cleanupCheck = new Timer("async action cleanup timer", true);

        cleanupCheck.scheduleAtFixedRate(new ResultCleaner(taskMap), 60000, 60000);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AsyncActionManager getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   as        DOCUMENT ME!
     * @param   user      DOCUMENT ME!
     * @param   taskname  DOCUMENT ME!
     * @param   body      DOCUMENT ME!
     * @param   saps      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String startAsyncAction(final ActionService as,
            final User user,
            final String taskname,
            final Object body,
            final ServerActionParameter[] saps) {
        final AsyncActionManagerProgressListener listener = new AsyncActionManagerProgressListener(
                folderNameForResults);
        final ActionTask task = new ActionTask(as, user, taskname, body, saps, listener);
        taskMap.put(task.getUuid(), listener);
        executor.submit(task);

        return task.getUuid();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uuid  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getProgress(final String uuid) {
        if (taskMap.get(uuid) != null) {
            return taskMap.get(uuid).getProgress();
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uuid  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getResult(final String uuid) {
        if (taskMap.get(uuid) != null) {
            final Object result = taskMap.get(uuid).getResult();
            taskMap.get(uuid).dispose();
            taskMap.remove(uuid);

            return result;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  uuid  DOCUMENT ME!
     */
    public void deleteAction(final String uuid) {
        if (taskMap.get(uuid) != null) {
            taskMap.get(uuid).dispose();
            taskMap.remove(uuid);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final AsyncActionManager INSTANCE = new AsyncActionManager();

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
    private static class ActionTask implements Runnable {

        //~ Static fields/initializers -----------------------------------------

        private static final ConnectionContext CC = ConnectionContext.create(
                AbstractConnectionContext.Category.ACTION,
                "AsyncActionManager");
        private static final Logger LOG = Logger.getLogger(ActionTask.class);

        //~ Instance fields ----------------------------------------------------

        private final ActionService as;
        private final User user;
        private final String taskname;
        private final Object body;
        private final ServerActionParameter[] saps;
        private final String uuid;
        private final AsyncActionProgressListener listener;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ActionTask object.
         *
         * @param  as        DOCUMENT ME!
         * @param  user      DOCUMENT ME!
         * @param  taskname  DOCUMENT ME!
         * @param  body      DOCUMENT ME!
         * @param  saps      DOCUMENT ME!
         * @param  listener  DOCUMENT ME!
         */
        public ActionTask(final ActionService as,
                final User user,
                final String taskname,
                final Object body,
                final ServerActionParameter[] saps,
                final AsyncActionProgressListener listener) {
            this.as = as;
            this.user = user;
            this.taskname = taskname;
            this.body = body;
            this.saps = saps;
            this.listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            try {
                listener.setProgress(-1);
                final Object result = as.executeTask(user, taskname, body, CC, saps);

                if (listener != null) {
                    listener.setResult(result);
                    listener.setProgress(100);
                }
            } catch (Exception e) {
                LOG.error("Error while executing action", e);
                if (listener != null) {
                    listener.setResult(e);
                    listener.setProgress(100);
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getUuid() {
            return uuid;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class AsyncActionManagerProgressListener implements AsyncActionProgressListener {

        //~ Static fields/initializers -----------------------------------------

        private static final Logger LOG = Logger.getLogger(AsyncActionManagerProgressListener.class);

        //~ Instance fields ----------------------------------------------------

        private final String folder;
        private int progress = -1;
        private boolean cancelled = false;
        private boolean resultReady = false;
        private File resultFile;
        private Date endTime = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AsyncActionManagerProgressListener object.
         *
         * @param  folder  DOCUMENT ME!
         */
        public AsyncActionManagerProgressListener(final String folder) {
            this.folder = folder;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void setProgress(final int percentage) {
            progress = percentage;
        }

        @Override
        public void setResult(final Object result) {
            if (!cancelled) {
                ObjectOutputStream oos = null;

                try {
                    resultFile = File.createTempFile("asyncAction", null, new File(folder));
                    oos = new ObjectOutputStream(new BufferedOutputStream(
                                new FileOutputStream(resultFile)));
                    oos.writeObject(result);
                } catch (Exception e) {
                    LOG.error("Cannot write Action result to file", e);
                } finally {
                    if (oos != null) {
                        try {
                            oos.close();
                        } catch (IOException ex) {
                            LOG.error("cannot close stream", ex);
                        }
                    }
                }

                endTime = new Date();
                progress = 100;
                resultReady = true;
            }
        }

        @Override
        public Object getResult() {
            while (!resultReady) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // nothing to do
                }
            }

            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new BufferedInputStream(
                            new FileInputStream(resultFile)));
                final Object result = ois.readObject();
                ois.close();

                return result;
            } catch (Exception e) {
                LOG.error("Cannot write Action result to file", e);
                return null;
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException ex) {
                        LOG.error("cannot close stream", ex);
                    }
                }
            }
        }

        @Override
        public int getProgress() {
            return progress;
        }

        @Override
        public void dispose() {
            cancelled = true;

            if ((resultFile != null) && resultFile.exists()) {
                resultFile.delete();
            }
        }

        @Override
        public Date getResultDate() {
            return endTime;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class ResultCleaner extends TimerTask {

        //~ Static fields/initializers -----------------------------------------

        private static long MS_PER_Minute = 60 * 1000;

        //~ Instance fields ----------------------------------------------------

        private Map<String, AsyncActionProgressListener> taskMap;
        private long minutesToKeep = 60;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ResultCleaner object.
         *
         * @param  taskMap  DOCUMENT ME!
         */
        public ResultCleaner(final Map<String, AsyncActionProgressListener> taskMap) {
            this.taskMap = taskMap;
        }

        /**
         * Creates a new ResultCleaner object.
         *
         * @param  taskMap        DOCUMENT ME!
         * @param  minutesToKeep  DOCUMENT ME!
         */
        public ResultCleaner(final Map<String, AsyncActionProgressListener> taskMap, final long minutesToKeep) {
            this.taskMap = taskMap;
            this.minutesToKeep = minutesToKeep;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            for (final String uuid : new ArrayList<>(taskMap.keySet())) {
                final AsyncActionProgressListener listener = taskMap.get(uuid);

                if (listener != null) {
                    final Date time = listener.getResultDate();

                    if (time != null) {
                        if ((time.getTime() + (minutesToKeep * MS_PER_Minute)) < System.currentTimeMillis()) {
                            taskMap.remove(uuid);
                            listener.dispose();
                        }
                    }
                }
            }
        }
    }
}
