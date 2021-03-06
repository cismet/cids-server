/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.newuser.User;

import Sirius.util.Editable;
import Sirius.util.Groupable;
import Sirius.util.Renderable;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.fromstring.StringCreateable;
import de.cismet.cids.tools.tostring.StringConvertable;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public interface MetaObject extends Sirius.server.localserver.object.Object,
    Editable,
    Groupable,
    Renderable,
    StringConvertable,
    StringCreateable,
    Serializable {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    HashMap getAllClasses();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean getBean();

    /**
     * getter for classKey.
     *
     * @return  classKey
     */
    String getClassKey();

    /**
     * getter for complex editor.
     *
     * @return  complex editor
     */
    @Override
    String getComplexEditor();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDebugString();

    /**
     * getter for description.
     *
     * @return  description
     */
    String getDescription();

    /**
     * getter for domain.
     *
     * @return  domain
     */
    String getDomain();

    /**
     * Getter for property editor.
     *
     * @return  Value of property editor.
     */
    String getEditor();

    /**
     * getter for grouping criterion in this case the domain (in the sense of the group by clause in SQL).
     *
     * @return  grouping criterion
     */
    @Override
    String getGroup();
    @Override
    int getId();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Logger getLogger();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MetaClass getMetaClass();

    /**
     * getter for name.
     *
     * @return  name
     */
    String getName();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPropertyString();

    /**
     * getter for renderer.
     *
     * @return  renderer
     */
    @Override
    String getRenderer();

    /**
     * getter for simple editor.
     *
     * @return  siomple editor
     */
    @Override
    String getSimpleEditor();

    /**
     * DOCUMENT ME!
     *
     * @param   classKeys  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection getURLs(Collection classKeys);

    /**
     * DOCUMENT ME!
     *
     * @param   classKeys  DOCUMENT ME!
     * @param   urlNames   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection getURLsByName(Collection classKeys, Collection urlNames);

    /**
     * Getter for property changed.
     *
     * @return  Value of property changed.
     */
    @Deprecated
    boolean isChanged();

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  true, iff the given user has write permission on the object
     */
    boolean hasObjectWritePermission(final User user);

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean hasObjectReadPermission(final User user);

    /**
     * DOCUMENT ME!
     *
     * @param   tester  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean propertyEquals(MetaObject tester);

    /**
     * DOCUMENT ME!
     *
     * @param  classes  DOCUMENT ME!
     */
    void setAllClasses(HashMap classes);

    /**
     * DOCUMENT ME!
     */
    void setAllClasses();

    /**
     * sets the same status for all Objects in the hirarchy recursively.
     *
     * @param  status  DOCUMENT ME!
     */
    void setAllStatus(int status);

    /**
     * DOCUMENT ME!
     */
    void setArrayKey2PrimaryKey();

    /**
     * Setter for property changed.
     *
     * @param  changed  New value of property changed.
     */
    @Deprecated
    void setChanged(boolean changed);

    /**
     * Setter for property editor.
     *
     * @param  editor  New value of property editor.
     */
    void setEditor(String editor);

    /**
     * DOCUMENT ME!
     *
     * @param  metaClass  DOCUMENT ME!
     */
    void setMetaClass(MetaClass metaClass);

    /**
     * setter for the primary key sets the value of the attribute being primary key.
     *
     * @param   key  value of the key
     *
     * @return  whether a primary key was found and its value set
     */
    boolean setPrimaryKey(Object key);

    /**
     * Setter for property renderer.
     *
     * @param  renderer  New value of property renderer.
     */
    void setRenderer(String renderer);

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String toString(HashMap classes);
}
