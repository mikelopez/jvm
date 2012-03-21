
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostInternetScsiHbaDigestProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostInternetScsiHbaDigestProperties">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="headerDigestType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="headerDigestInherited" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="dataDigestType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataDigestInherited" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostInternetScsiHbaDigestProperties", propOrder = {
    "headerDigestType",
    "headerDigestInherited",
    "dataDigestType",
    "dataDigestInherited"
})
public class HostInternetScsiHbaDigestProperties
    extends DynamicData
{

    protected String headerDigestType;
    protected Boolean headerDigestInherited;
    protected String dataDigestType;
    protected Boolean dataDigestInherited;

    /**
     * Gets the value of the headerDigestType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeaderDigestType() {
        return headerDigestType;
    }

    /**
     * Sets the value of the headerDigestType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeaderDigestType(String value) {
        this.headerDigestType = value;
    }

    /**
     * Gets the value of the headerDigestInherited property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHeaderDigestInherited() {
        return headerDigestInherited;
    }

    /**
     * Sets the value of the headerDigestInherited property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHeaderDigestInherited(Boolean value) {
        this.headerDigestInherited = value;
    }

    /**
     * Gets the value of the dataDigestType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataDigestType() {
        return dataDigestType;
    }

    /**
     * Sets the value of the dataDigestType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataDigestType(String value) {
        this.dataDigestType = value;
    }

    /**
     * Gets the value of the dataDigestInherited property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDataDigestInherited() {
        return dataDigestInherited;
    }

    /**
     * Sets the value of the dataDigestInherited property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDataDigestInherited(Boolean value) {
        this.dataDigestInherited = value;
    }

}
