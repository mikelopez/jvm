
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IpPool complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IpPool">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ipv4Config" type="{urn:vim25}IpPoolIpPoolConfigInfo" minOccurs="0"/>
 *         &lt;element name="ipv6Config" type="{urn:vim25}IpPoolIpPoolConfigInfo" minOccurs="0"/>
 *         &lt;element name="dnsDomain" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dnsSearchPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hostPrefix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="httpProxy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="networkAssociation" type="{urn:vim25}IpPoolAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IpPool", propOrder = {
    "id",
    "name",
    "ipv4Config",
    "ipv6Config",
    "dnsDomain",
    "dnsSearchPath",
    "hostPrefix",
    "httpProxy",
    "networkAssociation"
})
public class IpPool
    extends DynamicData
{

    protected Integer id;
    protected String name;
    protected IpPoolIpPoolConfigInfo ipv4Config;
    protected IpPoolIpPoolConfigInfo ipv6Config;
    protected String dnsDomain;
    protected String dnsSearchPath;
    protected String hostPrefix;
    protected String httpProxy;
    protected List<IpPoolAssociation> networkAssociation;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(Integer value) {
        this.id = value;
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
     * Gets the value of the ipv4Config property.
     * 
     * @return
     *     possible object is
     *     {@link IpPoolIpPoolConfigInfo }
     *     
     */
    public IpPoolIpPoolConfigInfo getIpv4Config() {
        return ipv4Config;
    }

    /**
     * Sets the value of the ipv4Config property.
     * 
     * @param value
     *     allowed object is
     *     {@link IpPoolIpPoolConfigInfo }
     *     
     */
    public void setIpv4Config(IpPoolIpPoolConfigInfo value) {
        this.ipv4Config = value;
    }

    /**
     * Gets the value of the ipv6Config property.
     * 
     * @return
     *     possible object is
     *     {@link IpPoolIpPoolConfigInfo }
     *     
     */
    public IpPoolIpPoolConfigInfo getIpv6Config() {
        return ipv6Config;
    }

    /**
     * Sets the value of the ipv6Config property.
     * 
     * @param value
     *     allowed object is
     *     {@link IpPoolIpPoolConfigInfo }
     *     
     */
    public void setIpv6Config(IpPoolIpPoolConfigInfo value) {
        this.ipv6Config = value;
    }

    /**
     * Gets the value of the dnsDomain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDnsDomain() {
        return dnsDomain;
    }

    /**
     * Sets the value of the dnsDomain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDnsDomain(String value) {
        this.dnsDomain = value;
    }

    /**
     * Gets the value of the dnsSearchPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDnsSearchPath() {
        return dnsSearchPath;
    }

    /**
     * Sets the value of the dnsSearchPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDnsSearchPath(String value) {
        this.dnsSearchPath = value;
    }

    /**
     * Gets the value of the hostPrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHostPrefix() {
        return hostPrefix;
    }

    /**
     * Sets the value of the hostPrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHostPrefix(String value) {
        this.hostPrefix = value;
    }

    /**
     * Gets the value of the httpProxy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHttpProxy() {
        return httpProxy;
    }

    /**
     * Sets the value of the httpProxy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHttpProxy(String value) {
        this.httpProxy = value;
    }

    /**
     * Gets the value of the networkAssociation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the networkAssociation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNetworkAssociation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IpPoolAssociation }
     * 
     * 
     */
    public List<IpPoolAssociation> getNetworkAssociation() {
        if (networkAssociation == null) {
            networkAssociation = new ArrayList<IpPoolAssociation>();
        }
        return this.networkAssociation;
    }

}
