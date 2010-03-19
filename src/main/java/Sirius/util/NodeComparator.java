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

import java.util.regex.*;
import java.util.*;

import Sirius.server.middleware.types.Node;

import java.math.BigInteger;

/**
 * Der Comparator splitet strings und benutzt als Delimiter ganze Zahlen. Beim Splitten beh\u00E4lt er die delimiter bei
 * und vergleicht dann jedes element des splitergebnisses
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class NodeComparator implements java.util.Comparator {

    //~ Static fields/initializers ---------------------------------------------

    // ganze Zahl
    static Pattern p = Pattern.compile("[0-9]+");

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
    public int compare(Object o1, Object o2) {
        String o1String = o1.toString();
        String o2String = o2.toString();

        if ((o1 instanceof Node) && (o2 instanceof Node)) {
            o1String = ((Node)o1).getName();
            o2String = ((Node)o2).getName();
        }

        if (o1String == null) {
            o1String = "";
        }
        if (o2String == null) {
            o2String = "";
        }

        int balance = 0;

        if (o1String.equals(o2String)) {              // beide sind gleich zur\u00FCck 0
            return balance;
        } else if (o1String.indexOf(o2String) > -1) { // der eine ist ein Substring des anderen
            return 1;
        } else if (o2String.indexOf(o1String) > -1) {
            return -1;
        } else {
            Object[] o1s = split(o1String, 0);

            // maximal gleiche l\u00E4nge beim vergleich
            Object[] o2s = split(o2String, o1s.length);

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
    public static Object[] split(CharSequence input, int limit) {
        int index = 0;
        boolean matchLimited = limit > 0;
        ArrayList matchList = new ArrayList();

        // p is static
        Matcher m = p.matcher(input);

        // Add segments before each match found
        while (m.find()) {
            if (!matchLimited || (matchList.size() < (limit - 1))) {
                String match = input.subSequence(index, m.start()).toString();
                matchList.add(match);

                // add splitcharacter
                String delimiter = input.subSequence(m.start(), m.end()).toString();

                matchList.add(new BigInteger(delimiter));
                index = m.end();
            } else if (matchList.size() == (limit - 1)) { // last one
                String match = input.subSequence(index, input.length()).toString();
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
            while ((resultSize > 0) && matchList.get(resultSize - 1).equals("")) {
                resultSize--;
            }
        }
        Object[] result = new Object[resultSize];
        return (Object[])matchList.subList(0, resultSize).toArray(result);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(String[] args) {
        String s = "Flurst\u00FCck: 218 / 2";
        String s2 = "Flurst\u00FCck: 218 / 11";

        System.out.println("STringcompare:: " + s.compareTo(s2));

        Object[] as = split(s, 0);

        for (int i = 0; i < as.length; i++) {
            System.out.println(as[i]);
        }

        Comparator c = new NodeComparator();

        System.out.println("nodecompare " + c.compare(s, s2));
    }
}
