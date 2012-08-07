/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SystemStatement.java
 *
 * Created on 21. November 2003, 18:19
 */
package Sirius.server.sql;
import Sirius.server.search.searchparameter.*;

import Sirius.util.collections.MultiMap;

import java.util.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class SystemStatement {

    //~ Instance fields --------------------------------------------------------

    /** parameters for this level of the query. */
    protected MultiMap parameters;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private boolean root;
    private int id;
    private String name;
    private boolean isUpdate;
    private boolean isBatch;
    private boolean isUnion;

    //
    private String statement;

    private int result;

    private String description;

    private boolean conjunction;

    private boolean search;

    //~ Constructors -----------------------------------------------------------

    /**
     * -----------------------------------------------------------------------------------
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
        this.parameters = new MultiMap();
        this.isBatch = false;
        this.conjunction = false;
    }

    /**
     * -----------------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------------------

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
    public MultiMap getParameters() {
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

    // public boolean toBePrepared(){return toBePrepared;}

    /**
     * DOCUMENT ME!
     *
     * @param  p  DOCUMENT ME!
     */
    public void addParameter(final SearchParameter p) {
        parameters.put(p.getKey(), p);
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
        // logger.debug("vor dem setzen setConj gerufen neu ="+conjunction  );
        this.conjunction = conjunction;
        // logger.debug(" nach dem setzensetConj gerufen neu ="+conjunction );
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
} // end of class SystemStatement
