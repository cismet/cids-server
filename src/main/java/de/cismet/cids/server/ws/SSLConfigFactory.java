/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SSLConfigFactory {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SSLConfigFactory object.
     */
    private SSLConfigFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static SSLConfigFactory getDefault() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverCertificatePath  DOCUMENT ME!
     * @param   clientKeystorePath     DOCUMENT ME!
     * @param   clientKeystorePW       DOCUMENT ME!
     * @param   clientKeyPW            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SSLConfigFactoryException  DOCUMENT ME!
     */
    public SSLConfig createClientConfig(final String serverCertificatePath,
            final String clientKeystorePath,
            final char[] clientKeystorePW,
            final char[] clientKeyPW) throws SSLConfigFactoryException {
        final Certificate serverCert = createCertificateFromFile(new File(serverCertificatePath),
                SSLConfig.CERTIFICATE_TYPE_X509);
        final KeyStore serverKeystore = createKeystoreFromFile(null, null, SSLConfig.KEYSTORE_TYPE_JAVA);
        try {
            serverKeystore.setCertificateEntry("cids-server-jetty", serverCert);                  // NOI18N
        } catch (final KeyStoreException ex) {
            throw new SSLConfigFactoryException("cannot add server certificate to keystore", ex); // NOI18N
        }

        final KeyStore clientKeystore = createKeystoreFromFile(new File(clientKeystorePath),
                clientKeystorePW,
                SSLConfig.KEYSTORE_TYPE_JAVA);

        return new SSLConfigImpl(serverKeystore, clientKeystore, null, clientKeyPW);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverCertificateInputStream  DOCUMENT ME!
     * @param   clientKeystorePath            DOCUMENT ME!
     * @param   clientKeystorePW              DOCUMENT ME!
     * @param   clientKeyPW                   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SSLConfigFactoryException  DOCUMENT ME!
     */
    public SSLConfig createClientConfig(final InputStream serverCertificateInputStream,
            final String clientKeystorePath,
            final char[] clientKeystorePW,
            final char[] clientKeyPW) throws SSLConfigFactoryException {
        final Certificate serverCert = createCertificateFromStream(
                serverCertificateInputStream,
                SSLConfig.CERTIFICATE_TYPE_X509);
        final KeyStore serverKeystore = createKeystoreFromFile(null, null, SSLConfig.KEYSTORE_TYPE_JAVA);
        try {
            serverKeystore.setCertificateEntry("cids-server-jetty", serverCert);                  // NOI18N
        } catch (final KeyStoreException ex) {
            throw new SSLConfigFactoryException("cannot add server certificate to keystore", ex); // NOI18N
        }

        final KeyStore clientKeystore = createKeystoreFromFile(new File(clientKeystorePath),
                clientKeystorePW,
                SSLConfig.KEYSTORE_TYPE_JAVA);

        return new SSLConfigImpl(serverKeystore, clientKeystore, null, clientKeyPW);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverCertIS  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SSLConfigFactoryException  DOCUMENT ME!
     */
    public SSLConfig createClientConfig(final InputStream serverCertIS) throws SSLConfigFactoryException {
        final Certificate serverCert = createCertificateFromStream(serverCertIS, SSLConfig.CERTIFICATE_TYPE_X509);

        final KeyStore serverKeystore = createKeystoreFromFile(null, null, SSLConfig.KEYSTORE_TYPE_JAVA);
        try {
            serverKeystore.setCertificateEntry("cids-server-jetty", serverCert);                  // NOI18N
        } catch (final KeyStoreException ex) {
            throw new SSLConfigFactoryException("cannot add server certificate to keystore", ex); // NOI18N
        }

        return new SSLConfigImpl(serverKeystore, null, null, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   certFile  DOCUMENT ME!
     * @param   certType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SSLConfigFactoryException  DOCUMENT ME!
     */
    public Certificate createCertificateFromFile(final File certFile, final String certType)
            throws SSLConfigFactoryException {
        try {
            return createCertificateFromStream(new BufferedInputStream(new FileInputStream(certFile)), certType);
        } catch (final FileNotFoundException ex) {
            throw new SSLConfigFactoryException("cannot read certificate file: " + certFile, ex); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   is        DOCUMENT ME!
     * @param   certType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SSLConfigFactoryException  DOCUMENT ME!
     */
    public Certificate createCertificateFromStream(final InputStream is, final String certType)
            throws SSLConfigFactoryException {
        try {
            final CertificateFactory cf = CertificateFactory.getInstance(certType);

            return cf.generateCertificate(is);
        } catch (final CertificateException ex) {
            throw new SSLConfigFactoryException("illegal certificate file", ex); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   keystoreFile  DOCUMENT ME!
     * @param   keystorePW    DOCUMENT ME!
     * @param   keystoreType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SSLConfigFactoryException  DOCUMENT ME!
     */
    public KeyStore createKeystoreFromFile(final File keystoreFile,
            final char[] keystorePW,
            final String keystoreType) throws SSLConfigFactoryException {
        try {
            final KeyStore keystore = KeyStore.getInstance(keystoreType);

            if (keystoreFile == null) {
                keystore.load(null, null);
            } else {
                keystore.load(new BufferedInputStream(new FileInputStream(keystoreFile)), keystorePW);
            }

            return keystore;
        } catch (final KeyStoreException ex) {
            throw new SSLConfigFactoryException("unsupported keystore type: " + keystoreType, ex);              // NOI18N
        } catch (final IOException ex) {
            throw new SSLConfigFactoryException("cannot read keystore file: " + keystoreFile, ex);              // NOI18N
        } catch (final NoSuchAlgorithmException ex) {
            throw new SSLConfigFactoryException("cannot check keystore integrity: " + keystoreFile, ex);        // NOI18N
        } catch (final CertificateException ex) {
            throw new SSLConfigFactoryException("cannot load certificates from keystore: " + keystoreFile, ex); // NOI18N
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final SSLConfigFactory INSTANCE = new SSLConfigFactory();
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class SSLConfigImpl implements SSLConfig {

        //~ Instance fields ----------------------------------------------------

        private final transient KeyStore serverKeystore;
        private final transient KeyStore clientKeystore;
        private final char[] serverKeyPW;
        private final char[] clientKeyPW;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SSLConfigImpl object.
         *
         * @param  serverKeystore  DOCUMENT ME!
         * @param  clientKeystore  DOCUMENT ME!
         * @param  serverKeyPW     DOCUMENT ME!
         * @param  clientKeyPW     DOCUMENT ME!
         */
        public SSLConfigImpl(final KeyStore serverKeystore,
                final KeyStore clientKeystore,
                final char[] serverKeyPW,
                final char[] clientKeyPW) {
            this.serverKeystore = serverKeystore;
            this.clientKeystore = clientKeystore;
            this.serverKeyPW = serverKeyPW;
            this.clientKeyPW = clientKeyPW;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public KeyStore getServerKeystore() {
            return serverKeystore;
        }

        @Override
        public KeyStore getClientKeystore() {
            return clientKeystore;
        }

        @Override
        public char[] getServerKeyPW() {
            return serverKeyPW;
        }

        @Override
        public char[] getClientKeyPW() {
            return clientKeyPW;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(super.toString());

            sb.append('[');
            sb.append("server keystore: ").append(serverKeystore);                             // NOI18N
            sb.append(',');
            sb.append("client keystore: ").append(clientKeystore);                             // NOI18N
            sb.append(',');
            sb.append("server key password: ").append((serverKeyPW == null) ? null : "*****"); // NOI18N
            sb.append(',');
            sb.append("client key password: ").append((clientKeyPW == null) ? null : "*****"); // NOI18N
            sb.append(']');

            return sb.toString();
        }
    }
}
