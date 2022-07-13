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
package Sirius.server.localserver.user;

import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ConfigAttrStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ConfigAttrStore.class);
    private static DBConnectionPool conPool;
    private static ServerProperties properties;

    //~ Instance fields --------------------------------------------------------

    private ConcurrentHashMap<String, CsAttrConfigTable> configAttrMapWithoutDomain = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CsAttrConfigTable> configAttrMapWithDomain = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CsAttrConfigExemptTable> configAttrMapExempt = new ConcurrentHashMap<>();
    private final Timer refreshTimer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigAttrStore object.
     */
    private ConfigAttrStore() {
        initialRefreshStore();
        refreshTimer = new Timer(true);
        refreshTimer.schedule(new Refresher(), 30000, 60000);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  conPool     DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    public static void initialise(final DBConnectionPool conPool, final ServerProperties properties) {
        ConfigAttrStore.conPool = conPool;
        ConfigAttrStore.properties = properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key   DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getConfigAttr(final String key, final User user) {
        final String mapKey = generateKey(user.getName(), null, key);

        final CsAttrConfigTable res = configAttrMapWithoutDomain.get(mapKey);

        if (res != null) {
            return res.getValue();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key        DOCUMENT ME!
     * @param   user       DOCUMENT ME!
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getConfigAttr(final String key, final User user, final UserGroup userGroup) {
        final String domain = userGroup.getDomain();

        if (properties.getServerName().equals(domain)) { // CHECKING NOW FOR BOTH ASSIGNEMENTS: LOCAL AND
                                                         // <SERVER_NAME>
            final String configAttr = getConfigAttr(key, user, userGroup, "LOCAL");

            if (configAttr != null) {
                return configAttr;
            }
        }

        final String configAttr = getConfigAttr(key, user, userGroup, domain);

        if (configAttr != null) {
            return configAttr;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key        DOCUMENT ME!
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getConfigAttr(final String key, final UserGroup userGroup) {
        if (userGroup != null) {
            final String userGroupDomain = userGroup.getDomain();

            if (properties.getServerName().equals(userGroup.getDomain())) { // CHECKING NOW FOR BOTH ASSIGNEMENTS:
                                                                            // LOCAL AND <SERVER_NAME>
                final String configAttr = getConfigAttr(key, userGroup, "LOCAL");
                if (configAttr != null) {
                    return configAttr;
                }
            }

            final String configAttr = getConfigAttr(key, userGroup, userGroupDomain);
            if (configAttr != null) {
                return configAttr;
            }
        }

        return null;
    }

    /**
     * ASSIGNED TO THE DOMAIN.
     *
     * @param   key     DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getConfigAttr(final String key, final String domain) {
        if (properties.getServerName().equals(domain)) { // CHECKING NOW FOR BOTH ASSIGNEMENTS: LOCAL AND <SERVER_NAME>
            final String configAttr = _getConfigAttr(key, "LOCAL");
            if (configAttr != null) {
                return configAttr;
            }
        }

        final String configAttr = _getConfigAttr(key, domain);
        if (configAttr != null) {
            return configAttr;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key     DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String _getConfigAttr(final String key, final String domain) {
        final String mapKey = generateKey(null, null, domain, key);

        final CsAttrConfigTable res = configAttrMapWithDomain.get(mapKey);

        if (res != null) {
            return res.getValue();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key        DOCUMENT ME!
     * @param   userGroup  DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getConfigAttr(final String key, final UserGroup userGroup, final String domain) {
        final String mapKey = generateKey(null, userGroup.getName(), domain, key);

        final CsAttrConfigTable res = configAttrMapWithDomain.get(mapKey);

        if (res != null) {
            return res.getValue();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key        DOCUMENT ME!
     * @param   user       DOCUMENT ME!
     * @param   userGroup  DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getConfigAttr(final String key, final User user, final UserGroup userGroup, final String domain) {
        final String mapKey = generateKey(user.getName(), userGroup.getName(), domain, key);

        final CsAttrConfigTable res = configAttrMapWithDomain.get(mapKey);

        if (res != null) {
            return res.getValue();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   key   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getExempt(final User user, final String key) {
        final String mapKey = generateExemptKey(user.getName(), key);

        final CsAttrConfigExemptTable res = configAttrMapExempt.get(mapKey);

        if (res != null) {
            return res.getUgName();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConfigAttrStore getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshStore() {
        final Thread refreshThread = new Thread(new Refresher(), "Refresh ConfigAttrStore");
        refreshThread.run();
    }

    /**
     * DOCUMENT ME!
     */
    private void initialRefreshStore() {
        final Refresher r = new Refresher();
        r.refresh(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   loginName  DOCUMENT ME!
     * @param   ugName     DOCUMENT ME!
     * @param   key        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String generateKey(final String loginName, final String ugName, final String key) {
        return String.valueOf(loginName) + "@" + String.valueOf(ugName) + ":" + String.valueOf(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   loginName   DOCUMENT ME!
     * @param   ugName      DOCUMENT ME!
     * @param   domainName  DOCUMENT ME!
     * @param   key         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String generateKey(final String loginName,
            final String ugName,
            final String domainName,
            final String key) {
        return String.valueOf(loginName) + "@" + String.valueOf(ugName) + "@" + String.valueOf(domainName) + ":"
                    + String.valueOf(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   loginName  DOCUMENT ME!
     * @param   key        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String generateExemptKey(final String loginName, final String key) {
        return String.valueOf(loginName) + ":" + String.valueOf(key);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final ConfigAttrStore INSTANCE = new ConfigAttrStore();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class Refresher extends TimerTask {

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            refresh(false);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  init  DOCUMENT ME!
         */
        public void refresh(final boolean init) {
            ResultSet csAttrConfig = null;

            try {
                csAttrConfig = conPool.submitInternalQuery(DBConnection.GET_ALL_CS_CONFIG_ATTR, new Object[0]);
                final ConcurrentHashMap<String, CsAttrConfigTable> mapWithDomain = new ConcurrentHashMap();
                final ConcurrentHashMap<String, CsAttrConfigTable> mapWithoutDomain = new ConcurrentHashMap();
                final ConcurrentHashMap<String, CsAttrConfigExemptTable> mapExempt = new ConcurrentHashMap();

                while (csAttrConfig.next()) {
                    final String key = csAttrConfig.getString("key");
                    final String type = csAttrConfig.getString("type");
                    final String value = csAttrConfig.getString("value");
                    final String filename = csAttrConfig.getString("filename");
                    final Integer uid = csAttrConfig.getObject("uid", Integer.class);
                    final String loginName = csAttrConfig.getString("login_name");
                    final String ugName = csAttrConfig.getString("ug_name");
                    final String domainName = csAttrConfig.getString("domain_name");
                    final CsAttrConfigTable data = new CsAttrConfigTable(
                            key,
                            type,
                            value,
                            filename,
                            uid,
                            loginName,
                            ugName,
                            domainName);

                    mapWithDomain.put(data.generateLoginNameGroupDomainKey(), data);
                    mapWithoutDomain.put(data.generateLoginNameGroupKey(), data);
                }

                DBConnection.closeResultSets(csAttrConfig);
                csAttrConfig = conPool.submitInternalQuery(DBConnection.GET_ALL_CS_CONFIG_ATTR_EXEMPT, new Object[0]);

                while (csAttrConfig.next()) {
                    final String loginName = csAttrConfig.getString("login_name");
                    final String key = csAttrConfig.getString("key");
                    final String ugName = csAttrConfig.getString("name");
                    final CsAttrConfigExemptTable data = new CsAttrConfigExemptTable(key, loginName, ugName);

                    mapExempt.put(data.generateKey(), data);
                }

                if (init) {
                    // At this moment, the instance should not be exist
                    configAttrMapWithoutDomain = mapWithoutDomain;
                    configAttrMapWithDomain = mapWithDomain;
                    configAttrMapExempt = mapExempt;
                } else {
                    synchronized (ConfigAttrStore.getInstance()) {
                        configAttrMapWithoutDomain = mapWithoutDomain;
                        configAttrMapWithDomain = mapWithDomain;
                        configAttrMapExempt = mapExempt;
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while retrieving config attributes", e);
            } finally {
                if (csAttrConfig != null) {
                    DBConnection.closeResultSets(csAttrConfig);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    private static class CsAttrConfigExemptTable {

        //~ Instance fields ----------------------------------------------------

        private String key;
        private String loginName;
        private String ugName;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CsAttrConfigExemptTable object.
         *
         * @param  key        DOCUMENT ME!
         * @param  loginName  DOCUMENT ME!
         * @param  ugName     DOCUMENT ME!
         */
        public CsAttrConfigExemptTable(final String key, final String loginName, final String ugName) {
            this.key = key;
            this.loginName = loginName;
            this.ugName = ugName;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String generateKey() {
            return generateExemptKey(loginName, key);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    private static class CsAttrConfigTable {

        //~ Instance fields ----------------------------------------------------

        private String key;
        private String type;
        private String value;
        private String filename;
        private Integer uid;
        private String loginName;
        private String ugName;
        private String domainName;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CsAttrConfigTable object.
         *
         * @param  key         DOCUMENT ME!
         * @param  type        DOCUMENT ME!
         * @param  value       DOCUMENT ME!
         * @param  filename    DOCUMENT ME!
         * @param  uid         DOCUMENT ME!
         * @param  login_name  DOCUMENT ME!
         * @param  ugName      DOCUMENT ME!
         * @param  domainName  DOCUMENT ME!
         */
        public CsAttrConfigTable(final String key,
                final String type,
                final String value,
                final String filename,
                final Integer uid,
                final String login_name,
                final String ugName,
                final String domainName) {
            this.key = key;
            this.type = type;
            this.value = value;
            this.filename = filename;
            this.uid = uid;
            this.loginName = login_name;
            this.ugName = ugName;
            this.domainName = domainName;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String generateLoginNameGroupKey() {
            return generateKey(loginName, ugName, key);
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String generateLoginNameGroupDomainKey() {
            return generateKey(loginName, ugName, domainName, key);
        }
    }
}
