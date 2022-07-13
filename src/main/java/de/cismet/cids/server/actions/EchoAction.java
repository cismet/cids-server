/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class EchoAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(EchoAction.class);

    public static final String TASK_NAME = "echo";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final StringBuilder paramsString = new StringBuilder("{");
        boolean firstParam = true;

        for (final ServerActionParameter sap : params) {
            if (!firstParam) {
                paramsString.append(", ");
            } else {
                firstParam = false;
            }
            paramsString.append("\"")
                    .append(sap.getKey())
                    .append("\": ")
                    .append((sap.getValue() instanceof String) ? "\"" : "")
                    .append(sap.getValue())
                    .append((sap.getValue() instanceof String) ? "\"" : "");
        }

        paramsString.append("}");

        return paramsString.toString();
    }
}
