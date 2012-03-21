
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RetrieveUserGroupsRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RetrieveUserGroupsRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="_this" type="{urn:vim25}ManagedObjectReference"/>
 *         &lt;element name="domain" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="searchStr" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="belongsToGroup" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="belongsToUser" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exactMatch" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="findUsers" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="findGroups" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveUserGroupsRequestType", propOrder = {
    "_this",
    "domain",
    "searchStr",
    "belongsToGroup",
    "belongsToUser",
    "exactMatch",
    "findUsers",
    "findGroups"
})
public class RetrieveUserGroupsRequestType {

    @XmlElement(required = true)
    protected ManagedObjectReference _this;
    protected String domain;
    @XmlElement(required = true)
    protected String searchStr;
    protected String belongsToGroup;
    protected String belongsToUser;
    protected boolean exactMatch;
    protected boolean findUsers;
    protected boolean findGroups;

    /**
     * Gets the value of the this property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedObjectReference }
     *     
     */
    public ManagedObjectReference getThis() {
        return _this;
    }

    /**
     * Sets the value of the this property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedObjectReference }
     *     
     */
    public void setThis(ManagedObjectReference value) {
        this._this = value;
    }

    /**
     * Gets the value of the domain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the value of the domain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomain(String value) {
        this.domain = value;
    }

    /**
     * Gets the value of the searchStr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSearchStr() {
        return searchStr;
    }

    /**
     * Sets the value of the searchStr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSearchStr(String value) {
        this.searchStr = value;
    }

    /**
     * Gets the value of the belongsToGroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBelongsToGroup() {
        return belongsToGroup;
    }

    /**
     * Sets the value of the belongsToGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBelongsToGroup(String value) {
        this.belongsToGroup = value;
    }

    /**
     * Gets the value of the belongsToUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBelongsToUser() {
        return belongsToUser;
    }

    /**
     * Sets the value of the belongsToUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBelongsToUser(String value) {
        this.belongsToUser = value;
    }

    /**
     * Gets the value of the exactMatch property.
     * 
     */
    public boolean isExactMatch() {
        return exactMatch;
    }

    /**
     * Sets the value of the exactMatch property.
     * 
     */
    public void setExactMatch(boolean value) {
        this.exactMatch = value;
    }

    /**
     * Gets the value of the findUsers property.
     * 
     */
    public boolean isFindUsers() {
        return findUsers;
    }

    /**
     * Sets the value of the findUsers property.
     * 
     */
    public void setFindUsers(boolean value) {
        this.findUsers = value;
    }

    /**
     * Gets the value of the findGroups property.
     * 
     */
    public boolean isFindGroups() {
        return findGroups;
    }

    /**
     * Sets the value of the findGroups property.
     * 
     */
    public void setFindGroups(boolean value) {
        this.findGroups = value;
    }

}
