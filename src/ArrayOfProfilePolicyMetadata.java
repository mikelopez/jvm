
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfProfilePolicyMetadata complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfProfilePolicyMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProfilePolicyMetadata" type="{urn:vim25}ProfilePolicyMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfProfilePolicyMetadata", propOrder = {
    "profilePolicyMetadata"
})
public class ArrayOfProfilePolicyMetadata {

    @XmlElement(name = "ProfilePolicyMetadata")
    protected List<ProfilePolicyMetadata> profilePolicyMetadata;

    /**
     * Gets the value of the profilePolicyMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the profilePolicyMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProfilePolicyMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProfilePolicyMetadata }
     * 
     * 
     */
    public List<ProfilePolicyMetadata> getProfilePolicyMetadata() {
        if (profilePolicyMetadata == null) {
            profilePolicyMetadata = new ArrayList<ProfilePolicyMetadata>();
        }
        return this.profilePolicyMetadata;
    }

}
