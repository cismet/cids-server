/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.tostring;

import de.cismet.cids.annotations.CidsAttribute;

import de.cismet.cids.tools.CustomToStringConverter;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class UrlConverter extends CustomToStringConverter implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -688440112385974683L;

    //~ Instance fields --------------------------------------------------------

    @CidsAttribute("URL_BASE_ID.PROT_PREFIX") // NOI18N
    public String prot;

    @CidsAttribute("URL_BASE_ID.SERVER") // NOI18N
    public String server;

    @CidsAttribute("URL_BASE_ID.PATH") // NOI18N
    public String path;

    @CidsAttribute("OBJECT_NAME") // NOI18N
    public String name;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String createString() {
        return prot + server + path + name;
    }
}
