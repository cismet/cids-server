/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.search;

import java.util.*;

import java.sql.*;

import Sirius.server.sql.*;
import Sirius.server.newuser.*;
import Sirius.server.middleware.types.*;
import Sirius.server.search.searchparameter.*;

/**
 * Short concise description. Additional verbose description.
 *
 * @param      descriptor  description.
 * @param      value       description.
 * @param      searchMask  description.
 *
 * @return     description.
 *
 * @exception  Exception  description.
 *
 * @version    $Revision$, $Date$
 * @see        package.class
 */
public class SearchOption implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    protected HashSet classes;

    protected HashSet userGroups;

    // Dieser Suchoption zugeordnete Query
    protected Query query;

    ///////////////////////////////////////////////////////////////////////////////////////////

    //~ Constructors -----------------------------------------------------------

    /**
     * Short concise description. Additional verbose description.
     *
     * @param   query  descriptor description.
     *
     * @return  description.
     *
     * @see     package.class
     */
    public SearchOption(Query query) {
        this(query, new HashSet(), new HashSet());
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param  query       DOCUMENT ME!
     * @param  classes     DOCUMENT ME!
     * @param  userGroups  DOCUMENT ME!
     */
    public SearchOption(Query query, HashSet classes, HashSet userGroups) {
        this.query = query;
        this.classes = classes;
        this.userGroups = userGroups;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param   classKey      DOCUMENT ME!
     * @param   userGroupKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isSelectable(String classKey, String userGroupKey) {
        if ((classes.size() > 0) && (userGroups.size() > 0)) {
            return classes.contains(classKey) && userGroups.contains(userGroupKey);
        } else {
            return true;
        }
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param   c    DOCUMENT ME!
     * @param   ugs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isSelectable(Collection c, Collection ugs) {
        if ((classes.size() > 0) && (userGroups.size() > 0)) {
            return classes.containsAll(c) && userGroups.containsAll(ugs);
        } else {
            return true;
        }
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isSelectable(UserGroup ug) {
        if (userGroups.size() > 0) {
            return userGroups.contains(ug.getKey());
        } else {
            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isSelectable(MetaClass c) {
        if (classes.size() > 0) {
            return classes.contains(c.getKey());
        } else {
            return true;
        }
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @return  DOCUMENT ME!
     */
    public final Query getQuery() {
        return query;
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @return  DOCUMENT ME!
     */
    public final String getQueryId() {
        return this.getQuery().getQueryIdentifier().getKey().toString();
    }
    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param  userGroupKey  DOCUMENT ME!
     */
    public final void addUserGroup(String userGroupKey) {
        userGroups.add(userGroupKey);
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param  classKey  DOCUMENT ME!
     */
    public final void addClass(String classKey) {
        classes.add(classKey);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * DOCUMENT ME!
     *
     * @param   parameter  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void setSearchParameter(SearchParameter parameter) throws Exception {
        this.query.setParameter(parameter);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void setDefaultSearchParameter(Object key, Object value) throws Exception {
        this.query.setParameter(new DefaultSearchParameter(key, value, false));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Iterator getParameterNames() {
        return this.query.getParameterKeys();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
}
