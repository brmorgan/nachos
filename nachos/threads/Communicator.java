package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */


public class Communicator
{
    //SO - Time to create both a private boolean to represent the word buffer and an integer to show the shared word.
    private boolean wordBufferObject;
    private int sharedWord;
    private Lock fryLock;
    private Condition currentSpeaker;
    private Condition speakers;
    private Condition listeners;

    /**
     * Allocate a new communicator.
     */
    public Communicator()
    {
        //SO - Within the constructor make both speaker and listener condition objects and then give the word buffer object a value.
        wordBufferObject = false;
        fryLock = new Lock();
        currentSpeaker = new Condition(fryLock);
        speakers = new Condition(fryLock);
        listeners = new Condition(fryLock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word)
    {
        // Speaker acquires the lock
        fryLock.acquire();
        // if the wordBuffer is full...
        if(wordBufferObject)
        {
            listeners.wake();
            speakers.sleep();
        }
        // set the shared word to the current one
        sharedWord = word;
        wordBufferObject = true;
        // wake all listeners and sleep this speaker
        listeners.wake();
        currentSpeaker.sleep();
        fryLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen()
    {
        fryLock.acquire();

        if(!wordBufferObject)
        {
            listeners.sleep();
        }
        int burner = sharedWord;
        wordBufferObject = false;
        // wake up all the speakers and release the lock
        currentSpeaker.wake();
        speakers.wake();
        fryLock.release();
	    return burner;
    }

}
