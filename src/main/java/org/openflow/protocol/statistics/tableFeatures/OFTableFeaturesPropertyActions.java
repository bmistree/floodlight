package org.openflow.protocol.statistics.tableFeatures;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.U16;

/**
 * Represents an ofp_table_features_prop_actions
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFTableFeaturesPropertyActions extends OFTableFeaturesProperty
    implements OFActionFactoryAware {

    protected OFActionFactory actionFactory;

    //TODO: Check if the reported actions only contain header
    //without more action specific data
    protected List<OFAction> actions;

    /**
     * Returns read-only copies of the actions contained in this Flow Mod
     * @return a list of ordered OFAction objects
     */
    public List<OFAction> getActions() {
        return this.actions;
    }

    /**
     * Sets the list of actions this Flow Mod contains
     * @param actions a list of ordered OFAction objects
     */
    public OFTableFeaturesPropertyActions setActions(List<OFAction> actions) {
        this.actions = actions;
        if (actions != null) {
            int l = MINIMUM_LENGTH;
            for (OFAction action : actions) {
                l += action.getLengthU();
            }
            this.length = U16.t(l);
        }
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        if (this.actionFactory == null)
            throw new RuntimeException("OFActionFactory not set");
        this.actions = this.actionFactory.parseActions(data, getLengthU() -
                MINIMUM_LENGTH);
        int padLength = 8*((length + 7)/8) - length;
        data.position(data.position() + padLength);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        if (actions != null) {
            for (OFAction action : actions) {
                action.writeTo(data);
            }
        }
        int padLength = 8*((length + 7)/8) - length;
        for (;padLength > 0; padLength--) 
            data.put((byte) 0); // pad
    }
    
    @Override
    public void setActionFactory(OFActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }
    
    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + ((actions == null) ? 0 : actions.hashCode());
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
        if (!(obj instanceof OFTableFeaturesPropertyActions)) {
            return false;
        }
        OFTableFeaturesPropertyActions other = (OFTableFeaturesPropertyActions) obj;
        if (actions == null) {
            if (other.actions != null) {
                return false;
            }
        } else if (!actions.equals(other.actions)) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFTableFeaturesPropertyActions [type=" + type +
            ", length=" + length + ", actions=" + actions +"]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFTableFeaturesPropertyActions clone() throws CloneNotSupportedException {
	    try {
	    	OFTableFeaturesPropertyActions tableFeaturesProp = (OFTableFeaturesPropertyActions) super.clone();
	        List<OFAction> neoActions = new LinkedList<OFAction>();
	        for(OFAction action: this.actions)
	            neoActions.add((OFAction) action.clone());
	        tableFeaturesProp.setActions(neoActions);
	        return tableFeaturesProp;
	    } catch (CloneNotSupportedException e) {
	        // Won't happen
	        throw new RuntimeException(e);
	    }
    }
}
