package net.floodlightcontroller.pronghornmodule;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import org.openflow.util.HexString;

import net.floodlightcontroller.core.ImmutablePort;
import net.floodlightcontroller.core.IOFSwitch.PortChangeType;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitchListener;

import org.slf4j.Logger;


public class PronghornSwitchListener implements IOFSwitchListener
{
    protected Logger log = null;
    
    protected ReentrantLock switch_listener_lock = new ReentrantLock();
    protected Set<ISwitchAddedRemovedListener> switch_listener_set =
        new HashSet();
    protected IFloodlightProviderService floodlight_provider = null;

    public void init(IFloodlightProviderService floodlight_provider, Logger log)
    {
        this.log = log;
        this.floodlight_provider = floodlight_provider;
        this.floodlight_provider.addOFSwitchListener(this);
    }


    public void register_switch_changes_listener(
        ISwitchAddedRemovedListener switch_added_removed_listener)
    {
        switch_listener_lock.lock();
        switch_listener_set.add(switch_added_removed_listener);
        switch_listener_lock.unlock();
    }

    public void unregister_switch_changes_listener(
        ISwitchAddedRemovedListener switch_added_removed_listener)
    {
        switch_listener_lock.lock();
        switch_listener_set.remove(switch_added_removed_listener);
        switch_listener_lock.unlock();
    }

    /** ISwitchListener methods*/

    @Override
    public void switchAdded(long switchId)
    {
        String unique_switch_id = HexString.toHexString(switchId);
        switch_listener_lock.lock();
        for (ISwitchAddedRemovedListener listener : switch_listener_set)
            listener.switch_added(unique_switch_id);
        switch_listener_lock.unlock();
    }

    @Override
    public void switchRemoved(long switchId)
    {
        String unique_switch_id = HexString.toHexString(switchId);
        switch_listener_lock.lock();
        for (ISwitchAddedRemovedListener listener : switch_listener_set)
            listener.switch_removed(unique_switch_id);
        switch_listener_lock.unlock();
    }

    @Override
    public void switchActivated(long switchId) {
        log.error(
            "PronghorSwitchListener assumes all switches are already active");
    }

    @Override
    public void switchPortChanged(
        long switchId, ImmutablePort port, PortChangeType type)
    {
        log.error(
            "PronghorSwitchListener assumes no switch port change.");
    }

    @Override
    public void switchChanged(long switchId)
    {
        log.error(
            "PronghorSwitchListener assumes no switch change.");
    }
}
