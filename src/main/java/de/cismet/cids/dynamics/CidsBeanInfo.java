/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.dynamics;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class CidsBeanInfo {

    //~ Static fields/initializers ---------------------------------------------

    public static final String JSON_CIDS_OBJECT_KEY_IDENTIFIER = "$self";
    public static final String JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER = "$ref";
    public static final String JSON_CIDS_OBJECT_PATCH_ADD_SUFFIX = "$patch$add";
    public static final String JSON_CIDS_OBJECT_PATCH_REMOVE_SUFFIX = "$patch$remove";
    public static final String JSON_CIDS_OBJECT_PATCH_UPDATE_SUFFIX = "$patch$update";

    //~ Instance fields --------------------------------------------------------

    String classKey;
    String domainKey;
    String objectKey;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsBeanInfo object.
     */
    public CidsBeanInfo() {
    }

    /**
     * Creates a new CidsBeanInfo object.
     *
     * @param  jsonObjectKey  DOCUMENT ME!
     */
    public CidsBeanInfo(final String jsonObjectKey) {
        // Class and domain
        final String classAndDomain = jsonObjectKey.substring(jsonObjectKey.indexOf("/") + 1,
                jsonObjectKey.lastIndexOf("/"));
        final String[] cdParts = classAndDomain.split("\\.");
        domainKey = cdParts[0];
        classKey = cdParts[1];

        // Objectkey
        objectKey = jsonObjectKey.substring(jsonObjectKey.lastIndexOf("/") + 1);
    }

    /**
     * Creates a new CidsBeanInfo object.
     *
     * @param  domainKey  DOCUMENT ME!
     * @param  classKey   DOCUMENT ME!
     * @param  objectKey  DOCUMENT ME!
     */
    public CidsBeanInfo(final String domainKey, final String classKey, final String objectKey) {
        this.classKey = classKey;
        this.domainKey = domainKey;
        this.objectKey = objectKey;
    }

    /**
     * Creates a new CidsBeanInfo object.
     *
     * @param  domainKey  DOCUMENT ME!
     * @param  classKey   DOCUMENT ME!
     * @param  objectPK   DOCUMENT ME!
     */
    public CidsBeanInfo(final String domainKey, final String classKey, final Integer objectPK) {
        this.classKey = classKey;
        this.domainKey = domainKey;
        if (objectPK != null) {
            this.objectKey = String.valueOf(objectPK);
        } else {
            this.objectKey = null;
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getClassKey() {
        return classKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classKey  DOCUMENT ME!
     */
    public void setClassKey(final String classKey) {
        this.classKey = classKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomainKey() {
        return domainKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domainKey  DOCUMENT ME!
     */
    public void setDomainKey(final String domainKey) {
        this.domainKey = domainKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectKey  DOCUMENT ME!
     */
    public void setObjectKey(final String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getJsonObjectKey() {
        return new StringBuffer("/").append(domainKey)
                    .append('.')
                    .append(classKey)
                    .append('/')
                    .append(objectKey)
                    .toString();
    }

    @Override
    public String toString() {
        return getJsonObjectKey();
    }
}
