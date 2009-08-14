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
 *
 * @author  pascal dihe
 */
public class MetaObjectXMLSerializer {
    private final static MetaObjectXMLSerializer instance = new MetaObjectXMLSerializer();
    
    /** Creates a new instance of MetaObjectXMLSerializer */
    private MetaObjectXMLSerializer() {
        
    }
    
    public final static MetaObjectXMLSerializer getInstance() {
        return instance;
    }
    
    public StringBuffer metaObjectToXML(MetaObject metaObject, HashMap metaClasses) {
        return this.metaObjectsToXML(new MetaObject[] {metaObject,}, metaClasses);
    }
    
    public StringBuffer metaObjectsToXML(MetaObject[] metaObjects, HashMap metaClasses) {
        StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>").append('\n');
        int tab = 1;
        
        buffer.append("<metaObjects>").append('\n');
        
        for(int i = 0; i < metaObjects.length; i++) {
            if(metaObjects[i] != null) {
                this.metaObjectToXML(buffer, metaObjects[i], null, metaClasses, tab);
            }
        }
        
        buffer.append("</metaObjects>").append('\n');
        
        return buffer;
    }
    
    private void metaObjectToXML(StringBuffer buffer, MetaObject metaObject, String name, HashMap metaClasses, int tab) {
        //System.out.println("metaObject: " + metaObject);
        //System.out.println("metaObject.getName(): " + metaObject.getName());
        
        buffer.append(writeTab(tab)).append("<metaObject");
        
        buffer.append(" id=\"").append(metaObject.getID()).append('\"');
        
        if(name == null || name.length() == 0) {
            name = metaObject.getName();
        }
        
        if(name != null && name.length() > 0) {
            buffer.append(" name=\"").append(escape(name)).append('\"');
        }
        
        buffer.append(" classId=\"").append(metaObject.getClassID()).append('\"');
        
        if(metaClasses != null &&  metaClasses.size() > 0) {
            String classKey = metaObject.getClassID() + '@' + metaObject.getDomain();
            if(metaClasses.containsKey(classKey)) {
                //String className = metaClasses.get(classKey).toString();
                String className = ((MetaClass)metaClasses.get(classKey)).getName();
                
                if(className != null && className.length() > 0) {
                    buffer.append(" className=\"").append(escape(className)).append('\"');
                }
            }
        }
        
        String domain = metaObject.getDomain();
        if(domain != null && domain.length() > 0) {
            buffer.append(" domain=\"").append(escape(domain)).append('\"');
        }
        
        String description = metaObject.getDescription();
        if(description != null && description.length() > 0) {
            buffer.append(" description=\"").append(escape(description)).append('\"');
        }
        
        //buffer.append(" group=\"").append(escape(metaObject.getGroup())).append('\"');
        
        buffer.append('>').append('\n');
        
        if(metaObject.getAttributes().size() > 0) {
            buffer.append(writeTab(tab)).append("<attributes>").append('\n');
            tab++;
            
            Iterator iterator = metaObject.getAttributes().values().iterator();
            while(iterator.hasNext()) {
                ObjectAttribute objectAttribute = (ObjectAttribute)iterator.next();
                Object object = objectAttribute.getValue();
                
                if(object instanceof MetaObject) {
                    this.metaObjectToXML(buffer, (MetaObject)object, objectAttribute.getName(), metaClasses, tab);
                } else {
                    buffer.append(writeTab(tab)).append("<attribute");
                    
                    buffer.append(" id=\"").append(objectAttribute.getID()).append('\"');
                    
                    name = objectAttribute.getName();
                    if(name != null && name.length() > 0) {
                        buffer.append(" name=\"").append(escape(name)).append('\"');
                    }
                    
                    description = objectAttribute.getDescription();
                    if(description != null && description.length() > 0) {
                        buffer.append(" description=\"").append(escape(description)).append('\"');
                    }
                    
                    buffer.append('>').append('\n');
                    
                    if(object != null) {
                        tab++;
                        buffer.append(writeTab(tab)).append(escape(String.valueOf(object))).append('\n');
                        tab--;
                    }
                    
                    buffer.append(writeTab(tab)).append("</attribute>").append('\n');
                }
            }
            
            tab--;
            buffer.append(writeTab(tab)).append("</attributes>").append('\n');
        }
        
        buffer.append(writeTab(tab)).append("</metaObject>").append('\n');
    }
    
    
    private StringBuffer writeTab(int num) {
        StringBuffer tab = new StringBuffer(num);
        
        for (int i = 0; i < num; i++) {
            tab.append('\t');
        }
        
        return tab;
    }
    
    private String escape(String xml) {
        return StringEscapeUtils.escapeXml(xml);
    }
}
