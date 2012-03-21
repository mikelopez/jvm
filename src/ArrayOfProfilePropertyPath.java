
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfProfilePropertyPath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfProfilePropertyPath">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProfilePropertyPath" type="{urn:vim25}ProfilePropertyPath" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfProfilePropertyPath", propOrder = {
    "profilePropertyPath"
})
public class ArrayOfProfilePropertyPath {

    @XmlElement(name = "ProfilePropertyPath")
    protected List<ProfilePropertyPath> profilePropertyPath;

    /**
     * Gets the value of the profilePropertyPath property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the profilePropertyPath property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProfilePropertyPath().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProfilePropertyPath }
     * 
     * 
     */
    public List<ProfilePropertyPath> getProfilePropertyPath() {
        if (profilePropertyPath == null) {
            profilePropertyPath = new ArrayList<ProfilePropertyPath>();
        }
        return this.profilePropertyPath;
    }

}
