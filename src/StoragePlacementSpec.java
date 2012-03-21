
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StoragePlacementSpec complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StoragePlacementSpec">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="priority" type="{urn:vim25}VirtualMachineMovePriority" minOccurs="0"/>
 *         &lt;element name="vm" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *         &lt;element name="podSelectionSpec" type="{urn:vim25}StorageDrsPodSelectionSpec"/>
 *         &lt;element name="cloneSpec" type="{urn:vim25}VirtualMachineCloneSpec" minOccurs="0"/>
 *         &lt;element name="cloneName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="configSpec" type="{urn:vim25}VirtualMachineConfigSpec" minOccurs="0"/>
 *         &lt;element name="relocateSpec" type="{urn:vim25}VirtualMachineRelocateSpec" minOccurs="0"/>
 *         &lt;element name="resourcePool" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *         &lt;element name="host" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *         &lt;element name="folder" type="{urn:vim25}ManagedObjectReference" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StoragePlacementSpec", propOrder = {
    "type",
    "priority",
    "vm",
    "podSelectionSpec",
    "cloneSpec",
    "cloneName",
    "configSpec",
    "relocateSpec",
    "resourcePool",
    "host",
    "folder"
})
public class StoragePlacementSpec
    extends DynamicData
{

    @XmlElement(required = true)
    protected String type;
    protected VirtualMachineMovePriority priority;
    protected ManagedObjectReference vm;
    @XmlElement(required = true)
    protected StorageDrsPodSelectionSpec podSelectionSpec;
    protected VirtualMachineCloneSpec cloneSpec;
    protected String cloneName;
    protected VirtualMachineConfigSpec configSpec;
    protected VirtualMachineRelocateSpec relocateSpec;
    protected ManagedObjectReference resourcePool;
    protected ManagedObjectReference host;
    protected ManagedObjectReference folder;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualMachineMovePriority }
     *     
     */
    public VirtualMachineMovePriority getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualMachineMovePriority }
     *     
     */
    public void setPriority(VirtualMachineMovePriority value) {
        this.priority = value;
    }

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
     * Gets the value of the podSelectionSpec property.
     * 
     * @return
     *     possible object is
     *     {@link StorageDrsPodSelectionSpec }
     *     
     */
    public StorageDrsPodSelectionSpec getPodSelectionSpec() {
        return podSelectionSpec;
    }

    /**
     * Sets the value of the podSelectionSpec property.
     * 
     * @param value
     *     allowed object is
     *     {@link StorageDrsPodSelectionSpec }
     *     
     */
    public void setPodSelectionSpec(StorageDrsPodSelectionSpec value) {
        this.podSelectionSpec = value;
    }

    /**
     * Gets the value of the cloneSpec property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualMachineCloneSpec }
     *     
     */
    public VirtualMachineCloneSpec getCloneSpec() {
        return cloneSpec;
    }

    /**
     * Sets the value of the cloneSpec property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualMachineCloneSpec }
     *     
     */
    public void setCloneSpec(VirtualMachineCloneSpec value) {
        this.cloneSpec = value;
    }

    /**
     * Gets the value of the cloneName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCloneName() {
        return cloneName;
    }

    /**
     * Sets the value of the cloneName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCloneName(String value) {
        this.cloneName = value;
    }

    /**
     * Gets the value of the configSpec property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualMachineConfigSpec }
     *     
     */
    public VirtualMachineConfigSpec getConfigSpec() {
        return configSpec;
    }

    /**
     * Sets the value of the configSpec property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualMachineConfigSpec }
     *     
     */
    public void setConfigSpec(VirtualMachineConfigSpec value) {
        this.configSpec = value;
    }

    /**
     * Gets the value of the relocateSpec property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualMachineRelocateSpec }
     *     
     */
    public VirtualMachineRelocateSpec getRelocateSpec() {
        return relocateSpec;
    }

    /**
     * Sets the value of the relocateSpec property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualMachineRelocateSpec }
     *     
     */
    public void setRelocateSpec(VirtualMachineRelocateSpec value) {
        this.relocateSpec = value;
    }

    /**
     * Gets the value of the resourcePool property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getResourcePool() {
        return resourcePool;
    }

    /**
     * Sets the value of the resourcePool property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setResourcePool(ManagedObjectReference value) {
        this.resourcePool = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setHost(ManagedObjectReference value) {
        this.host = value;
    }

    /**
     * Gets the value of the folder property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getFolder() {
        return folder;
    }

    /**
     * Sets the value of the folder property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setFolder(ManagedObjectReference value) {
        this.folder = value;
    }

}
