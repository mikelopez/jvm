
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfDvsOperationBulkFaultFaultOnHost complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfDvsOperationBulkFaultFaultOnHost">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DvsOperationBulkFaultFaultOnHost" type="{urn:vim25}DvsOperationBulkFaultFaultOnHost" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfDvsOperationBulkFaultFaultOnHost", propOrder = {
    "dvsOperationBulkFaultFaultOnHost"
})
public class ArrayOfDvsOperationBulkFaultFaultOnHost {

    @XmlElement(name = "DvsOperationBulkFaultFaultOnHost")
    protected List<DvsOperationBulkFaultFaultOnHost> dvsOperationBulkFaultFaultOnHost;

    /**
     * Gets the value of the dvsOperationBulkFaultFaultOnHost property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dvsOperationBulkFaultFaultOnHost property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDvsOperationBulkFaultFaultOnHost().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DvsOperationBulkFaultFaultOnHost }
     * 
     * 
     */
    public List<DvsOperationBulkFaultFaultOnHost> getDvsOperationBulkFaultFaultOnHost() {
        if (dvsOperationBulkFaultFaultOnHost == null) {
            dvsOperationBulkFaultFaultOnHost = new ArrayList<DvsOperationBulkFaultFaultOnHost>();
        }
        return this.dvsOperationBulkFaultFaultOnHost;
    }

}
