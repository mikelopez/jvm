
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostScsiTopologyTarget complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostScsiTopologyTarget">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="target" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="lun" type="{urn:vim25}HostScsiTopologyLun" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="transport" type="{urn:vim25}HostTargetTransport" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostScsiTopologyTarget", propOrder = {
    "key",
    "target",
    "lun",
    "transport"
})
public class HostScsiTopologyTarget
    extends DynamicData
{

    @XmlElement(required = true)
    protected String key;
    protected int target;
    protected List<HostScsiTopologyLun> lun;
    protected HostTargetTransport transport;

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the target property.
     * 
     */
    public int getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     */
    public void setTarget(int value) {
        this.target = value;
    }

    /**
     * Gets the value of the lun property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lun property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLun().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostScsiTopologyLun }
     * 
     * 
     */
    public List<HostScsiTopologyLun> getLun() {
        if (lun == null) {
            lun = new ArrayList<HostScsiTopologyLun>();
        }
        return this.lun;
    }

    /**
     * Gets the value of the transport property.
     * 
     * @return
     *     possible object is
     *     {@link HostTargetTransport }
     *     
     */
    public HostTargetTransport getTransport() {
        return transport;
    }

    /**
     * Sets the value of the transport property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostTargetTransport }
     *     
     */
    public void setTransport(HostTargetTransport value) {
        this.transport = value;
    }

}
