/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * StatementMap.java
 *
 * Created on 22. November 2003, 09:56
 */
package Sirius.server.sql;
import java.util.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class StatementMap extends Hashtable {

    //~ Constructors -----------------------------------------------------------

    /**
     * constructor///////////////////// constructor.
     *
     * @param  capacity  DOCUMENT ME!
     */
    public StatementMap(int capacity) {
        super(capacity);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * /////////////////////////////////////
     *
     * @param   key    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception            DOCUMENT ME!
     * @throws  java.lang.Exception  DOCUMENT ME!
     */
    public void add(int key, SystemStatement value) throws Exception {
        Integer Key = new Integer(key);
        super.put(Key, value);

        if (!super.containsKey(Key)) {
            throw new java.lang.Exception("Couldn't add SystemStatement ID:" + key);//NOI18N
        }
    } // end add

    /**
     * ///////////////////////////////////////
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception                       DOCUMENT ME!
     * @throws  java.lang.NullPointerException  DOCUMENT ME!
     */
    public SystemStatement getStatement(int key) throws Exception {
        Integer Key = new Integer(key); // map accepts objects only
        if (super.containsKey(Key)) {
            java.lang.Object candidate = super.get(Key);

            if (candidate instanceof SystemStatement) {
                return (SystemStatement)candidate;
            }

            throw new java.lang.NullPointerException("Entry is not a SystemStatement ID:" + key);//NOI18N
        } // endif

        throw new java.lang.NullPointerException("No entry ID :" + key); // to be changed in further versions when//NOI18N
                                                                         // exception concept is accomplished
    }                                                                    // end getStatemnt

    /**
     * ///// containsIntKey/////////////////////////////////
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsIntKey(int key) {
        return super.containsKey(new Integer(key));
    }

    public void rehash() {
        super.rehash();
    }
} // end of class StatementMap
