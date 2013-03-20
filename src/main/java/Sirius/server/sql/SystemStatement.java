/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.search.searchparameter.SearchParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class SystemStatement {

    //~ Instance fields --------------------------------------------------------

    /** parameters for this level of the query. */
    private final Map<Object, List<SearchParameter>> parameters;

    private boolean root;
    private int id;
    private String name;
    private boolean isUpdate;
    private boolean isBatch;
    private boolean isUnion;

    private String statement;

    private int result;

    private String description;

    private boolean conjunction;

    private boolean search;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SystemStatement object.
     *
     * @param  root       DOCUMENT ME!
     * @param  id         DOCUMENT ME!
     * @param  name       DOCUMENT ME!
     * @param  isUpdate   DOCUMENT ME!
     * @param  result     DOCUMENT ME!
     * @param  statement  DOCUMENT ME!
     */
    public SystemStatement(final boolean root,
            final int id,
            final String name,
            final boolean isUpdate,
            final int result,
            final String statement) {
        this.root = root;
        this.id = id;
        this.name = name;
        this.isUpdate = isUpdate;
        this.statement = statement;
        this.result = result;
        this.parameters = new HashMap<Object, List<SearchParameter>>();
        this.isBatch = false;
        this.conjunction = false;
    }

    /**
     * Creates a new SystemStatement object.
     *
     * @param  root         DOCUMENT ME!
     * @param  id           DOCUMENT ME!
     * @param  name         DOCUMENT ME!
     * @param  isUpdate     DOCUMENT ME!
     * @param  isBatch      DOCUMENT ME!
     * @param  result       DOCUMENT ME!
     * @param  statement    DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     */
    public SystemStatement(final boolean root,
            final int id,
            final String name,
            final boolean isUpdate,
            final boolean isBatch,
            final int result,
            final String statement,
            final String description) {
        this(root, id, name, isUpdate, result, statement);
        this.description = description;
        this.isBatch = isBatch;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getID() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getResultType() {
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Map<Object, List<SearchParameter>> getParameters() {
        return parameters;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStatement() {
        return statement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  p  DOCUMENT ME!
     */
    public void addParameter(final SearchParameter p) {
        final Object key = p.getKey();
        List<SearchParameter> params = parameters.get(key);
        if (params == null) {
            params = Collections.synchronizedList(new ArrayList<SearchParameter>());
            parameters.put(key, params);
        }

        if (!params.contains(p)) {
            params.add(p);
        }
    }

    /**
     * Getter for property root.
     *
     * @return  Value of property root.
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * Setter for property root.
     *
     * @param  root  New value of property root.
     */
    public void setRoot(final boolean root) {
        this.root = root;
    }

    /**
     * Getter for property isUpdate.
     *
     * @return  Value of property isUpdate.
     */
    public boolean isUpdate() {
        return isUpdate;
    }

    /**
     * Setter for property isUpdate.
     *
     * @param  isUpdate  New value of property isUpdate.
     */
    public void setIsUpdate(final boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for property isBatch.
     *
     * @return  Value of property isBatch.
     */
    public boolean isBatch() {
        return isBatch;
    }

    /**
     * Setter for property isBatch.
     *
     * @param  isBatch  New value of property isBatch.
     */
    public void setIsBatch(final boolean isBatch) {
        this.isBatch = isBatch;
    }

    /**
     * Getter for property isUnion.
     *
     * @return  Value of property isUnion.
     */
    public boolean isUnion() {
        return isUnion;
    }

    /**
     * Setter for property isUnion.
     *
     * @param  isUnion  New value of property isUnion.
     */
    public void setUnion(final boolean isUnion) {
        this.isUnion = isUnion;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isConjunction() {
        // logger.debug("isConj gerufen result ="+conjunction);
        return conjunction;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conjunction  DOCUMENT ME!
     */
    public void setConjunction(final boolean conjunction) {
        this.conjunction = conjunction;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSearch() {
        return search;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  search  DOCUMENT ME!
     */
    public void setSearch(final boolean search) {
        this.search = search;
    }
}
