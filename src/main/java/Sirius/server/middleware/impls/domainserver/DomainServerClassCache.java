/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.domainserver;

import Sirius.server.middleware.types.MetaClass;

import java.util.HashMap;

import de.cismet.cids.utils.MetaClassUtils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DomainServerClassCache {

    //~ Static fields/initializers ---------------------------------------------

    private static DomainServerClassCache instance;

    //~ Instance fields --------------------------------------------------------

    private HashMap allClassesById = null;
    private HashMap allClassesByTableName = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DomainServerClassCache object.
     */
    private DomainServerClassCache() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DomainServerClassCache getInstance() {
        if (instance == null) {
            synchronized (DomainServerClassCache.class) {
                if (instance == null) {
                    instance = new DomainServerClassCache();
                }
            }
        }
        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classArray  DOCUMENT ME!
     * @param  domain      DOCUMENT ME!
     */
    public void setAllClasses(final MetaClass[] classArray, final String domain) {
        this.allClassesById = MetaClassUtils.getClassHashtable(classArray, domain);
        this.allClassesByTableName = MetaClassUtils.getClassByTableNameHashtable(classArray);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    HashMap getAllClasses() {
        return allClassesById;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaClass getMetaClass(final String tableName) {
        return (MetaClass)allClassesByTableName.get(tableName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaClass getMetaClass(final int classId) {
        return (MetaClass)allClassesById.get(classId);
    }
}
