/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import java.io.Serializable;

import javax.ws.rs.core.MultivaluedMap;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ServerActionParameter implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private final String key;
    private final String value;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerActionParameter object.
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public ServerActionParameter(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getKey() {
        return key;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getValue() {
        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public static ServerActionParameter[] fromMVMap(final MultivaluedMap<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Params must be non null.");
        }
        int i = 0;
        final ServerActionParameter[] ret = new ServerActionParameter[params.size()];
        for (final String key : params.keySet()) {
            for (final String value : params.get(key)) {
                ret[i++] = new ServerActionParameter(key, value);
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return key + "->" + value;
    }
}
