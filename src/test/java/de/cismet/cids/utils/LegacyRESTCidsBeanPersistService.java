package de.cismet.cids.utils;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import org.apache.log4j.Logger;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@org.openide.util.lookup.ServiceProvider(service = CidsBeanPersistService.class)
public class LegacyRESTCidsBeanPersistService implements CidsBeanPersistService {

    protected final static Logger LOGGER = Logger.getLogger(LegacyRESTCidsBeanPersistService.class);
    protected RESTfulSerialInterfaceConnector connector = null;
    protected User user = null;

    public LegacyRESTCidsBeanPersistService() {
        // lookup.ServiceProvider class the constructor, LazyInitialiser calls it too!
        // -> delegate all calls to getInstance() so that lookup.ServiceProvider
        // used the methods/properties of the instance created by LazyInitialiser
        LOGGER.info("new LegacyRESTCidsBeanPersistService created");
    }

    public RESTfulSerialInterfaceConnector getConnector() {
        return getInstance().connector;
    }

    public void setConnector(RESTfulSerialInterfaceConnector connector) {
        getInstance().connector = connector;
    }

    public User getUser() {
        return getInstance().user;
    }

    public void setUser(User user) {
        getInstance().user = user;
    }

    @Override
    public CidsBean persistCidsBean(final CidsBean cidsBean) throws Exception, AssertionError {

        assert getInstance().isOnline() : "illegal state: call to persistCidsBean while LegacyRESTCidsBeanPersistService is offline"; // NOI18N

        final MetaObject metaObject = cidsBean.getMetaObject();
        final String domain = metaObject.getDomain();

        LOGGER.debug("persisting CidsBean '" + metaObject.getName() + "' ("
                + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ")");

        switch (metaObject.getStatus()) {
            case MetaObject.MODIFIED:
                getInstance().connector.updateMetaObject(getInstance().user, metaObject, domain);
                return connector
                        .getMetaObject(getInstance().user, metaObject.getID(), metaObject.getClassID(), domain)
                        .getBean();
                
            case MetaObject.TO_DELETE:
                getInstance().connector.deleteMetaObject(getInstance().user, metaObject, domain);
                return null;
                
            case MetaObject.NEW:
                final MetaObject mo = getInstance().connector.insertMetaObject(getInstance().user, metaObject, domain);
                if (mo == null) {
                    throw new Exception("illegal state: insert metaobject returned null");
                }
                return mo.getBean();
                
            default:
                throw new Exception("nothing to do, persist was called on CidsBean " + metaObject.getName() + "' ("
                        + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") that has not been modified: "
                        + metaObject.getStatusDebugString());
        }
    }

    public boolean isOnline() {
        return getInstance().connector != null && getInstance().user != null;
    }

    public static LegacyRESTCidsBeanPersistService getInstance() {
        return LegacyRESTCidsBeanPersistService.LazyInitialiser.INSTANCE;
    }

    private static final class LazyInitialiser {

        private static final LegacyRESTCidsBeanPersistService INSTANCE = new LegacyRESTCidsBeanPersistService();

        private LazyInitialiser() {
        }
    }

}
