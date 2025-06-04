/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import org.apache.log4j.Logger;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class DebuggingOnlySleepAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DebuggingOnlySleepAction.class);
    private static final ConnectionContext CC = ConnectionContext.create(
            ConnectionContext.Category.ACTION,
            "DebuggingSleep");

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        DELAY
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GraphqlAction object.
     */
    public DebuggingOnlySleepAction() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return "debuggingOnlySleep";
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        long delay = 0;
        LOG.info("start" + System.currentTimeMillis());

        for (final ServerActionParameter sap : params) {
            if (sap.getKey().equalsIgnoreCase(DebuggingOnlySleepAction.PARAMETER_TYPE.DELAY.toString())) {
                if (sap.getValue() instanceof Number) {
                    delay = ((Number)sap.getValue()).longValue();
                } else {
                    try {
                        delay = Long.parseLong(String.valueOf(sap.getValue()));
                    } catch (NumberFormatException e) {
                        LOG.error("Cannot parse delay: " + String.valueOf(sap.getValue()), e);
                    }
                }
            }
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            LOG.error("InterruptedException ", ex);
        }

        LOG.info("end: " + System.currentTimeMillis());
        return "{}";
    }
}
