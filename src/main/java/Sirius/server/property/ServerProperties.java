/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.property;

import Sirius.util.image.*;

import org.apache.log4j.Logger;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;

import de.cismet.tools.PasswordEncrypter;

/**
 * Verwaltet Informationen zur allgemeinen Serverkonfiguration. (Local-, Call-, Translationserver). Das jeweilige
 * Configfile kann folgende Schluessel und Werte besitzen.
 *
 * <table border=1>
 *   <tr>
 *     <td><b>KEY</b></td>
 *     <td><b>VALUE</b></td>
 *     <td><b>LS</b></td>
 *     <td><b>CS</b></td>
 *     <td><b>PS</b></td>
 *     <td><b>TS</b></td>
 *     <td><b>Registry</b></td>
 *   </tr>
 *   <tr>
 *     <td>serverName</td>
 *     <td>Der Name, mit dem dieser Server im System in Erscheinung tritt</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>port</td>
 *     <td>Der Port, mit dem dieser Server im System in Erscheinung tritt</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>defaultIconDirectory</td>
 *     <td>Standarddirectory fuer Icons</td>
 *     <td>x</td>
 *     <td></td>
 *     <td></td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>iconDirectory</td>
 *     <td>Directory fuer Icons</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>dbConnectionString</td>
 *     <td>der Connectionstring, um die Datenbank zu kontaktieren</td>
 *     <td></td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>translDbConnectionString</td>
 *     <td>der Connectionstring, um die TranslationDB zu kontaktieren</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>jdbcDriver</td>
 *     <td>der JDBC-Treiber fuer die jeweilige DB</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td></td>
 *   </tr>
 *   <tr>
 *     <td>dbType</td>
 *     <td>Art der DB</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>dbIP</td>
 *     <td>IP der Datenbank</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>dbPassword</td>
 *     <td>Benutzerpasswort fuer die DB</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>cacheStatements</td>
 *     <td>TRUE oder FALSE</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>fileSeparator</td>
 *     <td>"\\"fuer WinX-Systeme, "/" fuer Linux....</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *   </tr>
 *   <tr>
 *     <td>registryIPs</td>
 *     <td>die IP-Adressen, auf denen SiriusRegistries laufen und der Server sich anmelden soll. Mehrerer Adressen sind
 *       durch ; zu trennen</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td>x</td>
 *     <td></td>
 *   </tr>
 * </table>
 * <br>
 * Beispielfile:<br>
 * <br>
 * serverName=Altlasten<br>
 * defaultIconDirectory=i:\\MetaService\\Sirius\\Icons<br>
 * dbConnectionString=jdbc:odbc:altlasten_saarbruecken<br>
 * jdbcDriver=jdbc....<br>
 * cacheStatements=FALSE<br>
 * fileSeperator=\\<br>
 * registryIps=134.96.177.20;134.96.177.21;134.96.177.44<br>
 *
 * @author   Bernd Kiefer
 * @version  1.1 (schlob) *
 */
