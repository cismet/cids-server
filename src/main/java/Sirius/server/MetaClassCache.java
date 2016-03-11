/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

import Sirius.server.middleware.types.MetaClass;

import java.util.HashMap;

import de.cismet.cids.utils.MetaClassUtils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class MetaClassCache {

    //~ Instance fields --------------------------------------------------------

    private final HashMap<String, HashMap> allIdClassCaches = new HashMap<String, HashMap>();
    private final HashMap<String, HashMap> allTableNameClassCaches = new HashMap<String, HashMap>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaClassCache object.
     */
    private MetaClassCache() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MetaClassCache getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     */
    public void clearCache() {
        allIdClassCaches.clear();
        allTableNameClassCaches.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    public void clearCache(final String domain) {
        allIdClassCaches.remove(domain);
        allTableNameClassCaches.remove(domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classArray  DOCUMENT ME!
     * @param  domain      DOCUMENT ME!
     */
    public void setAllClasses(final MetaClass[] classArray, final String domain) {
        allIdClassCaches.put(domain, MetaClassUtils.getClassHashtable(classArray, domain));
        allTableNameClassCaches.put(domain, MetaClassUtils.getClassByTableNameHashtable(classArray));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap getAllClasses(final String domain) {
        return allIdClassCaches.get(domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain     DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaClass getMetaClass(final String domain, final String tableName) {
        return (MetaClass)allTableNameClassCaches.get(domain).get(tableName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain   DOCUMENT ME!
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaClass getMetaClass(final String domain, final int classId) {
        return (MetaClass)allIdClassCaches.get(domain).get(domain + classId);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final MetaClassCache INSTANCE = new MetaClassCache();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
