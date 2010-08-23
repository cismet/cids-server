/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DBClassifier // implements java.lang.Comparable,Sirius.server.property.Createable
{

    //~ Instance fields --------------------------------------------------------

    protected int noOfConnections; // indcluding this one

    protected String url;

    protected String login;

    protected String pwd;

    protected String driver;

    protected String sqlDialect;

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * ////////////////////////////////////////////////////////////////////
     */
    public DBClassifier() {
        noOfConnections = 0;
        url = "";//NOI18N
        login = "";//NOI18N
        pwd = "";//NOI18N
        driver = "org.postgresql.Driver";//NOI18N
        setSqlDialect("org.hibernate.dialect.PostgreSQLDialect");//NOI18N
    }

    /**
     * ////////////////////////////////////////////////////////////////////
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
     * ////////////////////////////////////////////////////////////////////
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
     */
    public DBClassifier(final String url,
            final String login,
            final String pwd,
            final String driver,
            final int noOfConnections,
            final String sqlDialect) {
        this(url, login, pwd, driver, noOfConnections);
        this.sqlDialect = sqlDialect;
    }

    //~ Methods ----------------------------------------------------------------

    ///////////////////////////////////////////////////////////////////////
//
//    public boolean equals(DBClassifier dbc)
//    {
//        if(dbc==this)
//            return true;
//
//        else if(url.equals(dbc.url)&&login.equals(dbc.login)&&pwd.equals(dbc.pwd)&&driver.equals(dbc.driver))
//            return true;
//
//        else
//            return false;
//
//    }
//

    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return url + "|" + login + "|" + pwd + "|" + driver + "|" + noOfConnections;//NOI18N
    }

    /**
     * ///////////////////////////////////////////////////////////////////////// public java.lang.Object
     * createObject(String constructorArgs,String delimiter) { String[] args =
     * DBClassifier.tokenizeString(constructorArgs,delimiter); if(args.length == 7) { return new
     * DBClassifier(args[0],args[1],args[2],args[3],args[4],new Boolean(args[5]).booleanValue(),new
     * Integer(args[6]).intValue()); } else { logger.error("<LS> ERROR :: creatObject falsche Anzahl
     * ConstructorParameter"); return null; } }
     * /////////////////////////////////////////////////////////////////////////
     *
     * @param   s          DOCUMENT ME!
     * @param   delimiter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String[] tokenizeString(final String s, final String delimiter) {
        final StringTokenizer tokenizer = new StringTokenizer(s, delimiter);
        final String[] stringArray = new String[tokenizer.countTokens()];
        int i = 0;

        while (tokenizer.hasMoreTokens()) {
            stringArray[i++] = tokenizer.nextToken();
        }

        return stringArray;
    }

    /**
     * /////////////////////////////////////////////////////////////////////////
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
    public java.lang.String getDriver() {
        return driver;
    }

    /**
     * Setter for property driver.
     *
     * @param  driver  New value of property driver.
     */
    public void setDriver(final java.lang.String driver) {
        this.driver = driver;
    }

    /**
     * Getter for property login.
     *
     * @return  Value of property login.
     */
    public java.lang.String getLogin() {
        return login;
    }

    /**
     * Setter for property login.
     *
     * @param  login  New value of property login.
     */
    public void setLogin(final java.lang.String login) {
        this.login = login;
    }

    /**
     * Getter for property pwd.
     *
     * @return  Value of property pwd.
     */
    public java.lang.String getPwd() {
        return pwd;
    }

    /**
     * Setter for property pwd.
     *
     * @param  pwd  New value of property pwd.
     */
    public void setPwd(final java.lang.String pwd) {
        this.pwd = pwd;
    }

    /**
     * Getter for property url.
     *
     * @return  Value of property url.
     */
    public java.lang.String getUrl() {
        return url;
    }

    /**
     * Setter for property url.
     *
     * @param  url  New value of property url.
     */
    public void setUrl(final java.lang.String url) {
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
}
