
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfIscsiDependencyEntity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfIscsiDependencyEntity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IscsiDependencyEntity" type="{urn:vim25}IscsiDependencyEntity" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfIscsiDependencyEntity", propOrder = {
    "iscsiDependencyEntity"
})
public class ArrayOfIscsiDependencyEntity {

    @XmlElement(name = "IscsiDependencyEntity")
    protected List<IscsiDependencyEntity> iscsiDependencyEntity;

    /**
     * Gets the value of the iscsiDependencyEntity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the iscsiDependencyEntity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIscsiDependencyEntity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IscsiDependencyEntity }
     * 
     * 
     */
    public List<IscsiDependencyEntity> getIscsiDependencyEntity() {
        if (iscsiDependencyEntity == null) {
            iscsiDependencyEntity = new ArrayList<IscsiDependencyEntity>();
        }
        return this.iscsiDependencyEntity;
    }

}
