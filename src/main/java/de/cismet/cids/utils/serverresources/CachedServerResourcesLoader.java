/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.utils.serverresources;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.io.IOUtils;

import java.awt.Font;

import java.io.InputStream;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CachedServerResourcesLoader {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            CachedServerResourcesLoader.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Type {

        //~ Enum constants -----------------------------------------------------

        JASPER_REPORT, TEXT, TRUETYPE_FONT
    }

    //~ Instance fields --------------------------------------------------------

    private final Map<String, Object> resourceMap = new HashMap<String, Object>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CachedResourceLoader object.
     */
    private CachedServerResourcesLoader() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  resource  DOCUMENT ME!
     */
    public void loadJasperReportResource(final JasperReportServerResources resource) {
        final String resourceValue = resource.getValue();
        loadResource(resourceValue, Type.JASPER_REPORT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  resource  DOCUMENT ME!
     */
    public void loadTextResource(final TextServerResources resource) {
        final String resourceValue = resource.getValue();
        loadResource(resourceValue, Type.TEXT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  resource  DOCUMENT ME!
     */
    public void loadTruetypeFontResource(final TruetypeFontServerResources resource) {
        final String resourceValue = resource.getValue();
        loadResource(resourceValue, Type.TRUETYPE_FONT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  resourcePath  DOCUMENT ME!
     * @param  type          DOCUMENT ME!
     */
    private void loadResource(final String resourcePath, final Type type) {
        final String resourceKey = getResourceKey(resourcePath, type);
        if (LOG.isDebugEnabled()) {
            LOG.debug("ResourceLoader loads: " + resourceKey);
        }

        final InputStream inputStream = CachedServerResourcesLoader.class.getResourceAsStream(resourcePath);
        Object resource;
        if (type != null) {
            try {
                switch (type) {
                    case TEXT: {
                        resource = IOUtils.toString(inputStream);
                    }
                    break;
                    case JASPER_REPORT: {
                        resource = JRLoader.loadObject(inputStream);
                    }
                    break;
                    case TRUETYPE_FONT: {
                        resource = Font.createFont(Font.TRUETYPE_FONT, inputStream);
                    }
                    break;
                    default: {
                        resource = null;
                    }
                }
            } catch (final Exception ex) {
                LOG.warn("Exception while loading resource: " + resourceKey, ex);
                resource = ex;
            }
        } else {
            resource = null;
        }

        resourceMap.put(resourceKey, resource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   resource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public JasperReport getJasperReportResource(final JasperReportServerResources resource) throws Exception {
        final String resourceValue = resource.getValue();
        if (!resourceMap.containsKey(getResourceKey(resourceValue, Type.JASPER_REPORT))) {
            loadJasperReportResource(resource);
        }
        return (JasperReport)getResource(resourceValue, Type.JASPER_REPORT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   resource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public String getStringResource(final TextServerResources resource) throws Exception {
        final String resourceValue = resource.getValue();
        if (!resourceMap.containsKey(getResourceKey(resourceValue, Type.TEXT))) {
            loadTextResource(resource);
        }
        return (String)getResource(resourceValue, Type.TEXT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   resource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public StringReader getStringReaderResource(final TextServerResources resource) throws Exception {
        return new StringReader(getStringResource(resource));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   resource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Font getTrueTypeFontResource(final TruetypeFontServerResources resource) throws Exception {
        final String resourceValue = resource.getValue();
        if (!resourceMap.containsKey(getResourceKey(resourceValue, Type.TRUETYPE_FONT))) {
            loadTruetypeFontResource(resource);
        }
        return (Font)getResource(resourceValue, Type.TRUETYPE_FONT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   resourcePath  DOCUMENT ME!
     * @param   type          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Object getResource(final String resourcePath, final Type type) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resource requested from ResourcePreloader [" + type.toString() + "]:" + resourcePath,
                new Exception());
        }
        final String resourceKey = getResourceKey(resourcePath, type);
        final Object resource = resourceMap.get(resourceKey);
        if (resource instanceof Exception) {
            final Exception ex = (Exception)resource;
            LOG.warn("resource request failed: " + resourceKey, ex);
            throw ex;
        } else {
            return resource;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   resourcePath  DOCUMENT ME!
     * @param   type          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String getResourceKey(final String resourcePath, final Type type) {
        return "[" + type.toString() + "]" + resourcePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CachedServerResourcesLoader getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final CachedServerResourcesLoader INSTANCE = new CachedServerResourcesLoader();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
