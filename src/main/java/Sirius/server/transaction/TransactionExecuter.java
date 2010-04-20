/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * TransactionExecuter.java
 *
 * Created on 10. August 2004, 11:35
 */
package Sirius.server.transaction;
import java.util.*;

import java.lang.reflect.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class TransactionExecuter {

    //~ Instance fields --------------------------------------------------------

    protected Object o;

    protected HashMap methods;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TransactionExecuter.
     *
     * @param  o  DOCUMENT ME!
     */
    public TransactionExecuter(Object o) {
        this.o = o;

        Method[] ms = o.getClass().getMethods();

        methods = new HashMap(ms.length);

        for (int i = 0; i < ms.length; i++) {
            String methodName = ms[i].getName();
            if (logger.isDebugEnabled()) {
                logger.debug("methodname registered  " + methodName);//NOI18N
            }
            methods.put(methodName, ms[i]);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   transactions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int execute(ArrayList transactions) {
        int successfull = 0;

        Iterator iter = transactions.iterator();

        while (iter.hasNext()) {
            Transaction t = (Transaction)iter.next();

            if (methods.containsKey(t.getName())) {
                Method m = (Method)methods.get(t.getName());

                try {
                    m.invoke(o, t.getParams());
                    successfull++;
                } catch (Exception e) {
                    logger.error("failed to execute " + t.getName(), e);//NOI18N
                }
            } else {
                logger.error("failed to execute " + t.getName() + " as method doesn't exist here");//NOI18N
            }
        }

        return successfull;
    }
}
