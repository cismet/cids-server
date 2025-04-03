/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.dataretrieval.DataRetrievalException;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.newuser.UserException;

import com.sun.jersey.api.client.ClientResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import java.io.IOException;

import java.rmi.RemoteException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.cismet.tools.Converter;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ServerExceptionMapper {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ServerExceptionMapper.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   t        DOCUMENT ME!
     * @param   builder  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Response toResponse(final Throwable t,
            final Response.ResponseBuilder builder) {
        final Response.ResponseBuilder response;
        if (builder == null) {
            response = Response.serverError();
        } else {
            response = builder;
        }

        if (t != null) {
            try {
                final boolean compressionEnabled = StartProxy.getInstance()
                            .getServerProperties()
                            .isCompressionEnabled();
                response.entity(Converter.serialiseToString(t, compressionEnabled));
            } catch (final IOException ex) {
                LOG.error("could not serialise throwable", ex); // NOI18N
            }
        }

        return response.build();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>                 DOCUMENT ME!
     * @param   response            DOCUMENT ME!
     * @param   type                DOCUMENT ME!
     * @param   compressionEnabled  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static <T extends Throwable> T fromResponse(final ClientResponse response,
            final Class<T> type,
            final boolean compressionEnabled) throws Exception {
        if (response != null) {
            final String responseMessage = response.getEntity(String.class);
            try {
                return Converter.deserialiseFromString(responseMessage, type, compressionEnabled);
            } catch (final Exception ex) {
                String shortResponseMessage = responseMessage;

                if (shortResponseMessage.length() > 1000) {
                    shortResponseMessage = shortResponseMessage.substring(0, 1000) + "...";
                }

                LOG.warn(compressionEnabled
                        ? ("expected compressed message but received uncompressed one:\n" + shortResponseMessage)
                        : ("expected uncompressed message but received compressed one:\n" + shortResponseMessage),
                    ex);
                return Converter.deserialiseFromString(responseMessage, type, !compressionEnabled);
            }
        }
        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Provider
    public static final class DataRetrievalExceptionMapper implements ExceptionMapper<DataRetrievalException> {

        //~ Methods ------------------------------------------------------------

        @Override
        public Response toResponse(final DataRetrievalException e) {
            final Response.ResponseBuilder builder = Response.status(HttpStatus.SC_NOT_FOUND);

            return ServerExceptionMapper.toResponse(
                    e,
                    builder);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Provider
    public static final class UserExceptionMapper implements ExceptionMapper<UserException> {

        //~ Methods ------------------------------------------------------------

        @Override
        public Response toResponse(final UserException e) {
            final Response.ResponseBuilder builder = Response.status(HttpStatus.SC_UNAUTHORIZED)
                        .header("WWW-Authenticate", "Username/Password realm=cidsServer"); // NOI18N

            return ServerExceptionMapper.toResponse(
                    e,
                    builder);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Provider
    public static final class RemoteExceptionMapper implements ExceptionMapper<RemoteException> {

        //~ Methods ------------------------------------------------------------

        @Override
        public Response toResponse(final RemoteException e) {
            final Response.ResponseBuilder builder = Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);

            return ServerExceptionMapper.toResponse(
                    e,
                    builder);
        }
    }
}
