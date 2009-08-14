/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Sirius.server.middleware.types;

/**
 *
 * @author thorsten
 */
public interface MetaClassStore {
    public MetaClass getMetaClass();
    public void setMetaClass(MetaClass metaClass);
}
