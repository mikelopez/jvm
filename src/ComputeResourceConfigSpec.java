
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComputeResourceConfigSpec complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComputeResourceConfigSpec">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="vmSwapPlacement" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="spbmEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComputeResourceConfigSpec", propOrder = {
    "vmSwapPlacement",
    "spbmEnabled"
})
@XmlSeeAlso({
    ClusterConfigSpecEx.class
})
public class ComputeResourceConfigSpec
    extends DynamicData
{

    protected String vmSwapPlacement;
    protected Boolean spbmEnabled;

    /**
     * Gets the value of the vmSwapPlacement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmSwapPlacement() {
        return vmSwapPlacement;
    }

    /**
     * Sets the value of the vmSwapPlacement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmSwapPlacement(String value) {
        this.vmSwapPlacement = value;
    }

    /**
     * Gets the value of the spbmEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSpbmEnabled() {
        return spbmEnabled;
    }

    /**
     * Sets the value of the spbmEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSpbmEnabled(Boolean value) {
        this.spbmEnabled = value;
    }

}
