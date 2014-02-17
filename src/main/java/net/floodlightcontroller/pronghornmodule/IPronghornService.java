package net.floodlightcontroller.pronghornmodule;

import java.io.IOException;

import net.floodlightcontroller.core.module.IFloodlightService;


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

    public void register_switch_changes_listener(
        ISwitchAddedRemovedListener switch_added_removed_listener);
    public void unregister_switch_changes_listener(
        ISwitchAddedRemovedListener switch_added_removed_listener);

    public void shutdown_all_now();
}
