/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Names.java
 *
 * Created on 24. Mai 2004, 08:37
 */
package Sirius.util;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class Names{

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Names.
     */
    private Names() {
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class URL {

        //~ Static fields/initializers -----------------------------------------

        private static final String tableName = "URL";

        public static final String URL_BASE_ID = "url_base_id";

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public static String getName() {
            return tableName;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class URL_BASE {

        //~ Static fields/initializers -----------------------------------------

        private static final String tableName = "URL_BASE";

        public static final String PROT_PREFIX = "prot_prefix";
        public static final String SERVER = "server";
        public static final String PATH = "path";

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public static String getName() {
            return tableName;
        }
    }

    /**
     * public static class Data_Object {. private static final String tableName = "Data_Object"; public static final
     * String ACCESS_PARAMETER = "Access_Parameter"; public static String getName() { return tableName; } }
     *
     * @version  $Revision$, $Date$
     */
    public static class Data_Object_Type {

        //~ Static fields/initializers -----------------------------------------

        private static final String tableName = "DATA_OBJECT_TYPE";

        public static final String ACCESS_PARAMETER = "ACCESS_PARAMETER";

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public static String getName() {
            return tableName;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Access_Parameter {

        //~ Static fields/initializers -----------------------------------------

        private static final String tableName = "ACCESS_PARAMETER";

        public static final String DATA_SOURCE_CLASS = "DATA_SOURCE_CLASS";

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public static String getName() {
            return tableName;
        }
    }
}
