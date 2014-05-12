/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CidsBeanDeepPropertyListener implements PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    private CidsBean rootBean;
    private String property;
    private CidsBean[] beanPath;
    private String[] propertyPath;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsBeanDeepPropertyListener object.
     *
     * @param  rootBean        DOCUMENT ME!
     * @param  deepProperties  DOCUMENT ME!
     */
    public CidsBeanDeepPropertyListener(final CidsBean rootBean, final String deepProperties) {
        this.rootBean = rootBean;
        this.property = deepProperties;
        refreshPath();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        // refresh falls zwischenbean sich verändert hat
        for (int index = 0; index < (beanPath.length - 1); index++) {
            final CidsBean oldCidsBean = beanPath[index];
            if (evt.getSource().equals(oldCidsBean) && evt.getPropertyName().equals(propertyPath[index])) {
                refreshPath();
                final String[] subArray = new String[index + 1];
                System.arraycopy(propertyPath, 0, subArray, 0, index + 1);
                propertyChangeSupport.firePropertyChange(implode(subArray, "."), oldCidsBean, beanPath[index]);
                return;
            }
        }

        final int lastIndex = propertyPath.length - 1;
        if ((propertyPath != null) && (beanPath != null) && evt.getPropertyName().equals(propertyPath[lastIndex])
                    && evt.getSource().equals(beanPath[lastIndex])) {
            propertyChangeSupport.firePropertyChange(property, evt.getOldValue(), evt.getNewValue());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stringArray  DOCUMENT ME!
     * @param   delimiter    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String implode(final String[] stringArray, final String delimiter) {
        if (stringArray.length == 0) {
            return "";
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append(stringArray[0]);
            for (int index = 1; index < stringArray.length; index++) {
                sb.append(delimiter);
                sb.append(stringArray[index]);
            }
            return sb.toString();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBean getBean() {
        return rootBean;
    }
    /**
     * Add PropertyChangeListener.
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshPath() {
        // erstmal sauber machen
        if (beanPath != null) {
            for (final CidsBean followBean : beanPath) {
                if (followBean != null) {
                    followBean.removePropertyChangeListener(this);
                }
            }
        }
        propertyPath = new String[0];
        beanPath = new CidsBean[0];

        // ist property gesetzt ?
        if (property != null) {
            // property zerstückeln und platz für die entsprechenden beans schaffen
            propertyPath = property.split("\\.");
            beanPath = new CidsBean[propertyPath.length];

            // erste bean ist immer die basisbean
            beanPath[0] = rootBean;
            for (int index = 0; index < propertyPath.length; index++) {
                final CidsBean cidsBeanAtIndex = beanPath[index];
                if (cidsBeanAtIndex != null) {
                    cidsBeanAtIndex.addPropertyChangeListener(this);
                    // nächste bean setzen außer für die letzte property
                    if (index < (propertyPath.length - 1)) {
//                        PropertyUtils.getProperty(followBeans[index], followProps[index]);
                        beanPath[index + 1] = (CidsBean)beanPath[index].getProperty(propertyPath[index]);
                    }
                } else {
                    break;
                }
            }
        }
    }
}
