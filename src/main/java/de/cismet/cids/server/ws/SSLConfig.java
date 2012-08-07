/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws;

import java.security.KeyStore;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface SSLConfig {

    //~ Instance fields --------------------------------------------------------

    String KEYSTORE_TYPE_JAVA = "JKS";      // NOI18N
    String CERTIFICATE_TYPE_X509 = "X.509"; // NOI18N
    String TMF_SUNX509 = "SunX509";         // NOI18N
    String CONTEXT_TYPE_TLS = "TLS";        // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    KeyStore getServerKeystore();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    KeyStore getClientKeystore();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    char[] getServerKeyPW();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    char[] getClientKeyPW();
}
