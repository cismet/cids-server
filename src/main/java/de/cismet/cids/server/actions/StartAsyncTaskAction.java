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

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class StartAsyncTaskAction extends DefaultServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(StartAsyncTaskAction.class);

    public static final String TASK_NAME = "startAsyncTask";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        TASKNAME
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... saps) {
        if (getMetaService() instanceof ActionService) {
            final ActionService as = (ActionService)getMetaService();
            String taskname = null;
            final List<ServerActionParameter> paraList = new ArrayList<ServerActionParameter>();

            if (saps != null) {
                for (final ServerActionParameter sap : saps) {
                    if (sap.getKey().equals(Parameter.TASKNAME.toString())) {
                        taskname = (String)sap.getValue();
                    } else {
                        paraList.add(sap);
                    }
                }
            }

            if (taskname != null) {
                final AsyncActionManager actionManager = AsyncActionManager.getInstance();
                return actionManager.startAsyncAction(
                        as,
                        getUser(),
                        taskname,
                        body,
                        paraList.toArray(new ServerActionParameter[paraList.size()]));
            }
        }

        return null;
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }
}
