/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * TransactionExecTest.java
 *
 * Created on 10. August 2004, 14:01
 */
package Sirius.server.transaction;
import java.util.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class TransactionExecTest {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TransactionExecTest.
     */
    public TransactionExecTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final t test = new t();

        final String mama = "printMama";
        final String param = "luv ya ";

        final Object[] params = new Object[1];

        params[0] = param;

        final TransactionExecuter tex = new TransactionExecuter(test);

        final Transaction x = new Transaction(mama, params);

        final ArrayList l = new ArrayList();

        l.add(x);

        // ----------

        tex.execute(l);

        // ------------
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class t {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new t object.
     */
    t() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  param  DOCUMENT ME!
     */
    public void printMama(final String param) {
        System.out.println(param + " MAMA");
    }
}
