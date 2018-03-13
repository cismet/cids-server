/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger.builtin;

import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.messages.CidsServerMessageManagerImpl;

import de.cismet.cids.trigger.AbstractCidsTrigger;
import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;

import de.cismet.commons.concurrency.CismetConcurrency;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CidsTrigger.class)
public class LwmoCacheInvalidationTrigger extends AbstractCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            LwmoCacheInvalidationTrigger.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsTriggerKey getTriggerKey() {
        return CidsTriggerKey.FORALL;
    }

    @Override
    public void afterInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        if (isCacheEnabled(cidsBean)) {
            sendLwmoCacheInvalidationMessage(cidsBean);
        }
    }

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        if (isCacheEnabled(cidsBean)) {
            sendLwmoCacheInvalidationMessage(cidsBean);
        }
    }

    @Override
    public void beforeInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeDelete(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterCommittedInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterCommittedUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterCommittedDelete(final CidsBean cidsBean, final User user) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final CidsTrigger o) {
        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean isCacheEnabled(final CidsBean cidsBean) {
        if (cidsBean == null) {
            return false;
        } else {
            return (cidsBean.getMetaObject().getMetaClass().getClassAttribute("CACHEHINT") != null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     */
    private void sendLwmoCacheInvalidationMessage(final CidsBean cidsBean) {
        if (cidsBean != null) {
            CismetConcurrency.getInstance("CidsTrigger")
                    .getDefaultExecutor()
                    .execute(new javax.swing.SwingWorker<Void, Void>() {

                            @Override
                            protected Void doInBackground() throws Exception {
                                CidsServerMessageManagerImpl.getInstance()
                                .publishMessage(
                                    LightweightMetaObject.CACHE_INVALIDATION_MESSAGE,
                                    new MetaObjectNode(cidsBean),
                                    false,
                                    ConnectionContext.createDeprecated());
                                return null;
                            }
                        });
        }
    }
}
