
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ClusterDasFailoverLevelAdvancedRuntimeInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClusterDasFailoverLevelAdvancedRuntimeInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}ClusterDasAdvancedRuntimeInfo">
 *       &lt;sequence>
 *         &lt;element name="slotInfo" type="{urn:vim25}ClusterDasFailoverLevelAdvancedRuntimeInfoSlotInfo"/>
 *         &lt;element name="totalSlots" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="usedSlots" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unreservedSlots" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="totalVms" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="totalHosts" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="totalGoodHosts" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hostSlots" type="{urn:vim25}ClusterDasFailoverLevelAdvancedRuntimeInfoHostSlots" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClusterDasFailoverLevelAdvancedRuntimeInfo", propOrder = {
    "slotInfo",
    "totalSlots",
    "usedSlots",
    "unreservedSlots",
    "totalVms",
    "totalHosts",
    "totalGoodHosts",
    "hostSlots"
})
public class ClusterDasFailoverLevelAdvancedRuntimeInfo
    extends ClusterDasAdvancedRuntimeInfo
{

    @XmlElement(required = true)
    protected ClusterDasFailoverLevelAdvancedRuntimeInfoSlotInfo slotInfo;
    protected int totalSlots;
    protected int usedSlots;
    protected int unreservedSlots;
    protected int totalVms;
    protected int totalHosts;
    protected int totalGoodHosts;
    protected List<ClusterDasFailoverLevelAdvancedRuntimeInfoHostSlots> hostSlots;

    /**
     * Gets the value of the slotInfo property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterDasFailoverLevelAdvancedRuntimeInfoSlotInfo }
     *     
     */
    public ClusterDasFailoverLevelAdvancedRuntimeInfoSlotInfo getSlotInfo() {
        return slotInfo;
    }

    /**
     * Sets the value of the slotInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterDasFailoverLevelAdvancedRuntimeInfoSlotInfo }
     *     
     */
    public void setSlotInfo(ClusterDasFailoverLevelAdvancedRuntimeInfoSlotInfo value) {
        this.slotInfo = value;
    }

    /**
     * Gets the value of the totalSlots property.
     * 
     */
    public int getTotalSlots() {
        return totalSlots;
    }

    /**
     * Sets the value of the totalSlots property.
     * 
     */
    public void setTotalSlots(int value) {
        this.totalSlots = value;
    }

    /**
     * Gets the value of the usedSlots property.
     * 
     */
    public int getUsedSlots() {
        return usedSlots;
    }

    /**
     * Sets the value of the usedSlots property.
     * 
     */
    public void setUsedSlots(int value) {
        this.usedSlots = value;
    }

    /**
     * Gets the value of the unreservedSlots property.
     * 
     */
    public int getUnreservedSlots() {
        return unreservedSlots;
    }

    /**
     * Sets the value of the unreservedSlots property.
     * 
     */
    public void setUnreservedSlots(int value) {
        this.unreservedSlots = value;
    }

    /**
     * Gets the value of the totalVms property.
     * 
     */
    public int getTotalVms() {
        return totalVms;
    }

    /**
     * Sets the value of the totalVms property.
     * 
     */
    public void setTotalVms(int value) {
        this.totalVms = value;
    }

    /**
     * Gets the value of the totalHosts property.
     * 
     */
    public int getTotalHosts() {
        return totalHosts;
    }

    /**
     * Sets the value of the totalHosts property.
     * 
     */
    public void setTotalHosts(int value) {
        this.totalHosts = value;
    }

    /**
     * Gets the value of the totalGoodHosts property.
     * 
     */
    public int getTotalGoodHosts() {
        return totalGoodHosts;
    }

    /**
     * Sets the value of the totalGoodHosts property.
     * 
     */
    public void setTotalGoodHosts(int value) {
        this.totalGoodHosts = value;
    }

    /**
     * Gets the value of the hostSlots property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hostSlots property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHostSlots().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClusterDasFailoverLevelAdvancedRuntimeInfoHostSlots }
     * 
     * 
     */
    public List<ClusterDasFailoverLevelAdvancedRuntimeInfoHostSlots> getHostSlots() {
        if (hostSlots == null) {
            hostSlots = new ArrayList<ClusterDasFailoverLevelAdvancedRuntimeInfoHostSlots>();
        }
        return this.hostSlots;
    }

}
