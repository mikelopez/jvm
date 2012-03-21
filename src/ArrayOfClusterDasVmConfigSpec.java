
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfClusterDasVmConfigSpec complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfClusterDasVmConfigSpec">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ClusterDasVmConfigSpec" type="{urn:vim25}ClusterDasVmConfigSpec" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfClusterDasVmConfigSpec", propOrder = {
    "clusterDasVmConfigSpec"
})
public class ArrayOfClusterDasVmConfigSpec {

    @XmlElement(name = "ClusterDasVmConfigSpec")
    protected List<ClusterDasVmConfigSpec> clusterDasVmConfigSpec;

    /**
     * Gets the value of the clusterDasVmConfigSpec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clusterDasVmConfigSpec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClusterDasVmConfigSpec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClusterDasVmConfigSpec }
     * 
     * 
     */
    public List<ClusterDasVmConfigSpec> getClusterDasVmConfigSpec() {
        if (clusterDasVmConfigSpec == null) {
            clusterDasVmConfigSpec = new ArrayList<ClusterDasVmConfigSpec>();
        }
        return this.clusterDasVmConfigSpec;
    }

}
