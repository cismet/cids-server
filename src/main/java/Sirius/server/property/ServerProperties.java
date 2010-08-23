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
        super(new FileInputStream(configFile));
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
     * Liefert den Wert des Keys "serverName".
     *
     * @return  Servername*
     */
    public final String getServerName() {
        return this.getString("serverName");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getServerPort() {
        return Integer.valueOf(getString("serverPort"));
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
            return Integer.valueOf(getString("server.rest.port"));                    // NOI18N
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
            return Boolean.valueOf(getString("server.rest.enable"));      // NOI18N
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
    public final String getRestServerKeystore() {
        return getString("server.rest.keystore.server"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestServerKeystorePW() {
        return getString("server.rest.keystore.server.password"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestServerKeystoreKeyPW() {
        return getString("server.rest.keystore.server.keypassword"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestClientKeystore() {
        return getString("server.rest.keystore.client"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isRestClientAuth() {
        return Boolean.valueOf(getString("server.rest.keystore.client.auth")); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRestClientKeystorePW() {
        return getString("server.rest.keystore.client.password"); // NOI18N
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
            return Boolean.valueOf(getString("server.rest.debug"));      // NOI18N
        } catch (final MissingResourceException e) {
            final String message = "server.rest.debug property not set"; // NOI18N
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
        return this.getString("defaultIconDirectory");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getIconDirectory() {
        return this.getObject("iconDirectory").toString();  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getQueryStoreDirectory() {
        return this.getObject("queryStoreDirectory").toString();  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDbConnectionString() {
        return this.getString("connection.url");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getPoolSize() {
        return getInt("connection.pool_size");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getJDBCDriver() {
        return this.getString("connection.driver_class");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getSQLDialect() {
        return this.getString("dialect");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDbPassword() {
        return PasswordEncrypter.decryptString(this.getString("connection.password"));  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDbUser() {
        return this.getString("connection.username");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getFileSeparator() {
        return this.getString("fileSeparator");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String[] getRegistryIps() {
        return this.getStrings("registryIPs");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getRMIRegistryPort() {
        return this.getString("rmiRegistryPort");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStartMode() {
        return this.getString("startMode");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getServerPolicy() {
        return this.getString("serverPolicy");  // NOI18N
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getClassNodePolicy() {
        return this.getString("classNodePolicy");  // NOI18N
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getPureNodePolicy() {
        return this.getString("pureNodePolicy");  // NOI18N
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getAttributePolicy() {
        return this.getString("attributePolicy");  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int[] getQuotedTypes() {
        return this.getInts("quotedTypes");  // NOI18N
    }

    /*  MetaJDBC-Treiber */

    /**
     * DOCUMENT ME!
     *
     * @return  typ des Cache oder null wenn key "cache_type" nicht vorhanden.
     */
    public final String getMetaJDBC_CacheType() {
        try {
            return this.getString("cache_type");  // NOI18N
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
            return this.getString("log4j_prop_file");  // NOI18N
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
            return this.getString("schema");  // NOI18N
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
            LOG.debug("<SRVProperties> call getDefaultIcons");//NOI18N
            LOG.debug("<SRVProperties> get DefaultIcons from Path: " + getDefaultIconDir() + file.exists());//NOI18N
        }

        File[] images = new File[0];
        Image[] sImages = new Image[0];

        try {
            if (file.isDirectory()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("<SRVProperties> valid Directory");//NOI18N
                }

                /**
                 * String[] inside = file.list();
                 * System.out.println(inside.length);
                 * for(int i=0; i<inside.length; i++)
                 * System.out.println(inside[i]);**/

                // System.out.println(file.listFiles());
                images = file.listFiles();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("<SRVProperties> found " + images.length + " icons");//NOI18N
                }
                sImages = new Image[images.length];

                String s = "";  // NOI18N
                for (int i = 0; i < images.length; i++) {
                    s += (images[i].getName() + ";");  // NOI18N
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(s);
                    }
                    sImages[i] = new Image(images[i].getAbsolutePath());
                }
            } else {
                throw new Exception("file is not an Directory");  // NOI18N
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
        final StringTokenizer tokenizer = new StringTokenizer(this.getString(key), ";");//NOI18N
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
            objects[i] = c.createObject(args[i], ",");  // NOI18N
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
        final StringTokenizer tokenizer = new StringTokenizer(this.getString(key), ";");//NOI18N

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
