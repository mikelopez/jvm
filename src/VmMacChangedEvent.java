
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VmMacChangedEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VmMacChangedEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VmEvent">
 *       &lt;sequence>
 *         &lt;element name="adapter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="oldMac" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="newMac" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VmMacChangedEvent", propOrder = {
    "adapter",
    "oldMac",
    "newMac"
})
public class VmMacChangedEvent
    extends VmEvent
{

    @XmlElement(required = true)
    protected String adapter;
    @XmlElement(required = true)
    protected String oldMac;
    @XmlElement(required = true)
    protected String newMac;

    /**
     * Gets the value of the adapter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdapter() {
        return adapter;
    }

    /**
     * Sets the value of the adapter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdapter(String value) {
        this.adapter = value;
    }

    /**
     * Gets the value of the oldMac property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldMac() {
        return oldMac;
    }

    /**
     * Sets the value of the oldMac property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldMac(String value) {
        this.oldMac = value;
    }

    /**
     * Gets the value of the newMac property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewMac() {
        return newMac;
    }

    /**
     * Sets the value of the newMac property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewMac(String value) {
        this.newMac = value;
    }

}
