
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DVSConfigSpec complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DVSConfigSpec">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="configVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numStandalonePorts" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="maxPorts" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="uplinkPortPolicy" type="{urn:vim25}DVSUplinkPortPolicy" minOccurs="0"/>
 *         &lt;element name="uplinkPortgroup" type="{urn:vim25}ManagedObjectReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="defaultPortConfig" type="{urn:vim25}DVPortSetting" minOccurs="0"/>
 *         &lt;element name="host" type="{urn:vim25}DistributedVirtualSwitchHostMemberConfigSpec" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extensionKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="policy" type="{urn:vim25}DVSPolicy" minOccurs="0"/>
 *         &lt;element name="vendorSpecificConfig" type="{urn:vim25}DistributedVirtualSwitchKeyedOpaqueBlob" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="contact" type="{urn:vim25}DVSContactInfo" minOccurs="0"/>
 *         &lt;element name="switchIpAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DVSConfigSpec", propOrder = {
    "configVersion",
    "name",
    "numStandalonePorts",
    "maxPorts",
    "uplinkPortPolicy",
    "uplinkPortgroup",
    "defaultPortConfig",
    "host",
    "extensionKey",
    "description",
    "policy",
    "vendorSpecificConfig",
    "contact",
    "switchIpAddress"
})
@XmlSeeAlso({
    VMwareDVSConfigSpec.class
})
public class DVSConfigSpec
    extends DynamicData
{

    protected String configVersion;
    protected String name;
    protected Integer numStandalonePorts;
    protected Integer maxPorts;
    protected DVSUplinkPortPolicy uplinkPortPolicy;
    protected List<ManagedObjectReference> uplinkPortgroup;
    protected DVPortSetting defaultPortConfig;
    protected List<DistributedVirtualSwitchHostMemberConfigSpec> host;
    protected String extensionKey;
    protected String description;
    protected DVSPolicy policy;
    protected List<DistributedVirtualSwitchKeyedOpaqueBlob> vendorSpecificConfig;
    protected DVSContactInfo contact;
    protected String switchIpAddress;

    /**
     * Gets the value of the configVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfigVersion() {
        return configVersion;
    }

    /**
     * Sets the value of the configVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfigVersion(String value) {
        this.configVersion = value;
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
     * Gets the value of the numStandalonePorts property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumStandalonePorts() {
        return numStandalonePorts;
    }

    /**
     * Sets the value of the numStandalonePorts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumStandalonePorts(Integer value) {
        this.numStandalonePorts = value;
    }

    /**
     * Gets the value of the maxPorts property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxPorts() {
        return maxPorts;
    }

    /**
     * Sets the value of the maxPorts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxPorts(Integer value) {
        this.maxPorts = value;
    }

    /**
     * Gets the value of the uplinkPortPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link DVSUplinkPortPolicy }
     *     
     */
    public DVSUplinkPortPolicy getUplinkPortPolicy() {
        return uplinkPortPolicy;
    }

    /**
     * Sets the value of the uplinkPortPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link DVSUplinkPortPolicy }
     *     
     */
    public void setUplinkPortPolicy(DVSUplinkPortPolicy value) {
        this.uplinkPortPolicy = value;
    }

    /**
     * Gets the value of the uplinkPortgroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the uplinkPortgroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUplinkPortgroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ManagedObjectReference }
     * 
     * 
     */
    public List<ManagedObjectReference> getUplinkPortgroup() {
        if (uplinkPortgroup == null) {
            uplinkPortgroup = new ArrayList<ManagedObjectReference>();
        }
        return this.uplinkPortgroup;
    }

    /**
     * Gets the value of the defaultPortConfig property.
     * 
     * @return
     *     possible object is
     *     {@link DVPortSetting }
     *     
     */
    public DVPortSetting getDefaultPortConfig() {
        return defaultPortConfig;
    }

    /**
     * Sets the value of the defaultPortConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link DVPortSetting }
     *     
     */
    public void setDefaultPortConfig(DVPortSetting value) {
        this.defaultPortConfig = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the host property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHost().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DistributedVirtualSwitchHostMemberConfigSpec }
     * 
     * 
     */
    public List<DistributedVirtualSwitchHostMemberConfigSpec> getHost() {
        if (host == null) {
            host = new ArrayList<DistributedVirtualSwitchHostMemberConfigSpec>();
        }
        return this.host;
    }

    /**
     * Gets the value of the extensionKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtensionKey() {
        return extensionKey;
    }

    /**
     * Sets the value of the extensionKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtensionKey(String value) {
        this.extensionKey = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the policy property.
     * 
     * @return
     *     possible object is
     *     {@link DVSPolicy }
     *     
     */
    public DVSPolicy getPolicy() {
        return policy;
    }

    /**
     * Sets the value of the policy property.
     * 
     * @param value
     *     allowed object is
     *     {@link DVSPolicy }
     *     
     */
    public void setPolicy(DVSPolicy value) {
        this.policy = value;
    }

    /**
     * Gets the value of the vendorSpecificConfig property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vendorSpecificConfig property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVendorSpecificConfig().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DistributedVirtualSwitchKeyedOpaqueBlob }
     * 
     * 
     */
    public List<DistributedVirtualSwitchKeyedOpaqueBlob> getVendorSpecificConfig() {
        if (vendorSpecificConfig == null) {
            vendorSpecificConfig = new ArrayList<DistributedVirtualSwitchKeyedOpaqueBlob>();
        }
        return this.vendorSpecificConfig;
    }

    /**
     * Gets the value of the contact property.
     * 
     * @return
     *     possible object is
     *     {@link DVSContactInfo }
     *     
     */
    public DVSContactInfo getContact() {
        return contact;
    }

    /**
     * Sets the value of the contact property.
     * 
     * @param value
     *     allowed object is
     *     {@link DVSContactInfo }
     *     
     */
    public void setContact(DVSContactInfo value) {
        this.contact = value;
    }

    /**
     * Gets the value of the switchIpAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwitchIpAddress() {
        return switchIpAddress;
    }

    /**
     * Sets the value of the switchIpAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwitchIpAddress(String value) {
        this.switchIpAddress = value;
    }

}
