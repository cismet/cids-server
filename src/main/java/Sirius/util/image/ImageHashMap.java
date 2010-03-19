/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.util.image;

import java.util.*;

import java.io.*;

import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ImageHashMap extends HashMap implements java.io.Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageHashMap object.
     *
     * @param  images  DOCUMENT ME!
     */
    public ImageHashMap(Image[] images) {
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
    public ImageHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Creates a new ImageHashMap object.
     *
     * @param  names  DOCUMENT ME!
     * @param  files  DOCUMENT ME!
     */
    public ImageHashMap(String[] names, String[] files) {
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
    public ImageIcon put(String name, String file) {
        ImageIcon icon = new ImageIcon(new Image(file).getImageData(), name);
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
    public ImageIcon put(Image image) {
        ImageIcon icon = new ImageIcon(image.getImageData(), image.getName());
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
    public ImageIcon put(File file) {
        ImageIcon icon = new ImageIcon(new Image(file).getImageData(), file.getName().trim());
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
    public ImageIcon get(String name) {
        return (ImageIcon)super.get(name);
    }
}
