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

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserServer;
import Sirius.server.sql.DBConnection;

import org.quartz.CronExpression;

import java.io.IOException;

import java.rmi.RemoteException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ScheduledServerActionManager {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            ScheduledServerActionManager.class);

    public static final String SSA_TABLE = "cs_scheduled_serveractions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_TASKNAME = "taskname";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_GROUPNAME = "groupname";
    public static final String COLUMN_BODY = "body_json";
    public static final String COLUMN_PARAMS = "params_json";
    public static final String COLUMN_START = "start_timestamp";
    public static final String COLUMN_RULE = "execution_rule";
    public static final String COLUMN_EXECUTION = "execution_timestamp";
    public static final String COLUMN_ABORTED = "aborted";
    public static final String COLUMN_RESULT = "result_json";

    //~ Instance fields --------------------------------------------------------

    private Connection con;

    private PreparedStatement maxId;

    private final HashMap<String, ScheduledServerActionInfo> ssaInfoMap =
        new HashMap<String, ScheduledServerActionInfo>();

    private final UserServer userServer;
    private final DomainServerImpl domainserver;
    private final String domain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ScheduledServerActionManager object.
     *
     * @param  domainserver  DOCUMENT ME!
     * @param  dbConnection  DOCUMENT ME!
     * @param  userServer    DOCUMENT ME!
     * @param  domain        DOCUMENT ME!
     */
    public ScheduledServerActionManager(final DomainServerImpl domainserver,
            final DBConnection dbConnection,
            final UserServer userServer,
            final String domain) {
        try {
            con = dbConnection.getConnection();
            maxId = con.prepareStatement("SELECT max(" + COLUMN_ID + ") FROM " + SSA_TABLE + ""); // NOI18N
        } catch (final SQLException ex) {
            LOG.error(ex, ex);
        }
        this.userServer = userServer;
        this.domainserver = domainserver;
        this.domain = domain;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void resumeAll() {
        try {
            final List<String> keys = getResumableKeys();

            for (final String key : keys) {
                LOG.info("resuming ScheduledServerAction: " + keys);
                final ScheduledServerActionInfo info = getInfoByKey(key);
                final List<ServerActionParameter> list = new ArrayList<ServerActionParameter>();
                list.add(new ServerActionParameter(ScheduledServerAction.SSAPK_DELAY, info.getOffsetDate()));
                list.add(new ServerActionParameter(ScheduledServerAction.SSAPK_RULE, info.getScheduleRule()));
                list.addAll(Arrays.asList(info.getParams()));
                domainserver.executeTask(getUserByName(info.getUserName(), info.getGroupName()),
                    info.getTaskName(),
                    info.getBody(),
                    list.toArray(new ServerActionParameter[0]));
            }
        } catch (final Exception ex) {
            LOG.warn(ex, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   username   DOCUMENT ME!
     * @param   groupname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UserException    DOCUMENT ME!
     * @throws  RemoteException  DOCUMENT ME!
     */
    private User getUserByName(final String username, final String groupname) throws UserException, RemoteException {
        final User user = userServer.getUser(domain, groupname, domain, username, null);
        return user;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException            DOCUMENT ME!
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     * @throws  InstantiationException  DOCUMENT ME!
     * @throws  IllegalAccessException  DOCUMENT ME!
     */
    public ScheduledServerActionInfo getInfoByKey(final String key) throws SQLException,
        IOException,
        ClassNotFoundException,
        InstantiationException,
        IllegalAccessException {
        final Statement selectByKeyStatement = con.createStatement();
        final ResultSet rs = selectByKeyStatement.executeQuery("SELECT * FROM " + SSA_TABLE + " WHERE " + COLUMN_KEY
                        + " = '" + key
                        + "' AND " + COLUMN_EXECUTION + " IS NULL ORDER BY " + COLUMN_START + " ASC"); // NOI18N
        while (rs.next()) {
            final String taskName = rs.getString(COLUMN_TASKNAME);

            final ScheduledServerActionInfo info = new ScheduledServerActionInfo(
                    rs.getInt(COLUMN_ID),
                    key,
                    taskName,
                    rs.getString(COLUMN_USERNAME),
                    rs.getString(COLUMN_GROUPNAME),
                    getServerAction(taskName).jsonToBody(rs.getString(COLUMN_BODY)),
                    getServerAction(taskName).jsonToParams(rs.getString(COLUMN_PARAMS)),
                    rs.getTimestamp(COLUMN_START),
                    rs.getString(COLUMN_RULE));
            return info;
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     * @throws  IOException   DOCUMENT ME!
     */
    public List<String> getResumableKeys() throws SQLException, IOException {
        final Statement selectKeysStatement = con.createStatement();
        final ResultSet rs = selectKeysStatement.executeQuery("SELECT " + COLUMN_KEY + " FROM " + SSA_TABLE
                        + " WHERE " + COLUMN_EXECUTION + " IS NULL GROUP BY " + COLUMN_KEY); // NOI18N

        final List<String> keys = new ArrayList<String>();
        while (rs.next()) {
            keys.add(rs.getString(1));
        }
        return keys;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user          DOCUMENT ME!
     * @param   key           DOCUMENT ME!
     * @param   serverAction  DOCUMENT ME!
     * @param   body          DOCUMENT ME!
     * @param   origParams    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ScheduledServerActionInfo scheduleAction(final User user,
            final String key,
            final ScheduledServerAction serverAction,
            final Object body,
            final ServerActionParameter... origParams) throws Exception {
        final ScheduledServerActionInfo cancelInfo = cancelAction(key);

        Date offsetTime = new Date();
        String scheduleRule = null;
        Boolean abort = false;
        final List<ServerActionParameter> trunkedParams = new ArrayList<ServerActionParameter>();
        for (final ServerActionParameter param : origParams) {
            final String paramKey = param.getKey();
            final Object paramValue = param.getValue();
            if (ScheduledServerAction.SSAPK_DELAY.equals(paramKey)) {
                if (paramValue instanceof Date) {
                    offsetTime = (Date)paramValue;
                }
            } else if (ScheduledServerAction.SSAPK_RULE.equals(paramKey)) {
                if (paramValue instanceof String) {
                    scheduleRule = (String)paramValue;
                }
            } else if (ScheduledServerAction.SSAPK_ABORT.equals(paramKey)) {
                abort = true;
            } else {
                trunkedParams.add(param);
            }
        }

        if (abort) {
            return cancelInfo;
        }

        final ScheduledServerActionInfo info = createInfo(
                key,
                serverAction.getTaskName(),
                user.getName(),
                (user.getUserGroup() != null) ? user.getUserGroup().getName() : null,
                body,
                trunkedParams.toArray(new ServerActionParameter[0]),
                offsetTime,
                scheduleRule);
        return launch(info);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   info  key DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ParseException  DOCUMENT ME!
     */
    private ScheduledServerActionInfo launch(final ScheduledServerActionInfo info) throws ParseException {
        final Timer timer = launchTimer(info);
        info.setTimer(timer);

        return info;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key           DOCUMENT ME!
     * @param   taskName      serverAction DOCUMENT ME!
     * @param   userName      DOCUMENT ME!
     * @param   groupName     DOCUMENT ME!
     * @param   body          DOCUMENT ME!
     * @param   params        DOCUMENT ME!
     * @param   offsetTime    DOCUMENT ME!
     * @param   scheduleRule  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public synchronized ScheduledServerActionInfo createInfo(final String key,
            final String taskName,
            final String userName,
            final String groupName,
            final Object body,
            final ServerActionParameter[] params,
            final Date offsetTime,
            final String scheduleRule) throws Exception {
        final ScheduledServerActionInfo info = new ScheduledServerActionInfo(
                getMaxId(),
                key,
                taskName,
                userName,
                groupName,
                body,
                params,
                offsetTime,
                scheduleRule);

        createDbEntry(info);

        return info;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   info  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ParseException  DOCUMENT ME!
     */
    private Timer launchTimer(final ScheduledServerActionInfo info) throws ParseException {
        synchronized (ssaInfoMap) {
            ssaInfoMap.put(info.getKey(), info);
        }

        final Date timeToExecute = calculateExecutionDate(info.getOffsetDate(), info.getScheduleRule());
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    final Object result = getServerAction(info.getTaskName()).execute(info.getBody(), info.getParams());
                    timerFinished(info, result);
                }
            }, timeToExecute);
        return timer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  info    DOCUMENT ME!
     * @param  result  DOCUMENT ME!
     */
    private void timerFinished(final ScheduledServerActionInfo info, final Object result) {
        try {
            final Date now = new Date();
            updateDbEntry(info.getId(), now, getServerAction(info.getTaskName()).resultToJson(result), false);

            synchronized (ssaInfoMap) {
                ssaInfoMap.remove(info.getKey());
            }

            final ScheduledServerActionInfo newInfo = createInfo(info.getKey(),
                    info.getTaskName(),
                    info.getUserName(),
                    info.getGroupName(),
                    info.getBody(),
                    getServerAction(info.getTaskName()).getNextParams(info.getParams()),
                    info.getOffsetDate(),
                    info.getScheduleRule());
            launch(newInfo);
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   offsetDate     DOCUMENT ME!
     * @param   executionRule  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ParseException  DOCUMENT ME!
     */
    public Date calculateExecutionDate(final Date offsetDate, final String executionRule) throws ParseException {
        final CronExpression expr = new CronExpression(executionRule);
        return expr.getNextValidTimeAfter(new Date());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException            DOCUMENT ME!
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     * @throws  InstantiationException  DOCUMENT ME!
     * @throws  IllegalAccessException  DOCUMENT ME!
     */
    public ScheduledServerActionInfo cancelAction(final String key) throws SQLException,
        IOException,
        ClassNotFoundException,
        InstantiationException,
        IllegalAccessException {
        ScheduledServerActionInfo info = null;

        synchronized (ssaInfoMap) {
            info = ssaInfoMap.remove(key);
            if (info != null) {
                if (info.getTimer() != null) {
                    info.getTimer().cancel();
                }
            }
        }

        info = getInfoByKey(key);
        if (info != null) {
            updateDbEntry(info.getId(), new Date(), null, true);
        }

        return info;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   info  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void createDbEntry(final ScheduledServerActionInfo info) throws Exception {
        final String paramsJson = getServerAction(info.getTaskName()).paramsToJson(info.getParams());
        final String bodyJson = getServerAction(info.getTaskName()).bodyToJson(info.getBody());

        final String column_id = Integer.toString(info.getId());
        final String column_username = "'" + info.getUserName() + "'";
        final String column_groupname = "'" + info.getGroupName() + "'";
        final String column_taskname = "'" + info.getTaskName() + "'";
        final String column_key = "'" + info.getKey() + "'";
        final String column_body = (bodyJson != null) ? ("'" + bodyJson + "'") : "NULL";
        final String column_params = (paramsJson != null) ? ("'" + paramsJson + "'") : "NULL";
        final String column_start = "'" + new Timestamp(info.getOffsetDate().getTime()) + "'";
        final String column_rule = (info.getScheduleRule() != null) ? ("'" + info.getScheduleRule() + "'") : "NULL";
        final String stmnt = "INSERT INTO " + SSA_TABLE
                    + " ( "
                    + COLUMN_ID + ", "
                    + COLUMN_USERNAME + ", "
                    + COLUMN_GROUPNAME + ", "
                    + COLUMN_TASKNAME + ", "
                    + COLUMN_KEY + ", "
                    + COLUMN_BODY + ", "
                    + COLUMN_PARAMS + ", "
                    + COLUMN_START + ", "
                    + COLUMN_RULE + ""
                    + ") VALUES ("
                    + "" + column_id + ", "
                    + column_username + ", "
                    + column_groupname + ", "
                    + column_taskname + ", "
                    + column_key + ", "
                    + column_body + ", "
                    + column_params + ", "
                    + column_start + ", "
                    + column_rule + ")";
        final Statement insert = con.createStatement();
        insert.executeUpdate(stmnt);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   taskname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    final ScheduledServerAction getServerAction(final String taskname) {
        final ServerAction serverAction = domainserver.getServerActionByTaskname(taskname);
        if (serverAction instanceof ScheduledServerAction) {
            return (ScheduledServerAction)serverAction;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id                   DOCUMENT ME!
     * @param   executionFinishedAt  DOCUMENT ME!
     * @param   result_json          DOCUMENT ME!
     * @param   aborted              DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void updateDbEntry(final int id,
            final Date executionFinishedAt,
            final String result_json,
            final boolean aborted) throws SQLException {
        final String stmnt;
        final String column_execution = "'" + new Timestamp(executionFinishedAt.getTime()) + "'";
        final String column_aborted = Boolean.toString(aborted);
        final String column_result = (result_json != null) ? ("'" + result_json + "'") : "NULL";
        stmnt = "UPDATE " + SSA_TABLE + " SET "
                    + COLUMN_EXECUTION + " = " + column_execution + ", "
                    + COLUMN_ABORTED + " = " + column_aborted + ", "
                    + COLUMN_RESULT + " = " + column_result + " "
                    + "WHERE " + COLUMN_ID + " = " + id;

        final Statement insert = con.createStatement();
        insert.executeUpdate(stmnt);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private int getMaxId() throws SQLException {
        int max = 0;
        final ResultSet maxRs = maxId.executeQuery();

        if (maxRs.next()) {
            max = maxRs.getInt(1) + 1;
        }
        return max;
    }
}
