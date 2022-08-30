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

import Sirius.server.newuser.User;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Date;

import de.cismet.cids.utils.UncaughtClientExceptionConfig;
import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class UncaughtClientExceptionServerAction implements UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final String TASK_NAME = "uncaughtClientException";
    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            UncaughtClientExceptionServerAction.class);

    //~ Instance fields --------------------------------------------------------

    private User user;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        try {
            writeToLog((Throwable)body);
            return null;
        } catch (final Exception ex) {
            LOG.info("error while logging UncaughtClientException", ex);
            return ex;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  clientEx  DOCUMENT ME!
     */
    private void writeToLog(final Throwable clientEx) {
        try {
            final UncaughtClientExceptionConfig config = ServerResourcesLoader.getInstance()
                        .loadJson(GeneralServerResources.CONFIG_UNCAUGHT_CLIENT_EXCEPTION_JSON.getValue(),
                            UncaughtClientExceptionConfig.class);
            final Date now = new Date();
            if ((config != null)) {
                final File logDirectory = new File(config.getLogDirectory());
                final SimpleDateFormat logFileDateFormat = new SimpleDateFormat(config.getLogFileDateFormat());
                final File logFile = new File(logDirectory, String.format("%s.log", logFileDateFormat.format(now)));
                final PrintWriter execptionWriter = new PrintWriter(new FileWriter(logFile, true));
                final SimpleDateFormat logMessageDateFormat = new SimpleDateFormat(config.getLogMessageDateFormat());
                final String message = String.format(config.getLogMessage(),
                        logMessageDateFormat.format(now),
                        getUser().getName());
                execptionWriter.write(message);
                clientEx.printStackTrace(execptionWriter);
                execptionWriter.flush();
            } else {
                LOG.info("uncaught client exception", clientEx);
            }
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        }
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }
}
