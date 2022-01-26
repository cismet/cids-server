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
package Sirius.server.localserver.object;

import java.sql.SQLException;

import java.util.Objects;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FieldNotFoundException extends SQLException {

    //~ Instance fields --------------------------------------------------------

    private final String fieldName;
    private final String tableName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FieldNotFoundException object.
     *
     * @param  fieldName  DOCUMENT ME!
     * @param  tableName  DOCUMENT ME!
     */
    public FieldNotFoundException(final String fieldName, final String tableName) {
        super();
        this.fieldName = fieldName;
        this.tableName = tableName;
    }

    /**
     * Creates a new FieldNotFoundException object.
     *
     * @param  message    DOCUMENT ME!
     * @param  t          DOCUMENT ME!
     * @param  fieldName  DOCUMENT ME!
     * @param  tableName  DOCUMENT ME!
     */
    public FieldNotFoundException(final String message,
            final Throwable t,
            final String fieldName,
            final String tableName) {
        super(message, t);
        this.fieldName = fieldName;
        this.tableName = tableName;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTableName() {
        return tableName;
    }

    @Override
    public boolean equals(final java.lang.Object obj) {
        if (obj instanceof FieldNotFoundException) {
            final FieldNotFoundException other = (FieldNotFoundException)obj;

            if ((other.fieldName != null) && (this.fieldName != null) && (other.tableName != null)
                        && (this.tableName != null)) {
                return other.fieldName.equals(this.fieldName) && other.tableName.equals(this.tableName);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (67 * hash) + Objects.hashCode(this.fieldName);
        hash = (67 * hash) + Objects.hashCode(this.tableName);
        return hash;
    }

    @Override
    public String getMessage() {
        if (super.getMessage() == null) {
            return "Could not found field " + fieldName + " from table " + tableName;
        } else {
            return super.getMessage();
        }
    }
}
