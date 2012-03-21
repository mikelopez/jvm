
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostPortGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostPortGroup">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="port" type="{urn:vim25}HostPortGroupPort" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="vswitch" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="computedPolicy" type="{urn:vim25}HostNetworkPolicy"/>
 *         &lt;element name="spec" type="{urn:vim25}HostPortGroupSpec"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostPortGroup", propOrder = {
    "key",
    "port",
    "vswitch",
    "computedPolicy",
    "spec"
})
public class HostPortGroup
    extends DynamicData
{

    protected String key;
    protected List<HostPortGroupPort> port;
    protected String vswitch;
    @XmlElement(required = true)
    protected HostNetworkPolicy computedPolicy;
    @XmlElement(required = true)
    protected HostPortGroupSpec spec;

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
     * Gets the value of the port property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the port property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPort().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostPortGroupPort }
     * 
     * 
     */
    public List<HostPortGroupPort> getPort() {
        if (port == null) {
            port = new ArrayList<HostPortGroupPort>();
        }
        return this.port;
    }

    /**
     * Gets the value of the vswitch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVswitch() {
        return vswitch;
    }

    /**
     * Sets the value of the vswitch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVswitch(String value) {
        this.vswitch = value;
    }

    /**
     * Gets the value of the computedPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link HostNetworkPolicy }
     *     
     */
    public HostNetworkPolicy getComputedPolicy() {
        return computedPolicy;
    }

    /**
     * Sets the value of the computedPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostNetworkPolicy }
     *     
     */
    public void setComputedPolicy(HostNetworkPolicy value) {
        this.computedPolicy = value;
    }

    /**
     * Gets the value of the spec property.
     * 
     * @return
     *     possible object is
     *     {@link HostPortGroupSpec }
     *     
     */
    public HostPortGroupSpec getSpec() {
        return spec;
    }

    /**
     * Sets the value of the spec property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostPortGroupSpec }
     *     
     */
    public void setSpec(HostPortGroupSpec value) {
        this.spec = value;
    }

}
