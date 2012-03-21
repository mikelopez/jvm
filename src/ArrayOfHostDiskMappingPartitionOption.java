
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfHostDiskMappingPartitionOption complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfHostDiskMappingPartitionOption">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HostDiskMappingPartitionOption" type="{urn:vim25}HostDiskMappingPartitionOption" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfHostDiskMappingPartitionOption", propOrder = {
    "hostDiskMappingPartitionOption"
})
public class ArrayOfHostDiskMappingPartitionOption {

    @XmlElement(name = "HostDiskMappingPartitionOption")
    protected List<HostDiskMappingPartitionOption> hostDiskMappingPartitionOption;

    /**
     * Gets the value of the hostDiskMappingPartitionOption property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hostDiskMappingPartitionOption property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHostDiskMappingPartitionOption().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostDiskMappingPartitionOption }
     * 
     * 
     */
    public List<HostDiskMappingPartitionOption> getHostDiskMappingPartitionOption() {
        if (hostDiskMappingPartitionOption == null) {
            hostDiskMappingPartitionOption = new ArrayList<HostDiskMappingPartitionOption>();
        }
        return this.hostDiskMappingPartitionOption;
    }

}
