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
package de.cismet.cids.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;

import java.io.IOException;

import java.util.HashMap;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class IntraObjectCacheJsonGenerator extends JsonGeneratorDelegate {

    //~ Instance fields --------------------------------------------------------

    HashMap<String, CidsBean> ioc = new HashMap<String, CidsBean>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IntraObjectCacheJsonGenerator object.
     *
     * @param  d  DOCUMENT ME!
     */
    public IntraObjectCacheJsonGenerator(final JsonGenerator d) {
        super(d);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsKey(final String key) {
        return ioc.containsKey(key);
    }
    /**
     * DOCUMENT ME!
     */
    public void clear() {
        ioc.clear();
    }
    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBean get(final String key) {
        return ioc.get(key);
    }
    /**
     * DOCUMENT ME!
     *
     * @param  key   DOCUMENT ME!
     * @param  bean  DOCUMENT ME!
     */
    public void put(final String key, final CidsBean bean) {
        ioc.put(key, bean);
    }

    @Override
    public void writeObject(final Object value) throws IOException, JsonProcessingException {
        super.writeObject(value);
    }
}
