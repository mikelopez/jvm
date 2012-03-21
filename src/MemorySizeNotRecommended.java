
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MemorySizeNotRecommended complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MemorySizeNotRecommended">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}VirtualHardwareCompatibilityIssue">
 *       &lt;sequence>
 *         &lt;element name="memorySizeMB" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="minMemorySizeMB" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maxMemorySizeMB" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MemorySizeNotRecommended", propOrder = {
    "memorySizeMB",
    "minMemorySizeMB",
    "maxMemorySizeMB"
})
public class MemorySizeNotRecommended
    extends VirtualHardwareCompatibilityIssue
{

    protected int memorySizeMB;
    protected int minMemorySizeMB;
    protected int maxMemorySizeMB;

    /**
     * Gets the value of the memorySizeMB property.
     * 
     */
    public int getMemorySizeMB() {
        return memorySizeMB;
    }

    /**
     * Sets the value of the memorySizeMB property.
     * 
     */
    public void setMemorySizeMB(int value) {
        this.memorySizeMB = value;
    }

    /**
     * Gets the value of the minMemorySizeMB property.
     * 
     */
    public int getMinMemorySizeMB() {
        return minMemorySizeMB;
    }

    /**
     * Sets the value of the minMemorySizeMB property.
     * 
     */
    public void setMinMemorySizeMB(int value) {
        this.minMemorySizeMB = value;
    }

    /**
     * Gets the value of the maxMemorySizeMB property.
     * 
     */
    public int getMaxMemorySizeMB() {
        return maxMemorySizeMB;
    }

    /**
     * Sets the value of the maxMemorySizeMB property.
     * 
     */
    public void setMaxMemorySizeMB(int value) {
        this.maxMemorySizeMB = value;
    }

}
