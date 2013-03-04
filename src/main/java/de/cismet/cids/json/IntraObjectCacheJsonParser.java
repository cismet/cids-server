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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

import java.util.HashMap;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class IntraObjectCacheJsonParser extends JsonParserDelegate {

    //~ Instance fields --------------------------------------------------------

    HashMap<String, CidsBean> ioc = new HashMap<String, CidsBean>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IntraObjectCacheJsonParser object.
     *
     * @param  d  DOCUMENT ME!
     */
    public IntraObjectCacheJsonParser(final JsonParser d) {
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
}
