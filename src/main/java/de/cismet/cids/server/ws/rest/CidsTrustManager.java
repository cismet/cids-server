/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.ws.rest;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Uses the default TrustManager and the given custom TrustManager to check certificates.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 * @see      <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#TrustManager">JSSE
 *           Reference Guide</a>
 */
public class CidsTrustManager implements X509TrustManager {

    //~ Instance fields --------------------------------------------------------

    /*
     * The default X509TrustManager returned by SunX509.  We'll delegate decisions to it, and fall back to the logic in
     * this class if the default X509TrustManager doesn't trust it.
     */
    X509TrustManager sunJSSEX509TrustManager;
    X509TrustManager cidsTrustManager;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsTrustManager object.
     *
     * @param   cidsTrustManager  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsTrustManager(final X509TrustManager cidsTrustManager) throws Exception {
        // create a "default" JSSE X509TrustManager.

        this.cidsTrustManager = cidsTrustManager;
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
        tmf.init((KeyStore)null);

        final TrustManager[] tms = tmf.getTrustManagers();

        /*
         * Iterate over the returned trustmanagers, look for an instance of X509TrustManager.  If found, use that as our
         * "default" trust manager.
         */
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                sunJSSEX509TrustManager = (X509TrustManager)tms[i];
                return;
            }
        }

        /*
         * Find some other way to initialize, or else we have to fail the constructor.
         */
        throw new Exception("Couldn't initialize");
    }

    //~ Methods ----------------------------------------------------------------

    /*
     * Delegate to the default trust manager.
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        try {
            sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
        } catch (CertificateException excep) {
            if (cidsTrustManager != null) {
                cidsTrustManager.checkClientTrusted(chain, authType);
            } else {
                throw excep;
            }
        }
    }

    /*
     * Delegate to the default trust manager.
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        try {
            sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException excep) {
            if (cidsTrustManager != null) {
                cidsTrustManager.checkServerTrusted(chain, authType);
            } else {
                throw excep;
            }
        }
    }

    /*
     * Merely pass this through.
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        final int size = sunJSSEX509TrustManager.getAcceptedIssuers().length
                    + cidsTrustManager.getAcceptedIssuers().length;
        final X509Certificate[] result = new X509Certificate[size];
        System.arraycopy(sunJSSEX509TrustManager.getAcceptedIssuers(),
            0,
            result,
            0,
            sunJSSEX509TrustManager.getAcceptedIssuers().length);
        System.arraycopy(cidsTrustManager.getAcceptedIssuers(),
            0,
            result,
            sunJSSEX509TrustManager.getAcceptedIssuers().length,
            cidsTrustManager.getAcceptedIssuers().length);

        return result;
    }
}
