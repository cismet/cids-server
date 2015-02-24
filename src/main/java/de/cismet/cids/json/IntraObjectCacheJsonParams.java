/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class IntraObjectCacheJsonParams {

    //~ Instance fields --------------------------------------------------------

    private int maxLevel = -1;
    private final Collection<String> expandPropNames = new ArrayList<String>();
    private final Collection<String> fieldsPropNames = new ArrayList<String>();
    private boolean omitNull = false;
    private boolean cacheDuplicates = true;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<String> getFieldsPropNames() {
        return fieldsPropNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fieldsPropNames  DOCUMENT ME!
     */
    public void setFieldsPropNames(final Collection<String> fieldsPropNames) {
        this.fieldsPropNames.clear();
        if (fieldsPropNames != null) {
            this.fieldsPropNames.addAll(fieldsPropNames);
            for (final String propName : fieldsPropNames) {
                final List<String> splitted = Arrays.asList(propName.split("\\."));
                for (int index = 1; index < splitted.size(); index++) {
                    final List<String> subPropList = splitted.subList(0, index);
                    final String subProp = implode(subPropList.toArray(new String[0]), ".");
                    if (!this.fieldsPropNames.contains(subProp)) {
                        this.fieldsPropNames.add(subProp);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<String> getExpandPropNames() {
        return expandPropNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  expandPropNames  DOCUMENT ME!
     */
    public void setExpandPropNames(final Collection<String> expandPropNames) {
        this.expandPropNames.clear();
        if (expandPropNames != null) {
            this.expandPropNames.addAll(expandPropNames);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  maxLevel  DOCUMENT ME!
     */
    public void setMaxLevel(final int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isOmitNull() {
        return omitNull;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  omitNull  DOCUMENT ME!
     */
    public void setOmitNull(final boolean omitNull) {
        this.omitNull = omitNull;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCacheDuplicates() {
        return cacheDuplicates;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cacheDuplicates  DOCUMENT ME!
     */
    public void setCacheDuplicates(final boolean cacheDuplicates) {
        this.cacheDuplicates = cacheDuplicates;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stringArray  DOCUMENT ME!
     * @param   delimiter    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String implode(final String[] stringArray, final String delimiter) {
        if (stringArray.length == 0) {
            return "";
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append(stringArray[0]);
            for (int index = 1; index < stringArray.length; index++) {
                sb.append(delimiter);
                sb.append(stringArray[index]);
            }
            return sb.toString();
        }
    }
}
