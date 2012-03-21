
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostDatastoreSystemCapabilities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostDatastoreSystemCapabilities">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="nfsMountCreationRequired" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="nfsMountCreationSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="localDatastoreSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="vmfsExtentExpansionSupported" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostDatastoreSystemCapabilities", propOrder = {
    "nfsMountCreationRequired",
    "nfsMountCreationSupported",
    "localDatastoreSupported",
    "vmfsExtentExpansionSupported"
})
public class HostDatastoreSystemCapabilities
    extends DynamicData
{

    protected boolean nfsMountCreationRequired;
    protected boolean nfsMountCreationSupported;
    protected boolean localDatastoreSupported;
    protected Boolean vmfsExtentExpansionSupported;

    /**
     * Gets the value of the nfsMountCreationRequired property.
     * 
     */
    public boolean isNfsMountCreationRequired() {
        return nfsMountCreationRequired;
    }

    /**
     * Sets the value of the nfsMountCreationRequired property.
     * 
     */
    public void setNfsMountCreationRequired(boolean value) {
        this.nfsMountCreationRequired = value;
    }

    /**
     * Gets the value of the nfsMountCreationSupported property.
     * 
     */
    public boolean isNfsMountCreationSupported() {
        return nfsMountCreationSupported;
    }

    /**
     * Sets the value of the nfsMountCreationSupported property.
     * 
     */
    public void setNfsMountCreationSupported(boolean value) {
        this.nfsMountCreationSupported = value;
    }

    /**
     * Gets the value of the localDatastoreSupported property.
     * 
     */
    public boolean isLocalDatastoreSupported() {
        return localDatastoreSupported;
    }

    /**
     * Sets the value of the localDatastoreSupported property.
     * 
     */
    public void setLocalDatastoreSupported(boolean value) {
        this.localDatastoreSupported = value;
    }

    /**
     * Gets the value of the vmfsExtentExpansionSupported property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isVmfsExtentExpansionSupported() {
        return vmfsExtentExpansionSupported;
    }

    /**
     * Sets the value of the vmfsExtentExpansionSupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVmfsExtentExpansionSupported(Boolean value) {
        this.vmfsExtentExpansionSupported = value;
    }

}
