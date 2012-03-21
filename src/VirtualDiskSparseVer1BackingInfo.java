
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualDiskSparseVer1BackingInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualDiskSparseVer1BackingInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VirtualDeviceFileBackingInfo">
 *       &lt;sequence>
 *         &lt;element name="diskMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="split" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="writeThrough" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="spaceUsedInKB" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="contentId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parent" type="{urn:vim25}VirtualDiskSparseVer1BackingInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualDiskSparseVer1BackingInfo", propOrder = {
    "diskMode",
    "split",
    "writeThrough",
    "spaceUsedInKB",
    "contentId",
    "parent"
})
public class VirtualDiskSparseVer1BackingInfo
    extends VirtualDeviceFileBackingInfo
{

    @XmlElement(required = true)
    protected String diskMode;
    protected Boolean split;
    protected Boolean writeThrough;
    protected Long spaceUsedInKB;
    protected String contentId;
    protected VirtualDiskSparseVer1BackingInfo parent;

    /**
     * Gets the value of the diskMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDiskMode() {
        return diskMode;
    }

    /**
     * Sets the value of the diskMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDiskMode(String value) {
        this.diskMode = value;
    }

    /**
     * Gets the value of the split property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSplit() {
        return split;
    }

    /**
     * Sets the value of the split property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSplit(Boolean value) {
        this.split = value;
    }

    /**
     * Gets the value of the writeThrough property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWriteThrough() {
        return writeThrough;
    }

    /**
     * Sets the value of the writeThrough property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWriteThrough(Boolean value) {
        this.writeThrough = value;
    }

    /**
     * Gets the value of the spaceUsedInKB property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSpaceUsedInKB() {
        return spaceUsedInKB;
    }

    /**
     * Sets the value of the spaceUsedInKB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSpaceUsedInKB(Long value) {
        this.spaceUsedInKB = value;
    }

    /**
     * Gets the value of the contentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * Sets the value of the contentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentId(String value) {
        this.contentId = value;
    }

    /**
     * Gets the value of the parent property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualDiskSparseVer1BackingInfo }
     *     
     */
    public VirtualDiskSparseVer1BackingInfo getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualDiskSparseVer1BackingInfo }
     *     
     */
    public void setParent(VirtualDiskSparseVer1BackingInfo value) {
        this.parent = value;
    }

}
