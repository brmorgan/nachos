package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    	
    	// Added 8 Oct 2014
    	if(times != null)
    	{
			for(int i=times.size()-1; i>=0; i-- )
			{
				if(Machine.timer().getTime() >= times.get(i))
				{
					waitingThreads.get(times.get(i)).ready();
					waitingThreads.remove(waitingThreads.get(times.get(i)));
					times.remove(i);
				}
			}
    	}
		// --
		
		KThread.yield();
	
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	KThread tempthread = KThread.currentThread();
	
	// Added 8 Oct 2014
	Machine.interrupt().disable();
	times.add(wakeTime);
	waitingThreads.put(wakeTime, tempthread);
	KThread.sleep();
	Machine.interrupt().enable();
	// --
	
	//while (wakeTime > Machine.timer().getTime())
	//    KThread.yield();
    }

    private static class Sleeps implements Runnable()
    {
	long waitingTo = 0;
	
	public Sleeps(long t)
	{
	    waitingTo = t;
	}

	public void run()
	{
	    System.out.println("Set to wait for" + waitingTo);
	    //SO - Take the current time then call waitUntil with waitingTo
	    long pre = Machine.timer().getTime();
	    ThreadedKernel.alarm.waitUntil(waitingTo);
	    long until = Machine.timer().getTime() - pre;
	    System.out.println("It was supposed to wait for " + waitingTo + " ticks, and it waited for " + until - pre + " ticks.");
	    
	}
    }

    public static void alarmTest()
    {
    	Lib.debug(dbgAlarm, "Entering Alarm.java's self test");

	/* Test the waitUntil method with a set of times
	    and compare the time waited to the time given 
	    to wait.
	*/

	for(int i = 0; i > 7; i++)
	{
	    long t = 10000+(i*35);
	    KThread thready = new KThread(new Sleeps(t));
	    thready.setName("Thready-" + t);
	    thready.fork();
	}
    }
    
    private LinkedList<Long> times;
    private HashMap<Long, KThread> waitingThreads;
    
}