public class ServerProperties extends java.util.PropertyResourceBundle {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ServerProperties.class);

    public static final String DEPLOY_ENV__PRODUCTION = "production";
    public static final String DEPLOY_ENV__DEVELOPMENT = "development";
    public static final String DEPLOY_ENV__TESTING = "testing";
    public static final String START_MODE__SIMPLE = "simple";
    public static final String START_MODE__PROXY = "proxy";

    private static final String PROP__RESOURCES_BASE_PATH = "cids.custom.server.resourcesBasePath";
    private static final String PROP__SERVER_NAME = "serverName";
    private static final String PROP__SERVER_PORT = "serverPort";
    private static final String PROP__SERVER_REST_PORT = "server.rest.port";
    private static final String PROP__SERVER_REST_ENABLE = "server.rest.enable";
    private static final String PROP__SERVER_COMPRESSION_ENABLE = "server.compression.enable";
    private static final String PROP__SERVER_CONNECTIONCONTEXT_CONFIG = "server.connectioncontext.config";
    private static final String PROP__SERVER_PROXY_URL = "server.proxy.url";
    private static final String PROP__SERVER_REST_KEYSTORE_SERVER = "server.rest.keystore.server";
    private static final String PROP__SERVER_REST_THREADNAMING_ENABLE = "server.rest.threadnaming.enable";
    private static final String PROP__SERVER_REST_THREADS_MAX = "server.rest.threads.max";
    private static final String PROP__SERVER_REST_THREADS_MIN = "server.rest.threads.min";
    private static final String PROP__SERVER_REST_KEYSTORE_SERVER_PASSWORD = "server.rest.keystore.server.password";
    private static final String PROP__SERVER_REST_KEYSTORE_SERVER_KEYPASSWORD =
        "server.rest.keystore.server.keypassword";
    private static final String PROP__SERVER_REST_KEYSTORE_CLIENT = "server.rest.keystore.client";
    private static final String PROP__SERVER_REST_KEYSTORE_CLIENT_AUTH = "server.rest.keystore.client.auth";
    private static final String PROP__SERVER_REST_KEYSTORE_CLIENT_PASSWORD = "server.rest.keystore.client.password";
    private static final String PROP__SERVER_REST_DEBUG = "server.rest.debug";
    private static final String PROP__SERVER_REST_SWAGER_ENABLED = "server.rest.swaggerEnabled";
    private static final String PROP__DEFAULT_ICON_DIRECTORY = "defaultIconDirectory";
    private static final String PROP__ICON_DIRECTORY = "iconDirectory";
    private static final String PROP__QUERY_STORE_DIRECTORY = "queryStoreDirectory";
    private static final String PROP__CONNECTION_URL = "connection.url";
    private static final String PROP__CONNECTION_POOL_SIZE = "connection.pool_size";
    private static final String PROP__CONNECTION_DRIVER_CLASS = "connection.driver_class";
    private static final String PROP__DIALECT = "dialect";
    private static final String PROP__CONNECTION_PASSWORD = "connection.password";
    private static final String PROP__CONNECTION_USERNAME = "connection.username";
    private static final String PROP__FILE_SEPARATOR = "fileSeparator";
    private static final String PROP__REGISTRY_IPS = "registryIPs";
    private static final String PROP__RMI_REGISTRY_PORT = "rmiRegistryPort";
    private static final String PROP__INTERNAL_DIALECT = "internalDialect";
    private static final String PROP__START_MODE = "startMode";
    private static final String PROP__DEPLOY_ENV = "deployEnv";
    private static final String PROP__SERVER_POLICY = "serverPolicy";
    private static final String PROP__CLASS_NODE_POLICY = "classNodePolicy";
    private static final String PROP__PURE_NODE_POLICY = "pureNodePolicy";
    private static final String PROP__ATTRIBUTE_POLICY = "attributePolicy";
    private static final String PROP__CACHE_TYPE = "cache_type";
    private static final String PROP__LOG4J_PROP_FILE = "log4j_prop_file";
    private static final String PROP__SCHEMA = "schema";
    private static final String PROP__PASSWORDCHANGE_TRIGGER_SCRIPT_BEFORE = "passwordchangeTriggerScriptBefore";
    private static final String PROP__PASSWORDCHANGE_TRIGGER_SCRIPT_AFTER = "passwordchangeTriggerScriptAfter";

    //~ Instance fields --------------------------------------------------------

    private String internalDialect;

    //~ Constructors -----------------------------------------------------------

    /**
     * Aufruf des entsprechenden Superkonstruktors.*
     *
     * @param   inputStream  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public ServerProperties(final InputStream inputStream) throws IOException {
        super(inputStream);

        internalDialect = null;
    }

    /**
     * Aufruf des entsprechenden Superkonstruktors.*
     *
     * @param   configFile  DOCUMENT ME!
     *
     * @throws  FileNotFoundException  DOCUMENT ME!
     * @throws  IOException            DOCUMENT ME!
     */
    public ServerProperties(final String configFile) throws FileNotFoundException, IOException {
        this(new FileInputStream(configFile));
    }

    /**
     * Aufruf des entsprechenden Superkonstruktors.*
     *
     * @param   file  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public ServerProperties(final File file) throws IOException {
        super(new FileInputStream(file));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getServerResourcesBasePath() {
        return this.getString(PROP__RESOURCES_BASE_PATH);
    }

    /**
     * Liefert den Wert des Keys "serverName".
     *
     * @return  Servername*
     */
    public final String getServerName() {
        return this.getString(PROP__SERVER_NAME); // NOI18N
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getServerPort() {
        try {
            final String serverPort = getString(PROP__SERVER_PORT);
            if (serverPort == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("server port not set, returning rmi registry port"); // NOI18N
                }

                return Integer.valueOf(getRMIRegistryPort());
            } else {
                return Integer.valueOf(serverPort);
            }
        } catch (final Exception e) {
            LOG.warn("error finding server port, returning rmi registry port", e); // NOI18N

            return Integer.valueOf(getRMIRegistryPort());
        }
    }

    /**
     * Delivers the server's rest port.<br/>
     * <br/>
     * <b>If the port is not retrievable from the property file the port number defaults to <code>9986</code></b>.
     *
     * @return  the server's rest port
     */
    public final int getRestPort() {
        try {
            return Integer.valueOf(getString(PROP__SERVER_REST_PORT));                // NOI18N
        } catch (final NumberFormatException e) {
            final String message = "could not parse server.rest.port property value"; // NOI18N
            LOG.warn(message, e);

            return 9986;
        } catch (final MissingResourceException e) {
            final String message = "server.rest.port property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return 9986;
        }
    }

    /**
     * Indicates whether rest shall be enabled.<br/>
     * <br/>
     * <b>If the flag is not retrievable from the property file it defaults to <code>false</code></b>.
     *
     * @return  whether the server shall enable rest
     */
    public final boolean isRestEnabled() {
        try {
            return Boolean.valueOf(getString(PROP__SERVER_REST_ENABLE));  // NOI18N
        } catch (final MissingResourceException e) {
            final String message = "server.rest.enable property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isCompressionEnabled() {
        try {
            return Boolean.valueOf(getString(PROP__SERVER_COMPRESSION_ENABLE));  // NOI18N
        } catch (final MissingResourceException e) {
            final String message = "server.compression.enable property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getConnectionContextConfig() {
        try {
            return getString(PROP__SERVER_CONNECTIONCONTEXT_CONFIG);
        } catch (final MissingResourceException e) {
            final String message = "server.connectioncontext.config property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getServerProxyURL() {
        return getString(PROP__SERVER_PROXY_URL);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestServerKeystore() {
        return getString(PROP__SERVER_REST_KEYSTORE_SERVER); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isRestThreadNamingEnabled() {
        try {
            return Boolean.valueOf(getString(PROP__SERVER_REST_THREADNAMING_ENABLE));  // NOI18N
        } catch (final MissingResourceException e) {
            final String message = "server.rest.threadnaming.enable property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getRestServerMaxThreads() {
        try {
            return Integer.valueOf(getString(PROP__SERVER_REST_THREADS_MAX));                    // NOI18N
        } catch (final NumberFormatException e) {
            final String message = "could not parse server.rest.threads.max value. default=255"; // NOI18N
            LOG.warn(message, e);
        } catch (final MissingResourceException e) {
            final String message = "server.rest.threads.max property not set. default=255";      // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }
        }
        return 255;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getRestServerMinThreads() {
        try {
            return Integer.valueOf(getString(PROP__SERVER_REST_THREADS_MIN));                  // NOI18N
        } catch (final NumberFormatException e) {
            final String message = "could not parse server.rest.threads.min value. default=1"; // NOI18N
            LOG.warn(message, e);
        } catch (final MissingResourceException e) {
            final String message = "server.rest.threads.min property not set. default=1";      // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }
        }
        return 1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestServerKeystorePW() {
        return getString(PROP__SERVER_REST_KEYSTORE_SERVER_PASSWORD); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestServerKeystoreKeyPW() {
        return getString(PROP__SERVER_REST_KEYSTORE_SERVER_KEYPASSWORD); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestClientKeystore() {
        return getString(PROP__SERVER_REST_KEYSTORE_CLIENT); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isRestClientAuth() {
        try {
            return Boolean.valueOf(getString(PROP__SERVER_REST_KEYSTORE_CLIENT_AUTH));  // NOI18N
        } catch (final MissingResourceException e) {
            final String message = "server.rest.keystore.client.auth property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestClientKeystorePW() {
        return getString(PROP__SERVER_REST_KEYSTORE_CLIENT_PASSWORD); // NOI18N
    }

    /**
     * Indicates whether rest shall be run in debug mode.<br/>
     * <br/>
     * <b>If the flag is not retrievable from the property file it defaults to <code>false</code></b>.
     *
     * @return  whether the server shall run rest in debug mode
     */
    public final boolean isRestDebug() {
        try {
            return Boolean.valueOf(getString(PROP__SERVER_REST_DEBUG));  // NOI18N
        } catch (final MissingResourceException e) {
            final String message = "server.rest.debug property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return false;
        }
    }

    /**
     * Indicates whether swagger should be enabled.<br/>
     * <br/>
     * <b>If the flag is not retrievable from the property file it defaults to <code>false</code></b>.
     *
     * @return  whether the server shall provide swagger
     */
    public final boolean isSwaggerEnabled() {
        try {
            return Boolean.valueOf(getString(PROP__SERVER_REST_SWAGER_ENABLED));  // NOI18N
        } catch (final MissingResourceException e) {
            final String message = "server.rest.swaggerEnabled property not set"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message, e);
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDefaultIconDir() {
        return this.getString(PROP__DEFAULT_ICON_DIRECTORY); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getIconDirectory() {
        return this.getObject(PROP__ICON_DIRECTORY).toString(); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getQueryStoreDirectory() {
        return this.getObject(PROP__QUERY_STORE_DIRECTORY).toString(); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDbConnectionString() {
        return this.getString(PROP__CONNECTION_URL); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getPoolSize() {
        return getInt(PROP__CONNECTION_POOL_SIZE); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getJDBCDriver() {
        return this.getString(PROP__CONNECTION_DRIVER_CLASS); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getSQLDialect() {
        return this.getString(PROP__DIALECT); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDbPassword() {
        return PasswordEncrypter.decryptString(this.getString(PROP__CONNECTION_PASSWORD)); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDbUser() {
        return this.getString(PROP__CONNECTION_USERNAME); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getFileSeparator() {
        return this.getString(PROP__FILE_SEPARATOR); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String[] getRegistryIps() {
        return this.getStrings(PROP__REGISTRY_IPS); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRMIRegistryPort() {
        try {
            final String rmiRegPort = getString(PROP__RMI_REGISTRY_PORT);
            if (rmiRegPort == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("rmi registry port not set, returning default value 1099"); // NOI18N
                }

                return "1099";
            } else {
                return rmiRegPort;
            }
        } catch (final Exception e) {
            LOG.warn("error finding rmi reg port, returning default value 1099", e); // NOI18N

            return "1099";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getInternalDialect() {
        if (internalDialect == null) {
            try {
                internalDialect = getString(PROP__INTERNAL_DIALECT);                  // NOI18N
            } catch (final MissingResourceException e) {
                LOG.warn("error reading internalDialect property, using default", e); // NOI18N

                internalDialect = "";
            }
        }

        return internalDialect;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStartMode() {
        return this.getString(PROP__START_MODE); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDeployEnv() {
        try {
            return this.getString(PROP__DEPLOY_ENV);
        } catch (final MissingResourceException e) {
            LOG.info(String.format("deployEnv missing, setting to %s", DEPLOY_ENV__DEVELOPMENT), e);
            return DEPLOY_ENV__DEVELOPMENT;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getServerPolicy() {
        return this.getString(PROP__SERVER_POLICY); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getClassNodePolicy() {
        return this.getString(PROP__CLASS_NODE_POLICY); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getPureNodePolicy() {
        return this.getString(PROP__PURE_NODE_POLICY); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getAttributePolicy() {
        return this.getString(PROP__ATTRIBUTE_POLICY); // NOI18N
    }

    /*  MetaJDBC-Treiber */
    /**
     * DOCUMENT ME!
     *
     * @return  typ des Cache oder null wenn key "cache_type" nicht vorhanden.
     */
    public final String getMetaJDBC_CacheType() {
        try {
            return this.getString(PROP__CACHE_TYPE); // NOI18N
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  Pfad zu log4j-File des MetaJDBC-Treibers oder null wenn key "log4j_prop_file" nicht vorhanden.
     */
    public final String getLog4jPropertyFile() {
        try {
            return this.getString(PROP__LOG4J_PROP_FILE); // NOI18N
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  Name der Schema oder null wenn key "schema" nicht vorhanden.
     */
    public final String getMetaJDBC_schema() {
        try {
            return this.getString(PROP__SCHEMA); // NOI18N
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getPasswordchangeTriggerScriptBefore() {
        try {
            return this.getString(PROP__PASSWORDCHANGE_TRIGGER_SCRIPT_BEFORE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getPasswordchangeTriggerScriptAfter() {
        try {
            return this.getString(PROP__PASSWORDCHANGE_TRIGGER_SCRIPT_AFTER); // NOI18N
        } catch (Exception e) {
            return null;
        }
    }

    /*  /MetaJDBC-Treiber */
    // ----------------------------------------------------------------------------------------------------------------------------
    /**
     * Liest alle defaultIcons aus dem defaultIconDirectory.
     *
     * @return  DOCUMENT ME!
     */
    public Image[] getDefaultIcons() {
        final File file = new File(getDefaultIconDir());

        // File file = new File("p:\\Metaservice\\Sirius\\System\\imgDefault");
        if (LOG.isDebugEnabled()) {
            LOG.debug("<SRVProperties> call getDefaultIcons");                                               // NOI18N
            LOG.debug("<SRVProperties> get DefaultIcons from Path: " + getDefaultIconDir() + file.exists()); // NOI18N
        }

        File[] images = null;
        Image[] sImages = new Image[0];

        try {
            if (file.isDirectory()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("<SRVProperties> valid Directory"); // NOI18N
                }

                images = file.listFiles();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("<SRVProperties> found " + images.length + " icons"); // NOI18N
                }
                sImages = new Image[images.length];

                String s = "";                                   // NOI18N
                for (int i = 0; i < images.length; i++) {
                    s += (images[i].getName() + ";");            // NOI18N
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(s);
                    }
                    sImages[i] = new Image(images[i].getAbsolutePath());
                }
            } else {
                throw new Exception("file is not an Directory"); // NOI18N
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return sImages;
    }

    // ----------------------------------------------------------------------------------------------------------------------------
    /**
     * Liefert den Wert eines Keys als StringArray, sofern die einzelnen Tokens duch Semikolon getrennt sind. z.b.
     * a;b;c;def;gh
     *
     * @param   key  der gesuchte key.
     *
     * @return  Stringarray mit den einzelnen Tokens*
     */
    public String[] getStrings(final String key) {
        final StringTokenizer tokenizer = new StringTokenizer(this.getString(key), ";"); // NOI18N
        final String[] stringArray = new String[tokenizer.countTokens()];
        int i = 0;

        while (tokenizer.hasMoreTokens()) {
            stringArray[i++] = tokenizer.nextToken();
        }

        return stringArray;
    }

    /**
     * ---------------------------------------------------------------------------------------------------------------
     *
     * @param   key        DOCUMENT ME!
     * @param   delimiter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getStrings(final String key, final String delimiter) {
        final StringTokenizer tokenizer = new StringTokenizer(this.getString(key), delimiter);
        final String[] stringArray = new String[tokenizer.countTokens()];
        int i = 0;

        while (tokenizer.hasMoreTokens()) {
            stringArray[i++] = tokenizer.nextToken();
        }

        return stringArray;
    }

    // ----------------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------------------------------
    /**
     * Liefert den Wert eines Keys als Object.
     *
     * @param   key  DOCUMENT ME!
     * @param   c    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object[] getObjectList(final String key, final Createable c) {
        final java.lang.String[] args = getStrings(key);
        final java.lang.Object[] objects = (Object[])Array.newInstance(c.getClass(), args.length);

        for (int i = 0; i < args.length; i++) {
            objects[i] = c.createObject(args[i], ","); // NOI18N
        }

        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int[] getInts(final String key) {
        final StringTokenizer tokenizer = new StringTokenizer(this.getString(key), ";"); // NOI18N

        final int[] intArray = new int[tokenizer.countTokens()];

        int i = 0;

        while (tokenizer.hasMoreTokens()) {
            intArray[i++] = new Integer(tokenizer.nextToken()).intValue();
        }

        return intArray;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getInt(final String key) {
        return new Integer(getString(key)).intValue();
    }
} // end class
