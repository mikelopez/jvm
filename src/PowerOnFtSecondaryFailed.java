
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PowerOnFtSecondaryFailed complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PowerOnFtSecondaryFailed">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VmFaultToleranceIssue">
 *       &lt;sequence>
 *         &lt;element name="vm" type="{urn:vim25}ManagedObjectReference"/>
 *         &lt;element name="vmName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hostSelectionBy" type="{urn:vim25}FtIssuesOnHostHostSelectionType"/>
 *         &lt;element name="hostErrors" type="{urn:vim25}LocalizedMethodFault" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="rootCause" type="{urn:vim25}LocalizedMethodFault"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PowerOnFtSecondaryFailed", propOrder = {
    "vm",
    "vmName",
    "hostSelectionBy",
    "hostErrors",
    "rootCause"
})
public class PowerOnFtSecondaryFailed
    extends VmFaultToleranceIssue
{

    @XmlElement(required = true)
    protected ManagedObjectReference vm;
    @XmlElement(required = true)
    protected String vmName;
    @XmlElement(required = true)
    protected FtIssuesOnHostHostSelectionType hostSelectionBy;
    protected List<LocalizedMethodFault> hostErrors;
    @XmlElement(required = true)
    protected LocalizedMethodFault rootCause;

    /**
     * Gets the value of the vm property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getVm() {
        return vm;
    }

    /**
     * Sets the value of the vm property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setVm(ManagedObjectReference value) {
        this.vm = value;
    }

    /**
     * Gets the value of the vmName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmName() {
        return vmName;
    }

    /**
     * Sets the value of the vmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmName(String value) {
        this.vmName = value;
    }

    /**
     * Gets the value of the hostSelectionBy property.
     * 
     * @return
     *     possible object is
     *     {@link FtIssuesOnHostHostSelectionType }
     *     
     */
    public FtIssuesOnHostHostSelectionType getHostSelectionBy() {
        return hostSelectionBy;
    }

    /**
     * Sets the value of the hostSelectionBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link FtIssuesOnHostHostSelectionType }
     *     
     */
    public void setHostSelectionBy(FtIssuesOnHostHostSelectionType value) {
        this.hostSelectionBy = value;
    }

    /**
     * Gets the value of the hostErrors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hostErrors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHostErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LocalizedMethodFault }
     * 
     * 
     */
    public List<LocalizedMethodFault> getHostErrors() {
        if (hostErrors == null) {
            hostErrors = new ArrayList<LocalizedMethodFault>();
        }
        return this.hostErrors;
    }

    /**
     * Gets the value of the rootCause property.
     * 
     * @return
     *     possible object is
     *     {@link LocalizedMethodFault }
     *     
     */
    public LocalizedMethodFault getRootCause() {
        return rootCause;
    }

    /**
     * Sets the value of the rootCause property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalizedMethodFault }
     *     
     */
    public void setRootCause(LocalizedMethodFault value) {
        this.rootCause = value;
    }

}
