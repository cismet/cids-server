/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class DefaultScheduledServerActionTestImpl extends DefaultScheduledServerAction {

    //~ Static fields/initializers ---------------------------------------------

    public static String TESTPARAM = "blatestparam";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String createKey(final ServerActionParameter... params) {
        return getTaskName();
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final StringBuffer result = new StringBuffer("testResult... input was: ");
        for (final ServerActionParameter param : params) {
            if (TESTPARAM.equals(param.getKey())) {
                result.append(TESTPARAM);
            }
        }
        return result.toString();
    }

    @Override
    public String getTaskName() {
        return "testAction";
    }

    @Override
    public ServerActionParameter[] jsonToParams(final String json) throws IOException {
        final List<ServerActionParameter> params = new ArrayList<ServerActionParameter>();
        final List<LinkedHashMap> rawParams = new ObjectMapper().readValue(json, List.class);
        for (final LinkedHashMap rawHm : rawParams) {
            final String key = (String)rawHm.get("key");
            if (TESTPARAM.equals(key)) {
                final Long value = ((Integer)rawHm.get("value")).longValue();
                params.add(new ServerActionParameter(key, value));
            }
        }
        return params.toArray(new ServerActionParameter[0]);
    }
}
