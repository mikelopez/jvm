
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StorageIORMInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StorageIORMInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="congestionThreshold" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="statsCollectionEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="statsAggregationDisabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StorageIORMInfo", propOrder = {
    "enabled",
    "congestionThreshold",
    "statsCollectionEnabled",
    "statsAggregationDisabled"
})
public class StorageIORMInfo
    extends DynamicData
{

    protected boolean enabled;
    protected int congestionThreshold;
    protected Boolean statsCollectionEnabled;
    protected Boolean statsAggregationDisabled;

    /**
     * Gets the value of the enabled property.
     * 
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the congestionThreshold property.
     * 
     */
    public int getCongestionThreshold() {
        return congestionThreshold;
    }

    /**
     * Sets the value of the congestionThreshold property.
     * 
     */
    public void setCongestionThreshold(int value) {
        this.congestionThreshold = value;
    }

    /**
     * Gets the value of the statsCollectionEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStatsCollectionEnabled() {
        return statsCollectionEnabled;
    }

    /**
     * Sets the value of the statsCollectionEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStatsCollectionEnabled(Boolean value) {
        this.statsCollectionEnabled = value;
    }

    /**
     * Gets the value of the statsAggregationDisabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStatsAggregationDisabled() {
        return statsAggregationDisabled;
    }

    /**
     * Sets the value of the statsAggregationDisabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStatsAggregationDisabled(Boolean value) {
        this.statsAggregationDisabled = value;
    }

}
