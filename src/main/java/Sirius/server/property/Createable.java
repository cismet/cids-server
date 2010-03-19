/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.property;

/**
 * zeigt an das eine methode createObject existiert welche aus einem string ein Object erzeugt.
 *
 * @version  $Revision$, $Date$
 */
public interface Createable {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   constructorArgs  DOCUMENT ME!
     * @param   delimiter        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    java.lang.Object createObject(java.lang.String constructorArgs, String delimiter);
}
