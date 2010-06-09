/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DefaultResultHandler.java
 *
 * Created on 26. September 2003, 15:04
 */
package Sirius.server.sql;
import Sirius.server.search.*;

import java.sql.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class DefaultResultHandler implements ResultHandler {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a new instance of DefaultResultHandler.
     *
     * @param   rs  DOCUMENT ME!
     * @param   q   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     * @throws  Exception     DOCUMENT ME!
     */

    @Override
    public Object handle(final ResultSet rs, final Query q) throws SQLException, Exception {
        // java.lang.Class resultType =q.getResultType();

        final Vector handledResult = new Vector(100, 100);

        // konstruktorparameter

        final int length = rs.getMetaData().getColumnCount();

        // rs.beforeFirst();
        while (rs.next()) {
            final Object[] values = new Object[length];

            for (int i = 0; i < values.length; i++) {
                values[i] = rs.getObject(i + 1);
            }

            handledResult.add(values);
        }

        handledResult.trimToSize();
        if (logger.isDebugEnabled()) {
            logger.debug("suchergebnis" + handledResult);
        }

        // if(!(resultType.newInstance() instanceof Createable))
        // {
        // return handledResult;
        //
        // }
        // else //result is Createable
        // {
        // Createable c = (Createable )resultType.newInstance();
        //
        // Vector tmp = new Vector(handledResult.size());
        //
        // for(int i =0;i < handledResult.size();i++)
        // tmp.add(c.newInstance((Object[])handledResult.get(i)));
        // //xxx vielleicht in die Schleife oben mit reinziehen
        //
        // return tmp;
        //
        //
        // }

        // auskommentieren wenn obiges wieder aktuell
        return handledResult;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   v  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toString(final Vector v) {
        final StringBuffer buf = new StringBuffer();
        buf.append("(");

        final Iterator i = v.iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            final Object o = i.next();
            buf.append(String.valueOf(o));
            hasNext = i.hasNext();
            if (hasNext) {
                buf.append(",");
            }
        }

        buf.append(")");
        return buf.toString();
    }
}
