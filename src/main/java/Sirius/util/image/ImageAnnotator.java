/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ImageAnotator.java
 *
 * Created on 13. Januar 2005, 10:07
 */
package Sirius.util.image;

import javax.imageio.*;

import java.io.*;

import javax.imageio.stream.*;

import java.net.URL;


//import java.net.MalformedURLException;
import java.awt.image.BufferedImage;
import java.awt.*;

import java.util.*;

//import com.sun.media.jai.codec.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ImageAnnotator {

    //~ Static fields/initializers ---------------------------------------------

    static int LINES = 3;

    //~ Instance fields --------------------------------------------------------

    BufferedImage image = null;
    BufferedImage watermark = null;

    int width = 0;
    int height = 0;

    int newHeight = 0;
    int newWidth = 0;

    String text = "";

    float ratio = 1.05f;

    java.awt.Color textColor = new Color(14, 68, 122);

    java.awt.Color backGroundColor = new java.awt.Color(148, 190, 232);

    String filename = "";

    boolean printFilename = true;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ImageAnotator.
     *
     * @param   filename           DOCUMENT ME!
     * @param   watermarkFilename  DOCUMENT ME!
     *
     * @throws  java.io.IOException  DOCUMENT ME!
     */
    public ImageAnnotator(String filename, String watermarkFilename) throws java.io.IOException {
        this.filename = filename;

        image = loadImage(filename);

        this.width = image.getWidth();
        this.height = image.getHeight();

        watermark = loadImage(watermarkFilename);
    }

    /**
     * Creates a new ImageAnnotator object.
     *
     * @param   filename           DOCUMENT ME!
     * @param   watermarkFilename  DOCUMENT ME!
     * @param   text               DOCUMENT ME!
     *
     * @throws  java.io.IOException  DOCUMENT ME!
     */
    public ImageAnnotator(String filename, String watermarkFilename, String text) throws java.io.IOException {
        this(filename, watermarkFilename);

        this.text = text;
    }

    /**
     * Creates a new ImageAnnotator object.
     *
     * @param   file       DOCUMENT ME!
     * @param   watermark  DOCUMENT ME!
     * @param   text       DOCUMENT ME!
     *
     * @throws  java.io.IOException  DOCUMENT ME!
     */
    public ImageAnnotator(URL file, URL watermark, String text) throws java.io.IOException {
        this.filename = filename;

        image = loadImage(file);

        this.width = image.getWidth();
        this.height = image.getHeight();

        try {
            this.watermark = loadImage(watermark);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.text = text;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   filename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.io.IOException  DOCUMENT ME!
     */
    protected BufferedImage loadImage(String filename) throws java.io.IOException {
        String type = extractImageType(filename);

        if (type.length() == 0) {
            type = "TIF";
        }

        // load
        File f = new File(filename);
        if (logger.isDebugEnabled()) {
            logger.debug("Info : file :" + filename + " file " + f);
        }

        Iterator readers = ImageIO.getImageReadersByFormatName(type);
        ImageReader reader = (ImageReader)readers.next();

        ImageInputStream iis = ImageIO.createImageInputStream(f);

        reader.setInput(iis, false);

        int imageIndex = 0;

        // width = reader.getWidth(imageIndex);
        // height = reader.getHeight(imageIndex);

        return reader.read(imageIndex);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   imgUrl  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.io.IOException  DOCUMENT ME!
     */
    protected BufferedImage loadImage(URL imgUrl) throws java.io.IOException {
        String type = extractImageType(imgUrl.getFile());

        if (type.length() == 0) {
            type = "TIF";
        }

        // retrieve appropriate imagereader
        Iterator readers = ImageIO.getImageReadersByFormatName(type);

        ImageReader reader = (ImageReader)readers.next();
        if (logger.isDebugEnabled()) {
            logger.debug(imgUrl);
        }

        ImageInputStream iis = ImageIO.createImageInputStream(imgUrl.openStream());

        reader.setInput(iis, false);

        int imageIndex = 0;

        // width = reader.getWidth(imageIndex);
        // height = reader.getHeight(imageIndex);

        return reader.read(imageIndex);
    }

    /**
     * Getter for property text.
     *
     * @return  Value of property text.
     */
    public java.lang.String getText() {
        return text;
    }

    /**
     * Setter for property text.
     *
     * @param  text  New value of property text.
     */
    public void setText(java.lang.String text) {
        this.text = text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  text  DOCUMENT ME!
     */
    public void addText(java.lang.String text) {
        this.text += text;
    }

    /**
     * Getter for property ratio.
     *
     * @return  Value of property ratio.
     */
    public float getRatio() {
        return ratio;
    }

    /**
     * Setter for property ratio.
     *
     * @param  ratio  New value of property ratio.
     */
    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    /**
     * Getter for property textColor.
     *
     * @return  Value of property textColor.
     */
    public java.awt.Color getTextColor() {
        return textColor;
    }

    /**
     * Setter for property textColor.
     *
     * @param  textColor  New value of property textColor.
     */
    public void setTextColor(java.awt.Color textColor) {
        this.textColor = textColor;
    }

    /**
     * Getter for property backGroundColor.
     *
     * @return  Value of property backGroundColor.
     */
    public java.awt.Color getBackGroundColor() {
        return backGroundColor;
    }

    /**
     * Setter for property backGroundColor.
     *
     * @param  backGroundColor  New value of property backGroundColor.
     */
    public void setBackGroundColor(java.awt.Color backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    /**
     * Getter for property image.
     *
     * @return  Value of property image.
     */
    public java.awt.image.BufferedImage getImage() {
        return image;
    }

    /**
     * Setter for property image.
     *
     * @param  image  New value of property image.
     */
    public void setImage(java.awt.image.BufferedImage image) {
        this.image = image;
    }

    /**
     * Getter for property watermark.
     *
     * @return  Value of property watermark.
     */
    public java.awt.image.BufferedImage getWatermark() {
        return watermark;
    }

    /**
     * Setter for property watermark.
     *
     * @param  watermark  New value of property watermark.
     */
    public void setWatermark(java.awt.image.BufferedImage watermark) {
        this.watermark = watermark;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public BufferedImage getAnnotatedImage() {
        newHeight = (int)(height * ratio);
        newWidth = width; // evtl ver\u00E4ndern aber heapspace

        BufferedImage bi = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics g = bi.getGraphics();

        // Wuppercolor (Hintergrund)
        g.setColor(backGroundColor);

        // f\u00FClle mit Hintergrundfarbe
        g.fillRect(0, 0, newWidth, newHeight);

        if (watermark != null) {
            // wasserzeichen
            g.drawImage(
                watermark,
                newWidth - (watermark.getWidth() + 5),
                newHeight - (watermark.getHeight() + 5),
                null);
        }

        // bild zeichnen
        g.drawImage(image, 0, 0, null);

        // grenzt ein bischen besser ab
        g.setColor(Color.black);
        g.drawLine(0, height, width, height);

        if (text.length() > 0) {
            drawText(g);
        }

        g.setColor(Color.white);
        Font f = new Font(g.getFont().getName(), Font.ITALIC, g.getFont().getSize());
        g.setFont(f);

        if (printFilename) {
            g.drawString("Bilddatei{" + filename + "}", 10, newHeight - 10);
        }

        return bi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  g  DOCUMENT ME!
     */
    protected void drawText(Graphics g) {
        // Alle absoluten Zahlen sind heuristiken

        g.setColor(textColor);

        int fontSize = (newHeight - height) / LINES; // mx 3 zeilen
        Font f = new Font("COURIER", Font.BOLD, fontSize);

        g.setFont(f);

        int stringPos = height + fontSize - 4;
        int from = 0;
        int to = 0;

        FontMetrics fm = g.getFontMetrics();

        int stringWidth = fm.stringWidth(text);

        // breite pro character (bei courier) ansonsten im Mittel !!!!!!!!!!! vorsicht
        double ppc = (stringWidth / text.length());
        if (logger.isDebugEnabled()) {
            logger.debug("breite pro character" + ppc + " text " + text.length());
        }

        // Anzahl der zeichen einer Zeile
        int block = (int)(width / ppc) - 2; // 2 zeichen rand

        // anzahl der Bl\u00F6cke
        int count = (text.length() / block) + 1;

        // Blockweise Schreiben
        for (int i = 0; i < count; i++) {
            if ((to + block) > text.length()) {
                to = text.length();
            } else {
                to += block;
            }
            if (logger.isDebugEnabled()) {
                logger.debug(from + " " + to + " BLOCK." + block);
            }

            String drawString = text.substring(from, to);

            int space = drawString.lastIndexOf(" ");

            if ((space != (drawString.length() - 1)) && (drawString.length() == block)) {
                to = space + 1;
                drawString = drawString.substring(0, to);
            }

            g.drawString(drawString, (int)ppc, stringPos);

            stringPos += fontSize - 2;

            from = to;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   im        DOCUMENT ME!
     * @param   filename  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void saveImage(BufferedImage im, String filename) throws IOException {
        String type = extractImageType(filename);

        if (type.length() == 0) {
            type = "TIF";
        }

        Iterator writers = ImageIO.getImageWritersByFormatName(type);
        ImageWriter writer = (ImageWriter)writers.next();

        File f = new File(filename);
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);

        writer.write(im);
    }

    /**
     * public void writeImage(BufferedImage im,Object output, String type) throws java.io.IOException { if(type == null
     * || type.length()==0) type="TIF"; Iterator writers = ImageIO.getImageWritersByFormatName(type); ImageWriter writer
     * = (ImageWriter)writers.next(); ImageOutputStream ios = ImageIO.createImageOutputStream(output);
     * //writer.setOutput(ios); writer.write(im); }.
     *
     * @param   filename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String extractImageType(String filename) {
        String type = "";
        int endingIndex = filename.lastIndexOf(".");
        ;

        if (endingIndex > 0) {
            type = filename.substring(endingIndex + 1, filename.length());
        }

        return type;
    }

    /**
     * Getter for property printFilename.
     *
     * @return  Value of property printFilename.
     */
    public boolean isPrintFilename() {
        return printFilename;
    }

    /**
     * Setter for property printFilename.
     *
     * @param  printFilename  New value of property printFilename.
     */
    public void setPrintFilename(boolean printFilename) {
        this.printFilename = printFilename;
    }

//    public BufferedImage scale(BufferedImage im, double sx, double sy)
//    {
//
//            im.createGraphics().scale(sx, sy);
//
//            return im;
//
//
//    }

    /**
     * DOCUMENT ME!
     *
     * @param   args  the command line arguments
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {
        String txt =
            "Freie Verwendung f\u00FCr den internen Dienstgebrauch, Publikation oder Weitergabe nur mit Einzelgenehmigung von R102 oder gem\u00E4\u00DF Rahmenvereinbarung mit R102";

        URL imgUrl = new URL("http", "localhost:8084/wm/images/", "wupp1");
        URL wUrl = new URL("http", "localhost:8084/wm/images/", "wupperwurm.gif");

        // ImageAnnotator t = new ImageAnnotator("c:\\1.tif","c:\\wupperwurm.gif",txt);

        ImageAnnotator t = new ImageAnnotator(imgUrl, wUrl, txt);

        // String txt = "Freie Verwendung f\u00FCr den internen Dienstgebrauch. Publikation oder Weitergabe
        // gem\u00E4\u00DF Rahmenvereinbarung mit R102 oder Einzelgenehmigung.";

        t.saveImage(t.getAnnotatedImage(), "c:\\3.tif");

        System.out.println("done");
    }
}
