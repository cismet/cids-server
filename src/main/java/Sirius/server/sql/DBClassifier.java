/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DBClassifier {

    //~ Instance fields --------------------------------------------------------

    protected int noOfConnections; // indcluding this one

    protected String url;

    protected String login;

    protected String pwd;

    protected String driver;

    protected String sqlDialect;

    protected String internalDialect;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DBClassifier object.
     */
    public DBClassifier() {
        noOfConnections = 0;
        url = "";                                               // NOI18N
        login = "";                                             // NOI18N
        pwd = "";                                               // NOI18N
        driver = "org.postgresql.Driver";                       // NOI18N
        sqlDialect = "org.hibernate.dialect.PostgreSQLDialect"; // NOI18N
    }

    /**
     * Creates a new DBClassifier object.
     *
     * @param  url     DOCUMENT ME!
     * @param  login   DOCUMENT ME!
     * @param  pwd     DOCUMENT ME!
     * @param  driver  DOCUMENT ME!
     */
    public DBClassifier(final String url, final String login, final String pwd, final String driver) {
        this();
        this.url = url;
        this.login = login;
        this.pwd = pwd;
        this.driver = driver;
    }

    /**
     * Creates a new DBClassifier object.
     *
     * @param  url              DOCUMENT ME!
     * @param  login            DOCUMENT ME!
     * @param  pwd              DOCUMENT ME!
     * @param  driver           DOCUMENT ME!
     * @param  noOfConnections  DOCUMENT ME!
     */
    public DBClassifier(final String url,
            final String login,
            final String pwd,
            final String driver,
            final int noOfConnections) {
        this(url, login, pwd, driver);
        this.noOfConnections = noOfConnections;
    }

    /**
     * Creates a new DBClassifier object.
     *
     * @param  url              DOCUMENT ME!
     * @param  login            DOCUMENT ME!
     * @param  pwd              DOCUMENT ME!
     * @param  driver           DOCUMENT ME!
     * @param  noOfConnections  DOCUMENT ME!
     * @param  sqlDialect       DOCUMENT ME!
     * @param  internalDialect  DOCUMENT ME!
     */
    public DBClassifier(final String url,
            final String login,
            final String pwd,
            final String driver,
            final int noOfConnections,
            final String sqlDialect,
            final String internalDialect) {
        this(url, login, pwd, driver, noOfConnections);
        this.sqlDialect = sqlDialect;
        this.internalDialect = internalDialect;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return ((url + "|" + login + "|" + pwd) == null) ? "null" : ("*****" + "|" + driver + "|" + noOfConnections); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param  n  DOCUMENT ME!
     */
    protected void setNoOfConnections(final int n) {
        noOfConnections = n;
    }

    /**
     * Getter for property driver.
     *
     * @return  Value of property driver.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Setter for property driver.
     *
     * @param  driver  New value of property driver.
     */
    public void setDriver(final String driver) {
        this.driver = driver;
    }

    /**
     * Getter for property login.
     *
     * @return  Value of property login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Setter for property login.
     *
     * @param  login  New value of property login.
     */
    public void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Getter for property pwd.
     *
     * @return  Value of property pwd.
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * Setter for property pwd.
     *
     * @param  pwd  New value of property pwd.
     */
    public void setPwd(final String pwd) {
        this.pwd = pwd;
    }

    /**
     * Getter for property url.
     *
     * @return  Value of property url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter for property url.
     *
     * @param  url  New value of property url.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * end class.
     *
     * @return  DOCUMENT ME!
     */
    public String getSqlDialect() {
        return sqlDialect;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sqlDialect  DOCUMENT ME!
     */
    public void setSqlDialect(final String sqlDialect) {
        this.sqlDialect = sqlDialect;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getInternalDialect() {
        return internalDialect;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  internalDialect  DOCUMENT ME!
     */
    public void setInternalDialect(final String internalDialect) {
        this.internalDialect = internalDialect;
    }
}
