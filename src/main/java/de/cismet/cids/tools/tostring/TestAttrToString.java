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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TestAttrToString.
     */
    public TestAttrToString() {
    }

    //~ Methods ----------------------------------------------------------------

    public String convert(de.cismet.cids.tools.tostring.StringConvertable o) {
        return "TestMops";//NOI18N
    }
}
