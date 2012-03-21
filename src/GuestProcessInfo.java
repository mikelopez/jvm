
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for GuestProcessInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GuestProcessInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cmdLine" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="endTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="exitCode" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GuestProcessInfo", propOrder = {
    "name",
    "pid",
    "owner",
    "cmdLine",
    "startTime",
    "endTime",
    "exitCode"
})
public class GuestProcessInfo
    extends DynamicData
{

    @XmlElement(required = true)
    protected String name;
    protected long pid;
    @XmlElement(required = true)
    protected String owner;
    @XmlElement(required = true)
    protected String cmdLine;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startTime;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endTime;
    protected Integer exitCode;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the pid property.
     * 
     */
    public long getPid() {
        return pid;
    }

    /**
     * Sets the value of the pid property.
     * 
     */
    public void setPid(long value) {
        this.pid = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the cmdLine property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmdLine() {
        return cmdLine;
    }

    /**
     * Sets the value of the cmdLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmdLine(String value) {
        this.cmdLine = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartTime(XMLGregorianCalendar value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the endTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndTime(XMLGregorianCalendar value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the exitCode property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getExitCode() {
        return exitCode;
    }

    /**
     * Sets the value of the exitCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setExitCode(Integer value) {
        this.exitCode = value;
    }

}
