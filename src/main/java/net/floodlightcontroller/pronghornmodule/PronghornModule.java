package net.floodlightcontroller.pronghornmodule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Future;
import java.util.List;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFType;
import org.openflow.protocol.OFError;
import org.openflow.util.HexString;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.OFStatisticsRequest;

import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PronghornModule
    implements IFloodlightModule, IOFMessageListener, IPronghornService
{
    protected static final Logger log = LoggerFactory.getLogger(PronghornModule.class);
            
    protected IFloodlightProviderService floodlightProvider;
    protected IRestApiService restApi;
    protected IStaticFlowEntryPusherService flow_entry_pusher;
    protected IThreadPoolService threadpool_service;
    protected ILinkDiscoveryService link_discovery_service;
    protected ConcurrentHashMap<IOFSwitch, BlockingQueue<OFMessage>> queues;
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices()
    {
        Collection<Class<? extends IFloodlightService>> l =
            new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IPronghornService.class);
        return l;
    }       

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls()
    {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
            new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IPronghornService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies()
    {
        Collection<Class<? extends IFloodlightService>> l =
            new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IRestApiService.class);
        l.add(IStaticFlowEntryPusherService.class);
        l.add(IThreadPoolService.class);
        l.add(ILinkDiscoveryService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
        throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApi = context.getServiceImpl(IRestApiService.class);
        threadpool_service = context.getServiceImpl(IThreadPoolService.class);
        link_discovery_service = context.getServiceImpl(ILinkDiscoveryService.class);
        
        flow_entry_pusher =
            context.getServiceImpl(IStaticFlowEntryPusherService.class);
        queues = new ConcurrentHashMap<IOFSwitch, BlockingQueue<OFMessage>>();
    }

    @Override
    public void shutdown_all_now()
    {
        ScheduledExecutorService executor_service = 
            threadpool_service.getScheduledExecutor();
        executor_service.shutdownNow();
    }
    
    @Override
    public void startUp(FloodlightModuleContext context)
    {
        floodlightProvider.addOFMessageListener(OFType.BARRIER_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.ERROR, this);
        restApi.addRestletRoutable(new PronghornWebRoutable());
    }

    
    @Override
    public void register_switch_listener(IOFSwitchListener switch_listener)
    {
        floodlightProvider.addOFSwitchListener(switch_listener);
        
    }
    @Override
    public void unregister_switch_listener(IOFSwitchListener switch_listener)
    {
        floodlightProvider.removeOFSwitchListener(switch_listener);
    }

    @Override
    public void register_link_discovery_listener(ILinkDiscoveryListener listener)
    {
        link_discovery_service.addListener(listener);
    }

    
    @Override
    public String getName() {
        return "PronghornModule";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name)
    {
        /// FIXME: Not entirely sure what this method does
        return false;
    }
    
    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name)
    {
        /// FIXME: Not entirely sure what this method does
        return false;
    }

    /* Create queue for sw if does not exist */
    private void ensureQueueExists(IOFSwitch sw) {
        if (!queues.contains(sw)) {
            queues.put(sw, new LinkedBlockingQueue<OFMessage>());
        }
    }

    @Override
    public net.floodlightcontroller.core.IListener.Command receive(
        IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
    {
        if (queues.get(sw) != null)
        {
            if ((msg.getType() == OFType.BARRIER_REPLY) ||
                (msg.getType() == OFType.ERROR))
            {
                try
                {
                    queues.get(sw).put(msg);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    log.error(
                        "Interrupted excpetion while putting",e.getMessage());
                    assert(false);
                }
            }
            // DEBUG
            else
            {
                // only expecting messages for errors and barrier
                // replies
                log.error("Received unknown message type in pronghorn.");
                assert(false);
            }
            // END DEBUG
        }
        
        return Command.CONTINUE;
    }

    
    /** IPronghornService interfaces*/        
    @Override
    public String sendBarrier(String switch_id)
    {
        RESTBarrierCallback cb = new RESTBarrierCallback();
        try {
            if (send_barrier(switch_id,cb) &&
                (! cb.had_error.get()))
                return "true";
        } catch (IOException ex) {
            // ignore IOException: returning false anyways.
        }
        return "false";
    }

    private class RESTBarrierCallback implements IPronghornBarrierCallback
    {
        public final AtomicBoolean had_error = new AtomicBoolean (false);
        
        @Override
        public void command_failure(int id)
        {
            had_error.set(true);
        }

        @Override
        public void barrier_success(){}
        @Override
        public void barrier_failure(){}
    }


    @Override
    public int add_entry (PronghornFlowTableEntry entry,String switch_id)
        throws IOException, IllegalArgumentException        
    {
        return send_flow_mod_msg(entry,switch_id);
    }
    
    @Override
    public int remove_entry (PronghornFlowTableEntry entry,String switch_id)
        throws IOException, IllegalArgumentException
    {
        return send_flow_mod_msg(entry,switch_id);
    }

    @Override
    public void barrier (
        String switch_id,IPronghornBarrierCallback cb) throws IOException
    {
        if (send_barrier(switch_id,cb))
            cb.barrier_success();
        else
            cb.barrier_failure();
    }

    private int send_flow_mod_msg (
        PronghornFlowTableEntry entry,String switch_id)
        throws IOException, IllegalArgumentException
        
    {
        long id = HexString.toLong(switch_id);
        IOFSwitch sw = floodlightProvider.getSwitch(id);
        ensureQueueExists(sw);

        int xid = sw.getNextTransactionId();
        OFFlowMod flow_mod_msg =
            entry.produce_flow_mod_msg(
                xid,floodlightProvider,flow_entry_pusher,log);
        sw.write(flow_mod_msg, null);
        return xid;
    }
    

    /**
       @returns {List<OFStatistics> or null} --- null if switch does
       not exist.
     */
    public Future<List<OFStatistics>> get_stats(String switch_id)
        throws IOException
    {
        long id = HexString.toLong(switch_id);
        IOFSwitch sw = floodlightProvider.getSwitch(id);
        if (sw == null)
            return null;
        return sw.queryStatistics(new OFStatisticsRequest());
    }

    
    /**
       @returns {boolean} --- True if the barrier completes before
       timing out.  False if it does not.  Note that a transaction may
       have failed even if this method returns true: in particular,
       this can happen if one of the command messages associated with
       the transaction returns an error.  These errors get passed back
       through cb.
     */
    private boolean send_barrier(String switchId,IPronghornBarrierCallback cb)
        throws IOException
    {
        long id = HexString.toLong(switchId);
        // send barrier request
        IOFSwitch sw = floodlightProvider.getSwitch(id);
        OFMessage barrierReq =
            floodlightProvider.getOFMessageFactory().getMessage(OFType.BARRIER_REQUEST);
        
        ensureQueueExists(sw);
        int xid = sw.getNextTransactionId();
        barrierReq.setXid(xid);
        sw.write(barrierReq, null);
        
        // block until barrier reply or timeout.
        while (true)
        {
            OFMessage queue_resp = null;
            try
            {
                queue_resp = queues.get(sw).poll(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
                log.error(
                    "InterruptedException on queue.",e.getMessage());
                assert(false);
                return false;
            }

            // timed out waiting for response
            if (queue_resp == null)
                return false;

            // check if the return message is for barrier or for error
            if (queue_resp.getType() == OFType.BARRIER_REPLY)
                return true;
            else if (queue_resp.getType() == OFType.ERROR)
            {
                // if we have a callback, then tell it that one of the
                // changes failed
                if (cb != null)
                {
                    int err_xid = queue_resp.getXid();
                    cb.command_failure(err_xid);
                }
            }
            // DEBUG
            else
            {
                log.error("Unknown openflow message type.");
                assert(false);
                return false;
            }
            // END DEBUG
        }
    }
    
}
