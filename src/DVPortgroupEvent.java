
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DVPortgroupEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DVPortgroupEvent">
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
@XmlType(name = "DVPortgroupEvent")
@XmlSeeAlso({
    DVPortgroupDestroyedEvent.class,
    DVPortgroupCreatedEvent.class,
    DVPortgroupReconfiguredEvent.class,
    DVPortgroupRenamedEvent.class
})
public class DVPortgroupEvent
    extends Event
{


}
