/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.proxy.TimeCalibrationHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class TraceRouteServerAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final String TASK_NAME = "traceRoute";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final long timestamp = System.currentTimeMillis();

        final Map<String, Long> absoluteDelays = new LinkedHashMap<>();
        long lastValue = 0;
        for (int i = params.length - 1; i >= 0; i--) {
            final ServerActionParameter param = params[i];
            final Long value = (param.getValue() instanceof Long) ? (Long)param.getValue() : null;
            if (value != null) {
                final long delay = value - timestamp;
                absoluteDelays.put(param.getKey(), delay);
//                lastValue = value;
            }
        }

        final Map<String, Long> relativeDelays = new LinkedHashMap<>();
        for (final String key : absoluteDelays.keySet()) {
            final Long value = absoluteDelays.get(key);
            if (value != null) {
                final long delay = lastValue - value;
                relativeDelays.put(key, delay);
                lastValue = value;
            }
        }

        try {
            return new ObjectMapper().writeValueAsString(new TraceRouteInfo(timestamp, relativeDelays));
        } catch (final JsonProcessingException ex) {
            return null;
        }
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class TraceRouteInfo {

        //~ Instance fields ----------------------------------------------------

        private Long timestamp;
        private Map<String, Long> delays = new LinkedHashMap<>();
    }
}
