
package com.vmware.vim25;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ClusterDasVmSettingsRestartPriority.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ClusterDasVmSettingsRestartPriority">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="disabled"/>
 *     &lt;enumeration value="low"/>
 *     &lt;enumeration value="medium"/>
 *     &lt;enumeration value="high"/>
 *     &lt;enumeration value="clusterRestartPriority"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ClusterDasVmSettingsRestartPriority")
@XmlEnum
public enum ClusterDasVmSettingsRestartPriority {

    @XmlEnumValue("disabled")
    DISABLED("disabled"),
    @XmlEnumValue("low")
    LOW("low"),
    @XmlEnumValue("medium")
    MEDIUM("medium"),
    @XmlEnumValue("high")
    HIGH("high"),
    @XmlEnumValue("clusterRestartPriority")
    CLUSTER_RESTART_PRIORITY("clusterRestartPriority");
    private final String value;

    ClusterDasVmSettingsRestartPriority(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ClusterDasVmSettingsRestartPriority fromValue(String v) {
        for (ClusterDasVmSettingsRestartPriority c: ClusterDasVmSettingsRestartPriority.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
