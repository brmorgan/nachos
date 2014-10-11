package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    // Added on 10 October 2014
    private static int childrenOnOahu, childrenOnMolokai, adultsOnOahu, adultsOnMolokai;
    private static int onBoat;
    private static int waitingChildren;
    private static int childrenOffBoat;
    private static boolean boatAtOahu;
    private static Lock oahuLock, molokaiLock;
    private static Condition2 sleepAdultOahu = new Condition2(oahuLock);
    private static Condition2 sleepChildOahu = new Condition2(oahuLock);
    private static Condition2 sleepAdultMolokai = new Condition2(molokaiLock);
    private static Condition2 sleepChildMolokai = new Condition2(molokaiLock);
    private static Condition2 waitingOnBoat = new Condition2(oahuLock);
    // --
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	
	// Added on 10 October 2014
	adultsOnOahu = adults;
	childrenOnOahu = children;
	adultsOnMolokai = 0;
	childrenOnMolokai = 0;
	onBoat = 0;
	waitingChildren = 0;
	childrenOffBoat=0;
	boatAtOahu = true;
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
	for(int i=0; i<adults; i++)
	{
		Runnable r = new Runnable()
		{
			public void run() { AdultItinerary(); }
		};
		KThread thread = new KThread(r);
		thread.setName("Adult #" + i);
		thread.fork();
	}
	for(int i=0; i<adults; i++)
	{
		Runnable r = new Runnable()
		{
			public void run() { ChildItinerary(); }
		};
		KThread thread = new KThread(r);
		thread.setName("Child #" + i);
		thread.fork();
	}
	
	// --
	
	/*
	Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
	    	}
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();
	*/
    }

    static void AdultItinerary()
    {
    	bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
    	//DO NOT PUT ANYTHING ABOVE THIS LINE.
    	/* This is where you should put your solutions. Make calls
	   	to the BoatGrader to show that it is synchronized. For
	   	example:
	       bg.AdultRowToMolokai();
	   	indicates that an adult has rowed the boat across to Molokai
    	*/
    	
    	// Added on 10 October 2014
    	boolean imOnOahu = true;
    	
    	while(imOnOahu)
    	{
	    	oahuLock.acquire();
	    	if(childrenOnOahu > 1 || !boatAtOahu || onBoat > 0)
	    	{
	    		sleepAdultOahu.sleep();
	    		oahuLock.release();
	    	}
	    	else
	    	{
	    		adultsOnOahu--;
	    		boatAtOahu = false;
	    		oahuLock.release();
	    	
	    		bg.AdultRowToMolokai();
	    		imOnOahu = false;
	    	
	    		molokaiLock.acquire();
	    		adultsOnMolokai++;
	    		sleepChildMolokai.wakeAll();
	    		sleepAdultMolokai.sleep();
	    		molokaiLock.release();
	    	}
    	}
    	
    	// --
    }

    static void ChildItinerary()
    {
    	bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
    	//DO NOT PUT ANYTHING ABOVE THIS LINE.
    	
    	// Added on 10 October 2014
    	while(childrenOnOahu + adultsOnOahu > 1)
    	{
    		oahuLock.acquire();
    		if(childrenOnOahu == 1)
    		{
    			sleepAdultOahu.wakeAll();
    		}
    		if(waitingChildren >= 2 || !boatAtOahu)
    		{
    			sleepChildOahu.sleep();
    			oahuLock.release();
    		}
    		else
    		{
    			if(waitingChildren == 0)
    			{
    				waitingChildren++;
    				sleepChildOahu.wake();
    				waitingOnBoat.sleep();
    				bg.ChildRideToMolokai();
    				waitingOnBoat.wake();
    			}
    			else
    			{
    				waitingChildren++;
    				waitingOnBoat.wake();
    				bg.ChildRowToMolokai();
    				waitingOnBoat.sleep();
    			}
    			waitingChildren--;
    			childrenOnOahu--;
    			boatAtOahu = false;
    			oahuLock.release();
    			
    			molokaiLock.acquire();
    			childrenOnMolokai++;
    			childrenOffBoat++;
    			if(childrenOffBoat == 1)
    				sleepChildMolokai.sleep();
    			childrenOnMolokai--;
    			childrenOffBoat = 0;
    			molokaiLock.release();
    			
    			bg.ChildRowToOahu();
    			
    			oahuLock.acquire();
    			childrenOnOahu++;
    			boatAtOahu = true;
    			oahuLock.release(); 			
    		}
    	}
    	
    	oahuLock.acquire();
    	childrenOnOahu--;
    	oahuLock.release();
    	
    	bg.ChildRowToMolokai();
    	
    	molokaiLock.acquire();
    	childrenOnMolokai++;
    	molokaiLock.release();
    	
    	// --
    }

    /*
    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    */
    
}
