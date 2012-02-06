/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SSLHostnameVerifier implements HostnameVerifier {

    //~ Instance fields --------------------------------------------------------

    private final transient HostnameVerifier defaultVerifier;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SSLHostnameVerifier object.
     */
    public SSLHostnameVerifier() {
        defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean verify(final String hostname, final SSLSession ssls) {
        if ("localhost".equals(hostname)) {
            return true;
        }

        return defaultVerifier.verify(hostname, ssls);
    }
}
