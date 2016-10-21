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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;

import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ServerResourcesLoader {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ServerResourcesLoader.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Type {

        //~ Enum constants -----------------------------------------------------

        JASPER_REPORT, TEXT, BINARY
    }

    //~ Instance fields --------------------------------------------------------

    private String resourcesBasePath = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CachedResourceLoader object.
     */
    private ServerResourcesLoader() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  resourcesBasePath  DOCUMENT ME!
     */
    public void setResourcesBasePath(final String resourcesBasePath) {
        this.resourcesBasePath = resourcesBasePath;
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
    public JasperReport loadJasperReportResource(final String resource) throws Exception {
        return (JasperReport)loadResource(resource, Type.JASPER_REPORT);
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
    public String loadTextResource(final String resource) throws Exception {
        return (String)loadResource(resource, Type.TEXT);
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
    public byte[] loadBinaryResource(final String resource) throws Exception {
        return (byte[])loadResource(resource, Type.BINARY);
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
    private Object loadResource(final String resourcePath, final Type type) throws Exception {
        final String resourceKey = getResourceKey(resourcePath, type);
        LOG.info("ResourceLoader loading " + resourceKey);

        try {
            if (resourcesBasePath == null) {
                throw new Exception("resourcesBasePath is null");
            }
            final String resourceFullPath = resourcesBasePath + resourcePath;
            final File resourceFile = new File(resourceFullPath);
            if (!resourceFile.exists()) {
                throw new Exception(resourcePath + " not found in " + resourcesBasePath);
            }
            if (!resourceFile.canRead()) {
                throw new Exception("can't read " + resourcePath + " in " + resourcesBasePath);
            }
            final InputStream inputStream = new FileInputStream(resourceFile);
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
                        case BINARY: {
                            resource = IOUtils.toByteArray(inputStream);
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

            return resource;
        } catch (final Exception ex) {
            LOG.warn("ResourceLoader failed on loading " + resourceKey, ex);
            throw ex;
        }
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
    public Properties loadPropertiesResource(final String resource) throws Exception {
        final Properties properties = new Properties();
        properties.load(loadStringReaderResource(resource));
        return properties;
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
    public StringReader loadStringReaderResource(final String resource) throws Exception {
        return new StringReader(loadTextResource(resource));
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
        return "[" + type.toString() + "] " + resourcePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerResourcesLoader getInstance() {
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

        private static final ServerResourcesLoader INSTANCE = new ServerResourcesLoader();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
