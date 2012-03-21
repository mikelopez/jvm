
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostConfigSummary complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostConfigSummary">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="sslThumbprint" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="product" type="{urn:vim25}AboutInfo" minOccurs="0"/>
 *         &lt;element name="vmotionEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="faultToleranceEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="featureVersion" type="{urn:vim25}HostFeatureVersionInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="agentVmDatastore" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *         &lt;element name="agentVmNetwork" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostConfigSummary", propOrder = {
    "name",
    "port",
    "sslThumbprint",
    "product",
    "vmotionEnabled",
    "faultToleranceEnabled",
    "featureVersion",
    "agentVmDatastore",
    "agentVmNetwork"
})
public class HostConfigSummary
    extends DynamicData
{

    @XmlElement(required = true)
    protected String name;
    protected int port;
    protected String sslThumbprint;
    protected AboutInfo product;
    protected boolean vmotionEnabled;
    protected Boolean faultToleranceEnabled;
    protected List<HostFeatureVersionInfo> featureVersion;
    protected ManagedObjectReference agentVmDatastore;
    protected ManagedObjectReference agentVmNetwork;

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
     * Gets the value of the port property.
     * 
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     */
    public void setPort(int value) {
        this.port = value;
    }

    /**
     * Gets the value of the sslThumbprint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSslThumbprint() {
        return sslThumbprint;
    }

    /**
     * Sets the value of the sslThumbprint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSslThumbprint(String value) {
        this.sslThumbprint = value;
    }

    /**
     * Gets the value of the product property.
     * 
     * @return
     *     possible object is
     *     {@link AboutInfo }
     *     
     */
    public AboutInfo getProduct() {
        return product;
    }

    /**
     * Sets the value of the product property.
     * 
     * @param value
     *     allowed object is
     *     {@link AboutInfo }
     *     
     */
    public void setProduct(AboutInfo value) {
        this.product = value;
    }

    /**
     * Gets the value of the vmotionEnabled property.
     * 
     */
    public boolean isVmotionEnabled() {
        return vmotionEnabled;
    }

    /**
     * Sets the value of the vmotionEnabled property.
     * 
     */
    public void setVmotionEnabled(boolean value) {
        this.vmotionEnabled = value;
    }

    /**
     * Gets the value of the faultToleranceEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFaultToleranceEnabled() {
        return faultToleranceEnabled;
    }

    /**
     * Sets the value of the faultToleranceEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFaultToleranceEnabled(Boolean value) {
        this.faultToleranceEnabled = value;
    }

    /**
     * Gets the value of the featureVersion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the featureVersion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFeatureVersion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostFeatureVersionInfo }
     * 
     * 
     */
    public List<HostFeatureVersionInfo> getFeatureVersion() {
        if (featureVersion == null) {
            featureVersion = new ArrayList<HostFeatureVersionInfo>();
        }
        return this.featureVersion;
    }

    /**
     * Gets the value of the agentVmDatastore property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getAgentVmDatastore() {
        return agentVmDatastore;
    }

    /**
     * Sets the value of the agentVmDatastore property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setAgentVmDatastore(ManagedObjectReference value) {
        this.agentVmDatastore = value;
    }

    /**
     * Gets the value of the agentVmNetwork property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getAgentVmNetwork() {
        return agentVmNetwork;
    }

    /**
     * Sets the value of the agentVmNetwork property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setAgentVmNetwork(ManagedObjectReference value) {
        this.agentVmNetwork = value;
    }

}
