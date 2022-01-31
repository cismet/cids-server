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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ServerResourcesLoader extends AbstractServerResourcesLoader {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ServerResourcesLoader.class);

    //~ Instance fields --------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */

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
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public JasperReport loadJasperReport(final ServerResource serverResource) throws Exception {
        if (!(serverResource instanceof JasperReportServerResource)) {
            throw new Exception("wrong ServerResource type");
        }
        return loadJasperReport((JasperReportServerResource)serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public JasperReport loadJasperReport(final JasperReportServerResource serverResource) throws Exception {
        return (JasperReport)load(serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public String loadText(final ServerResource serverResource) throws Exception {
        if (!(serverResource instanceof TextServerResource)) {
            throw new Exception("wrong ServerResource type");
        }
        return loadText((TextServerResource)serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public String loadText(final TextServerResource serverResource) throws Exception {
        return (String)load(serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public byte[] loadBinary(final ServerResource serverResource) throws Exception {
        if (!(serverResource instanceof BinaryServerResource)) {
            throw new Exception("wrong ServerResource type");
        }
        return loadBinary((BinaryServerResource)serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public byte[] loadBinary(final BinaryServerResource serverResource) throws Exception {
        return (byte[])load(serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  resourcePath DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public Object load(final ServerResource serverResource) throws Exception {
        final ServerResource.Type type = serverResource.getType();
        final String serverResourcePath = serverResource.getPath();
        LOG.info("ResourceLoader loading " + serverResource);

        try {
            if (resourcesBasePath == null) {
                throw new Exception("resourcesBasePath is null");
            }
            final String resourceFullPath = resourcesBasePath + serverResourcePath;
            final File resourceFile = new File(resourceFullPath);
            if (!resourceFile.exists()) {
                throw new Exception(serverResourcePath + " not found in " + resourcesBasePath);
            }
            if (!resourceFile.canRead()) {
                throw new Exception("can't read " + serverResourcePath + " in " + resourcesBasePath);
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
                    LOG.warn("Exception while loading resource: " + serverResource, ex);
                    resource = ex;
                }
            } else {
                resource = null;
            }

            return resource;
        } catch (final Exception ex) {
            LOG.warn("ResourceLoader failed on loading " + serverResource, ex);
            throw ex;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Properties loadProperties(final ServerResource serverResource) throws Exception {
        if (!(serverResource instanceof TextServerResource)) {
            throw new Exception("wrong ServerResource type");
        }
        final Properties properties = new Properties();
        properties.load(loadStringReader(serverResource));
        return properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>             DOCUMENT ME!
     * @param   serverResource  DOCUMENT ME!
     * @param   clazz           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public <T extends Object> T loadJson(final ServerResource serverResource, final Class<T> clazz) throws Exception {
        if (!(serverResource instanceof TextServerResource)) {
            throw new Exception("wrong ServerResource type");
        }
        return new ObjectMapper(new JsonFactory()).readValue(loadStringReader(serverResource), clazz);
    }

    @Override
    public <T extends Object> T loadJson(final JsonServerResource serverResource, final Class<T> clazz)
            throws Exception {
        return loadJson(serverResource, new JsonFactory(), clazz);
    }

    @Override
    public <T extends Object> T loadJson(final JsonServerResource serverResource,
            final JsonFactory jsonFactory,
            final Class<T> clazz) throws Exception {
        return new ObjectMapper(jsonFactory).readValue(loadStringReader(serverResource), clazz);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Iterator<Map.Entry<String, JsonNode>> loadJson(final ServerResource serverResource) throws Exception {
        if (!(serverResource instanceof TextServerResource)) {
            throw new Exception("wrong ServerResource type");
        }

        final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

        return mapper.readTree(loadStringReader(serverResource)).fields();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public Properties loadProperties(final PropertiesServerResource serverResource) throws Exception {
        return loadProperties((ServerResource)serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public StringReader loadStringReader(final ServerResource serverResource) throws Exception {
        if (!(serverResource instanceof TextServerResource)) {
            throw new Exception("wrong ServerResource type");
        }
        return loadStringReader((TextServerResource)serverResource);
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
