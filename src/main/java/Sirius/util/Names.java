/*
 * Names.java
 *
 * Created on 24. Mai 2004, 08:37
 */

package Sirius.util;

/**
 *
 * @author  awindholz
 */
public class Names extends Sirius.metajdbc.interpreter.cids.area.system.Names {
    
    /** Creates a new instance of Names */
    private Names() {}
    
    public static class URL {
        
        private static final String tableName = "URL";
        
        public static final String URL_BASE_ID = "url_base_id";
        
        public static String getName() {
            return tableName;
        }
    }
    
    public static class URL_BASE{
        
        private static final String tableName = "URL_BASE";
        
        public static final String PROT_PREFIX = "prot_prefix";
        public static final String SERVER = "server";
        public static final String PATH = "path";
        
        public static String getName() {
            return tableName;
        }
    }
    
    /*public static class Data_Object {
        
        private static final String tableName = "Data_Object";
        
        public static final String ACCESS_PARAMETER = "Access_Parameter";
        
        public static String getName() {
            return tableName;
        }
    }*/
    
    public static class Data_Object_Type {
        
        private static final String tableName = "DATA_OBJECT_TYPE";
        
        public static final String ACCESS_PARAMETER = "ACCESS_PARAMETER";
        
        public static String getName() {
            return tableName;
        }
    }
    
    public static class Access_Parameter {
        
        private static final String tableName = "ACCESS_PARAMETER";
        
        public static final String DATA_SOURCE_CLASS = "DATA_SOURCE_CLASS";
        
        public static String getName() {
            return tableName;
        }
    }
}
