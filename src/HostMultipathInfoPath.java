
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostMultipathInfoPath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostMultipathInfoPath">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pathState" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isWorkingPath" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="adapter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lun" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transport" type="{urn:vim25}HostTargetTransport" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostMultipathInfoPath", propOrder = {
    "key",
    "name",
    "pathState",
    "state",
    "isWorkingPath",
    "adapter",
    "lun",
    "transport"
})
public class HostMultipathInfoPath
    extends DynamicData
{

    @XmlElement(required = true)
    protected String key;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String pathState;
    protected String state;
    protected Boolean isWorkingPath;
    @XmlElement(required = true)
    protected String adapter;
    @XmlElement(required = true)
    protected String lun;
    protected HostTargetTransport transport;

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the pathState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPathState() {
        return pathState;
    }

    /**
     * Sets the value of the pathState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPathState(String value) {
        this.pathState = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String value) {
        this.state = value;
    }

    /**
     * Gets the value of the isWorkingPath property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsWorkingPath() {
        return isWorkingPath;
    }

    /**
     * Sets the value of the isWorkingPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsWorkingPath(Boolean value) {
        this.isWorkingPath = value;
    }

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
     * Gets the value of the lun property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLun() {
        return lun;
    }

    /**
     * Sets the value of the lun property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLun(String value) {
        this.lun = value;
    }

    /**
     * Gets the value of the transport property.
     * 
     * @return
     *     possible object is
     *     {@link HostTargetTransport }
     *     
     */
    public HostTargetTransport getTransport() {
        return transport;
    }

    /**
     * Sets the value of the transport property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostTargetTransport }
     *     
     */
    public void setTransport(HostTargetTransport value) {
        this.transport = value;
    }

}
