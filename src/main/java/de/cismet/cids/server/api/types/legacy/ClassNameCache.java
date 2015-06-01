/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.api.types.legacy;

import Sirius.server.middleware.types.MetaClass;
import de.cismet.cids.server.ws.rest.RESTfulInterfaceConnector;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

/**
 * Helper Class for maintaining a class name cache that stores the mappings 
 * from legacy meta class id to a class name (MetaClass.tableName) that is needed
 * to construct the $self reference (/domain/tableName).of the REST Cids Class
 * 
 * @author Pascal Dihé
 */
public class ClassNameCache {
    
    private static final transient Logger LOG = Logger.getLogger(ClassNameCache.class);
    private final Map<String, Map<String, String>> classNameCache;
    
    public ClassNameCache() {
        classNameCache = new HashMap<String, Map<String, String>>();
    }
    
     /**
     * Helper Method for filling a class key cache that stores the mappings 
     * from class id to class key (domain/tableName).
     * 
     * @param domain the domain of the classes
     * @param metaClasses an array of legacy meta classes
     * @return true if the cache was filled
     */
    public boolean fillCache(final String domain, final MetaClass[] metaClasses) {
        
        if (!this.isDomainCached(domain) 
                && metaClasses != null && metaClasses.length > 0) {
            LOG.info("class key cache for domain '" + domain + "' is empty, filling with "
                            +  metaClasses.length + " meta class ids");
            final Map classKeyMap = new HashMap<String, String>();
            for(final MetaClass metaClass:metaClasses) {
                classKeyMap.put(Integer.toString(metaClass.getID()), metaClass.getTableName());
            }
            classNameCache.put(domain, classKeyMap);
            
            return true;
        }
        
        return false;       
    }
    
    /**
     * Returns true if the classes for the domain have been cached
     * 
     * @param domain the domain to be chached
     * @return true if classes for the domain are cached
     */
    public boolean isDomainCached(final String domain) {
        return this.classNameCache.containsKey(domain);
    }
    
    /**
     * Returns the name (table name) of a legacy meta class with the specified id
     * for the specified domain. 
     * 
     * @param domain domain of the meta class
     * @param classId legacy class id of the meta class
     * @return name (table name) of the legacy meta class or null if the id or domain is not cached
     */
    public String getClassNameForClassId(final String domain, final int classId) {
        return this.getClassNameForClassId(domain, Integer.toString(classId));
    }
    
    /**
     * Returns the name (table name) of a legacy meta class with the specified id
     * for the specified domain. 
     * 
     * @param domain domain of the meta class
     * @param classId legacy class id of the meta class
     * @return name (table name) of the legacy meta class or null if the id or domain is not cached
     */
    public String getClassNameForClassId(final String domain, String classId) {
        
        if (!this.isDomainCached(domain)) {
            LOG.error("class name cache does not contain class ids for domain '" + domain
                    + "', need to fill the cache first!");
            return null;
        }

        Map<String, String> classNameMap = this.classNameCache.get(domain);
        if (classNameMap == null || classNameMap.isEmpty()) {
            final String message = "could not find classes for domain '" + domain + "', class name map is empty!";
            LOG.error(message);
            return null;
        }

        if (!classNameMap.containsKey(classId)) {
            final String message = "could not find class with id '" + classId
                    + "' at domain '" + domain + "', class name map does not contain id.";
            LOG.error(message);
            return null;
        }

        final String className = classNameMap.get(classId);
        return className;
    }
    
    /**
     * Returns the id of a legacy meta class with the specified name (table name)
     * for the specified domain. 
     * 
     * @param domain domain of the meta class
     * @param className class name of the meta class
     * @return id of the legacy meta class or -1 if the name or domain is not cached
     */
    public int getClassIdForClassName(final String domain, final String className) {
        if (!this.isDomainCached(domain)) {
            LOG.error("class name cache does not contain class names for domain '" + domain
                    + "', need to fill the cache first!");
            return -1;
        }
        
        Map<String, String> classNameMap = this.classNameCache.get(domain);
        if (classNameMap == null || classNameMap.isEmpty()) {
            final String message = "could not find classes for domain '" 
                    + domain + "', class name map is empty!";
            LOG.error(message);
            return -1;
        }

        for(Entry<String, String> entry: classNameMap.entrySet()) {
            if(entry.getValue().equalsIgnoreCase(className)) {
                final int classId = Integer.valueOf(entry.getKey());
                return classId;
            }
        }
        
        final String message = "could not find class id for name '"+className+"' for domain '" 
                    + domain + "' in class name map!";
            LOG.error(message);
            return -1;
    }
}
