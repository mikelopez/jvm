
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualAppSummary complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualAppSummary">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}ResourcePoolSummary">
 *       &lt;sequence>
 *         &lt;element name="product" type="{urn:vim25}VAppProductInfo" minOccurs="0"/>
 *         &lt;element name="vAppState" type="{urn:vim25}VirtualAppVAppState" minOccurs="0"/>
 *         &lt;element name="suspended" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="installBootRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="instanceUuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualAppSummary", propOrder = {
    "product",
    "vAppState",
    "suspended",
    "installBootRequired",
    "instanceUuid"
})
public class VirtualAppSummary
    extends ResourcePoolSummary
{

    protected VAppProductInfo product;
    protected VirtualAppVAppState vAppState;
    protected Boolean suspended;
    protected Boolean installBootRequired;
    protected String instanceUuid;

    /**
     * Gets the value of the product property.
     * 
     * @return
     *     possible object is
     *     {@link VAppProductInfo }
     *     
     */
    public VAppProductInfo getProduct() {
        return product;
    }

    /**
     * Sets the value of the product property.
     * 
     * @param value
     *     allowed object is
     *     {@link VAppProductInfo }
     *     
     */
    public void setProduct(VAppProductInfo value) {
        this.product = value;
    }

    /**
     * Gets the value of the vAppState property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualAppVAppState }
     *     
     */
    public VirtualAppVAppState getVAppState() {
        return vAppState;
    }

    /**
     * Sets the value of the vAppState property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualAppVAppState }
     *     
     */
    public void setVAppState(VirtualAppVAppState value) {
        this.vAppState = value;
    }

    /**
     * Gets the value of the suspended property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSuspended() {
        return suspended;
    }

    /**
     * Sets the value of the suspended property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSuspended(Boolean value) {
        this.suspended = value;
    }

    /**
     * Gets the value of the installBootRequired property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInstallBootRequired() {
        return installBootRequired;
    }

    /**
     * Sets the value of the installBootRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInstallBootRequired(Boolean value) {
        this.installBootRequired = value;
    }

    /**
     * Gets the value of the instanceUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceUuid() {
        return instanceUuid;
    }

    /**
     * Sets the value of the instanceUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceUuid(String value) {
        this.instanceUuid = value;
    }

}
