/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.proxy.TimeCalibrationHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class TraceRouteServerAction extends EchoAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final String TASK_NAME = "traceRoute";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final long currentTimeMs = System.currentTimeMillis();

        final List<ServerActionParameter> diffParams = new ArrayList<>(params.length);
        Long lastValue = currentTimeMs;
        for (int i = params.length - 1; i >= 0; i--) {
            final ServerActionParameter param = params[i];
            final Long value = (param.getValue() instanceof Long) ? (Long)param.getValue() : null;
            final Long diff = ((value != null) && (lastValue != null)) ? (lastValue - value) : null;
            diffParams.add(new ServerActionParameter(param.getKey(), diff));
            lastValue = value;
        }
        return super.execute(body, diffParams.toArray(new ServerActionParameter[0]));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverName  DOCUMENT ME!
     * @param   taskName    DOCUMENT ME!
     * @param   domainName  DOCUMENT ME!
     * @param   params      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerActionParameter[] extendParams(final String serverName,
            final String taskName,
            final String domainName,
            final ServerActionParameter... params) {
        if (TraceRouteServerAction.TASK_NAME.equals(taskName) && (params != null)) {
            final List<ServerActionParameter> extendedParams = new ArrayList<>(Arrays.asList(params));

            final Long calibrateTime = TimeCalibrationHandler.getInstance().getCalibratedTime(domainName);
            extendedParams.add(new ServerActionParameter<>(serverName, calibrateTime));
            return extendedParams.toArray(new ServerActionParameter[0]);
        } else {
            return params;
        }
    }
}
