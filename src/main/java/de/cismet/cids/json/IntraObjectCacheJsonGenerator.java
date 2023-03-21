/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class IntraObjectCacheJsonGenerator extends JsonGeneratorDelegate {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(IntraObjectCacheJsonGenerator.class);

    //~ Instance fields --------------------------------------------------------

    HashMap<String, CidsBean> ioc = new HashMap<String, CidsBean>();
    final List<String> propNames = new ArrayList<String>();
    transient IntraObjectCacheJsonParams params;
    int level = -1;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IntraObjectCacheJsonGenerator object.
     *
     * @param  d  DOCUMENT ME!
     */
    public IntraObjectCacheJsonGenerator(final JsonGenerator d) {
        super(d);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsKey(final String key) {
        return ioc.containsKey(key);
    }

    /**
     * DOCUMENT ME!
     */
    public void clear() {
        ioc.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBean get(final String key) {
        return ioc.get(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key   DOCUMENT ME!
     * @param  bean  DOCUMENT ME!
     */
    public void put(final String key, final CidsBean bean) {
        ioc.put(key, bean);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @throws  IOException              DOCUMENT ME!
     * @throws  JsonProcessingException  DOCUMENT ME!
     */
    @Override
    public void writeObject(final Object value) throws IOException, JsonProcessingException {
        if (value == null) {
            // important: call method that does check value write:
            writeNull();
        } else {
            /* 02-Mar-2009, tatu: we are NOT to call _verifyValueWrite here,
             *   because that will be done when codec actually serializes  contained POJO. If we did call it it would
             * advance state  causing exception later on
             */
            if (getCodec() != null) {
                getCodec().writeValue(this, value);
                return;
            }
            _writeSimpleObject(value);
        }
    }

    /**
     * Helper method to try to call appropriate write method for given untyped Object. At this point, no structural
     * conversions should be done, only simple basic types are to be coerced as necessary.
     *
     * @param   value  Non-null value to write
     *
     * @throws  IOException              DOCUMENT ME!
     * @throws  JsonGenerationException  DOCUMENT ME!
     * @throws  IllegalStateException    DOCUMENT ME!
     */
    @Override
    protected void _writeSimpleObject(final Object value) throws IOException, JsonGenerationException {
        /* 31-Dec-2009, tatu: Actually, we could just handle some basic
         *    types even without codec. This can improve interoperability,   and specifically help with TokenBuffer.
         */
        if (value == null) {
            writeNull();
            return;
        }
        if (value instanceof String) {
            writeString((String)value);
            return;
        }
        if (value instanceof Number) {
            final Number n = (Number)value;
            if (n instanceof Integer) {
                writeNumber(n.intValue());
                return;
            } else if (n instanceof Long) {
                writeNumber(n.longValue());
                return;
            } else if (n instanceof Double) {
                writeNumber(n.doubleValue());
                return;
            } else if (n instanceof Float) {
                writeNumber(n.floatValue());
                return;
            } else if (n instanceof Short) {
                writeNumber(n.shortValue());
                return;
            } else if (n instanceof Byte) {
                writeNumber(n.byteValue());
                return;
            } else if (n instanceof BigInteger) {
                writeNumber((BigInteger)n);
                return;
            } else if (n instanceof BigDecimal) {
                writeNumber((BigDecimal)n);
                return;

                // then Atomic types

            } else if (n instanceof AtomicInteger) {
                writeNumber(((AtomicInteger)n).get());
                return;
            } else if (n instanceof AtomicLong) {
                writeNumber(((AtomicLong)n).get());
                return;
            }
        } else if (value instanceof byte[]) {
            writeBinary((byte[])value);
            return;
        } else if (value instanceof Boolean) {
            writeBoolean((Boolean)value);
            return;
        } else if (value instanceof AtomicBoolean) {
            writeBoolean(((AtomicBoolean)value).get());
            return;
        }
        throw new IllegalStateException(
            "No ObjectCodec defined for the generator, can only serialize simple wrapper types (type passed "
                    + value.getClass().getName()
                    + ")");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public IntraObjectCacheJsonParams getParams() {
        return params;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  params  DOCUMENT ME!
     */
    public void setParams(final IntraObjectCacheJsonParams params) {
        this.params = params;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propName  DOCUMENT ME!
     */
    public void appendPropertyName(final String propName) {
        if (level > -1) {
            if (level >= propNames.size()) {
                this.propNames.add(propName);
            } else {
                this.propNames.set(level, propName);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   level  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPropName(final int level) {
        final int toLevel = (level >= propNames.size()) ? propNames.size() : level;
        final String propName = IntraObjectCacheJsonParams.implode(propNames.subList(0, toLevel).toArray(new String[0]),
                ".");
        return propName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCurrentBasePropName() {
        return getPropName(level);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCurrentPropName() {
        return getPropName(level + 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean checkLevelExpand() {
        if (params == null) {
            return true;
        }

        final boolean isLevelSet = (params != null) && (params.getMaxLevel() >= 0);
        final boolean isExpandSet = (params != null) && !params.getExpandPropNames().isEmpty();

        final String basePropNameToCheck = getCurrentBasePropName();

        if (isExpandSet) {
            final int maxLevel;
            if (isLevelSet) {
                maxLevel = params.getMaxLevel();
            } else {
                maxLevel = level;
            }

            // root level always expanded
            if (this.level <= 0) {
                return true;
            }

            // searching for expand properties that begins with the actual property
            for (final String expandPropName : params.getExpandPropNames()) {
                if (expandPropName.startsWith(basePropNameToCheck + ".")) {
                    return true;
                }
            }

            final List<String> props = Arrays.asList(basePropNameToCheck.split("\\."));
            // backward search for expandName (full property, then fullproperty - 1 level, etc...).
            // only needs to go back maxLevel times (index >= (props.size() - maxLevel)
            // taking care not to have negative index (index >= 0)
            for (int index = props.size(); (index >= 0) && (index >= (props.size() - maxLevel)); index--) {
                final String tester = IntraObjectCacheJsonParams.implode(props.subList(0, index).toArray(
                            new String[0]),
                        ".");
                if (params.getExpandPropNames().contains(tester)) {
                    // property of expanded object within level restriction
                    return true;
                }
            }

            return false;
        } else if (isLevelSet) {
            return this.level <= params.getMaxLevel();
        } else {
            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean checkFields() {
        if ((params != null) && !params.getFieldsPropNames().isEmpty()) {
            final String propNameToCheck = getCurrentPropName();
            return (propNameToCheck != null) && params.getFieldsPropNames().contains(propNameToCheck);
        } else {
            return true;
        }
    }

    @Override
    public void writeStartObject() throws IOException, JsonGenerationException {
        this.level++;
        super.writeStartObject(); // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeStartObject(final Object forValue) throws IOException {
        this.level++;
        super.writeStartObject(forValue);
    }

    @Override
    public void writeStartObject(final Object forValue, final int size) throws IOException {
        this.level++;
        super.writeStartObject(forValue, size);
    }

    @Override
    public void writeEndObject() throws IOException, JsonGenerationException {
        super.writeEndObject(); // To change body of generated methods, choose Tools | Templates.
        this.level--;
    }
}
