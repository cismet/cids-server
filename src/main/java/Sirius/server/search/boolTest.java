/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * boolTest.java
 *
 * Created on 9. August 2006, 14:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.search;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class boolTest {

    //~ Instance fields --------------------------------------------------------

    private boolean c = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of boolTest.
     */
    public boolTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  c  DOCUMENT ME!
     */
    public void setC(final boolean c) {
        this.c = c;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean getC() {
        return c;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final boolTest b = new boolTest();

        System.out.println(b.getC());
        final boolean a = true;
        b.setC(a);
        System.out.println(b.getC());
    }
}
