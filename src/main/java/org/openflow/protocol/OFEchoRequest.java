package org.openflow.protocol;

import java.nio.ByteBuffer;

import org.openflow.util.U16;

/**
 * Represents an ofp_echo_request message
 * 
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */

public class OFEchoRequest extends OFMessage {
    byte[] payload;

    public OFEchoRequest() {
        super();
        this.type = OFType.ECHO_REQUEST;
    }

    @Override
    public void readFrom(ByteBuffer bb) {
        super.readFrom(bb);
        int datalen = this.getLengthU() - OFMessage.MINIMUM_LENGTH;
        if (datalen > 0) {
            this.payload = new byte[datalen];
            bb.get(payload);
        }
    }

    /**
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @param payload
     *            the payload to set
     */
    public OFEchoRequest setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public void writeTo(ByteBuffer bb) {
        super.writeTo(bb);
        if (payload != null)
            bb.put(payload);
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = U16.t(OFMessage.MINIMUM_LENGTH + ((payload != null) ? payload.length : 0));
    }
}
