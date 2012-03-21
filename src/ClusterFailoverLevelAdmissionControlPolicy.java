
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ClusterFailoverLevelAdmissionControlPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClusterFailoverLevelAdmissionControlPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}ClusterDasAdmissionControlPolicy">
 *       &lt;sequence>
 *         &lt;element name="failoverLevel" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClusterFailoverLevelAdmissionControlPolicy", propOrder = {
    "failoverLevel"
})
public class ClusterFailoverLevelAdmissionControlPolicy
    extends ClusterDasAdmissionControlPolicy
{

    protected int failoverLevel;

    /**
     * Gets the value of the failoverLevel property.
     * 
     */
    public int getFailoverLevel() {
        return failoverLevel;
    }

    /**
     * Sets the value of the failoverLevel property.
     * 
     */
    public void setFailoverLevel(int value) {
        this.failoverLevel = value;
    }

}
