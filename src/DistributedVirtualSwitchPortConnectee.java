
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DistributedVirtualSwitchPortConnectee complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DistributedVirtualSwitchPortConnectee">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="connectedEntity" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *         &lt;element name="nicKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="addressHint" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistributedVirtualSwitchPortConnectee", propOrder = {
    "connectedEntity",
    "nicKey",
    "type",
    "addressHint"
})
public class DistributedVirtualSwitchPortConnectee
    extends DynamicData
{

    protected ManagedObjectReference connectedEntity;
    protected String nicKey;
    protected String type;
    protected String addressHint;

    /**
     * Gets the value of the connectedEntity property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getConnectedEntity() {
        return connectedEntity;
    }

    /**
     * Sets the value of the connectedEntity property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setConnectedEntity(ManagedObjectReference value) {
        this.connectedEntity = value;
    }

    /**
     * Gets the value of the nicKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNicKey() {
        return nicKey;
    }

    /**
     * Sets the value of the nicKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNicKey(String value) {
        this.nicKey = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the addressHint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddressHint() {
        return addressHint;
    }

    /**
     * Sets the value of the addressHint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddressHint(String value) {
        this.addressHint = value;
    }

}
