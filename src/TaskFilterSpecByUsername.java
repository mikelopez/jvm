
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TaskFilterSpecByUsername complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TaskFilterSpecByUsername">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}DynamicData">
 *       &lt;sequence>
 *         &lt;element name="systemUser" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="userList" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskFilterSpecByUsername", propOrder = {
    "systemUser",
    "userList"
})
public class TaskFilterSpecByUsername
    extends DynamicData
{

    protected boolean systemUser;
    protected List<String> userList;

    /**
     * Gets the value of the systemUser property.
     * 
     */
    public boolean isSystemUser() {
        return systemUser;
    }

    /**
     * Sets the value of the systemUser property.
     * 
     */
    public void setSystemUser(boolean value) {
        this.systemUser = value;
    }

    /**
     * Gets the value of the userList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUserList() {
        if (userList == null) {
            userList = new ArrayList<String>();
        }
        return this.userList;
    }

}
