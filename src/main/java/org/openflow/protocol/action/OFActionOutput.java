/**
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
package org.openflow.protocol.action;

import java.nio.ByteBuffer;

import org.openflow.protocol.OFPhysicalPort;
import org.openflow.util.U16;

/**
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFActionOutput extends OFAction implements Cloneable {
    public static int MINIMUM_LENGTH = 16;

    protected static int OFPCML_ZERO = 0;
    protected static int OFPCML_MAX = 0xffe5;
    protected static int OFPCML_NO_BUFFER = 0xffff;

    protected int portNumber;
    protected short maxLength;

    public OFActionOutput() {
        super.setType(OFActionType.OUTPUT);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFActionOutput(int portNumber) {
        this(portNumber, (short)0);
    }

    public OFActionOutput(int portNumber, short maxLength) {
        super();
        super.setType(OFActionType.OUTPUT);
        super.setLength((short) MINIMUM_LENGTH);
        this.portNumber = portNumber;
        this.maxLength = maxLength;
    }

    public OFActionOutput(OFPhysicalPort port) {
        this(port.getPortNumber(), (short)0);
    }

    public OFActionOutput(OFPhysicalPort port, short maxLength) {
        this(port.getPortNumber(), maxLength);
    }

    /**
     * Get the output portNumber
     * @return
     */
    public int getPort() {
        return this.portNumber;
    }

    /**
     * Set the output portNumber
     * @param portNumber
     */
    public OFActionOutput setPort(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    /**
     * Get the max length to send to the controller
     * @return
     */
    public short getMaxLength() {
        return this.maxLength;
    }

    /**
     * Set the max length to send to the controller
     * @param maxLength
     */
    public OFActionOutput setMaxLength(short maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.portNumber = data.getInt();
        this.maxLength = data.getShort();
        data.position(data.position() + 6); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(portNumber);
        data.putShort(maxLength);
        data.putShort((short) 0); // pad
        data.putInt((int) 0); // pad

    }

    @Override
    public int hashCode() {
        final int prime = 367;
        int result = super.hashCode();
        result = prime * result + maxLength;
        result = prime * result + portNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFActionOutput)) {
            return false;
        }
        OFActionOutput other = (OFActionOutput) obj;
        if (maxLength != other.maxLength) {
            return false;
        }
        if (portNumber != other.portNumber) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFActionOutput [maxLength=" + maxLength + ", portNumber=" + portNumber
                + ", length=" + length + ", type=" + type + "]";
    }
}
