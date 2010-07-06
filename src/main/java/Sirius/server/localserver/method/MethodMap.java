/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.method;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MethodMap extends java.util.HashMap implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 1088110127510028259L;

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodMap.class);
//constructor

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MethodMap object.
     */
    public MethodMap() {
        super();
    }
    /**
     * constructor.
     *
     * @param  capacity  DOCUMENT ME!
     * @param  factor    DOCUMENT ME!
     */
    public MethodMap(final int capacity, final float factor) {
        super(capacity, factor);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * //////////////////////////////////////////////////
     *
     * @param   key     DOCUMENT ME!
     * @param   method  DOCUMENT ME!
     *
     * @throws  Exception            DOCUMENT ME!
     * @throws  java.lang.Exception  DOCUMENT ME!
     */
    public void add(final String key, final Method method) throws Exception {
        if (!this.containsMethod(key)) {
            super.put(method.getKey(), method);
        } else {
            final Method m = this.getMethod(key);
            m.getPermissions().addPermissions(method.getPermissions()); // add only permissions
            m.getClassKeys().addAll(method.getClassKeys());

            if (m.isClassMultiple() != method.isClassMultiple()) { // wenn unterschiedliche Angaben wird alles auf
                                                                   // false gesetzt
                m.c_multiple = false;
            }
            if (m.isMultiple() != method.isMultiple()) {
                m.o_multiple = false;
            }
        }

        if (!containsMethod(method.getKey())) {
            throw new java.lang.Exception("Couldn't add Method:" + method.getKey());   // NOI18N
        }
    } // end add

    /**
     * /////////////////////////////////////////////////////////////
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception                       DOCUMENT ME!
     * @throws  java.lang.NullPointerException  DOCUMENT ME!
     */
    public Method getMethod(final String key) throws Exception {
        if (containsMethod(key)) {
            final java.lang.Object candidate = super.get(key);
            if (candidate instanceof Method) {
                return (Method)candidate;
            }
            throw new java.lang.NullPointerException("Entry is not a Method :" + key);   // NOI18N
        } // endif

        throw new java.lang.NullPointerException("No entry Method :" + key);    // NOI18N // to be changed in further versions
                                                                             // when exception concept is
                                                                             // accomplished
    }                                                                        // end getMethod

    /**
     * ///// containsIKey/////////////////////////////////
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsMethod(final java.lang.Object key) {
        return super.containsKey(key);
    }

// \u00FCberschreiben da berechtigungen f\u00FCr gleiche methoden auf verschiedenen ls vergeben werden

    @Override
    public void putAll(final Map t) {
        if ((t == null) || t.isEmpty()) {
            return;
        }

        final Iterator i = t.values().iterator();
        while (i.hasNext()) {
            final Method e = (Method)i.next();
            try {
                this.add((String)e.getKey(), e);
            } catch (Exception ex) {
                if (logger != null) {
                    logger.error("Error while adding method (putAll)", ex);   // NOI18N
                } else {
                    System.err.println("Error while adding method (putAll)/n" + ex.getMessage());   // NOI18N
                }
            }
        }
    }

//    public void rehash()
//    {super.rehash();}

} // end of class MethodMap
