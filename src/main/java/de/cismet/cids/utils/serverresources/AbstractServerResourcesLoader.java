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

import net.sf.jasperreports.engine.JasperReport;

import java.io.StringReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractServerResourcesLoader {

    //~ Instance fields --------------------------------------------------------

    private final Map<PropertiesServerResource, ServerResourcePropertiesHandler> propertiesMap = new HashMap<>();
    private final Map<JsonServerResource, ServerResourceJsonHandler> jsonMap = new HashMap<>();
    private final Map<BinaryServerResource, byte[]> binariesMap = new HashMap<>();
    private final Map<JasperReportServerResource, JasperReport> reportsMap = new HashMap<>();
    private final Map<TextServerResource, String> textsMap = new HashMap<>();
    private final Map<ServerResource, Object> objectsMap = new HashMap<>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ServerResourcePropertiesHandler get(final PropertiesServerResource serverResource) throws Exception {
        return get(serverResource, true);
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
    public ServerResourceJsonHandler get(final JsonServerResource serverResource) throws Exception {
        return get(serverResource, true);
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
    public byte[] get(final BinaryServerResource serverResource) throws Exception {
        return get(serverResource, true);
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
    public JasperReport get(final JasperReportServerResource serverResource) throws Exception {
        return get(serverResource, true);
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
    public String get(final TextServerResource serverResource) throws Exception {
        return get(serverResource, true);
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
    public Object get(final ServerResource serverResource) throws Exception {
        return get(serverResource, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   useCache        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ServerResourcePropertiesHandler get(final PropertiesServerResource serverResource, final boolean useCache)
            throws Exception {
        if (useCache) {
            synchronized (propertiesMap) {
                if (propertiesMap.containsKey(serverResource)) {
                    return propertiesMap.get(serverResource);
                }
            }
        }
        return createPropertiesHandler(serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   useCache        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public JasperReport get(final JasperReportServerResource serverResource, final boolean useCache) throws Exception {
        if (useCache) {
            synchronized (reportsMap) {
                if (reportsMap.containsKey(serverResource)) {
                    return reportsMap.get(serverResource);
                }
            }
        }
        final JasperReport jasperReport = ServerResourcesLoader.getInstance().loadJasperReport(serverResource);
        put(serverResource, jasperReport);
        return jasperReport;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   useCache        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public byte[] get(final BinaryServerResource serverResource, final boolean useCache) throws Exception {
        if (useCache) {
            synchronized (binariesMap) {
                if (binariesMap.containsKey(serverResource)) {
                    return binariesMap.get(serverResource);
                }
            }
        }
        final byte[] bytes = ServerResourcesLoader.getInstance().loadBinary(serverResource);
        put(serverResource, bytes);
        return bytes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   useCache        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ServerResourceJsonHandler get(final JsonServerResource serverResource, final boolean useCache)
            throws Exception {
        if (useCache) {
            synchronized (jsonMap) {
                if (jsonMap.containsKey(serverResource)) {
                    return jsonMap.get(serverResource);
                }
            }
        }
        return createJsonHandler(serverResource);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   useCache        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public String get(final TextServerResource serverResource, final boolean useCache) throws Exception {
        if (useCache) {
            synchronized (textsMap) {
                if (textsMap.containsKey(serverResource)) {
                    return textsMap.get(serverResource);
                }
            }
        }
        final String text = ServerResourcesLoader.getInstance().loadText(serverResource);
        put(serverResource, text);
        return text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   useCache        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Object get(final ServerResource serverResource, final boolean useCache) throws Exception {
        if (useCache) {
            synchronized (objectsMap) {
                if (objectsMap.containsKey(serverResource)) {
                    return objectsMap.get(serverResource);
                }
            }
        }
        final Object object = ServerResourcesLoader.getInstance().load(serverResource);
        put(serverResource, object);
        return object;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  serverResource  DOCUMENT ME!
     */
    public void reset(final ServerResource serverResource) {
        if (serverResource instanceof JasperReportServerResource) {
            synchronized (reportsMap) {
                reportsMap.remove((JasperReportServerResource)serverResource);
            }
        } else if (serverResource instanceof PropertiesServerResource) {
            synchronized (propertiesMap) {
                propertiesMap.remove((PropertiesServerResource)serverResource);
            }
        } else if (serverResource instanceof TextServerResource) {
            synchronized (textsMap) {
                textsMap.remove((TextServerResource)serverResource);
            }
        } else if (serverResource instanceof BinaryServerResource) {
            synchronized (binariesMap) {
                binariesMap.remove((BinaryServerResource)serverResource);
            }
        } else {
            synchronized (objectsMap) {
                objectsMap.remove((BinaryServerResource)serverResource);
            }
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
    protected ServerResourcePropertiesHandler createPropertiesHandler(final PropertiesServerResource serverResource)
            throws Exception {
        ServerResourcePropertiesHandler propertiesHandler = (ServerResourcePropertiesHandler)
            serverResource.getPropertiesClass().newInstance();
        if (propertiesHandler == null) {
            propertiesHandler = new DefaultServerResourcePropertiesHandler();
        }
        final Properties properties = loadProperties(serverResource);
        propertiesHandler.setProperties(properties);
        put(serverResource, propertiesHandler);
        return propertiesHandler;
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
    protected ServerResourceJsonHandler createJsonHandler(final JsonServerResource serverResource) throws Exception {
        ServerResourceJsonHandler jsonHandlerCreator = (ServerResourceJsonHandler)serverResource
                    .getJsonHandlerClass().newInstance();
        if (jsonHandlerCreator == null) {
            jsonHandlerCreator = new DefaultServerResourceJsonHandler();
        }
        final ServerResourceJsonHandler jsonHandler = loadJson(serverResource, jsonHandlerCreator.getClass());
        put(serverResource, jsonHandler);
        return jsonHandler;
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
    public abstract Properties loadProperties(final PropertiesServerResource serverResource) throws Exception;

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
    @Deprecated
    public abstract <T extends Object> T loadJson(final ServerResource serverResource, final Class<T> clazz)
            throws Exception;

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
    public abstract <T extends Object> T loadJson(final JsonServerResource serverResource, final Class<T> clazz)
            throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   <T>             DOCUMENT ME!
     * @param   serverResource  DOCUMENT ME!
     * @param   jsonFactory     DOCUMENT ME!
     * @param   clazz           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public abstract <T extends Object> T loadJson(final JsonServerResource serverResource,
            final JsonFactory jsonFactory,
            final Class<T> clazz) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public abstract JasperReport loadJasperReport(final JasperReportServerResource serverResource) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public abstract String loadText(final TextServerResource serverResource) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public abstract byte[] loadBinary(final BinaryServerResource serverResource) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public abstract Object load(final ServerResource serverResource) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public StringReader loadStringReader(final TextServerResource serverResource) throws Exception {
        return new StringReader(loadText((TextServerResource)serverResource));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource     DOCUMENT ME!
     * @param   propertiesHandler  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected ServerResourcePropertiesHandler put(final PropertiesServerResource serverResource,
            final ServerResourcePropertiesHandler propertiesHandler) {
        synchronized (propertiesMap) {
            return propertiesMap.put(serverResource, propertiesHandler);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   bytes           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected byte[] put(final BinaryServerResource serverResource,
            final byte[] bytes) {
        synchronized (binariesMap) {
            return binariesMap.put(serverResource, bytes);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   text            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String put(final TextServerResource serverResource,
            final String text) {
        synchronized (textsMap) {
            return textsMap.put(serverResource, text);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   jasperReport    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected JasperReport put(final JasperReportServerResource serverResource,
            final JasperReport jasperReport) {
        synchronized (reportsMap) {
            return reportsMap.put(serverResource, jasperReport);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverResource  DOCUMENT ME!
     * @param   object          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Object put(final ServerResource serverResource,
            final Object object) {
        synchronized (objectsMap) {
            return objectsMap.put(serverResource, object);
        }
    }
}
