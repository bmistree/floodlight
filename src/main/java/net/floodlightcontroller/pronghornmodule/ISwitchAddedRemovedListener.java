package net.floodlightcontroller.pronghornmodule;

public interface ISwitchAddedRemovedListener
{
    public void switch_added(String unique_switch_id);
    public void switch_removed(String unique_switch_id);
}