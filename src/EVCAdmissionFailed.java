
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EVCAdmissionFailed complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EVCAdmissionFailed">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}NotSupportedHostInCluster">
 *       &lt;sequence>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EVCAdmissionFailed")
@XmlSeeAlso({
    EVCAdmissionFailedHostSoftwareForMode.class,
    EVCAdmissionFailedCPUModelForMode.class,
    EVCAdmissionFailedCPUFeaturesForMode.class,
    EVCAdmissionFailedCPUModel.class,
    EVCAdmissionFailedCPUVendorUnknown.class,
    EVCAdmissionFailedHostDisconnected.class,
    EVCAdmissionFailedCPUVendor.class,
    EVCAdmissionFailedVmActive.class,
    EVCAdmissionFailedHostSoftware.class
})
public class EVCAdmissionFailed
    extends NotSupportedHostInCluster
{


}
