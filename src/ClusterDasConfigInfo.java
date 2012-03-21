
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ClusterDasConfigInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClusterDasConfigInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="vmMonitoring" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hostMonitoring" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="failoverLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="admissionControlPolicy" type="{urn:vim25}ClusterDasAdmissionControlPolicy" minOccurs="0"/>
 *         &lt;element name="admissionControlEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="defaultVmSettings" type="{urn:vim25}ClusterDasVmSettings" minOccurs="0"/>
 *         &lt;element name="option" type="{urn:vim25}OptionValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="heartbeatDatastore" type="{urn:vim25}ManagedObjectReference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="hBDatastoreCandidatePolicy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClusterDasConfigInfo", propOrder = {
    "enabled",
    "vmMonitoring",
    "hostMonitoring",
    "failoverLevel",
    "admissionControlPolicy",
    "admissionControlEnabled",
    "defaultVmSettings",
    "option",
    "heartbeatDatastore",
    "hbDatastoreCandidatePolicy"
})
public class ClusterDasConfigInfo
    extends DynamicData
{

    protected Boolean enabled;
    protected String vmMonitoring;
    protected String hostMonitoring;
    protected Integer failoverLevel;
    protected ClusterDasAdmissionControlPolicy admissionControlPolicy;
    protected Boolean admissionControlEnabled;
    protected ClusterDasVmSettings defaultVmSettings;
    protected List<OptionValue> option;
    protected List<ManagedObjectReference> heartbeatDatastore;
    @XmlElement(name = "hBDatastoreCandidatePolicy")
    protected String hbDatastoreCandidatePolicy;

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the vmMonitoring property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmMonitoring() {
        return vmMonitoring;
    }

    /**
     * Sets the value of the vmMonitoring property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmMonitoring(String value) {
        this.vmMonitoring = value;
    }

    /**
     * Gets the value of the hostMonitoring property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHostMonitoring() {
        return hostMonitoring;
    }

    /**
     * Sets the value of the hostMonitoring property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHostMonitoring(String value) {
        this.hostMonitoring = value;
    }

    /**
     * Gets the value of the failoverLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFailoverLevel() {
        return failoverLevel;
    }

    /**
     * Sets the value of the failoverLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFailoverLevel(Integer value) {
        this.failoverLevel = value;
    }

    /**
     * Gets the value of the admissionControlPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterDasAdmissionControlPolicy }
     *     
     */
    public ClusterDasAdmissionControlPolicy getAdmissionControlPolicy() {
        return admissionControlPolicy;
    }

    /**
     * Sets the value of the admissionControlPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterDasAdmissionControlPolicy }
     *     
     */
    public void setAdmissionControlPolicy(ClusterDasAdmissionControlPolicy value) {
        this.admissionControlPolicy = value;
    }

    /**
     * Gets the value of the admissionControlEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAdmissionControlEnabled() {
        return admissionControlEnabled;
    }

    /**
     * Sets the value of the admissionControlEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdmissionControlEnabled(Boolean value) {
        this.admissionControlEnabled = value;
    }

    /**
     * Gets the value of the defaultVmSettings property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterDasVmSettings }
     *     
     */
    public ClusterDasVmSettings getDefaultVmSettings() {
        return defaultVmSettings;
    }

    /**
     * Sets the value of the defaultVmSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterDasVmSettings }
     *     
     */
    public void setDefaultVmSettings(ClusterDasVmSettings value) {
        this.defaultVmSettings = value;
    }

    /**
     * Gets the value of the option property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the option property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOption().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OptionValue }
     * 
     * 
     */
    public List<OptionValue> getOption() {
        if (option == null) {
            option = new ArrayList<OptionValue>();
        }
        return this.option;
    }

    /**
     * Gets the value of the heartbeatDatastore property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the heartbeatDatastore property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHeartbeatDatastore().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ManagedObjectReference }
     * 
     * 
     */
    public List<ManagedObjectReference> getHeartbeatDatastore() {
        if (heartbeatDatastore == null) {
            heartbeatDatastore = new ArrayList<ManagedObjectReference>();
        }
        return this.heartbeatDatastore;
    }

    /**
     * Gets the value of the hbDatastoreCandidatePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHBDatastoreCandidatePolicy() {
        return hbDatastoreCandidatePolicy;
    }

    /**
     * Sets the value of the hbDatastoreCandidatePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHBDatastoreCandidatePolicy(String value) {
        this.hbDatastoreCandidatePolicy = value;
    }

}
