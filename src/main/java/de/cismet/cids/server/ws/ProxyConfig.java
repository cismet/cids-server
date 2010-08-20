/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ProxyConfig {

    //~ Instance fields --------------------------------------------------------

    private transient String proxyURL;
    private transient String username;
    private transient String password;
    private transient String computerName;
    private transient String domain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProxyConfig object.
     */
    public ProxyConfig() {
        this(null, null, null, null, null);
    }

    /**
     * Creates a new ProxyConfig object.
     *
     * @param  proxyURL  DOCUMENT ME!
     */
    public ProxyConfig(final String proxyURL) {
        this(proxyURL, null, null, null, null);
    }

    /**
     * Creates a new ProxyConfig object.
     *
     * @param  proxyURL  DOCUMENT ME!
     * @param  username  DOCUMENT ME!
     * @param  password  DOCUMENT ME!
     */
    public ProxyConfig(final String proxyURL, final String username, final String password) {
        this(proxyURL, username, password, null, null);
    }

    /**
     * Creates a new ProxyConfig object.
     *
     * @param  proxyURL      DOCUMENT ME!
     * @param  username      DOCUMENT ME!
     * @param  password      DOCUMENT ME!
     * @param  computerName  DOCUMENT ME!
     * @param  domain        DOCUMENT ME!
     */
    public ProxyConfig(final String proxyURL,
            final String username,
            final String password,
            final String computerName,
            final String domain) {
        this.proxyURL = proxyURL;
        this.username = username;
        this.password = password;
        this.computerName = computerName;
        this.domain = domain;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getComputerName() {
        return computerName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  computerName  DOCUMENT ME!
     */
    public void setComputerName(final String computerName) {
        this.computerName = computerName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPassword() {
        return password;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  password  DOCUMENT ME!
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getProxyURL() {
        return proxyURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  proxyURL  DOCUMENT ME!
     */
    public void setProxyURL(final String proxyURL) {
        this.proxyURL = proxyURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUsername() {
        return username;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  username  DOCUMENT ME!
     */
    public void setUsername(final String username) {
        this.username = username;
    }
}
