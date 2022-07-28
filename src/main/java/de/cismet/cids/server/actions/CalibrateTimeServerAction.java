/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.impls.proxy.TimeCalibrationHandler;

import java.util.LinkedHashMap;
import java.util.Map;

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
        final Map<String, Long> map = new LinkedHashMap<>();
        map.put(DomainServerImpl.getServerProperties().getServerName(), System.currentTimeMillis());
        return map;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverName  DOCUMENT ME!
     * @param   taskName    DOCUMENT ME!
     * @param   taskDomain  DOCUMENT ME!
     * @param   result      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object calibrate(final String serverName,
            final String taskName,
            final String taskDomain,
            final Object result) {
        if (CalibrateTimeServerAction.TASK_NAME.equals(taskName) && (result instanceof Map)) {
            final Map<String, Long> map = (Map)result;
            final Long domainTime = map.get(taskDomain);
            map.put(serverName, TimeCalibrationHandler.getInstance().calibrate(taskDomain, domainTime));
            return map;
        } else {
            return result;
        }
    }
}
