package net.floodlightcontroller.pronghornmodule;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.List;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.OFStatisticsRequest;

import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;


public interface IPronghornService extends IFloodlightService {
    public String sendBarrier(String switchId);

    /**
       Returns unique id associated with 
     */
    public int add_entry (PronghornFlowTableEntry entry,String switch_id)
        throws IOException, IllegalArgumentException;
    public int remove_entry (PronghornFlowTableEntry entry, String switch_id) 
        throws IOException, IllegalArgumentException;
    public void barrier (
        String switch_id,IPronghornBarrierCallback cb) throws IOException;

    public void register_switch_listener(IOFSwitchListener switch_listener);
    public void unregister_switch_listener(IOFSwitchListener switch_listener);

    public void register_link_discovery_listener(ILinkDiscoveryListener listener);


    /**
       @returns {List<OFStatistics> or null} --- null if switch does
       not exist.
     */
    public Future<List<OFStatistics>> get_aggregate_stats(String switch_id)
        throws IOException;

    /**
       @returns {List<OFStatistics> or null} --- null if switch does
       not exist.
     */
    public Future<List<OFStatistics>> get_port_stats(String switch_id)
        throws IOException;

    
    // note: link discovery service provides no way to actually unregister.
    //public void unregister_link_discovery_listener(ILinkDiscoveryListener listener);
    
    public void shutdown_all_now();
}
