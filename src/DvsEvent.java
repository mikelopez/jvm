
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DvsEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DvsEvent">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}Event">
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
@XmlType(name = "DvsEvent")
@XmlSeeAlso({
    DvsPortDeletedEvent.class,
    DvsUpgradeAvailableEvent.class,
    DvsUpgradedEvent.class,
    DvsPortLinkDownEvent.class,
    DvsCreatedEvent.class,
    DvsRenamedEvent.class,
    DvsHostLeftEvent.class,
    DvsPortCreatedEvent.class,
    DvsHostWentOutOfSyncEvent.class,
    DvsPortJoinPortgroupEvent.class,
    DvsPortLinkUpEvent.class,
    DvsUpgradeRejectedEvent.class,
    DvsPortConnectedEvent.class,
    OutOfSyncDvsHost.class,
    DvsPortUnblockedEvent.class,
    DvsHostBackInSyncEvent.class,
    DvsPortExitedPassthruEvent.class,
    DvsPortBlockedEvent.class,
    DvsUpgradeInProgressEvent.class,
    DvsHostStatusUpdated.class,
    DvsPortLeavePortgroupEvent.class,
    DvsReconfiguredEvent.class,
    DvsPortDisconnectedEvent.class,
    DvsDestroyedEvent.class,
    DvsHostJoinedEvent.class,
    DvsPortEnteredPassthruEvent.class,
    DvsPortReconfiguredEvent.class,
    DvsMergedEvent.class
})
public class DvsEvent
    extends Event
{


}
