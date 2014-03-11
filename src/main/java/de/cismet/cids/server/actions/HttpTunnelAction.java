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

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringReader;

import java.net.URL;

import java.util.HashMap;

import de.cismet.security.AccessHandler;
import de.cismet.security.WebAccessManager;

import de.cismet.security.handler.DefaultHTTPAccessHandler;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class HttpTunnelAction implements ServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            HttpTunnelAction.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        URL, REQUEST, METHOD, OPTIONS
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        try {
            String request = "";
            URL url = null;
            AccessHandler.ACCESS_METHODS method = AccessHandler.ACCESS_METHODS.GET_REQUEST_NO_TUNNEL;
            HashMap<String, String> options = new HashMap<String, String>();

            for (final ServerActionParameter sap : params) {
                if (sap.getKey().equals(PARAMETER_TYPE.URL.toString())) {
                    url = (URL)sap.getValue();
                } else if (sap.getKey().equals(PARAMETER_TYPE.METHOD.toString())) {
                    method = (AccessHandler.ACCESS_METHODS)sap.getValue();
                } else if (sap.getKey().equals(PARAMETER_TYPE.REQUEST.toString())) {
                    request = (String)sap.getValue();
                } else if (sap.getKey().equals(PARAMETER_TYPE.OPTIONS.toString()) && (sap.getValue() != null)) {
                    options = (HashMap<String, String>)sap.getValue();
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
            final AccessHandler handler = new DefaultHTTPAccessHandler();
            final InputStream is = handler.doRequest(
                    url,
                    new StringReader(request),
                    notunnelmethod,
                    options);
            final byte[] result = IOUtils.toByteArray(is);
            return result;
        } catch (Exception exception) {
            LOG.error("Problem during HttpTunnelAction", exception);
            throw new RuntimeException("Problem during HttpTunnelAction", exception);
        }
    }

    @Override
    public String getTaskName() {
        return "httpTunnelAction";
    }
}
