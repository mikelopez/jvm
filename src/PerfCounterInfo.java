
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PerfCounterInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PerfCounterInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="nameInfo" type="{urn:vim25}ElementDescription"/>
 *         &lt;element name="groupInfo" type="{urn:vim25}ElementDescription"/>
 *         &lt;element name="unitInfo" type="{urn:vim25}ElementDescription"/>
 *         &lt;element name="rollupType" type="{urn:vim25}PerfSummaryType"/>
 *         &lt;element name="statsType" type="{urn:vim25}PerfStatsType"/>
 *         &lt;element name="level" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="perDeviceLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="associatedCounterId" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerfCounterInfo", propOrder = {
    "key",
    "nameInfo",
    "groupInfo",
    "unitInfo",
    "rollupType",
    "statsType",
    "level",
    "perDeviceLevel",
    "associatedCounterId"
})
public class PerfCounterInfo
    extends DynamicData
{

    protected int key;
    @XmlElement(required = true)
    protected ElementDescription nameInfo;
    @XmlElement(required = true)
    protected ElementDescription groupInfo;
    @XmlElement(required = true)
    protected ElementDescription unitInfo;
    @XmlElement(required = true)
    protected PerfSummaryType rollupType;
    @XmlElement(required = true)
    protected PerfStatsType statsType;
    protected Integer level;
    protected Integer perDeviceLevel;
    @XmlElement(type = Integer.class)
    protected List<Integer> associatedCounterId;

    /**
     * Gets the value of the key property.
     * 
     */
    public int getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     */
    public void setKey(int value) {
        this.key = value;
    }

    /**
     * Gets the value of the nameInfo property.
     * 
     * @return
     *     possible object is
     *     {@link ElementDescription }
     *     
     */
    public ElementDescription getNameInfo() {
        return nameInfo;
    }

    /**
     * Sets the value of the nameInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementDescription }
     *     
     */
    public void setNameInfo(ElementDescription value) {
        this.nameInfo = value;
    }

    /**
     * Gets the value of the groupInfo property.
     * 
     * @return
     *     possible object is
     *     {@link ElementDescription }
     *     
     */
    public ElementDescription getGroupInfo() {
        return groupInfo;
    }

    /**
     * Sets the value of the groupInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementDescription }
     *     
     */
    public void setGroupInfo(ElementDescription value) {
        this.groupInfo = value;
    }

    /**
     * Gets the value of the unitInfo property.
     * 
     * @return
     *     possible object is
     *     {@link ElementDescription }
     *     
     */
    public ElementDescription getUnitInfo() {
        return unitInfo;
    }

    /**
     * Sets the value of the unitInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementDescription }
     *     
     */
    public void setUnitInfo(ElementDescription value) {
        this.unitInfo = value;
    }

    /**
     * Gets the value of the rollupType property.
     * 
     * @return
     *     possible object is
     *     {@link PerfSummaryType }
     *     
     */
    public PerfSummaryType getRollupType() {
        return rollupType;
    }

    /**
     * Sets the value of the rollupType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerfSummaryType }
     *     
     */
    public void setRollupType(PerfSummaryType value) {
        this.rollupType = value;
    }

    /**
     * Gets the value of the statsType property.
     * 
     * @return
     *     possible object is
     *     {@link PerfStatsType }
     *     
     */
    public PerfStatsType getStatsType() {
        return statsType;
    }

    /**
     * Sets the value of the statsType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerfStatsType }
     *     
     */
    public void setStatsType(PerfStatsType value) {
        this.statsType = value;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLevel(Integer value) {
        this.level = value;
    }

    /**
     * Gets the value of the perDeviceLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPerDeviceLevel() {
        return perDeviceLevel;
    }

    /**
     * Sets the value of the perDeviceLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPerDeviceLevel(Integer value) {
        this.perDeviceLevel = value;
    }

    /**
     * Gets the value of the associatedCounterId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the associatedCounterId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssociatedCounterId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getAssociatedCounterId() {
        if (associatedCounterId == null) {
            associatedCounterId = new ArrayList<Integer>();
        }
        return this.associatedCounterId;
    }

}
