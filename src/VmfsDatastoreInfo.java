
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VmfsDatastoreInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VmfsDatastoreInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DatastoreInfo">
 *       &lt;sequence>
 *         &lt;element name="vmfs" type="{urn:vim25}HostVmfsVolume" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VmfsDatastoreInfo", propOrder = {
    "vmfs"
})
public class VmfsDatastoreInfo
    extends DatastoreInfo
{

    protected HostVmfsVolume vmfs;

    /**
     * Gets the value of the vmfs property.
     * 
     * @return
     *     possible object is
     *     {@link HostVmfsVolume }
     *     
     */
    public HostVmfsVolume getVmfs() {
        return vmfs;
    }

    /**
     * Sets the value of the vmfs property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostVmfsVolume }
     *     
     */
    public void setVmfs(HostVmfsVolume value) {
        this.vmfs = value;
    }

}
