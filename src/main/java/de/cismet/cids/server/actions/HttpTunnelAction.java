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
package de.cismet.cids.server.actions;

import Sirius.server.newuser.User;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringReader;

import java.net.URL;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import de.cismet.cidsx.base.types.MediaTypes;
import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.actions.RestApiCidsServerAction;
import de.cismet.cidsx.server.api.types.ActionInfo;
import de.cismet.cidsx.server.api.types.GenericResourceWithContentType;
import de.cismet.cidsx.server.api.types.ParameterInfo;

import de.cismet.commons.security.AccessHandler;
import de.cismet.commons.security.exceptions.BadHttpStatusCodeException;
import de.cismet.commons.security.exceptions.CannotReadFromURLException;
import de.cismet.commons.security.handler.SimpleHttpAccessHandler;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerAction.class)
public class HttpTunnelAction implements RestApiCidsServerAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final String TASK_NAME = "httpTunnelAction";

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            HttpTunnelAction.class);

    public static String CREDENTIALS_USERNAME_KEY = "username";
    public static String CREDENTIALS_PASSWORD_KEY = "password";

    private static final int BIG_SIZE_LOG_THRESHOLD = 104857600; // 100MB
    private static final int MB = 1048576;                       // 100MB

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        URL, REQUEST, METHOD, OPTIONS, CREDENTIALS
    }

    //~ Instance fields --------------------------------------------------------


    private User user = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HttpTunnelAction object.
     */
    public HttpTunnelAction() {
        actionInfo = new ActionInfo();
        actionInfo.setName("HTTP Tunnel Action");
        actionInfo.setActionKey(TASK_NAME);
        actionInfo.setDescription("Tunnels a HTTP Request");

        final List<ParameterInfo> parameterDescriptions = new LinkedList<ParameterInfo>();
        ParameterInfo parameterDescription;

        parameterDescription = new ParameterInfo();
        parameterDescription.setKey(PARAMETER_TYPE.URL.name());
        parameterDescription.setType(Type.JAVA_CLASS);
        parameterDescription.setAdditionalTypeInfo(URL.class.getName());
        parameterDescription.setDescription("REQUEST URL");
        parameterDescriptions.add(parameterDescription);

        parameterDescription = new ParameterInfo();
        parameterDescription.setKey(PARAMETER_TYPE.METHOD.name());
        parameterDescription.setType(Type.JAVA_CLASS);
        parameterDescription.setAdditionalTypeInfo(AccessHandler.ACCESS_METHODS.class.getName());
        parameterDescription.setDescription("REQUEST METHOD, e.g. GET");
        parameterDescriptions.add(parameterDescription);

        parameterDescription = new ParameterInfo();
        parameterDescription.setKey(PARAMETER_TYPE.METHOD.name());
        parameterDescription.setType(Type.JAVA_CLASS);
        parameterDescription.setAdditionalTypeInfo(AccessHandler.ACCESS_METHODS.class.getName());
        parameterDescription.setDescription("REQUEST METHOD, e.g. GET");
        parameterDescriptions.add(parameterDescription);

        parameterDescription = new ParameterInfo();
        parameterDescription.setKey(PARAMETER_TYPE.OPTIONS.name());
        parameterDescription.setType(Type.JAVA_CLASS);
        parameterDescription.setAdditionalTypeInfo(HashMap.class.getName());
        parameterDescription.setDescription("REQUEST OPTIONS");
        parameterDescriptions.add(parameterDescription);

        parameterDescription = new ParameterInfo();
        parameterDescription.setKey(PARAMETER_TYPE.CREDENTIALS.name());
        parameterDescription.setType(Type.JAVA_CLASS);
        parameterDescription.setAdditionalTypeInfo(HashMap.class.getName());
        parameterDescription.setDescription("REQUEST OPTIONS");
        parameterDescriptions.add(parameterDescription);

        actionInfo.setParameterDescription(parameterDescriptions);

        final ParameterInfo returnDescription = new ParameterInfo();
        returnDescription.setKey("return");
        returnDescription.setType(Type.BYTE);
        returnDescription.setArray(true);
        returnDescription.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
        actionInfo.setResultDescription(returnDescription);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public ActionInfo getActionInfo() {
        return this.actionInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   body    DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    @Override
    public GenericResourceWithContentType execute(final Object body, final ServerActionParameter... params) {
        String request = "";
        URL url = null;
        try {
            AccessHandler.ACCESS_METHODS method = AccessHandler.ACCESS_METHODS.GET_REQUEST_NO_TUNNEL;
            HashMap<String, String> options = new HashMap<String, String>();
            HashMap<String, String> credentials = new HashMap<String, String>();

            for (final ServerActionParameter sap : params) {
                if (sap != null) {
                    final Object paramValue = sap.getValue();
                    if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.URL.toString())) {
                        if (paramValue instanceof URL) {
                            url = (URL)paramValue;
                        } else {
                            url = new URL(paramValue.toString());
                        }
                    } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.METHOD.toString())) {
                        if (paramValue instanceof AccessHandler.ACCESS_METHODS) {
                            method = (AccessHandler.ACCESS_METHODS)paramValue;
                        } else {
                            method = AccessHandler.ACCESS_METHODS.valueOf(paramValue.toString());
                        }
                    } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.REQUEST.toString())) {
                        request = paramValue.toString();
                    } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.OPTIONS.toString())
                                && (sap.getValue() != null)) {
                        options = (HashMap)paramValue;
                    } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.CREDENTIALS.toString())
                                && (sap.getValue() != null)) {
                        credentials = (HashMap)paramValue;
                    } else {
                        LOG.warn("ignoring unsupported parameter '" + sap.getKey() + "' = " + paramValue);
                    }
                }
            }

            AccessHandler.ACCESS_METHODS notunnelmethod;
            if (method == AccessHandler.ACCESS_METHODS.GET_REQUEST) {
                notunnelmethod = AccessHandler.ACCESS_METHODS.GET_REQUEST_NO_TUNNEL;
            } else if (method == AccessHandler.ACCESS_METHODS.POST_REQUEST) {
                notunnelmethod = AccessHandler.ACCESS_METHODS.POST_REQUEST_NO_TUNNEL;
            } else if (method == AccessHandler.ACCESS_METHODS.HEAD_REQUEST) {
                notunnelmethod = AccessHandler.ACCESS_METHODS.HEAD_REQUEST_NO_TUNNEL;
            } else {
                throw new RuntimeException(
                    "try to tunnel a request that has a \"*_NO_TUNNEL\" method. This should not happen.");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("tunneled Request for:" + url + "?" + request + " (end of url)");
            }

            final String usermame;
            final String password;
            if (credentials != null) {
                usermame = credentials.get(CREDENTIALS_USERNAME_KEY);
                password = credentials.get(CREDENTIALS_PASSWORD_KEY);
            } else {
                usermame = null;
                password = null;
            }
            final UsernamePasswordCredentials creds;
            if ((usermame != null) && (password != null)) {
                creds = new UsernamePasswordCredentials(usermame, password);
            } else {
                creds = null;
            }

            final SimpleHttpAccessHandler handler = new SimpleHttpAccessHandler();
            final InputStream is = handler.doRequest(
                    url,
                    new StringReader(request),
                    notunnelmethod,
                    options,
                    creds);
            final byte[] result = IOUtils.toByteArray(is);
            if (((result != null) && (result.length > BIG_SIZE_LOG_THRESHOLD))) {
                final double size = ((double)result.length) / ((double)MB);
                final String message = "BIG REQUEST: " + (Math.round(size * 100) / 100.0) + " MB from " + user + "\n"
                            + url + request;
                LOG.info(message);
            }

             return new GenericResourceWithContentType(MediaType.APPLICATION_OCTET_STREAM, result);
        } catch (final BadHttpStatusCodeException badStatusCodeEx) {
            final String errorinfo = ("Problem during HttpTunnelAction(" + url + "=, request=" + request + ")");
            if (LOG.isDebugEnabled()) {
                LOG.error(errorinfo + "\n" + badStatusCodeEx.getMessage(), badStatusCodeEx);
            }
            return null;
        } catch (final CannotReadFromURLException exception) {
            final String errorinfo = ("Problem during HttpTunnelAction(" + url + "=, request=" + request + ")");
            if (LOG.isDebugEnabled()) {
                LOG.error(errorinfo + "\n" + exception.getMessage(), exception);
            }
            return new GenericResourceWithContentType(MediaTypes.APPLICATION_X_JAVA_SERIALIZED_OBJECT, exception);
        } catch (final Exception exception) {
            final String errorinfo = ("Problem during HttpTunnelAction(" + url + "=, request=" + request + ")");
            if (LOG.isDebugEnabled()) {
                LOG.error(errorinfo + "\n" + exception.getMessage(), exception);
            }
            throw new RuntimeException(errorinfo, exception);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getTaskName() {
        return TASK_NAME;
    }
}
