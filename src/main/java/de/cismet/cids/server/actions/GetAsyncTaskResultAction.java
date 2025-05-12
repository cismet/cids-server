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

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class GetAsyncTaskResultAction extends DefaultServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GetAsyncTaskResultAction.class);

    public static final String TASK_NAME = "getAsyncTaskResult";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        UUID
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... saps) {
        if (getMetaService() instanceof ActionService) {
            final ActionService as = (ActionService)getMetaService();
            String uuid = null;

            if (saps != null) {
                for (final ServerActionParameter sap : saps) {
                    if (sap.getKey().equals(Parameter.UUID.toString())) {
                        uuid = (String)sap.getValue();
                    }
                }
            }

            if (uuid != null) {
                final AsyncActionManager actionManager = AsyncActionManager.getInstance();
                return actionManager.getResult(uuid);
            }
        }

        return null;
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }
}
