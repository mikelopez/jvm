
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PatchBinariesNotFound complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PatchBinariesNotFound">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VimFault">
 *       &lt;sequence>
 *         &lt;element name="patchID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="binary" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatchBinariesNotFound", propOrder = {
    "patchID",
    "binary"
})
public class PatchBinariesNotFound
    extends VimFault
{

    @XmlElement(required = true)
    protected String patchID;
    protected List<String> binary;

    /**
     * Gets the value of the patchID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatchID() {
        return patchID;
    }

    /**
     * Sets the value of the patchID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatchID(String value) {
        this.patchID = value;
    }

    /**
     * Gets the value of the binary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the binary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBinary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBinary() {
        if (binary == null) {
            binary = new ArrayList<String>();
        }
        return this.binary;
    }

}
