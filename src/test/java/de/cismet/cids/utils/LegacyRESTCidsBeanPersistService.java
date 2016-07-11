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
        return LegacyRESTCidsBeanPersistService.getInstance().connector;
    }

    public void setConnector(RESTfulSerialInterfaceConnector connector) {
        LegacyRESTCidsBeanPersistService.getInstance().connector = connector;
    }

    public User getUser() {
        return LegacyRESTCidsBeanPersistService.getInstance().user;
    }

    public void setUser(User user) {
        LegacyRESTCidsBeanPersistService.getInstance().user = user;
    }

    @Override
    public CidsBean persistCidsBean(final CidsBean cidsBean) throws Exception, AssertionError {

        assert LegacyRESTCidsBeanPersistService.getInstance().isOnline() : "illegal state: call to persistCidsBean while LegacyRESTCidsBeanPersistService is offline"; // NOI18N

        final MetaObject metaObject = cidsBean.getMetaObject();
        final String domain = metaObject.getDomain();

        LOGGER.debug("persisting CidsBean '" + metaObject.getName() + "' ("
                + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ")");

        switch (metaObject.getStatus()) {
            case MetaObject.MODIFIED:
                LegacyRESTCidsBeanPersistService.getInstance().connector.updateMetaObject(
                        LegacyRESTCidsBeanPersistService.getInstance().user,
                        metaObject,
                        domain);

                final MetaObject updatedMetaObject
                        = LegacyRESTCidsBeanPersistService.getInstance().connector.getMetaObject(
                                LegacyRESTCidsBeanPersistService.getInstance().user,
                                metaObject.getID(),
                                metaObject.getClassID(),
                                domain);

                assert updatedMetaObject != null : "MetaObject '" + metaObject.getName() + "' ("
                        + metaObject.getKey() + "' successfully updated";

                return updatedMetaObject.getBean();

            case MetaObject.TO_DELETE:
                LegacyRESTCidsBeanPersistService.getInstance().connector.deleteMetaObject(
                        LegacyRESTCidsBeanPersistService.getInstance().user,
                        metaObject, domain);
                return null;

            case MetaObject.NEW:
                final MetaObject insertedMetaObject
                        = LegacyRESTCidsBeanPersistService.getInstance().connector.insertMetaObject(
                                LegacyRESTCidsBeanPersistService.getInstance().user,
                                metaObject,
                                domain);

                assert insertedMetaObject != null : "MetaObject '" + metaObject.getName() + "' ("
                        + metaObject.getKey() + "' successfully inserted";

                return insertedMetaObject.getBean();

            default:
                throw new Exception("nothing to do, persist was called on CidsBean " + metaObject.getName() + "' ("
                        + cidsBean.getCidsBeanInfo().getJsonObjectKey() + ") that has not been modified: "
                        + metaObject.getStatusDebugString());
        }
    }

    public boolean isOnline() {
        return LegacyRESTCidsBeanPersistService.getInstance().connector != null && getInstance().user != null;
    }

    public static LegacyRESTCidsBeanPersistService getInstance() {
        return LegacyRESTCidsBeanPersistService.LazyInitialiser.INSTANCE;
    }

    private static final class LazyInitialiser {

        private static final LegacyRESTCidsBeanPersistService INSTANCE
                = new LegacyRESTCidsBeanPersistService();

        private LazyInitialiser() {
        }
    }

}
