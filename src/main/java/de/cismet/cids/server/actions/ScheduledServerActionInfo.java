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

import java.io.Serializable;

import java.util.Date;
import java.util.Timer;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ScheduledServerActionInfo implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private final int id;
    private final String key;
    private final String taskName;
    private final String userName;
    private final String groupName;
    private final Object body;
    private final ServerActionParameter[] params;
    private final String scheduleRule;
    private final Date offsetDate;

//    private final transient ScheduledServerAction serverAction;

    private transient Timer timer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ScheduledServerActionInfo object.
     *
     * @param  id            DOCUMENT ME!
     * @param  key           DOCUMENT ME!
     * @param  taskName      DOCUMENT ME!
     * @param  userName      serverAction DOCUMENT ME!
     * @param  groupName     DOCUMENT ME!
     * @param  body          DOCUMENT ME!
     * @param  params        DOCUMENT ME!
     * @param  offsetDate    DOCUMENT ME!
     * @param  scheduleRule  DOCUMENT ME!
     */
    public ScheduledServerActionInfo(final int id,
            final String key,
//            final ScheduledServerAction serverAction,
            final String taskName,
            final String userName,
            final String groupName,
            final Object body,
            final ServerActionParameter[] params,
            final Date offsetDate,
            final String scheduleRule) {
        this.id = id;
        this.taskName = taskName;
        this.userName = userName;
        this.groupName = groupName;
        this.key = key;
//        this.serverAction = serverAction;
        this.body = body;
        this.params = params;
        this.offsetDate = offsetDate;
        this.scheduleRule = scheduleRule;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getKey() {
        return key;
    }

    /**
     * /** * DOCUMENT ME! * * @return DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserName() {
        return userName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getBody() {
        return body;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerActionParameter[] getParams() {
        return params;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getScheduleRule() {
        return scheduleRule;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getOffsetDate() {
        return offsetDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  timer  DOCUMENT ME!
     */
    public void setTimer(final Timer timer) {
        this.timer = timer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGroupName() {
        return groupName;
    }
}
