
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CopyVirtualDiskRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CopyVirtualDiskRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="_this" type="{urn:vim25}ManagedObjectReference"/>
 *         &lt;element name="sourceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sourceDatacenter" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *         &lt;element name="destName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="destDatacenter" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *         &lt;element name="destSpec" type="{urn:vim25}VirtualDiskSpec" minOccurs="0"/>
 *         &lt;element name="force" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CopyVirtualDiskRequestType", propOrder = {
    "_this",
    "sourceName",
    "sourceDatacenter",
    "destName",
    "destDatacenter",
    "destSpec",
    "force"
})
public class CopyVirtualDiskRequestType {

    @XmlElement(required = true)
    protected ManagedObjectReference _this;
    @XmlElement(required = true)
    protected String sourceName;
    protected ManagedObjectReference sourceDatacenter;
    @XmlElement(required = true)
    protected String destName;
    protected ManagedObjectReference destDatacenter;
    protected VirtualDiskSpec destSpec;
    protected Boolean force;

    /**
     * Gets the value of the this property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getThis() {
        return _this;
    }

    /**
     * Sets the value of the this property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setThis(ManagedObjectReference value) {
        this._this = value;
    }

    /**
     * Gets the value of the sourceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Sets the value of the sourceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceName(String value) {
        this.sourceName = value;
    }

    /**
     * Gets the value of the sourceDatacenter property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getSourceDatacenter() {
        return sourceDatacenter;
    }

    /**
     * Sets the value of the sourceDatacenter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setSourceDatacenter(ManagedObjectReference value) {
        this.sourceDatacenter = value;
    }

    /**
     * Gets the value of the destName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestName() {
        return destName;
    }

    /**
     * Sets the value of the destName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestName(String value) {
        this.destName = value;
    }

    /**
     * Gets the value of the destDatacenter property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getDestDatacenter() {
        return destDatacenter;
    }

    /**
     * Sets the value of the destDatacenter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setDestDatacenter(ManagedObjectReference value) {
        this.destDatacenter = value;
    }

    /**
     * Gets the value of the destSpec property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualDiskSpec }
     *     
     */
    public VirtualDiskSpec getDestSpec() {
        return destSpec;
    }

    /**
     * Sets the value of the destSpec property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualDiskSpec }
     *     
     */
    public void setDestSpec(VirtualDiskSpec value) {
        this.destSpec = value;
    }

    /**
     * Gets the value of the force property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForce() {
        return force;
    }

    /**
     * Sets the value of the force property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForce(Boolean value) {
        this.force = value;
    }

}
