
package com.vmware.vim25;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfDiagnosticManagerBundleInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfDiagnosticManagerBundleInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DiagnosticManagerBundleInfo" type="{urn:vim25}DiagnosticManagerBundleInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfDiagnosticManagerBundleInfo", propOrder = {
    "diagnosticManagerBundleInfo"
})
public class ArrayOfDiagnosticManagerBundleInfo {

    @XmlElement(name = "DiagnosticManagerBundleInfo")
    protected List<DiagnosticManagerBundleInfo> diagnosticManagerBundleInfo;

    /**
     * Gets the value of the diagnosticManagerBundleInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the diagnosticManagerBundleInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDiagnosticManagerBundleInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DiagnosticManagerBundleInfo }
     * 
     * 
     */
    public List<DiagnosticManagerBundleInfo> getDiagnosticManagerBundleInfo() {
        if (diagnosticManagerBundleInfo == null) {
            diagnosticManagerBundleInfo = new ArrayList<DiagnosticManagerBundleInfo>();
        }
        return this.diagnosticManagerBundleInfo;
    }

}
