
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfClusterDasVmConfigInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfClusterDasVmConfigInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ClusterDasVmConfigInfo" type="{urn:vim25}ClusterDasVmConfigInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfClusterDasVmConfigInfo", propOrder = {
    "clusterDasVmConfigInfo"
})
public class ArrayOfClusterDasVmConfigInfo {

    @XmlElement(name = "ClusterDasVmConfigInfo")
    protected List<ClusterDasVmConfigInfo> clusterDasVmConfigInfo;

    /**
     * Gets the value of the clusterDasVmConfigInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clusterDasVmConfigInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClusterDasVmConfigInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClusterDasVmConfigInfo }
     * 
     * 
     */
    public List<ClusterDasVmConfigInfo> getClusterDasVmConfigInfo() {
        if (clusterDasVmConfigInfo == null) {
            clusterDasVmConfigInfo = new ArrayList<ClusterDasVmConfigInfo>();
        }
        return this.clusterDasVmConfigInfo;
    }

}
