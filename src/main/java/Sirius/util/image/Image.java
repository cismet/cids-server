/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.util.image;

import java.io.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Image implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------


    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /////////////////members//////////////////////////////////

    private byte[] imageData;
    private String name;
    private String description;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Image object.
     */
    public Image() {
        imageData = new byte[0];
        name = "noname";
        description = "nodescription";
    }

    /**
     * /////////////////constructor///////////////////////////////
     *
     * @param  filepath  DOCUMENT ME!
     */
    public Image(final String filepath) {
        this(new File(filepath));
    }

    /**
     * Creates a new Image object.
     *
     * @param  inFile  DOCUMENT ME!
     */
    public Image(final File inFile) {
        try {
            final InputStream stream;
            name = inFile.getName();
            description = new String("nn");
            imageData = new byte[(int)inFile.length()];
            stream = new FileInputStream(inFile);

            // read the file into imageData
            final int bytesRead = stream.read(imageData, 0, (int)inFile.length());

            if (bytesRead == -1) { // error occured during readingprocess
                throw new Exception("read fehlgeschlagen");
            } else if (bytesRead != (int)inFile.length()) {
                throw new Exception("Bildinformation wahrscheinlich Fehlerhaft");
            }

            stream.close();
        } catch (java.io.FileNotFoundException fnfe) {
            logger.warn(" File not found: ", fnfe);
            System.err.println("INFO :: File not found: " + fnfe.getMessage());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ////////////////methods////////////////////////////////////////////////
     *
     * @return  DOCUMENT ME!
     */
    public final byte[] getImageData() {
        return imageData;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDescription() {
        return description;
    }
}
