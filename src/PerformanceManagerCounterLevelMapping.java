
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PerformanceManagerCounterLevelMapping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PerformanceManagerCounterLevelMapping">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="counterId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="aggregateLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="perDeviceLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerformanceManagerCounterLevelMapping", propOrder = {
    "counterId",
    "aggregateLevel",
    "perDeviceLevel"
})
public class PerformanceManagerCounterLevelMapping
    extends DynamicData
{

    protected int counterId;
    protected Integer aggregateLevel;
    protected Integer perDeviceLevel;

    /**
     * Gets the value of the counterId property.
     * 
     */
    public int getCounterId() {
        return counterId;
    }

    /**
     * Sets the value of the counterId property.
     * 
     */
    public void setCounterId(int value) {
        this.counterId = value;
    }

    /**
     * Gets the value of the aggregateLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAggregateLevel() {
        return aggregateLevel;
    }

    /**
     * Sets the value of the aggregateLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAggregateLevel(Integer value) {
        this.aggregateLevel = value;
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

}
