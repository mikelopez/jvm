
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AlarmTriggeringAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AlarmTriggeringAction">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}AlarmAction">
 *       &lt;sequence>
 *         &lt;element name="action" type="{urn:vim25}Action"/>
 *         &lt;element name="transitionSpecs" type="{urn:vim25}AlarmTriggeringActionTransitionSpec" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="green2yellow" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="yellow2red" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="red2yellow" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="yellow2green" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlarmTriggeringAction", propOrder = {
    "action",
    "transitionSpecs",
    "green2Yellow",
    "yellow2Red",
    "red2Yellow",
    "yellow2Green"
})
public class AlarmTriggeringAction
    extends AlarmAction
{

    @XmlElement(required = true)
    protected Action action;
    protected List<AlarmTriggeringActionTransitionSpec> transitionSpecs;
    @XmlElement(name = "green2yellow")
    protected boolean green2Yellow;
    @XmlElement(name = "yellow2red")
    protected boolean yellow2Red;
    @XmlElement(name = "red2yellow")
    protected boolean red2Yellow;
    @XmlElement(name = "yellow2green")
    protected boolean yellow2Green;

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link Action }
     *     
     */
    public Action getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link Action }
     *     
     */
    public void setAction(Action value) {
        this.action = value;
    }

    /**
     * Gets the value of the transitionSpecs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transitionSpecs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransitionSpecs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AlarmTriggeringActionTransitionSpec }
     * 
     * 
     */
    public List<AlarmTriggeringActionTransitionSpec> getTransitionSpecs() {
        if (transitionSpecs == null) {
            transitionSpecs = new ArrayList<AlarmTriggeringActionTransitionSpec>();
        }
        return this.transitionSpecs;
    }

    /**
     * Gets the value of the green2Yellow property.
     * 
     */
    public boolean isGreen2Yellow() {
        return green2Yellow;
    }

    /**
     * Sets the value of the green2Yellow property.
     * 
     */
    public void setGreen2Yellow(boolean value) {
        this.green2Yellow = value;
    }

    /**
     * Gets the value of the yellow2Red property.
     * 
     */
    public boolean isYellow2Red() {
        return yellow2Red;
    }

    /**
     * Sets the value of the yellow2Red property.
     * 
     */
    public void setYellow2Red(boolean value) {
        this.yellow2Red = value;
    }

    /**
     * Gets the value of the red2Yellow property.
     * 
     */
    public boolean isRed2Yellow() {
        return red2Yellow;
    }

    /**
     * Sets the value of the red2Yellow property.
     * 
     */
    public void setRed2Yellow(boolean value) {
        this.red2Yellow = value;
    }

    /**
     * Gets the value of the yellow2Green property.
     * 
     */
    public boolean isYellow2Green() {
        return yellow2Green;
    }

    /**
     * Sets the value of the yellow2Green property.
     * 
     */
    public void setYellow2Green(boolean value) {
        this.yellow2Green = value;
    }

}
