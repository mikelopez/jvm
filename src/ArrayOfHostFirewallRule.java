
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfHostFirewallRule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfHostFirewallRule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HostFirewallRule" type="{urn:vim25}HostFirewallRule" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfHostFirewallRule", propOrder = {
    "hostFirewallRule"
})
public class ArrayOfHostFirewallRule {

    @XmlElement(name = "HostFirewallRule")
    protected List<HostFirewallRule> hostFirewallRule;

    /**
     * Gets the value of the hostFirewallRule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hostFirewallRule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHostFirewallRule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostFirewallRule }
     * 
     * 
     */
    public List<HostFirewallRule> getHostFirewallRule() {
        if (hostFirewallRule == null) {
            hostFirewallRule = new ArrayList<HostFirewallRule>();
        }
        return this.hostFirewallRule;
    }

}
