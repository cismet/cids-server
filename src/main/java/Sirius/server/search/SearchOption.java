/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.search;

import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;
import Sirius.server.search.searchparameter.*;
import Sirius.server.sql.*;

import java.sql.*;

import java.util.*;

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
    public SearchOption(final Query query) {
        this(query, new HashSet(), new HashSet());
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param  query       DOCUMENT ME!
     * @param  classes     DOCUMENT ME!
     * @param  userGroups  DOCUMENT ME!
     */
    public SearchOption(final Query query, final HashSet classes, final HashSet userGroups) {
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
    public final boolean isSelectable(final String classKey, final String userGroupKey) {
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
    public final boolean isSelectable(final Collection c, final Collection ugs) {
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
    public final boolean isSelectable(final UserGroup ug) {
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
    public final boolean isSelectable(final MetaClass c) {
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
    public final void addUserGroup(final String userGroupKey) {
        userGroups.add(userGroupKey);
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////
     *
     * @param  classKey  DOCUMENT ME!
     */
    public final void addClass(final String classKey) {
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
    public void setSearchParameter(final SearchParameter parameter) throws Exception {
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
    public void setDefaultSearchParameter(final Object key, final Object value) throws Exception {
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
