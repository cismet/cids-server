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

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import org.postgresql.util.Base64;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.cismet.cids.base.types.Type;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.api.types.CidsClass;
import de.cismet.cids.server.api.types.CidsNode;
import de.cismet.cids.server.api.types.SearchInfo;
import de.cismet.cids.server.api.types.SearchParameter;
import de.cismet.cids.server.api.types.SearchParameterInfo;
import de.cismet.cids.server.api.types.SearchParameters;
import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.search.RestApiCidsServerSearch;

/**
 * Helper Methods for dealing with CidsServerSearch and and SearchInfo.
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

    private final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

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
     * Inspects a CidsServerSearch Instance and tries to automatically serive a proper SearchInfo object.
     *
     * @param   cidsServerSearch  server search to be inspected
     *
     * @return  search info or null if the inspection fails
     */
    public SearchInfo searchInfoFromCidsServerSearch(final CidsServerSearch cidsServerSearch) {
        try {
            final Class<? extends CidsServerSearch> serverSearchClass = cidsServerSearch.getClass();
            LOG.info("processing CidsServerSearch '" + serverSearchClass.getName() + "'");

            final Map<String, Class> searchParameters = this.findSearchParameters(cidsServerSearch.getClass());

            final SearchInfo searchInfo = new SearchInfo();
            final String searchKey = serverSearchClass.getName();
            final LinkedList<SearchParameterInfo> searchParameterInfos = new LinkedList<SearchParameterInfo>();
            Class<?> resultCollectionClass = Object.class;

            // Process the parameters
            for (final String paramName : searchParameters.keySet()) {
                final SearchParameterInfo searchParameterInfo = new SearchParameterInfo();
                final Class paramClass = searchParameters.get(paramName);
                final Type paramType = Type.typeForJavaClass(paramClass);

                searchParameterInfo.setKey(paramName);
                searchParameterInfo.setType(paramType);
                searchParameterInfo.setArray(paramClass.isArray());

                if ((paramType == Type.JAVA_CLASS)
                            || (paramType == Type.JAVA_SERIALIZABLE)
                            || (paramType == Type.UNDEFINED)) {
                    searchParameterInfo.setAdditionalTypeInfo(paramClass.getName());
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("adding search parameter '" + paramName + "' of class '"
                                + paramClass.getName() + "' and Type '" + paramType + "'");
                }
                searchParameterInfos.add(searchParameterInfo);
            }

            // process the return type
            try {
                final Method performServerSearch = serverSearchClass.getMethod("performServerSearch");
                final java.lang.reflect.Type returnType = performServerSearch.getGenericReturnType();
                final ParameterizedType collectionType = (ParameterizedType)returnType;
                resultCollectionClass = (Class<?>)collectionType.getActualTypeArguments()[0];
                if (LOG.isDebugEnabled()) {
                    LOG.debug("generic result collection type of CidsServerSearch '"
                                + searchKey + "' is '" + resultCollectionClass.getName() + "'");
                }
            } catch (final Exception ex) {
                LOG.warn("could not determine collection type of CidsServerSearch '"
                            + searchKey + "': " + ex.getMessage(),
                    ex);
            }

            final SearchParameterInfo returnParameterInfo = new SearchParameterInfo();
            final Type paramType = Type.typeForJavaClass(resultCollectionClass);
            returnParameterInfo.setKey("return");
            returnParameterInfo.setType(paramType);
            returnParameterInfo.setArray(true);

            // add the addtional type info only when absolutely necessary
            if ((paramType == Type.JAVA_CLASS)
                        || (paramType == Type.JAVA_SERIALIZABLE)
                        || (paramType == Type.UNDEFINED)) {
                returnParameterInfo.setAdditionalTypeInfo(resultCollectionClass.getName());
            }

            searchInfo.setKey(searchKey);
            searchInfo.setName(serverSearchClass.getSimpleName());
            searchInfo.setDescription("CIDS Server Search");
            searchInfo.setParameterDescription(searchParameterInfos);
            searchInfo.setResultDescription(returnParameterInfo);

            return searchInfo;
        } catch (Throwable t) {
            LOG.error("could not inspect CidsServerSearch '"
                        + cidsServerSearch.getClass().getName() + "': " + t.getMessage(),
                t);
        }

        return null;
    }

    /**
     * Lookups all available Cids Server Search, collects the respective Search Infos and adds it to the cache.
     */
    private void fillCache() {
        if (cacheFilled) {
            LOG.warn("ServerSearchCache already filled");
        }

        final Collection<? extends RestApiCidsServerSearch> lookupableServerSearches = Lookup.getDefault()
                    .lookupAll(RestApiCidsServerSearch.class);
        final Collection<? extends CidsServerSearch> cidsServerSearches = Lookup.getDefault()
                    .lookupAll(CidsServerSearch.class);

        LOG.info("loading " + lookupableServerSearches.size() + " Lookupable Server Search and trying to inspect "
                    + cidsServerSearches.size() + " cids Server Searches");

        for (final RestApiCidsServerSearch lookupableServerSearch : lookupableServerSearches) {
            final SearchInfo searchInfo = lookupableServerSearch.getSearchInfo();
            final Class serverSearchClass = lookupableServerSearch.getClass();
            final String searchKey = searchInfo.getKey();
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding Lookupable Server Search '" + searchKey + "'");
            }
            this.serverSearchClassMap.put(searchKey, serverSearchClass);
            this.serverSearchInfoMap.put(searchKey, searchInfo);
        }

        for (final CidsServerSearch cidsServerSearch : cidsServerSearches) {
            final String searchKey = cidsServerSearch.getClass().getName();
            if (!this.serverSearchInfoMap.containsKey(searchKey)) {
                final SearchInfo searchInfo = this.searchInfoFromCidsServerSearch(cidsServerSearch);
                if (searchInfo != null) {
                    final Class serverSearchClass = cidsServerSearch.getClass();
                    if (!searchKey.equals(searchInfo.getKey())) {
                        LOG.warn("search key missmatch: '" + searchKey
                                    + "' != '" + searchInfo.getKey() + "'!");
                    }

                    if (!this.serverSearchInfoMap.containsKey(cidsServerSearch.getClass().getName())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("adding Cids Server Search '" + searchKey + "'");
                        }
                        this.serverSearchClassMap.put(searchKey, serverSearchClass);
                        this.serverSearchInfoMap.put(searchKey, searchInfo);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Cids Server Search '" + searchKey
                                        + "' already registered by Lookupable Server Search.");
                        }
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cids Server Search '" + cidsServerSearch.getClass().getName()
                                + "' already registered by Lookupable Server Search.");
                }
            }
        }

        cacheFilled = true;
    }

    /**
     * Tries to find a cached SearchInfo object of the specified cids server search.
     *
     * @param   searchKey  the search key (e.g.java class name) of the search
     *
     * @return  SearchInfo instance or null
     */
    public SearchInfo getServerSearchInfo(final String searchKey) {
        if (!this.serverSearchInfoMap.containsKey(searchKey)) {
            LOG.warn("could not find SearchInfo for search key '" + searchKey + "'");
        }

        return this.serverSearchInfoMap.get(searchKey);
    }

    /**
     * Returns all cached ServerSearchInfos.
     *
     * @return  ServerSearchInfo Collection
     */
    public List<SearchInfo> getServerSearchInfos() {
        return new LinkedList<SearchInfo>(this.serverSearchInfoMap.values());
    }

    /**
     * Tries to find a cached CidsServerSearch class for the specified cids server search key.
     *
     * @param   searchKey  key (e.g. class name) of the server search
     *
     * @return  CidsServerSearch Class or null
     */
    public Class<? extends CidsServerSearch> getServerSearchClass(final String searchKey) {
        if (!this.serverSearchClassMap.containsKey(searchKey)) {
            LOG.warn("could not find legacy search java class for search key '" + searchKey + "'");
        }

        return this.serverSearchClassMap.get(searchKey);
    }

    /**
     * Populates an instance of a CidsServerSearch with parameters from the searchParameters object.
     *
     * @param   searchInfo        meta information about a the CidsServerSearch
     * @param   searchParameters  search parameters that are set in the instance
     *
     * @return  CidsServerSearch with parameters
     *
     * @throws  Exception  if a parameter could not be set
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

        final Map<String, Method> parameterWriteMethods = this.findSearchParameterWriteMethods(searchClass);

        if (parameterWriteMethods.size() != searchParameters.size()) {
            LOG.warn("number of introspected search parameters (" + parameterWriteMethods.size()
                        + ") of cids server search '" + searchInfo.getKey()
                        + "' does not match number of search parameters (" + searchParameters.size()
                        + ") provided by the client");
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
            final SearchParameterInfo searchParameterInfo = searchInfo.getSearchParameterInfo(paramKey);
            if (searchParameterInfo == null) {
                final String message = "could not create instance of cids server search '"
                            + searchInfo.getKey() + "': server search parameter '"
                            + paramKey + " ' could not be found!";
                LOG.error(message);
                throw new Exception(message);
            }

            String paramClassName;
            final Object paramValue;

            if ((searchParameterInfo.getType() == Type.ENTITY_REFERENCE)
                        || (searchParameterInfo.getType() == Type.ENTITY)
                        || (searchParameterInfo.getType() == Type.ENTITY_INFO)
                        || (searchParameterInfo.getType() == Type.NODE)) {
                // FIXME: if required, handle MetaObject, MetaClass and MetaNode
                // in custom serializer / deserilaitzer implementation of
                // SearchParameter.class
                final String message = "The Search Parameter '" + searchParameterInfo.getKey()
                            + "' (" + searchParameterInfo.getType().name() + ") of the cids server search '"
                            + searchInfo.getKey()
                            + " is a MetaObject, MetaClass or MetaNode and currently not supported by automatic serialization.";
                LOG.error(message);
                throw new IllegalArgumentException(message);
            } else if ((searchParameterInfo.getType() == Type.JAVA_SERIALIZABLE)
                        || (searchParameterInfo.getType() == Type.JAVA_CLASS)) {
                if (searchParameterInfo.getAdditionalTypeInfo() != null) {
                    paramClassName = searchParameterInfo.getAdditionalTypeInfo();
                } else {
                    final String message = "could not create instance of cids server search '"
                                + searchInfo.getKey() + "': java type search parameter '"
                                + paramKey + "' is unknown!";
                    LOG.error(message);
                    throw new Exception(message);
                }
            } else {
                paramClassName = searchParameterInfo.getType().getJavaType();
            }

            if (searchParameterInfo.isArray()) {
                paramClassName += "[]";
            }

            try {
                final Class paramClass = ClassUtils.getClass(paramClassName);
                if (searchParameterInfo.getType() == Type.JAVA_SERIALIZABLE) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("deserializing binary parameter '" + searchInfo.getKey() + "'");
                    }
                    paramValue = fromBase64String(searchParameter.getValue().toString());
                } else if (searchParameterInfo.isArray()) {
                    // jackson created a collection, not an array!
                    if (Collection.class.isInstance(searchParameter.getValue())) {
                        paramValue = Arrays.copyOf(((Collection)searchParameter.getValue()).toArray(),
                                ((Collection)searchParameter.getValue()).size(),
                                paramClass);
                    } else {
                        final String message = "Search Parameter Type Missmatch: Type of parameter '"
                                    + paramKey + "' specified in search parameter info as '" + paramClassName
                                    + "' is an array, but actual value is not a Collection but of type '"
                                    + searchParameter.getValue().getClass().getName() + "'";
                        LOG.error(message);
                        throw new IllegalArgumentException(message);
                    }
                } else {
                    // jackson took already care about the deserialization
                    paramValue = searchParameter.getValue();
                    if (!paramClass.isPrimitive() && !paramClass.equals(paramValue.getClass())) {
                        LOG.warn("Search Parameter Type Missmatch: Type of parameter '"
                                    + paramKey + "' specified in search parameter info as '" + paramClassName
                                    + "' does not match type '" + paramValue.getClass().getName()
                                    + "' of actual value.");
                    }
                }

                // ensure compatilbility with old clients that use uppercase property names
                final String paramPropertyName = Introspector.decapitalize(paramKey);
                if (parameterWriteMethods.containsKey(paramPropertyName)) {
                    final Method writeMethod = parameterWriteMethods.get(paramPropertyName);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("setting parameter '" + paramPropertyName + "' of actual type '"
                                    + paramValue.getClass() + "', reported type '"
                                    + paramClassName + "' (" + paramClass.getName() + ") and set method type '"
                                    + writeMethod.getGenericParameterTypes()[0] + "' to value: " + paramValue);
                    }
                    writeMethod.invoke(cidsServerSearch, paramValue);
                } else {
                    LOG.error("the cids server search '" + searchInfo.getKey() + "' parameter '" + paramPropertyName
                                + "' of actual type '" + paramValue.getClass() + "' and reported type '"
                                + paramClassName + "' (" + paramClass.getName()
                                + ") could not be set since the corresponding set method does not exist!");
                }
            } catch (final Throwable t) {
                final String message = "The cids server search '" + searchInfo.getKey() + "' parameter '" + paramKey
                            + "' of reported type '"
                            + paramClassName + "' could not be set: " + t.getMessage();
                LOG.error(message, t);
                throw new Exception(message, t);
            }
        }

        return cidsServerSearch;
    }

    /**
     * Extracts Search Parameters from a CidsServerSearch Search object. Needs the respective SearchInfo and
     * SearchParameterInfo to do so. <strong>Warning:</strong> Doesn't currently support MetaObject, MetaClass and
     * MetaNode as they need special conversion before JSON serialization.
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
        final Map<String, Method> parameterReadMethods = this.findSearchParameterReadMethods(
                cidsServerSearch.getClass());

        if (searchInfo == null) {
            final String message = "could not find cids server search  '" + searchKey + "'";
            LOG.error(message);
            throw new Exception(message);
        }
        final SearchParameters searchParameters = new SearchParameters();
        final LinkedList<SearchParameter> searchParametersList = new LinkedList<SearchParameter>();
        searchParameters.setList(searchParametersList);
        for (final SearchParameterInfo searchParameterInfo : searchInfo.getParameterDescription()) {
            // currently not supported as search parameters
            if ((searchParameterInfo.getType() == Type.ENTITY_REFERENCE)
                        || (searchParameterInfo.getType() == Type.ENTITY_INFO)
                        || (searchParameterInfo.getType() == Type.ENTITY)
                        || (searchParameterInfo.getType() == Type.NODE)) {
                final String message = "The parameter type '"
                            + searchParameterInfo.getType().getJavaType()
                            + "' of parameter '" + searchParameterInfo.getKey()
                            + "' of cids server search '" + searchKey
                            + "' is currently not supported!";
                LOG.error(message);
                throw new IllegalArgumentException(message);
            }

            final String paramKey = searchParameterInfo.getKey();

            try {
                final String paramPropertyName = Introspector.decapitalize(paramKey);
                Object parameterValue = null;
                if (parameterReadMethods.containsKey(paramPropertyName)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("reading parameter '" + paramPropertyName + "' of reported type '"
                                    + searchParameterInfo.getType() + "'");
                    }
                    final Method readMethod = parameterReadMethods.get(paramPropertyName);
                    parameterValue = readMethod.invoke(cidsServerSearch);
                } else {
                    LOG.error("the cids server search '" + searchInfo.getKey() + "' parameter '" + paramPropertyName
                                + "' of reported type '"
                                + searchParameterInfo.getType()
                                + "' could not be get since the corresponding get method does not exist!");
                }

                if (parameterValue != null) {
                    if (searchParameterInfo.getType() == Type.JAVA_SERIALIZABLE) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("serializing search parameter '" + searchParameterInfo.getKey() + "' from class '"
                                        + searchParameterInfo.getType() + "' ("
                                        + parameterValue.getClass().getSimpleName() + ")");
                        }
                        parameterValue = toBase64String(parameterValue);
                    }

                    final SearchParameter searchParameter = new SearchParameter();
                    searchParameter.setKey(searchParameterInfo.getKey());

                    // no serialization required, jackson takes care automatically!
                    searchParameter.setValue(parameterValue);

                    searchParametersList.add(searchParameter);
                } else {
                    LOG.warn("could not get parameter '" + searchParameterInfo.getKey()
                                + "' from cids server search instance '" + searchKey
                                + "': parameter is null -> ignoring the SearchParameter");
                }
            } catch (Throwable t) {
                final String message = "could not get parameter '" + searchParameterInfo.getKey()
                            + "' from cids server search instance '" + searchKey
                            + "': " + t.getMessage();
                LOG.error(message);
                throw new Exception(message, t);
            }
        }

        return searchParameters;
    }

    /**
     * Converts a ObjectNode search result collection to the respective Java Objects. Support MetaClass, MetaObject,
     * MetaNode by default and tries to deserialize binry (Base 64 encoded) and jackson serialized objects.<br>
     * <strong>Warning:</strong><br>
     * Does not support automatic deserialization of LightWightMetaObjects! A helper method for LightWightMetaObject
     * deserialization is available at
     * {@link CidsBeanFactory# lightweightMetaObjectFromCidsBean(de.cismet.cids.dynamics.CidsBean, int, java.lang.String, Sirius.server.newuser.User, de.cismet.cids.server.api.types.legacy.ClassNameCache)}
     *
     * @param   jsonNodes   JSON object nodes to be converted
     * @param   searchInfo  meta information needed for the type conversion
     *
     * @return  collection of converted java object
     *
     * @throws  Exception  if any error occurs during the conversion
     */
    public Collection resultCollectionfromJsonNodes(
            final List<JsonNode> jsonNodes,
            final SearchInfo searchInfo) throws Exception {
        final Collection resultCollection = new LinkedList();
        int i = 0;
        final boolean isMetaClass = searchInfo.getResultDescription().getType() == Type.ENTITY_INFO;
        final boolean isMetaObject = searchInfo.getResultDescription().getType() == Type.ENTITY;
        final boolean isLightWightMetaObject = searchInfo.getResultDescription().getType() == Type.ENTITY_REFERENCE;
        final boolean isMetaNode = searchInfo.getResultDescription().getType() == Type.NODE;
        final boolean isBinaryObject = searchInfo.getResultDescription().getType() == Type.JAVA_SERIALIZABLE;
        final String returnTypeName = searchInfo.getResultDescription().getAdditionalTypeInfo();
        final String searchKey = searchInfo.getKey();

        for (final JsonNode jsonNode : jsonNodes) {
            i++;
            if (isMetaClass) {
                try {
                    final CidsClass cidsClass = this.mapper.treeToValue(jsonNode, CidsClass.class);
                    final MetaClass metaClass = CidsClassFactory.getFactory()
                                .legacyCidsClassFromRestCidsClass(cidsClass);
                    resultCollection.add(metaClass);
                } catch (Exception ex) {
                    final String message = "could not convert result item #"
                                + i + " of cids custom server search '" + searchInfo.getKey()
                                + "' to MetaClass: " + ex.getMessage();
                    LOG.error(message);
                    throw new Exception(message, ex);
                }
            } else if (isMetaNode) {
                try {
                    final CidsNode cidsNode = this.mapper.treeToValue(jsonNode, CidsNode.class);
                    final Node metaNode = CidsNodeFactory.getFactory().legacyCidsNodeFromRestCidsNode(cidsNode);
                    resultCollection.add(metaNode);
                } catch (Exception ex) {
                    final String message = "could not convert result item #"
                                + i + " of cids custom server search '" + searchKey
                                + "' to Node: " + ex.getMessage();
                    LOG.error(message, ex);
                    throw new Exception(message, ex);
                }
            } else if (isMetaObject || isLightWightMetaObject) {
                try {
                    final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(false, jsonNode.toString());
                    final MetaObject metaObject = cidsBean.getMetaObject();
                    resultCollection.add(metaObject);
                } catch (Exception ex) {
                    final String message = "could not convert result item #"
                                + i + " of cids custom server search '" + searchKey
                                + "' to MetaObject: " + ex.getMessage();
                    LOG.error(message);
                    throw new Exception(message, ex);
                }
            } else if (isBinaryObject) {
                try {
//                        LOG.warn("returned collection of custom server search '" + searchKey
//                                + "' contains binary serialized objects. Performing binary deserialization to java class "
//                        + returnTypeName);

                    final Object resultObject = ServerSearchFactory.fromBase64String(jsonNode.asText());
                    resultCollection.add(resultObject);
                } catch (Exception ex) {
                    final String message = "binary deserialization of result item #"
                                + i + " of cids custom server search '" + searchKey
                                + "' to '" + returnTypeName + "' failed: " + ex.getMessage();
                    LOG.error(message);
                    throw new Exception(message, ex);
                }
            } else {
                try {
//                        LOG.warn("returned collection of custom server search '" + searchKey
//                                + "' contains custom java objects. Performing Jackson deserialization to java class "
//                        + returnTypeName);

                    final Class returnTypeClass = ClassUtils.getClass(returnTypeName);
                    final Object resultObject = mapper.treeToValue(jsonNode, returnTypeClass);
                    resultCollection.add(resultObject);
                } catch (Exception ex) {
                    final String message = "Jackson deserialization of result item #"
                                + i + " of cids custom server search '" + searchKey
                                + "' to '" + returnTypeName + "' failed: " + ex.getMessage();
                    LOG.error(message);
                    throw new Exception(message, ex);
                }
            }
        }

        if (i > 0) {
            if (isMetaClass) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + " meta classes (entity info) found and converted by cids server search '" + searchKey
                                + "'");
                }
            }
            if (isMetaObject) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + " meta objects (entities) found and converted by cids server search '" + searchKey
                                + "'");
                }
            } else if (isLightWightMetaObject) {
                if (LOG.isDebugEnabled()) {
                    LOG.warn(i + "LightWightMetaObject (entities references) returned by cids server search '"
                                + searchKey
                                + "' are not supported by this method and have been converted to MetaObjects! "
                                + "Please use CidsBeanFactory#lightweightMetaObjectFromCidsBean instead.");
                }
            } else if (isMetaNode) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + " nodes found and converted by cids server search '" + searchKey + "'");
                }
            } else if (isBinaryObject) {
                if (LOG.isDebugEnabled()) {
                    LOG.warn(i + " binary serialized objects of type '"
                                + returnTypeName
                                + "' found and converted by cids server search '" + searchKey + "'");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.warn(i + "Jackson serialized objects of type '"
                                + returnTypeName
                                + "' found and converted by cids server search '" + searchKey + "'");
                }
            }
        }

        return resultCollection;
    }

    /**
     * Converts a java.lang.Object collection search result to JSON Objects (ObjectNodes). Supports MetaClass,
     * MetaObject, MetaNode by default and tries to serialize binary (Base 64 encoded) and plain java objects.<br>
     * <strong>Warning:</strong><br>
     * Does not support automatic serialization of LightWightMetaObjects! A helper method for LightWightMetaObject to
     * CidsBean serialization is available at
     * {@link CidsBeanFactory#cidsBeanFromLightweightMetaObject(Sirius.server.middleware.types.LightweightMetaObject, Sirius.server.middleware.types.MetaClass) )}
     *
     * @param   searchResults   DOCUMENT ME!
     * @param   searchInfo      DOCUMENT ME!
     * @param   classNameCache  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public List<JsonNode> jsonNodesFromResultCollection(
            final Collection searchResults,
            final SearchInfo searchInfo,
            final ClassNameCache classNameCache) throws Exception {
        final List<JsonNode> jsonNodes = new LinkedList<JsonNode>();
        int i = 0;
        final boolean isMetaClass = searchInfo.getResultDescription().getType() == Type.ENTITY_INFO;
        final boolean isMetaObject = searchInfo.getResultDescription().getType() == Type.ENTITY;
        final boolean isLightWightMetaObject = searchInfo.getResultDescription().getType() == Type.ENTITY_REFERENCE;
        final boolean isMetaNode = searchInfo.getResultDescription().getType() == Type.NODE;
        final boolean isBinaryObject = searchInfo.getResultDescription().getType() == Type.JAVA_SERIALIZABLE;
        final String searchKey = searchInfo.getKey();

        for (final Object searchResult : searchResults) {
            final JsonNode jsonNode;
            if (isMetaClass) {
                if (MetaClass.class.isAssignableFrom(searchResult.getClass())) {
                    final MetaClass metaClass = (MetaClass)searchResult;
                    final CidsClass cidsClass = CidsClassFactory.getFactory()
                                .restCidsClassFromLegacyCidsClass(metaClass);
                    jsonNode = mapper.convertValue(cidsClass, JsonNode.class);
                } else {
                    final String message = "cannot convert search result item #"
                                + i + " to MetaClass, wrong result type:'"
                                + searchResult.getClass().getSimpleName() + "' ";
                    LOG.error(message);
                    throw new Exception(message);
                }
            } else if (isMetaObject || isLightWightMetaObject) {
                if (MetaObject.class.isAssignableFrom(searchResult.getClass())) {
                    final MetaObject metaObject = (MetaObject)searchResult;
                    final CidsBean cidsBean = metaObject.getBean();
                    jsonNode = mapper.reader().readTree(cidsBean.toJSONString(false));
                } else {
                    final String message = "cannot convert search result item #"
                                + i + " to MetaObject, wrong result type:'"
                                + searchResult.getClass().getSimpleName() + "' ";
                    LOG.error(message);
                    throw new Exception(message);
                }
            } else if (isMetaNode) {
                if (Node.class.isAssignableFrom(searchResult.getClass())) {
                    final Node legacyNode = (Node)searchResult;
                    final String className = classNameCache.getClassNameForClassId(legacyNode.getDomain(),
                            legacyNode.getClassId());
                    final CidsNode cidsNode = CidsNodeFactory.getFactory()
                                .restCidsNodeFromLegacyCidsNode(legacyNode, className);
                    jsonNode = mapper.convertValue(cidsNode, JsonNode.class);
                } else {
                    final String message = "cannot convert search result item #"
                                + i + " to MetaNode, wrong result type:'"
                                + searchResult.getClass().getSimpleName() + "' ";
                    LOG.error(message);
                    throw new Exception(message);
                }
            } else if (isBinaryObject) {
                final String stringRepresentation = toBase64String(searchResult);
                jsonNode = mapper.convertValue(stringRepresentation, JsonNode.class);
            } else {
                jsonNode = mapper.convertValue(searchResult, JsonNode.class);
            }

            jsonNodes.add(jsonNode);
            i++;
        }

        if (i > 0) {
            if (isMetaClass) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + " meta classes (entity info) found and converted by cids server search '"
                                + searchKey + "'");
                }
            } else if (isMetaObject) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + " meta objects (entities) found and converted by cids server search '" + searchKey
                                + "'");
                }
            } else if (isLightWightMetaObject) {
                if (LOG.isDebugEnabled()) {
                    LOG.warn(i + "LightWightMetaObject (entities references) returned by cids server search '"
                                + searchKey + "' converted to full entities! "
                                + "Please use CidsBeanFactory#cidsBeanFromLightweightMetaObject instead.");
                }
            } else if (isMetaNode) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + " nodes found and converted by cids server search '" + searchKey + "'");
                }
            } else if (isBinaryObject) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + "  java objects found and binary serialized by cids server search '" + searchKey
                                + "'");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(i + " java objects of type '"
                                + searchResults.iterator().next().getClass().getName()
                                + "' found and converted by cids server search '" + searchKey + "'");
                }
            }
        }
        return jsonNodes;
    }

    /**
     * Helper method that introspects a CidsServerSearch Class and returns a map with all readable and writable
     * properties that correspond to the search parameters.
     *
     * @param   <C>                DOCUMENT ME!
     * @param   serverSearchClass  server search class to be introspected
     *
     * @return  map with property names and their classes
     *
     * @throws  IntrospectionException  if the server search class could not be introspected
     */
    public <C extends CidsServerSearch> Map<String, Class> findSearchParameters(final Class<C> serverSearchClass)
            throws IntrospectionException {
        final Map<String, Class> propertiesMap = new LinkedHashMap<String, Class>();
        final BeanInfo beanInfo = Introspector.getBeanInfo(serverSearchClass, serverSearchClass.getSuperclass());
        for (final PropertyDescriptor propertyDescritor : beanInfo.getPropertyDescriptors()) {
            if ((propertyDescritor.getReadMethod() != null)
                        && (propertyDescritor.getWriteMethod() != null)) {
                propertiesMap.put(propertyDescritor.getName(), propertyDescritor.getPropertyType());
            }
        }

        return propertiesMap;
    }

    /**
     * Helper method that introspects a CidsServerSearch Class and returns a map with all read methods for all readable
     * and writable properties that correspond to the search parameters.
     *
     * @param   <C>                Class that extends CidsServerSearch
     * @param   serverSearchClass  server search class to be introspected
     *
     * @return  map with property names and their read methods
     *
     * @throws  IntrospectionException  if the server search class could not be introspected
     */
    public <C extends CidsServerSearch> Map<String, Method> findSearchParameterReadMethods(
            final Class<C> serverSearchClass) throws IntrospectionException {
        final Map<String, Method> readMethods = new LinkedHashMap<String, Method>();
        final BeanInfo beanInfo = Introspector.getBeanInfo(serverSearchClass, serverSearchClass.getSuperclass());
        for (final PropertyDescriptor propertyDescritor : beanInfo.getPropertyDescriptors()) {
            if ((propertyDescritor.getReadMethod() != null)
                        && (propertyDescritor.getWriteMethod() != null)) {
                readMethods.put(propertyDescritor.getName(), propertyDescritor.getReadMethod());
            }
        }

        return readMethods;
    }

    /**
     * Helper method that introspects a CidsServerSearch Class and returns a map with all write methods for all readable
     * and writable properties that correspond to the search parameters.
     *
     * @param   <C>                Class that extends CidsServerSearch
     * @param   serverSearchClass  server search class to be introspected
     *
     * @return  map with property names and their write methods
     *
     * @throws  IntrospectionException  if the server search class could not be introspected
     */
    public <C extends CidsServerSearch> Map<String, Method> findSearchParameterWriteMethods(
            final Class<C> serverSearchClass) throws IntrospectionException {
        final Map<String, Method> writeMethods = new LinkedHashMap<String, Method>();
        final BeanInfo beanInfo = Introspector.getBeanInfo(serverSearchClass, serverSearchClass.getSuperclass());
        for (final PropertyDescriptor propertyDescritor : beanInfo.getPropertyDescriptors()) {
            if ((propertyDescritor.getReadMethod() != null)
                        && (propertyDescritor.getWriteMethod() != null)) {
                writeMethods.put(propertyDescritor.getName(), propertyDescritor.getWriteMethod());
            }
        }

        return writeMethods;
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
    private static String toBase64String(final Object object) throws IOException {
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
    private static Object fromBase64String(final String s) throws IOException, ClassNotFoundException {
        final byte[] data = Base64.decode(s);
        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        final Object o = ois.readObject();
        ois.close();
        return o;
    }
}
