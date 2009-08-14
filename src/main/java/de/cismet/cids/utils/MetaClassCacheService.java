/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cids.utils;

import Sirius.server.middleware.types.MetaClass;
import java.util.Hashtable;

/**
 *
 * @author thorsten
 */
public interface MetaClassCacheService {
    public MetaClass getMetaClass(String domain,String tableName);
    public MetaClass getMetaClass(String domain,int classId);
    public Hashtable getAllClasses(String domain);
}
