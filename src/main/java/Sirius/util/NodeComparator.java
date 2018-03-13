/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AdressComparator.java
 *
 * Created on 20. Oktober 2005, 13:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package Sirius.util;

import Sirius.server.middleware.types.Node;

import java.math.BigInteger;

import java.util.*;
import java.util.regex.*;

/**
 * Der Comparator splitet strings und benutzt als Delimiter ganze Zahlen. Beim Splitten beh\u00E4lt er die delimiter bei
 * und vergleicht dann jedes element des splitergebnisses
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class NodeComparator implements java.util.Comparator<Node> {

    //~ Static fields/initializers ---------------------------------------------

    // ganze Zahl
    static Pattern p = Pattern.compile("[0-9]+"); // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AdressComparator.
     */
    public NodeComparator() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   o1  DOCUMENT ME!
     * @param   o2  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compare(final Node o1, final Node o2) {
        final String o1String = ((o1 != null) && (o1.getName() != null)) ? o1.getName() : "";
        final String o2String = ((o2 != null) && (o2.getName() != null)) ? o2.getName() : "";

        int balance = 0;

        if (o1String.equals(o2String)) {          // beide sind gleich zur\u00FCck 0
            return balance;
        } else if (o1String.contains(o2String)) { // der eine ist ein Substring des anderen
            return 1;
        } else if (o2String.contains(o1String)) {
            return -1;
        } else {
            final Object[] o1s = split(o1String, 0);

            // maximal gleiche l\u00E4nge beim vergleich
            final Object[] o2s = split(o2String, o1s.length);

            for (int i = 0; i < o2s.length; i++) {
                if (o1s[i] instanceof java.math.BigInteger /*&& o2s[i] instanceof java.math.BigInteger*/) {
                    balance = ((BigInteger)o1s[i]).compareTo((BigInteger)o2s[i]);
                } else if (o1s[i] instanceof java.lang.String) {
                    balance = ((String)o1s[i]).compareTo(o2s[i].toString());
                }

                if (balance != 0) {
                    return balance;
                }
            }

            return balance;
        }
    }
    /**
     * this works like the standard split method except that it keeps the delimiter.
     *
     * @param   input  DOCUMENT ME!
     * @param   limit  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object[] split(final CharSequence input, final int limit) {
        int index = 0;
        final boolean matchLimited = limit > 0;
        final ArrayList matchList = new ArrayList();

        // p is static
        final Matcher m = p.matcher(input);

        // Add segments before each match found
        while (m.find()) {
            if (!matchLimited || (matchList.size() < (limit - 1))) {
                final String match = input.subSequence(index, m.start()).toString();
                matchList.add(match);

                // add splitcharacter
                final String delimiter = input.subSequence(m.start(), m.end()).toString();

                matchList.add(new BigInteger(delimiter));
                index = m.end();
            } else if (matchList.size() == (limit - 1)) { // last one
                final String match = input.subSequence(index, input.length()).toString();
                matchList.add(match);
                index = m.end();
            }
        }

        // If no match was found, return this
        if (index == 0) {
            return new String[] { input.toString() };
        }

        // Add remaining segment
        if (!matchLimited || (matchList.size() < limit)) {
            matchList.add(input.subSequence(index, input.length()).toString());
        }

        // Construct result
        int resultSize = matchList.size();
        if (limit == 0) {
            while ((resultSize > 0) && matchList.get(resultSize - 1).equals("")) { // NOI18N
                resultSize--;
            }
        }
        final Object[] result = new Object[resultSize];
        return (Object[])matchList.subList(0, resultSize).toArray(result);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final String s = "Flurst\u00FCck: 218 / 2";   // NOI18N
        final String s2 = "Flurst\u00FCck: 218 / 11"; // NOI18N

        System.out.println("STringcompare:: " + s.compareTo(s2)); // NOI18N

        final Object[] as = split(s, 0);

        for (int i = 0; i < as.length; i++) {
            System.out.println(as[i]);
        }

        final Comparator c = new NodeComparator();

        System.out.println("nodecompare " + c.compare(s, s2)); // NOI18N
    }
}
