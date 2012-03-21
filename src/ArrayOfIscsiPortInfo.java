
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfIscsiPortInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfIscsiPortInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IscsiPortInfo" type="{urn:vim25}IscsiPortInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfIscsiPortInfo", propOrder = {
    "iscsiPortInfo"
})
public class ArrayOfIscsiPortInfo {

    @XmlElement(name = "IscsiPortInfo")
    protected List<IscsiPortInfo> iscsiPortInfo;

    /**
     * Gets the value of the iscsiPortInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the iscsiPortInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIscsiPortInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IscsiPortInfo }
     * 
     * 
     */
    public List<IscsiPortInfo> getIscsiPortInfo() {
        if (iscsiPortInfo == null) {
            iscsiPortInfo = new ArrayList<IscsiPortInfo>();
        }
        return this.iscsiPortInfo;
    }

}
