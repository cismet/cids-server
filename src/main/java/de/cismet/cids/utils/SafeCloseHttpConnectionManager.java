/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import org.apache.commons.httpclient.ConnectionPoolTimeoutException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import java.nio.channels.SocketChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * A connection manager, whose secure connections can be closed without delay.
 *
 * <p>When a TLS socket with a read timeout is closed, the JDK waits up to this read timeout for the close_notify
 * response of the server (see sun.security.ssl.SSLSocketInputRecord.deplete()). If the connection was silently dropped,
 * e.g. by a firewall, closing the connection blocks until the read timeout expires. This connection manager wraps the
 * sockets of all secure connections, so that the read timeout will be disabled, before the socket is closed.</p>
 *
 * <p>Only the connections of this manager are affected. The globally registered protocols remain unchanged and their
 * socket factories are still used to create the sockets.</p>
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SafeCloseHttpConnectionManager extends MultiThreadedHttpConnectionManager {

    //~ Instance fields --------------------------------------------------------

    private final Map<ProtocolSocketFactory, Protocol> safeCloseProtocols = new HashMap<>();

    //~ Methods ----------------------------------------------------------------

    @Override
    public HttpConnection getConnectionWithTimeout(final HostConfiguration hostConfiguration, final long timeout)
            throws ConnectionPoolTimeoutException {
        return super.getConnectionWithTimeout(toSafeCloseConfiguration(hostConfiguration), timeout);
    }

    /**
     * Creates a copy of the given host configuration, which uses a protocol with a safe close socket factory, if the
     * protocol of the given configuration is secure.
     *
     * @param   hostConfiguration  the host configuration of the request
     *
     * @return  the host configuration, which should be used to get the connection
     */
    private HostConfiguration toSafeCloseConfiguration(final HostConfiguration hostConfiguration) {
        if (hostConfiguration == null) {
            return null;
        }

        final Protocol protocol = hostConfiguration.getProtocol();

        if ((protocol == null) || !protocol.isSecure()
                    || (protocol.getSocketFactory() instanceof SafeCloseSocketFactory)) {
            return hostConfiguration;
        }

        final HostConfiguration safeCloseConfiguration = new HostConfiguration(hostConfiguration);
        safeCloseConfiguration.setHost(hostConfiguration.getHost(),
            hostConfiguration.getPort(),
            getSafeCloseProtocol(protocol));

        return safeCloseConfiguration;
    }

    /**
     * Provides a protocol, which uses the socket factory of the given protocol, but wraps the created sockets. The
     * protocols are cached, because the connection pool distinguishes the hosts by their protocol.
     *
     * @param   protocol  a secure protocol
     *
     * @return  the protocol with the safe close socket factory
     */
    private synchronized Protocol getSafeCloseProtocol(final Protocol protocol) {
        Protocol safeCloseProtocol = safeCloseProtocols.get(protocol.getSocketFactory());

        if (safeCloseProtocol == null) {
            safeCloseProtocol = new Protocol(
                    protocol.getScheme(),
                    (ProtocolSocketFactory)new SafeCloseSocketFactory(
                        (SecureProtocolSocketFactory)protocol.getSocketFactory()),
                    protocol.getDefaultPort());
            safeCloseProtocols.put(protocol.getSocketFactory(), safeCloseProtocol);
        }

        return safeCloseProtocol;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Delegates the socket creation to the given factory and wraps the created sockets.
     *
     * @version  $Revision$, $Date$
     */
    private static final class SafeCloseSocketFactory implements SecureProtocolSocketFactory {

        //~ Instance fields ----------------------------------------------------

        private final SecureProtocolSocketFactory delegate;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SafeCloseSocketFactory object.
         *
         * @param  delegate  the factory, which creates the sockets
         */
        SafeCloseSocketFactory(final SecureProtocolSocketFactory delegate) {
            this.delegate = delegate;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Socket createSocket(final String host,
                final int port,
                final InetAddress localAddress,
                final int localPort) throws IOException {
            return wrap(delegate.createSocket(host, port, localAddress, localPort));
        }

        @Override
        public Socket createSocket(final String host,
                final int port,
                final InetAddress localAddress,
                final int localPort,
                final HttpConnectionParams params) throws IOException {
            return wrap(delegate.createSocket(host, port, localAddress, localPort, params));
        }

        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            return wrap(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
                throws IOException {
            return wrap(delegate.createSocket(socket, host, port, autoClose));
        }

        /**
         * DOCUMENT ME!
         *
         * @param   socket  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private static Socket wrap(final Socket socket) {
            return (socket == null) ? null : new SafeCloseSocket(socket);
        }

        @Override
        public boolean equals(final Object obj) {
            return (obj instanceof SafeCloseSocketFactory)
                        && delegate.equals(((SafeCloseSocketFactory)obj).delegate);
        }

        @Override
        public int hashCode() {
            return (31 * delegate.hashCode()) + 1;
        }
    }

    /**
     * Delegates all calls to the given socket, but disables the read timeout, before the socket or one of its streams
     * will be closed. The socket is only intended to be used by the HttpConnection class.
     *
     * @version  $Revision$, $Date$
     */
    private static final class SafeCloseSocket extends Socket {

        //~ Instance fields ----------------------------------------------------

        private final Socket delegate;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SafeCloseSocket object.
         *
         * @param  delegate  the wrapped socket
         */
        SafeCloseSocket(final Socket delegate) {
            this.delegate = delegate;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * Without a read timeout, the JDK does not wait for the close_notify response of the server, when a TLS socket
         * will be closed.
         */
        private void disableReadTimeout() {
            try {
                if (!delegate.isClosed()) {
                    delegate.setSoTimeout(0);
                }
            } catch (final SocketException e) {
                // the socket will be closed anyway
            }
        }

        @Override
        public void close() throws IOException {
            disableReadTimeout();
            delegate.close();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FilterInputStream(delegate.getInputStream()) {

                    @Override
                    public void close() throws IOException {
                        disableReadTimeout();
                        super.close();
                    }
                };
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new FilterOutputStream(delegate.getOutputStream()) {

                    @Override
                    public void write(final byte[] b, final int off, final int len) throws IOException {
                        // FilterOutputStream would write every byte separately
                        out.write(b, off, len);
                    }

                    @Override
                    public void close() throws IOException {
                        disableReadTimeout();
                        super.close();
                    }
                };
        }

        @Override
        public void connect(final SocketAddress endpoint) throws IOException {
            delegate.connect(endpoint);
        }

        @Override
        public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
            delegate.connect(endpoint, timeout);
        }

        @Override
        public void bind(final SocketAddress bindpoint) throws IOException {
            delegate.bind(bindpoint);
        }

        @Override
        public InetAddress getInetAddress() {
            return delegate.getInetAddress();
        }

        @Override
        public InetAddress getLocalAddress() {
            return delegate.getLocalAddress();
        }

        @Override
        public int getPort() {
            return delegate.getPort();
        }

        @Override
        public int getLocalPort() {
            return delegate.getLocalPort();
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return delegate.getRemoteSocketAddress();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return delegate.getLocalSocketAddress();
        }

        @Override
        public SocketChannel getChannel() {
            return delegate.getChannel();
        }

        @Override
        public void setTcpNoDelay(final boolean on) throws SocketException {
            delegate.setTcpNoDelay(on);
        }

        @Override
        public boolean getTcpNoDelay() throws SocketException {
            return delegate.getTcpNoDelay();
        }

        @Override
        public void setSoLinger(final boolean on, final int linger) throws SocketException {
            delegate.setSoLinger(on, linger);
        }

        @Override
        public int getSoLinger() throws SocketException {
            return delegate.getSoLinger();
        }

        @Override
        public void sendUrgentData(final int data) throws IOException {
            delegate.sendUrgentData(data);
        }

        @Override
        public void setOOBInline(final boolean on) throws SocketException {
            delegate.setOOBInline(on);
        }

        @Override
        public boolean getOOBInline() throws SocketException {
            return delegate.getOOBInline();
        }

        @Override
        public void setSoTimeout(final int timeout) throws SocketException {
            delegate.setSoTimeout(timeout);
        }

        @Override
        public int getSoTimeout() throws SocketException {
            return delegate.getSoTimeout();
        }

        @Override
        public void setSendBufferSize(final int size) throws SocketException {
            delegate.setSendBufferSize(size);
        }

        @Override
        public int getSendBufferSize() throws SocketException {
            return delegate.getSendBufferSize();
        }

        @Override
        public void setReceiveBufferSize(final int size) throws SocketException {
            delegate.setReceiveBufferSize(size);
        }

        @Override
        public int getReceiveBufferSize() throws SocketException {
            return delegate.getReceiveBufferSize();
        }

        @Override
        public void setKeepAlive(final boolean on) throws SocketException {
            delegate.setKeepAlive(on);
        }

        @Override
        public boolean getKeepAlive() throws SocketException {
            return delegate.getKeepAlive();
        }

        @Override
        public void setTrafficClass(final int tc) throws SocketException {
            delegate.setTrafficClass(tc);
        }

        @Override
        public int getTrafficClass() throws SocketException {
            return delegate.getTrafficClass();
        }

        @Override
        public void setReuseAddress(final boolean on) throws SocketException {
            delegate.setReuseAddress(on);
        }

        @Override
        public boolean getReuseAddress() throws SocketException {
            return delegate.getReuseAddress();
        }

        @Override
        public void shutdownInput() throws IOException {
            delegate.shutdownInput();
        }

        @Override
        public void shutdownOutput() throws IOException {
            delegate.shutdownOutput();
        }

        @Override
        public boolean isConnected() {
            return delegate.isConnected();
        }

        @Override
        public boolean isBound() {
            return delegate.isBound();
        }

        @Override
        public boolean isClosed() {
            return delegate.isClosed();
        }

        @Override
        public boolean isInputShutdown() {
            return delegate.isInputShutdown();
        }

        @Override
        public boolean isOutputShutdown() {
            return delegate.isOutputShutdown();
        }

        @Override
        public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
            delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
