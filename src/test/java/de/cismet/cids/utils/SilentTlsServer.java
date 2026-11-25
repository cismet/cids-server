/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.nio.charset.StandardCharsets;

import java.security.KeyStore;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * A TLS server, which behaves like a peer, whose connections were silently dropped by a firewall. It answers every
 * request, but afterwards it neither reads from the connection nor closes it. A request to the path /hang will not be
 * answered at all.
 *
 * <p>While the server is running, the https protocol of commons-httpclient is replaced by a protocol, which trusts the
 * certificate of the server. The original protocol will be restored, when the server is stopped.</p>
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SilentTlsServer {

    //~ Static fields/initializers ---------------------------------------------

    public static final String HANG_PATH = "/hang";

    private static final String KEYSTORE = "silent-tls-server.p12";
    private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();
    // the JDK only waits for the close_notify response of the peer, if TLS 1.3 is used
    private static final String TLS_13 = "TLSv1.3";

    //~ Instance fields --------------------------------------------------------

    private final SSLServerSocket serverSocket;
    private final Protocol originalHttps;
    private final List<Socket> sockets = new CopyOnWriteArrayList<>();
    private final CountDownLatch stopped = new CountDownLatch(1);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates and starts a new SilentTlsServer object on a free port of the loopback interface.
     *
     * @throws  Exception  if the server cannot be started
     */
    public SilentTlsServer() throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try(final InputStream is = SilentTlsServer.class.getResourceAsStream(KEYSTORE)) {
            keyStore.load(is, KEYSTORE_PASSWORD);
        }

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD);

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        serverSocket = (SSLServerSocket)context.getServerSocketFactory()
                    .createServerSocket(0, 50, InetAddress.getLoopbackAddress());
        serverSocket.setEnabledProtocols(new String[] { TLS_13 });

        originalHttps = Protocol.getProtocol("https");
        Protocol.registerProtocol(
            "https",
            new Protocol("https", (ProtocolSocketFactory)new TestSocketFactory(context.getSocketFactory()), 443));

        final Thread acceptor = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        accept();
                    }
                }, "SilentTlsServer-acceptor");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Checks, if the JDK supports TLS 1.3.
     *
     * @return  true, if TLS 1.3 is supported
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static boolean isTls13Supported() throws Exception {
        return Arrays.asList(SSLContext.getDefault().getSupportedSSLParameters().getProtocols()).contains(TLS_13);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the base url of the server with a trailing slash
     */
    public String getBaseUrl() {
        return "https://localhost:" + serverSocket.getLocalPort() + "/";
    }

    /**
     * Stops the server, closes all connections and restores the original https protocol.
     */
    public void stop() {
        stopped.countDown();
        Protocol.registerProtocol("https", originalHttps);

        try {
            serverSocket.close();
        } catch (final IOException e) {
            // nothing to do
        }

        for (final Socket socket : sockets) {
            try {
                socket.close();
            } catch (final IOException e) {
                // nothing to do
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void accept() {
        while (!serverSocket.isClosed()) {
            try {
                final Socket socket = serverSocket.accept();
                sockets.add(socket);

                final Thread handler = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                handle(socket);
                            }
                        }, "SilentTlsServer-handler");
                handler.setDaemon(true);
                handler.start();
            } catch (final IOException e) {
                // the server was stopped
            }
        }
    }

    /**
     * Answers the first request of the connection and stays silent afterwards.
     *
     * @param  socket  the connection to the client
     */
    private void handle(final Socket socket) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(),
                        StandardCharsets.US_ASCII));
            final String requestLine = reader.readLine();
            String header = reader.readLine();

            while ((header != null) && !header.isEmpty()) {
                header = reader.readLine();
            }

            if ((requestLine != null) && !requestLine.contains(" " + HANG_PATH + " ")) {
                final OutputStream out = socket.getOutputStream();
                out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 2\r\n\r\nok".getBytes(
                        StandardCharsets.US_ASCII));
                out.flush();
            }

            // like a dropped connection: neither read nor close
            stopped.await();
        } catch (final Exception e) {
            // the server was stopped
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Creates the client sockets with an SSL context, which trusts the certificate of the server.
     *
     * @version  $Revision$, $Date$
     */
    private static final class TestSocketFactory implements SecureProtocolSocketFactory {

        //~ Instance fields ----------------------------------------------------

        private final SSLSocketFactory factory;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TestSocketFactory object.
         *
         * @param  factory  DOCUMENT ME!
         */
        TestSocketFactory(final SSLSocketFactory factory) {
            this.factory = factory;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Socket createSocket(final String host,
                final int port,
                final InetAddress localAddress,
                final int localPort) throws IOException {
            return factory.createSocket(host, port, localAddress, localPort);
        }

        @Override
        public Socket createSocket(final String host,
                final int port,
                final InetAddress localAddress,
                final int localPort,
                final HttpConnectionParams params) throws IOException {
            final Socket socket = factory.createSocket();
            socket.bind(new InetSocketAddress(localAddress, localPort));
            socket.connect(new InetSocketAddress(host, port), params.getConnectionTimeout());

            return socket;
        }

        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            return factory.createSocket(host, port);
        }

        @Override
        public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
                throws IOException {
            return factory.createSocket(socket, host, port, autoClose);
        }
    }
}
