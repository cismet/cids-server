package Sirius.server.middleware.impls.domainserver;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cismet.cids.server.CallServerService;
import de.cismet.cids.utils.MetaClassCacheService;
import de.cismet.cidsx.server.api.types.CidsClass;
import de.cismet.cidsx.server.api.types.legacy.CidsClassFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import org.apache.log4j.Logger;

/**
 * Offline Meta Class Cache Service initialized with MetaClasses loadeded from
 * JSON CidsBeanInfo objects
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@org.openide.util.lookup.ServiceProvider(
        service = MetaClassCacheService.class,
        supersedes = {"Sirius.server.middleware.impls.domainserver.DomainServerMetaClassService"})
public class OfflineMetaClassCacheService implements MetaClassCacheService {

    protected final static String CLASSES_JSON_PACKAGE = "de/cismet/cids/dynamics/classes/";

    protected final static HashMap<Integer, MetaClass> ALL_CLASSES_BY_ID = new HashMap<Integer, MetaClass>();
    protected final static HashMap<String, MetaClass> ALL_CLASSES_BY_TABLE_NAME = new HashMap<String, MetaClass>();

    protected final static Logger LOGGER = Logger.getLogger(OfflineMetaClassCacheService.class);
    protected static final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());

    protected boolean online = false;

    public OfflineMetaClassCacheService() {

        if (ALL_CLASSES_BY_ID.isEmpty() && ALL_CLASSES_BY_TABLE_NAME.isEmpty()) {
            LOGGER.info("loading meta classes");
            try {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                final URL resources = classLoader.getResource(CLASSES_JSON_PACKAGE);

                final Scanner scanner = new Scanner((InputStream) resources.getContent()).useDelimiter("\\n");
                while (scanner.hasNext()) {
                    final String jsonFile = CLASSES_JSON_PACKAGE + scanner.next();
                    LOGGER.info("loading cids class from json file " + jsonFile);
                    try {

                        final CidsClass cidsClass = MAPPER.readValue(
                                new BufferedReader(
                                        new InputStreamReader(classLoader.getResourceAsStream(jsonFile))),
                                CidsClass.class);
                        LOGGER.debug(cidsClass.getKey() + " deserialized");
                        final MetaClass metaClass = CidsClassFactory.getFactory().legacyCidsClassFromRestCidsClass(cidsClass);
                        ALL_CLASSES_BY_ID.put(metaClass.getId(), metaClass);
                        ALL_CLASSES_BY_TABLE_NAME.put(metaClass.getTableName(), metaClass);

                    } catch (Exception ex) {
                        LOGGER.error("could not deserialize cids class from url " + jsonFile, ex);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("could not locate meta class json files: " + ex.getMessage(), ex);
            }

            LOGGER.info(ALL_CLASSES_BY_ID.size() + " meta classes loaded");

        } else {
            LOGGER.info("meta classes already loaded");
        }
    }

    @Override
    public MetaClass getMetaClass(final String domain, final String tableName) {
        return ALL_CLASSES_BY_TABLE_NAME.get(tableName);
    }

    @Override
    public MetaClass getMetaClass(final String domain, final int classId) {
        return ALL_CLASSES_BY_ID.get(classId);
    }

    @Override
    public HashMap<String, MetaClass> getAllClasses(final String domain) {
        final HashMap<String, MetaClass> allClasses = new HashMap<String, MetaClass>();
        for (Integer classId : ALL_CLASSES_BY_ID.keySet()) {
            final String classKey = domain + classId;
            allClasses.put(classKey, ALL_CLASSES_BY_ID.get(classId));
        }

        return allClasses;
    }

    private static final class LazyInitialiser {

        private static final OfflineMetaClassCacheService INSTANCE = new OfflineMetaClassCacheService();

        private LazyInitialiser() {
        }
    }

    public static OfflineMetaClassCacheService getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * Makes OfflineMetaClassCacheService to OnlineMetaClassCacheService when
     * server connection is available.
     *
     * @param user
     * @param connector
     */
    public void updateFromServer(final User user,
            final CallServerService connector) {
        int i = 0;
        for (final MetaClass offlineMetaClass : ALL_CLASSES_BY_ID.values()) {
            try {
                final MetaClass onlineMetaClass = connector.getClass(user,
                        offlineMetaClass.getID(),
                        offlineMetaClass.getDomain());

                if (onlineMetaClass != null) {
                    if ((ALL_CLASSES_BY_ID.put(onlineMetaClass.getID(), onlineMetaClass) != null)
                            && (ALL_CLASSES_BY_TABLE_NAME.put(onlineMetaClass.getTableName(), onlineMetaClass) != null)) {
                        i++;
                    }

                }
            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }

        if (i == ALL_CLASSES_BY_ID.size()) {
            LOGGER.info("OfflineMetaClassCacheService updated with " + i + " of "
                    + ALL_CLASSES_BY_ID.size() + " MetaClasses from Server");
            this.online = true;
        } else {
            LOGGER.warn("OfflineMetaClassCacheService updated with only " + i + " of "
                    + ALL_CLASSES_BY_ID.size() + " MetaClasses from Server");
        }
    }

    public boolean isOnline() {
        return online;
    }
}
