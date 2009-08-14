/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.types;

import Sirius.util.Editable;
import Sirius.util.Groupable;
import Sirius.util.Renderable;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.tools.fromstring.StringCreateable;
import de.cismet.cids.tools.tostring.StringConvertable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 *
 * @author srichter
 */
public interface MetaObject extends Sirius.server.localserver.object.Object, Editable, Groupable, Renderable, StringConvertable, StringCreateable, Serializable {

    /**
     * method fo rthe visitor pattern (resolves recursion)
     * @param mov
     * @param o
     * @return
     */
    Object accept(TypeVisitor mov, Object o);

    Hashtable getAllClasses();

    CidsBean getBean();

    /**
     * getter for classKey
     * @return classKey
     */
    String getClassKey();

    /**
     * getter for complex editor
     * @return complex editor
     */
    String getComplexEditor();

    String getDebugString();

    /**
     * getter for description
     * @return description
     */
    String getDescription();

    /**
     * getter for domain
     * @return domain
     */
    String getDomain();

    /**
     * Getter for property editor.
     * @return Value of property editor.
     */
    String getEditor();

    /**
     * getter for grouping criterion in this case the domain
     * (in the sense of the group by clause in SQL)
     * @return grouping criterion
     */
    String getGroup();

    int getId();

    Logger getLogger();

    MetaClass getMetaClass();

    /**
     * getter for name
     * @return name
     */
    String getName();

    String getPropertyString();

    /**
     * getter for renderer
     * @return renderer
     */
    String getRenderer();

    /**
     * getter for simple editor
     * @return siomple editor
     */
    String getSimpleEditor();

    Collection getURLs(Collection classKeys);

    Collection getURLsByName(Collection classKeys, Collection urlNames);

    /**
     * Getter for property changed.
     * @return Value of property changed.
     */
    boolean isChanged();

    boolean propertyEquals(MetaObject tester);

    void setAllClasses(Hashtable classes);

    void setAllClasses();

    /**
     * sets the same status for all Objects in the hirarchy recursively
     */
    void setAllStatus(int status);

    void setArrayKey2PrimaryKey();

    /**
     * Setter for property changed.
     * @param changed New value of property changed.
     */
    void setChanged(boolean changed);

    /**
     * Setter for property editor.
     * @param editor New value of property editor.
     */
    void setEditor(String editor);

    void setLogger();

    void setMetaClass(MetaClass metaClass);

    /**
     * setter for the primary key
     * sets the value of the attribute being primary key
     * @param key value of the key
     * @return whether a primary key was found and its value set
     */
    boolean setPrimaryKey(Object key);

    /**
     * Setter for property renderer.
     * @param renderer New value of property renderer.
     */
    void setRenderer(String renderer);

    /**
     *
     * @param classes
     * @return
     */
    String toString(HashMap classes);
    
}
