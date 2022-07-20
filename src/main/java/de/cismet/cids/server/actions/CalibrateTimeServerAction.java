/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.proxy.TimeCalibrationHandler;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class CalibrateTimeServerAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final String TASK_NAME = "calibrateTime";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        return System.currentTimeMillis();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   taskName    DOCUMENT ME!
     * @param   taskDomain  DOCUMENT ME!
     * @param   result      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object calibrate(final String taskName, final String taskDomain, final Object result) {
        if (CalibrateTimeServerAction.TASK_NAME.equals(taskName) && (result instanceof Long)) {
            return TimeCalibrationHandler.getInstance().calibrate(taskDomain, (Long)result);
        } else {
            return result;
        }
    }
}
