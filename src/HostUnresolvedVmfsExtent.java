
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostUnresolvedVmfsExtent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostUnresolvedVmfsExtent">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="device" type="{urn:vim25}HostScsiDiskPartition"/>
 *         &lt;element name="devicePath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="vmfsUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isHeadExtent" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="ordinal" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startBlock" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="endBlock" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostUnresolvedVmfsExtent", propOrder = {
    "device",
    "devicePath",
    "vmfsUuid",
    "isHeadExtent",
    "ordinal",
    "startBlock",
    "endBlock",
    "reason"
})
public class HostUnresolvedVmfsExtent
    extends DynamicData
{

    @XmlElement(required = true)
    protected HostScsiDiskPartition device;
    @XmlElement(required = true)
    protected String devicePath;
    @XmlElement(required = true)
    protected String vmfsUuid;
    protected boolean isHeadExtent;
    protected int ordinal;
    protected int startBlock;
    protected int endBlock;
    @XmlElement(required = true)
    protected String reason;

    /**
     * Gets the value of the device property.
     * 
     * @return
     *     possible object is
     *     {@link HostScsiDiskPartition }
     *     
     */
    public HostScsiDiskPartition getDevice() {
        return device;
    }

    /**
     * Sets the value of the device property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostScsiDiskPartition }
     *     
     */
    public void setDevice(HostScsiDiskPartition value) {
        this.device = value;
    }

    /**
     * Gets the value of the devicePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDevicePath() {
        return devicePath;
    }

    /**
     * Sets the value of the devicePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDevicePath(String value) {
        this.devicePath = value;
    }

    /**
     * Gets the value of the vmfsUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmfsUuid() {
        return vmfsUuid;
    }

    /**
     * Sets the value of the vmfsUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmfsUuid(String value) {
        this.vmfsUuid = value;
    }

    /**
     * Gets the value of the isHeadExtent property.
     * 
     */
    public boolean isIsHeadExtent() {
        return isHeadExtent;
    }

    /**
     * Sets the value of the isHeadExtent property.
     * 
     */
    public void setIsHeadExtent(boolean value) {
        this.isHeadExtent = value;
    }

    /**
     * Gets the value of the ordinal property.
     * 
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Sets the value of the ordinal property.
     * 
     */
    public void setOrdinal(int value) {
        this.ordinal = value;
    }

    /**
     * Gets the value of the startBlock property.
     * 
     */
    public int getStartBlock() {
        return startBlock;
    }

    /**
     * Sets the value of the startBlock property.
     * 
     */
    public void setStartBlock(int value) {
        this.startBlock = value;
    }

    /**
     * Gets the value of the endBlock property.
     * 
     */
    public int getEndBlock() {
        return endBlock;
    }

    /**
     * Sets the value of the endBlock property.
     * 
     */
    public void setEndBlock(int value) {
        this.endBlock = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
    }

}
