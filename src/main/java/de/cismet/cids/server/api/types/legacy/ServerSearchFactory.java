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
package de.cismet.cids.server.api.types.legacy;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import org.postgresql.util.Base64;

import org.reflections.ReflectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import de.cismet.cids.server.api.types.SearchInfo;
import de.cismet.cids.server.api.types.SearchParameter;
import de.cismet.cids.server.api.types.SearchParameters;
import de.cismet.cids.server.search.CidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class ServerSearchFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ServerSearchFactory.class);

    private static ServerSearchFactory factory = null;

    //~ Instance fields --------------------------------------------------------

    private final HashMap<String, Class<? extends CidsServerSearch>> serverSearchClassMap =
        new HashMap<String, Class<? extends CidsServerSearch>>();
    private final HashMap<String, SearchInfo> serverSearchInfoMap = new HashMap<String, SearchInfo>();

    private boolean cacheFilled = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerSearchFactory object.
     */
    private ServerSearchFactory() {
        this.fillCache();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final ServerSearchFactory getFactory() {
        if (factory == null) {
            factory = new ServerSearchFactory();
        }

        return factory;
    }

    /**
     * DOCUMENT ME!
     */
    private void fillCache() {
        if (cacheFilled) {
            LOG.warn("ServerSearchCache already filled");
        }

        final Collection<? extends CidsServerSearch> subTypes = Lookup.getDefault().lookupAll(CidsServerSearch.class);
        LOG.info("loading " + subTypes.size() + " CidsServerSearches");
        for (final CidsServerSearch subType : subTypes) {
            try {
                final Class<? extends CidsServerSearch> serverSearchClass = subType.getClass();
                LOG.info("processing CidsServerSearche '" + serverSearchClass.getName() + "'");
                final Set<Method> setters = ReflectionUtils.getAllMethods(
                        serverSearchClass,
                        ReflectionUtils.withModifier(Modifier.PUBLIC),
                        ReflectionUtils.withPrefix("set"));
                final Set<Method> settersParent = ReflectionUtils.getAllMethods(
                        CidsServerSearch.class,
                        ReflectionUtils.withModifier(Modifier.PUBLIC),
                        ReflectionUtils.withPrefix("set"));
                final Collection<String> setterParentNames = new ArrayList<String>();
                for (final Method setterParent : settersParent) {
                    setterParentNames.add(setterParent.getName());
                }

                final SearchInfo searchInfo = new SearchInfo();
                final String searchKey = serverSearchClass.getSimpleName();

                final HashMap<String, String> serverSearchParamMap = new HashMap<String, String>();
                for (final Method setter : setters) {
                    if (!setterParentNames.contains(setter.getName())) {
                        final Class[] paramTypes = setter.getParameterTypes();
                        for (int index = 0; index < paramTypes.length; index++) {
                            final Class paramTyp = paramTypes[index];
                            final String paramName = setter.getName().split("set")[1];
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("processing CidsServerSearch '" + serverSearchClass.getName()
                                            + "' parameter '" + paramName + "'");
                            }
                            if (paramTypes.length > 1) {
                                serverSearchParamMap.put(paramName + "[" + index + "]", paramTyp.getName());
                            } else {
                                serverSearchParamMap.put(paramName, paramTyp.getName());
                            }
                        }
                    }
                }

                searchInfo.setKey(searchKey);
                searchInfo.setName(serverSearchClass.getSimpleName());
                searchInfo.setDescription("legacy cidsServerSearch");
                searchInfo.setParameterDescription(serverSearchParamMap);

                this.serverSearchClassMap.put(searchKey, serverSearchClass);
                this.serverSearchInfoMap.put(searchKey, searchInfo);
            } catch (Throwable t) {
                LOG.error("could not process CidsServerSearch '"
                            + subType.getClass().getName() + "': " + t.getMessage(),
                    t);
            }
        }

        cacheFilled = true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   searchKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SearchInfo getServerSearchInfo(final String searchKey) {
        if (!this.serverSearchInfoMap.containsKey(searchKey)) {
            LOG.warn("could not find SearchInfo for search key '" + searchKey + "'");
        }

        return this.serverSearchInfoMap.get(searchKey);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<SearchInfo> getServerSearchInfos() {
        return new LinkedList<SearchInfo>(this.serverSearchInfoMap.values());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   searchKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Class<? extends CidsServerSearch> getServerSearchClass(final String searchKey) {
        if (!this.serverSearchClassMap.containsKey(searchKey)) {
            LOG.warn("could not find legacy search java class for search key '" + searchKey + "'");
        }

        return this.serverSearchClassMap.get(searchKey);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   searchInfo        DOCUMENT ME!
     * @param   searchParameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsServerSearch serverSearchInstanceFromSearchParameters(
            final SearchInfo searchInfo,
            final List<SearchParameter> searchParameters) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating instance of cids server search '" + searchInfo.getKey() + "'");
        }

        final Class<? extends CidsServerSearch> searchClass = this.getServerSearchClass(searchInfo.getKey());

        if (searchClass == null) {
            final String message = "could not create instance of cids server search '"
                        + searchInfo.getKey() + "': server search class not found!";
            LOG.error(message);
            throw new Exception(message);
        }

        final CidsServerSearch cidsServerSearch = searchClass.newInstance();

        if (cidsServerSearch == null) {
            final String message = "could not create instance of cids server search '"
                        + searchInfo.getKey() + "': server search instance could not be created";
            LOG.error(message);
            throw new Exception(message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting " + searchParameters.size() + " search parameters of cids server search '"
                        + searchInfo.getKey() + "'");
        }
        for (final SearchParameter searchParameter : searchParameters) {
            final String paramKey = searchParameter.getKey();
            final String paramClassName = searchInfo.getParameterDescription().get(paramKey);
            if (paramClassName == null) {
                final String message = "could not create instance of cids server search '"
                            + searchInfo.getKey() + "': server search parameter '"
                            + paramKey + " 'could not be found created";
                LOG.error(message);
                throw new Exception(message);
            }

            final Class paramClass = ClassUtils.getClass(paramClassName);
            final Object paramValue;
            final String rawParamValue = searchParameter.getValue();

            if (paramClass.isPrimitive()) {
                if (paramClass.equals(byte.class)) {
                    paramValue = Byte.valueOf(rawParamValue);
                } else if (paramClass.equals(short.class)) {
                    paramValue = Short.valueOf(rawParamValue);
                } else if (paramClass.equals(int.class)) {
                    paramValue = Integer.valueOf(rawParamValue);
                } else if (paramClass.equals(long.class)) {
                    paramValue = Long.valueOf(rawParamValue);
                } else if (paramClass.equals(float.class)) {
                    paramValue = Float.valueOf(rawParamValue);
                } else if (paramClass.equals(double.class)) {
                    paramValue = Double.valueOf(rawParamValue);
                } else if (paramClass.equals(boolean.class)) {
                    paramValue = Boolean.valueOf(rawParamValue);
                } else if (paramClass.equals(char.class)) {
                    paramValue = rawParamValue.toCharArray()[0];
                } else {
                    LOG.warn("unsupported primitive search parameter class '" + paramClassName
                                + " setting search parameter value to null");
                    paramValue = null; // should not be possible
                }
            } else {
                if (paramClass.equals(Byte.class)) {
                    paramValue = Byte.valueOf(rawParamValue);
                } else if (paramClass.equals(Short.class)) {
                    paramValue = Short.valueOf(rawParamValue);
                } else if (paramClass.equals(Integer.class)) {
                    paramValue = Integer.valueOf(rawParamValue);
                } else if (paramClass.equals(Long.class)) {
                    paramValue = Long.valueOf(rawParamValue);
                } else if (paramClass.equals(Float.class)) {
                    paramValue = Float.valueOf(rawParamValue);
                } else if (paramClass.equals(Double.class)) {
                    paramValue = Double.valueOf(rawParamValue);
                } else if (paramClass.equals(Boolean.class)) {
                    paramValue = Boolean.valueOf(rawParamValue);
                } else if (paramClass.equals(Character.class)) {
                    paramValue = rawParamValue.toCharArray()[0];
                } else if (paramClass.equals(String.class)) {
                    paramValue = rawParamValue;
                } else {
                    LOG.warn("unsupported search parameter class '" + paramClassName
                                + " setting search parameter '" + rawParamValue + "' from Base64 encoded String!");
                    paramValue = fromString(rawParamValue);
                }
            }

            cidsServerSearch.getClass().getMethod("set" + paramKey, paramClass).invoke(cidsServerSearch, paramValue);
        }

        return cidsServerSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   searchKey         DOCUMENT ME!
     * @param   cidsServerSearch  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public SearchParameters searchParametersFromServerSearchInstance(
            final String searchKey,
            final CidsServerSearch cidsServerSearch) throws Exception {
        // final String searchKey = cidsServerSearch.getClass().getName();
        final SearchInfo searchInfo = ServerSearchFactory.getFactory().getServerSearchInfo(searchKey);

        if (searchInfo == null) {
            final String message = "could not find cids server search  '" + searchKey + "'";
            LOG.error(message);
            throw new Exception(message);
        }
        final SearchParameters searchParameters = new SearchParameters();
        final LinkedList<SearchParameter> searchParametersList = new LinkedList<SearchParameter>();
        searchParameters.setList(searchParametersList);
        for (final String parameterName : searchInfo.getParameterDescription().keySet()) {
            try {
                final Object parameterValue = cidsServerSearch.getClass()
                            .getMethod("get" + parameterName)
                            .invoke(cidsServerSearch);
                if (parameterValue != null) {
                    final Class paramClass = parameterValue.getClass();
                    final SearchParameter searchParameter = new SearchParameter();
                    searchParameter.setKey(parameterName);

                    if (paramClass.isPrimitive()
                                || paramClass.equals(Byte.class)
                                || paramClass.equals(Short.class)
                                || paramClass.equals(Integer.class)
                                || paramClass.equals(Long.class)
                                || paramClass.equals(Float.class)
                                || paramClass.equals(Double.class)
                                || paramClass.equals(Boolean.class)
                                || paramClass.equals(Character.class)
                                || paramClass.equals(String.class)) {
                        searchParameter.setValue(String.valueOf(parameterValue));
                    } else {
                        LOG.warn("unsupported search parameter class '" + paramClass.getName()
                                    + " setting search parameter '" + parameterName + "' to Base64 encoded String!");
                        searchParameter.setValue(toString(parameterValue));
                    }

                    searchParametersList.add(searchParameter);
                } else {
                    LOG.warn("could not get parameter '" + parameterName
                                + "' from cids server search instance '" + searchKey
                                + "': parameter is null -> ignoring SearchParameter");
                }
            } catch (Exception ex) {
                final String message = "could not get parameter '" + parameterName
                            + "' from cids server search instance '" + searchKey
                            + "': " + ex.getMessage();
                LOG.error(message);
                throw new Exception(message, ex);
            }
        }

        return searchParameters;
    }

    /**
     * Write the object to a Base64 string.
     *
     * @param   object  o DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static String toString(final Object object) throws IOException {
        if (object.getClass().isAssignableFrom(Serializable.class)) {
            final Serializable serializable = (Serializable)object;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(serializable);
            oos.close();
            return Base64.encodeBytes(baos.toByteArray());
        } else {
            LOG.warn("object of type '" + object.getClass() + "' is not serializable, returning null!");
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private static Object fromString(final String s) throws IOException, ClassNotFoundException {
        final byte[] data = Base64.decode(s);
        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        final Object o = ois.readObject();
        ois.close();
        return o;
    }
}
