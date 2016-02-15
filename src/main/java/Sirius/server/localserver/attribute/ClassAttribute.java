/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import Sirius.server.newuser.permission.Policy;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ClassAttribute extends Attribute implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    public static final String HISTORY_ENABLED = "history_enabled";       // NOI18N
    public static final String HISTORY_OPTION_ANONYMOUS = "anonymous";    // NOI18N
    public static final String CACHE_ENABLED = "cache"; // NOI18N
    public static final String TO_STRING_CACHE_ENABLED = "tostringcache"; // NOI18N
    public static final String GEOMETRY_CACHE_ENABLED = "geometrycache"; // NOI18N
    public static final String LIGHTWEIGHT_JSON_CACHE_ENABLED = "lightweightcache"; // NOI18N

    private static final transient Logger LOG = Logger.getLogger(ClassAttribute.class);

    //~ Instance fields --------------------------------------------------------

    protected int classID;

    protected int typeID;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassAttribute object.
     *
     * @param  id       DOCUMENT ME!
     * @param  classID  DOCUMENT ME!
     * @param  name     DOCUMENT ME!
     * @param  typeID   DOCUMENT ME!
     * @param  policy   DOCUMENT ME!
     */
    public ClassAttribute(final String id,
            final int classID,
            final String name,
            final int typeID,
            final Policy policy) {
        super(id, name, "", policy); // NOI18N
        this.classID = classID;
        super.visible = true;
        this.typeID = typeID;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final int getClassID() {
        return classID;
    }

    /**
     * Getter for property typeID.
     *
     * @return  Value of property typeID.
     */
    public final int getTypeID() {
        return typeID;
    }

    /**
     * Setter for property typeID.
     *
     * @param  typeID  New value of property typeID.
     */
    public final void setTypeID(final int typeID) {
        this.typeID = typeID;
    }

    /**
     * This method tries to parse options from the value of the class attribute. Option map is built as follows:
     *
     * <ul>
     *   <li>Arbitrary options can be given separated by ",+" (consecutive commas)</li>
     *   <li>An option can be a KV-Pair, separated by "=+" (consecutive equality signs)</li>
     *   <li>If option is not KV-Pair, option value map shall contain an empty string for the option</li>
     *   <li>Options are trimmed</li>
     * </ul>
     *
     * @return  an option key value map, never null.
     */
    public Map<String, String> getOptions() {
        final Map<String, String> options = new HashMap<String, String>(3, 0.8f);

        if (value instanceof String) {
            final String[] optionArray = ((String)value).split(",+");        // NOI18N
            for (final String optionToken : optionArray) {
                final String[] optionKV = optionToken.trim().split("=+", 2); // NOI18N
                final String optionKey = optionKV[0].trim();
                final String optionValue;
                if (optionKV.length == 2) {
                    optionValue = optionKV[1].trim();
                } else {
                    optionValue = "";
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("found option key (" + optionKey + ") and option value (" + optionValue + ")"); // NOI18N
                }

                options.put(optionKey, optionValue);
            }
        }

        return options;
    }
}
