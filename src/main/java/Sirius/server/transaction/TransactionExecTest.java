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
    public static void main(String[] args) {
        t test = new t();

        String mama = "printMama";//NOI18N
        String param = "luv ya ";//NOI18N

        Object[] params = new Object[1];

        params[0] = param;

        TransactionExecuter tex = new TransactionExecuter(test);

        Transaction x = new Transaction(mama, params);

        ArrayList l = new ArrayList();

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
    public void printMama(String param) {
        System.out.println(param + " MAMA");//NOI18N
    }
}
