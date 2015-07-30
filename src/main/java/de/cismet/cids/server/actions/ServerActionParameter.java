/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import java.io.Serializable;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@XmlRootElement
@Data
public class ServerActionParameter<T> implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private final String key;
    private final T value;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    @JsonIgnore
    public static ServerActionParameter[] fromMVMap(final MultivaluedMap<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Params must be non null.");
        }
        int i = 0;
        final ServerActionParameter[] ret = new ServerActionParameter[params.size()];
        for (final String key : params.keySet()) {
            for (final String value : params.get(key)) {
                ret[i++] = new ServerActionParameter<String>(key, value);
            }
        }
        return ret;
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
    @JsonIgnore
    public static ServerActionParameter[] fromMap(final Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("Params must be non null.");
        }
        int i = 0;
        final ServerActionParameter[] ret = new ServerActionParameter[params.size()];
        for (final String key : params.keySet()) {
            final Object value = params.get(key);
            ret[i++] = new ServerActionParameter(key, value);
        }
        return ret;
    }

    @Override
    public String toString() {
        return key + "->" + value;
    }
}
