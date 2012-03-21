
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostDiskPartitionAttributes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostDiskPartitionAttributes">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="partition" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startSector" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="endSector" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="guid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="logical" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="attributes" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *         &lt;element name="partitionAlignment" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostDiskPartitionAttributes", propOrder = {
    "partition",
    "startSector",
    "endSector",
    "type",
    "guid",
    "logical",
    "attributes",
    "partitionAlignment"
})
public class HostDiskPartitionAttributes
    extends DynamicData
{

    protected int partition;
    protected long startSector;
    protected long endSector;
    @XmlElement(required = true)
    protected String type;
    protected String guid;
    protected boolean logical;
    protected byte attributes;
    protected Long partitionAlignment;

    /**
     * Gets the value of the partition property.
     * 
     */
    public int getPartition() {
        return partition;
    }

    /**
     * Sets the value of the partition property.
     * 
     */
    public void setPartition(int value) {
        this.partition = value;
    }

    /**
     * Gets the value of the startSector property.
     * 
     */
    public long getStartSector() {
        return startSector;
    }

    /**
     * Sets the value of the startSector property.
     * 
     */
    public void setStartSector(long value) {
        this.startSector = value;
    }

    /**
     * Gets the value of the endSector property.
     * 
     */
    public long getEndSector() {
        return endSector;
    }

    /**
     * Sets the value of the endSector property.
     * 
     */
    public void setEndSector(long value) {
        this.endSector = value;
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
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the logical property.
     * 
     */
    public boolean isLogical() {
        return logical;
    }

    /**
     * Sets the value of the logical property.
     * 
     */
    public void setLogical(boolean value) {
        this.logical = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     */
    public byte getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     */
    public void setAttributes(byte value) {
        this.attributes = value;
    }

    /**
     * Gets the value of the partitionAlignment property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPartitionAlignment() {
        return partitionAlignment;
    }

    /**
     * Sets the value of the partitionAlignment property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPartitionAlignment(Long value) {
        this.partitionAlignment = value;
    }

}
