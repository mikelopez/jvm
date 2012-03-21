
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostHardwareSummary complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostHardwareSummary">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="vendor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="otherIdentifyingInfo" type="{urn:vim25}HostSystemIdentificationInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="memorySize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="cpuModel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cpuMhz" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="numCpuPkgs" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *         &lt;element name="numCpuCores" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *         &lt;element name="numCpuThreads" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *         &lt;element name="numNics" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="numHBAs" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostHardwareSummary", propOrder = {
    "vendor",
    "model",
    "uuid",
    "otherIdentifyingInfo",
    "memorySize",
    "cpuModel",
    "cpuMhz",
    "numCpuPkgs",
    "numCpuCores",
    "numCpuThreads",
    "numNics",
    "numHBAs"
})
public class HostHardwareSummary
    extends DynamicData
{

    @XmlElement(required = true)
    protected String vendor;
    @XmlElement(required = true)
    protected String model;
    @XmlElement(required = true)
    protected String uuid;
    protected List<HostSystemIdentificationInfo> otherIdentifyingInfo;
    protected long memorySize;
    @XmlElement(required = true)
    protected String cpuModel;
    protected int cpuMhz;
    protected short numCpuPkgs;
    protected short numCpuCores;
    protected short numCpuThreads;
    protected int numNics;
    protected int numHBAs;

    /**
     * Gets the value of the vendor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Sets the value of the vendor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVendor(String value) {
        this.vendor = value;
    }

    /**
     * Gets the value of the model property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModel(String value) {
        this.model = value;
    }

    /**
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the otherIdentifyingInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherIdentifyingInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherIdentifyingInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostSystemIdentificationInfo }
     * 
     * 
     */
    public List<HostSystemIdentificationInfo> getOtherIdentifyingInfo() {
        if (otherIdentifyingInfo == null) {
            otherIdentifyingInfo = new ArrayList<HostSystemIdentificationInfo>();
        }
        return this.otherIdentifyingInfo;
    }

    /**
     * Gets the value of the memorySize property.
     * 
     */
    public long getMemorySize() {
        return memorySize;
    }

    /**
     * Sets the value of the memorySize property.
     * 
     */
    public void setMemorySize(long value) {
        this.memorySize = value;
    }

    /**
     * Gets the value of the cpuModel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCpuModel() {
        return cpuModel;
    }

    /**
     * Sets the value of the cpuModel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCpuModel(String value) {
        this.cpuModel = value;
    }

    /**
     * Gets the value of the cpuMhz property.
     * 
     */
    public int getCpuMhz() {
        return cpuMhz;
    }

    /**
     * Sets the value of the cpuMhz property.
     * 
     */
    public void setCpuMhz(int value) {
        this.cpuMhz = value;
    }

    /**
     * Gets the value of the numCpuPkgs property.
     * 
     */
    public short getNumCpuPkgs() {
        return numCpuPkgs;
    }

    /**
     * Sets the value of the numCpuPkgs property.
     * 
     */
    public void setNumCpuPkgs(short value) {
        this.numCpuPkgs = value;
    }

    /**
     * Gets the value of the numCpuCores property.
     * 
     */
    public short getNumCpuCores() {
        return numCpuCores;
    }

    /**
     * Sets the value of the numCpuCores property.
     * 
     */
    public void setNumCpuCores(short value) {
        this.numCpuCores = value;
    }

    /**
     * Gets the value of the numCpuThreads property.
     * 
     */
    public short getNumCpuThreads() {
        return numCpuThreads;
    }

    /**
     * Sets the value of the numCpuThreads property.
     * 
     */
    public void setNumCpuThreads(short value) {
        this.numCpuThreads = value;
    }

    /**
     * Gets the value of the numNics property.
     * 
     */
    public int getNumNics() {
        return numNics;
    }

    /**
     * Sets the value of the numNics property.
     * 
     */
    public void setNumNics(int value) {
        this.numNics = value;
    }

    /**
     * Gets the value of the numHBAs property.
     * 
     */
    public int getNumHBAs() {
        return numHBAs;
    }

    /**
     * Sets the value of the numHBAs property.
     * 
     */
    public void setNumHBAs(int value) {
        this.numHBAs = value;
    }

}
