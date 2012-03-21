
package com.vmware.vim25;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "VmFaultToleranceIssueFault", targetNamespace = "urn:vim25")
public class VmFaultToleranceIssueFaultMsg
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private VmFaultToleranceIssue faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public VmFaultToleranceIssueFaultMsg(String message, VmFaultToleranceIssue faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public VmFaultToleranceIssueFaultMsg(String message, VmFaultToleranceIssue faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.vmware.vim25.VmFaultToleranceIssue
     */
    public VmFaultToleranceIssue getFaultInfo() {
        return faultInfo;
    }

}
