
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VmToolsUpgradeFault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VmToolsUpgradeFault">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VimFault">
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
@XmlType(name = "VmToolsUpgradeFault")
@XmlSeeAlso({
    ToolsAlreadyUpgraded.class,
    ToolsUpgradeCancelled.class,
    ToolsImageNotAvailable.class,
    ToolsAutoUpgradeNotSupported.class,
    ToolsImageSignatureCheckFailed.class
})
public class VmToolsUpgradeFault
    extends VimFault
{


}
