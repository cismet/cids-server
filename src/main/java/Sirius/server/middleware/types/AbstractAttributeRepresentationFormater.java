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
package Sirius.server.middleware.types;

import java.io.Serializable;

import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractAttributeRepresentationFormater implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private Map<String, Object> attributes;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getRepresentation();

    /**
     * DOCUMENT ME!
     *
     * @param  attributes  DOCUMENT ME!
     */
    void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public final Object getAttribute(String aName) {
        if (attributes == null) {
            throw new IllegalStateException("Attribute map has not been initialized (is null)!");
        }
        if (aName != null) {
            aName = aName.toLowerCase();
        }
        return attributes.get(aName);
    }
}
