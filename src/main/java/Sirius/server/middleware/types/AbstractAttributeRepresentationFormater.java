/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.types;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author srichter
 */
public abstract class AbstractAttributeRepresentationFormater implements Serializable {

    private Map<String, Object> attributes;

    public abstract String getRepresentation();

    void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public final Object getAttribute(String aName) {
        if (attributes == null) {
            throw new IllegalStateException("Attribute map has not been initialized (is null)!");
        }
        if (aName != null) {
            aName = aName.toLowerCase();
        }
        return attributes.get(aName);
    }
}
