
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualMachineMemoryReservationSpec complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualMachineMemoryReservationSpec">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="virtualMachineReserved" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="allocationPolicy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualMachineMemoryReservationSpec", propOrder = {
    "virtualMachineReserved",
    "allocationPolicy"
})
public class VirtualMachineMemoryReservationSpec
    extends DynamicData
{

    protected Long virtualMachineReserved;
    protected String allocationPolicy;

    /**
     * Gets the value of the virtualMachineReserved property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getVirtualMachineReserved() {
        return virtualMachineReserved;
    }

    /**
     * Sets the value of the virtualMachineReserved property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVirtualMachineReserved(Long value) {
        this.virtualMachineReserved = value;
    }

    /**
     * Gets the value of the allocationPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllocationPolicy() {
        return allocationPolicy;
    }

    /**
     * Sets the value of the allocationPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllocationPolicy(String value) {
        this.allocationPolicy = value;
    }

}
