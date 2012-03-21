
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScsiLunType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ScsiLunType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="disk"/>
 *     &lt;enumeration value="tape"/>
 *     &lt;enumeration value="printer"/>
 *     &lt;enumeration value="processor"/>
 *     &lt;enumeration value="worm"/>
 *     &lt;enumeration value="cdrom"/>
 *     &lt;enumeration value="scanner"/>
 *     &lt;enumeration value="opticalDevice"/>
 *     &lt;enumeration value="mediaChanger"/>
 *     &lt;enumeration value="communications"/>
 *     &lt;enumeration value="storageArrayController"/>
 *     &lt;enumeration value="enclosure"/>
 *     &lt;enumeration value="unknown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ScsiLunType")
@XmlEnum
public enum ScsiLunType {

    @XmlEnumValue("disk")
    DISK("disk"),
    @XmlEnumValue("tape")
    TAPE("tape"),
    @XmlEnumValue("printer")
    PRINTER("printer"),
    @XmlEnumValue("processor")
    PROCESSOR("processor"),
    @XmlEnumValue("worm")
    WORM("worm"),
    @XmlEnumValue("cdrom")
    CDROM("cdrom"),
    @XmlEnumValue("scanner")
    SCANNER("scanner"),
    @XmlEnumValue("opticalDevice")
    OPTICAL_DEVICE("opticalDevice"),
    @XmlEnumValue("mediaChanger")
    MEDIA_CHANGER("mediaChanger"),
    @XmlEnumValue("communications")
    COMMUNICATIONS("communications"),
    @XmlEnumValue("storageArrayController")
    STORAGE_ARRAY_CONTROLLER("storageArrayController"),
    @XmlEnumValue("enclosure")
    ENCLOSURE("enclosure"),
    @XmlEnumValue("unknown")
    UNKNOWN("unknown");
    private final String value;

    ScsiLunType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScsiLunType fromValue(String v) {
        for (ScsiLunType c: ScsiLunType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
