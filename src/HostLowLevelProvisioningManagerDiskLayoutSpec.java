
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostLowLevelProvisioningManagerDiskLayoutSpec complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostLowLevelProvisioningManagerDiskLayoutSpec">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="controllerType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="busNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unitNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="srcFilename" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dstFilename" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostLowLevelProvisioningManagerDiskLayoutSpec", propOrder = {
    "controllerType",
    "busNumber",
    "unitNumber",
    "srcFilename",
    "dstFilename"
})
public class HostLowLevelProvisioningManagerDiskLayoutSpec
    extends DynamicData
{

    @XmlElement(required = true)
    protected String controllerType;
    protected int busNumber;
    protected int unitNumber;
    @XmlElement(required = true)
    protected String srcFilename;
    @XmlElement(required = true)
    protected String dstFilename;

    /**
     * Gets the value of the controllerType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getControllerType() {
        return controllerType;
    }

    /**
     * Sets the value of the controllerType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setControllerType(String value) {
        this.controllerType = value;
    }

    /**
     * Gets the value of the busNumber property.
     * 
     */
    public int getBusNumber() {
        return busNumber;
    }

    /**
     * Sets the value of the busNumber property.
     * 
     */
    public void setBusNumber(int value) {
        this.busNumber = value;
    }

    /**
     * Gets the value of the unitNumber property.
     * 
     */
    public int getUnitNumber() {
        return unitNumber;
    }

    /**
     * Sets the value of the unitNumber property.
     * 
     */
    public void setUnitNumber(int value) {
        this.unitNumber = value;
    }

    /**
     * Gets the value of the srcFilename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrcFilename() {
        return srcFilename;
    }

    /**
     * Sets the value of the srcFilename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrcFilename(String value) {
        this.srcFilename = value;
    }

    /**
     * Gets the value of the dstFilename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDstFilename() {
        return dstFilename;
    }

    /**
     * Sets the value of the dstFilename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDstFilename(String value) {
        this.dstFilename = value;
    }

}
