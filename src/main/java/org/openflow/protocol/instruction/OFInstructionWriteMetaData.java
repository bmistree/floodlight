package org.openflow.protocol.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents an ofp_instruction_write_metadata
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstructionWriteMetaData extends OFInstruction {
    public static int MINIMUM_LENGTH = 24;

    protected long metadata;
    protected long metadataMask;

    public OFInstructionWriteMetaData() {
        super.setType(OFInstructionType.WRITE_METADATA);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFInstructionWriteMetaData(long metadata, long metadataMask) {
        super.setType(OFInstructionType.WRITE_METADATA);
        super.setLength((short) MINIMUM_LENGTH);
        this.metadata = metadata;
        this.metadataMask = metadataMask;   
    }

    /**
     * Get metadata
     * @return
     */
    public long getMetadata() {
        return this.metadata;
    }

    /**
     * Set metadata
     * @param metadata
     */
    public OFInstructionWriteMetaData setMetadata(long metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get metadataMask
     * @return
     */
    public long getMetadataMask() {
        return this.metadataMask;
    }

    /**
     * Set metadataMask
     * @param metadataMask
     */
    public OFInstructionWriteMetaData setMetadataMask(long metadataMask) {
        this.metadataMask = metadataMask;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        data.position(data.position() + 4); // pad
        this.metadata = data.getLong();
        this.metadataMask = data.getLong();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt((int) 0); // pad
        data.putLong(metadata);
        data.putLong(metadataMask);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + (int) (metadata ^ (metadata >>> 32));
        result = prime * result + (int) (metadataMask ^ (metadataMask >>> 32));
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
        if (!(obj instanceof OFInstructionWriteMetaData)) {
            return false;
        }
        OFInstructionWriteMetaData other = (OFInstructionWriteMetaData) obj;
        if (metadata != other.metadata) {
            return false;
        }
        if (metadataMask != other.metadataMask) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFInstructionWriteMetaData [metadata=" + metadata + ", metadataMask=" + metadataMask + "]";
    }

}
