/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FromStringCreator.java
 *
 * Created on 26. August 2004, 13:41
 */
package de.cismet.cids.tools.fromstring;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public abstract class FromStringCreator implements java.io.Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of FromStringCreator.
     */
    public FromStringCreator() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   objectRepresentation  DOCUMENT ME!
     * @param   hull                  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public abstract Object create(String objectRepresentation, Object hull) throws Exception;
}
