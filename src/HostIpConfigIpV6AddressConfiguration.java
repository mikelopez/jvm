
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostIpConfigIpV6AddressConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostIpConfigIpV6AddressConfiguration">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="ipV6Address" type="{urn:vim25}HostIpConfigIpV6Address" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="autoConfigurationEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="dhcpV6Enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostIpConfigIpV6AddressConfiguration", propOrder = {
    "ipV6Address",
    "autoConfigurationEnabled",
    "dhcpV6Enabled"
})
public class HostIpConfigIpV6AddressConfiguration
    extends DynamicData
{

    protected List<HostIpConfigIpV6Address> ipV6Address;
    protected Boolean autoConfigurationEnabled;
    protected Boolean dhcpV6Enabled;

    /**
     * Gets the value of the ipV6Address property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ipV6Address property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIpV6Address().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostIpConfigIpV6Address }
     * 
     * 
     */
    public List<HostIpConfigIpV6Address> getIpV6Address() {
        if (ipV6Address == null) {
            ipV6Address = new ArrayList<HostIpConfigIpV6Address>();
        }
        return this.ipV6Address;
    }

    /**
     * Gets the value of the autoConfigurationEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAutoConfigurationEnabled() {
        return autoConfigurationEnabled;
    }

    /**
     * Sets the value of the autoConfigurationEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoConfigurationEnabled(Boolean value) {
        this.autoConfigurationEnabled = value;
    }

    /**
     * Gets the value of the dhcpV6Enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDhcpV6Enabled() {
        return dhcpV6Enabled;
    }

    /**
     * Sets the value of the dhcpV6Enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDhcpV6Enabled(Boolean value) {
        this.dhcpV6Enabled = value;
    }

}
