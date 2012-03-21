
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfServiceConsolePortGroupProfile complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfServiceConsolePortGroupProfile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ServiceConsolePortGroupProfile" type="{urn:vim25}ServiceConsolePortGroupProfile" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfServiceConsolePortGroupProfile", propOrder = {
    "serviceConsolePortGroupProfile"
})
public class ArrayOfServiceConsolePortGroupProfile {

    @XmlElement(name = "ServiceConsolePortGroupProfile")
    protected List<ServiceConsolePortGroupProfile> serviceConsolePortGroupProfile;

    /**
     * Gets the value of the serviceConsolePortGroupProfile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceConsolePortGroupProfile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceConsolePortGroupProfile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceConsolePortGroupProfile }
     * 
     * 
     */
    public List<ServiceConsolePortGroupProfile> getServiceConsolePortGroupProfile() {
        if (serviceConsolePortGroupProfile == null) {
            serviceConsolePortGroupProfile = new ArrayList<ServiceConsolePortGroupProfile>();
        }
        return this.serviceConsolePortGroupProfile;
    }

}
