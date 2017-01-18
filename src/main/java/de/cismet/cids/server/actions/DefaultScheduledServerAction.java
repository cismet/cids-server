/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class DefaultScheduledServerAction implements ScheduledServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            DefaultScheduledServerAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ScheduledServerActionImpl object.
     */
    public DefaultScheduledServerAction() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   rule  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerActionParameter<String> createExecutionRuleSAP(final String rule) {
        return new ServerActionParameter<String>(SSAPK_RULE, rule);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   startTime  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerActionParameter<Date> createStartTimeSAP(final Date startTime) {
        return new ServerActionParameter<Date>(SSAPK_START, startTime);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerActionParameter<String> createAbortionSAP() {
        return new ServerActionParameter<String>(SSAPK_ABORT, null);
    }

    @Override
    public String paramsToJson(final ServerActionParameter[] params) throws IOException {
        final String json = new ObjectMapper().writeValueAsString(params);
        return json;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    @Override
    public ServerActionParameter[] jsonToParams(final String json) throws IOException {
        final List<ServerActionParameter> params = new ArrayList<ServerActionParameter>();
        final List<LinkedHashMap> rawParams = new ObjectMapper().readValue(json, List.class);
        for (final LinkedHashMap rawHm : rawParams) {
            final String key = (String)rawHm.get("key");
            params.add(new ServerActionParameter(key, rawHm.get("value")));
        }
        return params.toArray(new ServerActionParameter[0]);
    }

    @Override
    public String bodyToJson(final Object body) throws IOException {
        if (body == null) {
            return null;
        }
        final String json = new ObjectMapper().writeValueAsString(body);
        return json;
    }

    @Override
    public Object jsonToBody(final String json) throws IOException {
        if (json == null) {
            return null;
        }
        final Object body = new ObjectMapper().readValue(json, Object.class);
        return body;
    }

    @Override
    public String resultToJson(final Object result) throws IOException {
        if (result == null) {
            return null;
        }
        final String json = new ObjectMapper().writeValueAsString(result);
        return json;
    }

    @Override
    public Object jsonToResult(final String json) throws IOException {
        if (json == null) {
            return null;
        }
        final Object result = new ObjectMapper().readValue(json, Object.class);
        return result;
    }

    @Override
    public ServerActionParameter[] getNextParams(final ServerActionParameter... params) {
        return params;
    }
}
