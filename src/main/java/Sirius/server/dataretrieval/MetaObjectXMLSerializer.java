/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MetaObjectXMLSerializer.java
 *
 * Created on 8. Mai 2007, 10:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.dataretrieval;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;

import Sirius.server.middleware.types.*;
import Sirius.server.localserver.attribute.ObjectAttribute;

/**
 * DOCUMENT ME!
 *
 * @author   pascal dihe
 * @version  $Revision$, $Date$
 */
public class MetaObjectXMLSerializer {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaObjectXMLSerializer instance = new MetaObjectXMLSerializer();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MetaObjectXMLSerializer.
     */
    private MetaObjectXMLSerializer() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final MetaObjectXMLSerializer getInstance() {
        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject   DOCUMENT ME!
     * @param   metaClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public StringBuffer metaObjectToXML(MetaObject metaObject, HashMap metaClasses) {
        return this.metaObjectsToXML(new MetaObject[] { metaObject, }, metaClasses);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObjects  DOCUMENT ME!
     * @param   metaClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public StringBuffer metaObjectsToXML(MetaObject[] metaObjects, HashMap metaClasses) {
        StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>").append('\n');  // NOI18N
        int tab = 1;

        buffer.append("<metaObjects>").append('\n');  // NOI18N

        for (int i = 0; i < metaObjects.length; i++) {
            if (metaObjects[i] != null) {
                this.metaObjectToXML(buffer, metaObjects[i], null, metaClasses, tab);
            }
        }

        buffer.append("</metaObjects>").append('\n');  // NOI18N

        return buffer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buffer       DOCUMENT ME!
     * @param  metaObject   DOCUMENT ME!
     * @param  name         DOCUMENT ME!
     * @param  metaClasses  DOCUMENT ME!
     * @param  tab          DOCUMENT ME!
     */
    private void metaObjectToXML(
            StringBuffer buffer,
            MetaObject metaObject,
            String name,
            HashMap metaClasses,
            int tab) {
        // System.out.println("metaObject: " + metaObject);
        // System.out.println("metaObject.getName(): " + metaObject.getName());

        buffer.append(writeTab(tab)).append("<metaObject");  // NOI18N

        buffer.append(" id=\"").append(metaObject.getID()).append('\"');  // NOI18N

        if ((name == null) || (name.length() == 0)) {
            name = metaObject.getName();
        }

        if ((name != null) && (name.length() > 0)) {
            buffer.append(" name=\"").append(escape(name)).append('\"');  // NOI18N
        }

        buffer.append(" classId=\"").append(metaObject.getClassID()).append('\"');  // NOI18N

        if ((metaClasses != null) && (metaClasses.size() > 0)) {
            String classKey = metaObject.getClassID() + '@' + metaObject.getDomain();  // NOI18N
            if (metaClasses.containsKey(classKey)) {
                // String className = metaClasses.get(classKey).toString();
                String className = ((MetaClass)metaClasses.get(classKey)).getName();

                if ((className != null) && (className.length() > 0)) {
                    buffer.append(" className=\"").append(escape(className)).append('\"');  // NOI18N
                }
            }
        }

        String domain = metaObject.getDomain();
        if ((domain != null) && (domain.length() > 0)) {
            buffer.append(" domain=\"").append(escape(domain)).append('\"');  // NOI18N
        }

        String description = metaObject.getDescription();
        if ((description != null) && (description.length() > 0)) {
            buffer.append(" description=\"").append(escape(description)).append('\"');  // NOI18N
        }

        // buffer.append(" group=\"").append(escape(metaObject.getGroup())).append('\"');

        buffer.append('>').append('\n');  // NOI18N

        if (metaObject.getAttributes().size() > 0) {
            buffer.append(writeTab(tab)).append("<attributes>").append('\n');  // NOI18N
            tab++;

            Iterator iterator = metaObject.getAttributes().values().iterator();
            while (iterator.hasNext()) {
                ObjectAttribute objectAttribute = (ObjectAttribute)iterator.next();
                Object object = objectAttribute.getValue();

                if (object instanceof MetaObject) {
                    this.metaObjectToXML(buffer, (MetaObject)object, objectAttribute.getName(), metaClasses, tab);
                } else {
                    buffer.append(writeTab(tab)).append("<attribute");  // NOI18N

                    buffer.append(" id=\"").append(objectAttribute.getID()).append('\"');  // NOI18N

                    name = objectAttribute.getName();
                    if ((name != null) && (name.length() > 0)) {
                        buffer.append(" name=\"").append(escape(name)).append('\"');  // NOI18N
                    }

                    description = objectAttribute.getDescription();
                    if ((description != null) && (description.length() > 0)) {
                        buffer.append(" description=\"").append(escape(description)).append('\"');  // NOI18N
                    }

                    buffer.append('>').append('\n');  // NOI18N

                    if (object != null) {
                        tab++;
                        buffer.append(writeTab(tab)).append(escape(String.valueOf(object))).append('\n');  // NOI18N
                        tab--;
                    }

                    buffer.append(writeTab(tab)).append("</attribute>").append('\n');  // NOI18N
                }
            }

            tab--;
            buffer.append(writeTab(tab)).append("</attributes>").append('\n');  // NOI18N
        }

        buffer.append(writeTab(tab)).append("</metaObject>").append('\n');  // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   num  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private StringBuffer writeTab(int num) {
        StringBuffer tab = new StringBuffer(num);

        for (int i = 0; i < num; i++) {
            tab.append('\t');  // NOI18N
        }

        return tab;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   xml  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String escape(String xml) {
        return StringEscapeUtils.escapeXml(xml);
    }
}
