
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualController complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualController">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VirtualDevice">
 *       &lt;sequence>
 *         &lt;element name="busNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="device" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualController", propOrder = {
    "busNumber",
    "device"
})
@XmlSeeAlso({
    VirtualPS2Controller.class,
    VirtualPCIController.class,
    VirtualUSBXHCIController.class,
    VirtualUSBController.class,
    VirtualIDEController.class,
    VirtualSCSIController.class,
    VirtualSIOController.class
})
public class VirtualController
    extends VirtualDevice
{

    protected int busNumber;
    @XmlElement(type = Integer.class)
    protected List<Integer> device;

    /**
     * Gets the value of the busNumber property.
     * 
     */
    public int getBusNumber() {
        return busNumber;
    }

    /**
     * Sets the value of the busNumber property.
     * 
     */
    public void setBusNumber(int value) {
        this.busNumber = value;
    }

    /**
     * Gets the value of the device property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the device property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDevice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getDevice() {
        if (device == null) {
            device = new ArrayList<Integer>();
        }
        return this.device;
    }

}
