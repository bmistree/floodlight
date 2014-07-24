package net.floodlightcontroller.pronghornmodule;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;

import org.slf4j.Logger;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

import static net.floodlightcontroller.packet.IPv4.toIPv4Address;

public class PronghornFlowTableEntry
{
    public final static short DEFAULT_PRIORITY = 32767;

    public enum Operation {
        INSERT, REMOVE
    }
    public Operation op;
    
    
    public String entry_name = null;
    public boolean active = false;
    public String actions = null;
    public Short priority = DEFAULT_PRIORITY;

    
    public Integer ingress_port = null;
    public String src_mac_address = null;
    public String dst_mac_address = null;
    public Short vlan_id = null;
    public Byte vlan_priority_code_point = null;
    public Short ether_type = null;
    public Byte tos_bits = null;
    public Byte network_protocol = null;

    public String ip_src = null;
    public String ip_dst = null;

    public Short src_port = null;
    public Short dst_port = null;

    public PronghornFlowTableEntry(Operation op)
    {
        this.op = op;
    }

    public OFFlowMod produce_flow_mod_msg(
        int xid,
        IFloodlightProviderService floodlight_provider,
        IStaticFlowEntryPusherService flow_entry_pusher,
        Logger log)  throws IllegalArgumentException 
    {
        OFFlowMod flow_mod_msg =
            (OFFlowMod)floodlight_provider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
        flow_mod_msg.setXid(xid);

        flow_entry_pusher.initDefaultFlowMod(flow_mod_msg, entry_name);

        // this message is not a response to any packet in.
        flow_mod_msg.setBufferId(-1);

        if (op == Operation.INSERT)
            flow_mod_msg.setCommand(OFFlowMod.OFPFC_ADD);
        else
            flow_mod_msg.setCommand(OFFlowMod.OFPFC_DELETE_STRICT);

        // I don't know that this is valid: essentially, I don't want
        // to be informed when this field is removed.
        flow_mod_msg.setFlags((short)0);

        // entries will never disappear on their own from flow table.
        flow_mod_msg.setHardTimeout((short)0);
        flow_mod_msg.setIdleTimeout((short)0);

        // set match
        flow_mod_msg.setMatch(construct_match());

        // set output port: applies deletes to all output ports
        //flow_mod_msg.setOutPort(OFPort.OFPP_NONE);

        // set priority
        flow_mod_msg.setPriority(priority);

        // set actions
        flow_entry_pusher.parseActionString(
            flow_mod_msg, actions, log);

        return flow_mod_msg;
    }


    private OFMatch construct_match() throws IllegalArgumentException 
    {
        // generate a list key-value pairs.  Then, creates a
        // comma-separated list out of these key-value pairs.
        OFMatch match = new OFMatch();
        
        List<String> match_string_as_list = new ArrayList<String>();
        if (ingress_port != null)
            match.setInPort(ingress_port);
        
        if (src_mac_address != null)
            match.setDataLayerSource(src_mac_address);

        if (dst_mac_address != null)
            match.setDataLayerDestination(dst_mac_address);

        if (vlan_id != null)
            match.setDataLayerVirtualLan(vlan_id);

        if (vlan_priority_code_point != null)
        {
            match.setDataLayerVirtualLanPriorityCodePoint(
                vlan_priority_code_point);
        }

        if (ether_type != null)
            match.setDataLayerType(ether_type);

        if (tos_bits != null)
            match.setNetworkTypeOfService(tos_bits);

        if (network_protocol != null)
            match.setNetworkProtocol(network_protocol);

        if (ip_src != null)
            match.setNetworkSource(toIPv4Address(ip_src));

        if (ip_dst != null)
            match.setNetworkDestination(toIPv4Address(ip_dst));
        
        if (src_port != null)
            match.setTransportSource(src_port);

        if (dst_port != null)
            match.setTransportDestination(dst_port);

        return match;
    }
}