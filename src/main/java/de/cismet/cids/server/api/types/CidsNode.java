/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.api.types;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.log4j.Logger;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.cismet.cids.base.types.Key;

/**
 * cids node REST API Type and JSON Serializer / Deserializer Configuration. Contains several legacy properties to
 * ensure compatibility with legacy MetaService API.
 *
 * <p>TODO: Integrate into <strong>cids-server-rest-types project</strong>!</p>
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CidsNode implements Key {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsNode.class);

    //~ Instance fields --------------------------------------------------------

    @XmlTransient
    @JsonIgnore
    /**
     * ID is part of the Key ($self reference). No need to store it twice.
     */
    private String id;
    private String name;
    private String description;
    @XmlTransient
    @JsonIgnore
    /**
     * Domain is part of the Key ($self reference). No need to store it twice.
     */
    private String domain;
    private String classKey = null;
    private String objectKey = null;
    private String dynamicChildren;
    private boolean clientSort = false;
    private boolean derivePermissionsFromClass = true;
    private boolean leaf = false;
    private boolean dynamic = false;
    private String icon = null;
    /**
     * DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    @JsonProperty("LEGACY_ICON_FACTORY")
    private int iconFactory;
    private String policy = "STANDARD";
    /**
     * DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    @JsonProperty("LEGACY_ARTIFICIAL_ID")
    private String artificialId = null;

    /**
     * DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    @JsonProperty("LEGACY_CLASS_ID")
    private int classId;

    //~ Methods ----------------------------------------------------------------

    /**
     * Return the key resp. the $self reference of the cids node instance.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    @JsonGetter("$self")
    public String getKey() {
        return new StringBuffer("/").append(domain).append('.').append(id).toString();
    }

    /**
     * Sets the key resp. the $self reference of the cids node instance and derives and sets additionally the domain
     * property.
     *
     * @param  key  the $self reference of the cids class
     */
    @JsonSetter("$self")
    public void setKey(final String key) {
        final int domainSeparator = key.lastIndexOf('.');
        if ((domainSeparator > 3) && (key.length() > (domainSeparator + 1))) {
            // ignore trailing /
            this.domain = key.substring(1, domainSeparator);
            this.id = key.substring(domainSeparator + 1);
        } else {
            LOG.error("invalid node key provided: '" + key
                        + "', expected $self reference: '/DOMAIN.NODE_ID'");
            this.domain = "LOCAL";
            this.id = "-1";
        }
    }

    /**
     * Returns the object id of legacy object node derived from the property object key.
     *
     * @return      legacy object id of legacy object node
     *
     * @deprecated  DOCUMENT ME!
     */
    @JsonProperty("LEGACY_OBJECT_ID")
    public int getObjectId() {
        if ((this.objectKey != null) && (this.objectKey.lastIndexOf('/') != -1)) {
            try {
                final int objectId = Integer.parseInt(this.objectKey.substring(this.objectKey.lastIndexOf('/') + 1));
                return objectId;
            } catch (Exception ex) {
                LOG.error("could not get object id from object key '" + this.objectKey
                            + "' for node '" + this.name + "' (" + this.id + "): "
                            + ex.getMessage(),
                    ex);
            }
        } else {
            LOG.warn("could not get object id from object key '" + this.objectKey
                        + "' for node '" + this.name + "' (" + this.id + ")");
        }

        return -1;
    }
}
