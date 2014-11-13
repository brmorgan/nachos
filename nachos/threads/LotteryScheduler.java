package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler 
{
	public static final int MINPRIORITY = 1;
	public static final int MAXPRIORITY = Integer.MAX_VALUE;
	
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() 
    {
    	
    }
    
    public void setPriority(KThread thread, int priority)
    {
    	Lib.assertTrue(Machine.interrupt().disabled());
	    Lib.assertTrue(priority >= MINPRIORITY );
	    getThreadState(thread).setPriority(priority);
    }
    
    public boolean increasePriority()
    {
    	Machine.interrupt().disable();
    	
    	KThread thread = KThread.currentThread();
    	int priority = getPriority(thread);
    	if(priority == MAXPRIORITY)
    		return false;
    	setPriority(thread, priority+1);
    	
    	Machine.interrupt().enable();
    	return true;
    }
    
    public boolean decreasePriority()
    {
    	Machine.interrupt().disable();
    	
    	KThread thread = KThread.currentThread();
    	int priority = getPriority(thread);
    	if(priority == MINPRIORITY)
    		return false;
    	setPriority(thread, priority-1);
    	
    	Machine.interrupt().enable();
    	return true;
    }
    
    public int getEffectivePriority(ThreadState mystate)
    {
    	int tickets = mystate.priority;
    	for(PriorityQueue myQueue: mystate.donateQueue)
    	{
    		for(KThread currentThread : myQueue.waitQueue)
    		{
    			if(getThreadState(currentThread) == mystate)
    				continue;
    			tickets += getThreadState(currentThread).getEffectivePriority();
    		}
    	}
    	return tickets;
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
//    public ThreadQueue newThreadQueue(boolean transferPriority) 
//    {
//    	// implement me
//    	return null;
//    }
}
