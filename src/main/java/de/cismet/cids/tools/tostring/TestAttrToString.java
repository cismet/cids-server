/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * TestAttrToString.java
 *
 * Created on 28. Mai 2007, 15:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.tools.tostring;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class TestAttrToString extends GeometryStringConverter {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -1024080852301058710L;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TestAttrToString.
     */
    public TestAttrToString() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String convert(final de.cismet.cids.tools.tostring.StringConvertable o) {
        return "TestMops";//NOI18N
    }
}
