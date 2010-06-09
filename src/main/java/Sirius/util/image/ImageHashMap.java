/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.util.image;

import java.io.*;

import java.util.*;

import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ImageHashMap extends HashMap implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -2422765008443979812L;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageHashMap object.
     *
     * @param  images  DOCUMENT ME!
     */
    public ImageHashMap(final Image[] images) {
        super(images.length, 1.0f);
        for (int i = 0; i < images.length; i++) {
            this.put(images[i]);
        }
    }
    /**
     * Creates a new ImageHashMap object.
     *
     * @param  initialCapacity  DOCUMENT ME!
     * @param  loadFactor       DOCUMENT ME!
     */
    public ImageHashMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Creates a new ImageHashMap object.
     *
     * @param  names  DOCUMENT ME!
     * @param  files  DOCUMENT ME!
     */
    public ImageHashMap(final String[] names, final String[] files) {
        super(names.length, 1.0f);

        if (names.length == files.length) {
            for (int i = 0; i < names.length; i++) {
                this.put(names[i], files[i]);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     * @param   file  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageIcon put(final String name, final String file) {
        final ImageIcon icon = new ImageIcon(new Image(file).getImageData(), name);
        super.put(name, icon);
        return this.get(name);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   image  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageIcon put(final Image image) {
        final ImageIcon icon = new ImageIcon(image.getImageData(), image.getName());
        super.put(image.getName(), icon);
        return this.get(image.getName());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   file  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageIcon put(final File file) {
        final ImageIcon icon = new ImageIcon(new Image(file).getImageData(), file.getName().trim());
        super.put(file.getName().trim(), icon);
        return this.get(file.getName().trim());
    }
    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageIcon get(final String name) {
        return (ImageIcon)super.get(name);
    }
}
