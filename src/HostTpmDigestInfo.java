
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HostTpmDigestInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HostTpmDigestInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:vim25}HostDigestInfo">
 *       &lt;sequence>
 *         &lt;element name="pcrNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostTpmDigestInfo", propOrder = {
    "pcrNumber"
})
public class HostTpmDigestInfo
    extends HostDigestInfo
{

    protected int pcrNumber;

    /**
     * Gets the value of the pcrNumber property.
     * 
     */
    public int getPcrNumber() {
        return pcrNumber;
    }

    /**
     * Sets the value of the pcrNumber property.
     * 
     */
    public void setPcrNumber(int value) {
        this.pcrNumber = value;
    }

}
