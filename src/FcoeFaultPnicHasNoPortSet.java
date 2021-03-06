
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FcoeFaultPnicHasNoPortSet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FcoeFaultPnicHasNoPortSet">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}FcoeFault">
 *       &lt;sequence>
 *         &lt;element name="nicDevice" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FcoeFaultPnicHasNoPortSet", propOrder = {
    "nicDevice"
})
public class FcoeFaultPnicHasNoPortSet
    extends FcoeFault
{

    @XmlElement(required = true)
    protected String nicDevice;

    /**
     * Gets the value of the nicDevice property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNicDevice() {
        return nicDevice;
    }

    /**
     * Sets the value of the nicDevice property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNicDevice(String value) {
        this.nicDevice = value;
    }

}
