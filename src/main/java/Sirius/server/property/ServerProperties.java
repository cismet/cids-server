/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.property;

import java.io.*;

import java.util.*;

import Sirius.util.image.*;

import de.cismet.tools.PasswordEncrypter;

import java.lang.reflect.*;

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

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

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
    public ServerProperties(String configFile) throws FileNotFoundException, IOException {
        super(new FileInputStream(configFile));
    }

    /**
     * Aufruf des entsprechenden Superkonstruktors.*
     *
     * @param   file  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public ServerProperties(File file) throws IOException {
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
        return new Integer(this.getString("serverPort")).intValue();  // NOI18N
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
    public final String getStartMode() {
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
        File file = new File(getDefaultIconDir());

        // File file = new File("p:\\Metaservice\\Sirius\\System\\imgDefault");
        if (logger.isDebugEnabled()) {
            logger.debug("<SRVProperties> call getDefaultIcons");  // NOI18N
            logger.debug("<SRVProperties> get DefaultIcons from Path: " + getDefaultIconDir() + file.exists());  // NOI18N
        }

        File[] images = new File[0];
        Image[] sImages = new Image[0];

        try {
            if (file.isDirectory()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<SRVProperties> valid Directory");  // NOI18N
                }

                /**
                 * String[] inside = file.list();
                 * System.out.println(inside.length);
                 * for(int i=0; i<inside.length; i++)
                 * System.out.println(inside[i]);**/

                // System.out.println(file.listFiles());
                images = file.listFiles();
                if (logger.isDebugEnabled()) {
                    logger.debug("<SRVProperties> found " + images.length + " icons");  // NOI18N
                }
                sImages = new Image[images.length];

                String s = "";  // NOI18N
                for (int i = 0; i < images.length; i++) {
                    s += (images[i].getName() + ";");  // NOI18N
                    if (logger.isDebugEnabled()) {
                        logger.debug(s);
                    }
                    sImages[i] = new Image(images[i].getAbsolutePath());
                }
            } else {
                throw new Exception("file is not an Directory");  // NOI18N
            }
        } catch (Exception e) {
            logger.error(e);
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
    public String[] getStrings(String key) {
        StringTokenizer tokenizer = new StringTokenizer(this.getString(key), ";");  // NOI18N
        String[] stringArray = new String[tokenizer.countTokens()];
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
    public String[] getStrings(String key, String delimiter) {
        StringTokenizer tokenizer = new StringTokenizer(this.getString(key), delimiter);
        String[] stringArray = new String[tokenizer.countTokens()];
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
    public Object[] getObjectList(String key, Createable c) {
        java.lang.String[] args = getStrings(key);
        java.lang.Object[] objects = (Object[])Array.newInstance(c.getClass(), args.length);

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
    public int[] getInts(String key) {
        StringTokenizer tokenizer = new StringTokenizer(this.getString(key), ";");  // NOI18N

        int[] intArray = new int[tokenizer.countTokens()];

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
    public int getInt(String key) {
        return new Integer(getString(key)).intValue();
    }
} // end class
