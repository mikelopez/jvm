
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualUSBController complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualUSBController">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VirtualController">
 *       &lt;sequence>
 *         &lt;element name="autoConnectDevices" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ehciEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualUSBController", propOrder = {
    "autoConnectDevices",
    "ehciEnabled"
})
public class VirtualUSBController
    extends VirtualController
{

    protected Boolean autoConnectDevices;
    protected Boolean ehciEnabled;

    /**
     * Gets the value of the autoConnectDevices property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAutoConnectDevices() {
        return autoConnectDevices;
    }

    /**
     * Sets the value of the autoConnectDevices property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoConnectDevices(Boolean value) {
        this.autoConnectDevices = value;
    }

    /**
     * Gets the value of the ehciEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEhciEnabled() {
        return ehciEnabled;
    }

    /**
     * Sets the value of the ehciEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEhciEnabled(Boolean value) {
        this.ehciEnabled = value;
    }

}
