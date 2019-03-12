
import java.util.Scanner;
import java.util.*;
import java.io.*;
public class Lab3
{

 /* This function takes the input file as an argument, parses the file, and stores
  * the information */
 public static void readInput(File input) {
   try {readFile = new Scanner(input);} 
   catch (FileNotFoundException e) {System.out.println("input file not found");}
   
   numProcesses = readFile.nextInt();
   processes = new Process[numProcesses];
   numInitiatesPerProcess = new int[numProcesses];
   numResources = readFile.nextInt();
   resources = new Resource[numResources];
   for (int i = 0; i < numProcesses; i++) {processes[i] = new Process(i + 1);}

   for (int i = 0; i < numResources; i++) {resources[i] = new Resource((i + 1), readFile.nextInt());}
   while (readFile.hasNext()) {
    Activity a = new Activity(readFile.next(), readFile.nextInt(), readFile.nextInt(), readFile.nextInt(), readFile.nextInt());
    processes[a.ID - 1].list.add(a);}
 }
  
 static Scanner readFile = null;
 static int numProcesses;
 static int numResources;
 static Resource[] resources;
 static Process[] processes;
 static int time;
 static int numTerminated;
 static ArrayList < Process > blocked;
 static int abortedCount;
 static int[] numInitiatesPerProcess;

/*  This function prints the output information for Banker's Algorithm to the screen. */
public static void printBanker() {
  /* Sum up total wait time and total time overall for output */
  int total_time = 0;
  int total_wait = 0;
  float percent_wait;
  System.out.format("%20s", "Banker\n");
  for (int i = 0; i < processes.length; i++) {
   /* Only factor in total time and wait time if the process completed successfully 
    * (was not aborted) */
   if (!processes[i].isAborted) {
    total_time = total_time + processes[i].end;
    total_wait = total_wait + processes[i].waiting;
   }
   float waitPercent;
   /* If no wait time, set waitPercent to 0 to avoid dividing by 0 in calculation */
   if (processes[i].waiting == 0) {
    waitPercent = 0;
   } else {
    waitPercent = (float)(processes[i].waiting * 100) / (processes[i].end);
   }
   /* If task was aborted, print message */
   if (processes[i].isAborted) {
    System.out.println("Task " + (i + 1) + "\t" + "aborted");
   }
   /* If task completed successfully, print its end time, waiting time, and percent time spent waiting */
   else {
	//System.out.format("%s\t%d\t%2d\t%2d\t", "Task", (i+1), processes[i].end, processes[i].waiting);

    System.out.print("Task " + (i + 1) + "\t" + processes[i].end + "\t" + processes[i].waiting + "   ");
    System.out.format("%2.0f%s", waitPercent, "%");
    System.out.println();
   } //waitPercent = 0;

  }
  /* Calcualte the total percent waiting time */
  percent_wait = Math.round((total_wait * 100.0f) / total_time);

  //System.out.format("%s\t%2d\t%2d\t", "total", total_time, total_wait);
  System.out.print("total " + " \t" + total_time + "\t" + total_wait + "   ");
  System.out.format("%2.0f%s\n", percent_wait, "%");
 }

/* This function prints the claim, holding, and max resource information for each process. For 
 * debugging purposes only. */
public static void printMatrix()
{	System.out.println();
	System.out.println("\tClaim\tHas\tNeeds");
	for(int i = 0; i < numProcesses; i++){
		System.out.print("p" + (i+1) + ":\t");
		Process p = processes[i];
		Activity a = p.currActivity();
		for(int j = 0; j < numResources; j++){
			System.out.print(p.claim[j] + "\t" + p.holding[j] + "\t" + p.need[j]);
			
			
		}
		System.out.println();
		System.out.println("-----------------------------");
		
	}
	
	for(int j=0; j<numResources;j++){
		System.out.println("\ttotal: " + resources[j].total + "\n\tavail: " + resources[j].avail);
		
	}
	System.out.println();
	
	
}

/* This function checks if the future state after granting a request is safe.  */
public static boolean isSafe2(Activity a)
{
	/* boolean array to keep track of which processes can run to completion */
	boolean[] canComplete = new boolean[numProcesses];
	/* local array to store the available units for each resource */
	int[] unitsAvailOfResource = new int[numResources];
	
	/* Local 2D arrays to check the future state of processes needs without changing 
	 * any global variables */
	int[][] tempHolding = new int[numProcesses][numResources];
	int[][] tempClaim = new int[numProcesses][numResources];
	int[][] tempNeed  = new int[numProcesses][numResources];
	
	/* Initialize units avail of each resource to the current avail, and the availNext in order
	 * to check future state */
	for(int x = 0; x < unitsAvailOfResource.length; x++)
	{
		unitsAvailOfResource[x] = resources[x].avail + resources[x].availNext;
	}
	
	for(int j= 0; j < numProcesses; j++)
	{
		for(int x = 0; x < numResources; x++)
		{
			
			tempHolding[j][x] = processes[j].holding[x];
			tempNeed[j][x] = processes[j].need[x];
			tempClaim[j][x] = processes[j].claim[x];
		}
	}
	int pNum = a.ID - 1;
	int rNum = a.resourceType-1;
	int amount = a.amount;
	
	tempHolding[pNum][rNum] += amount;
	tempNeed[pNum][rNum] -= amount;
	unitsAvailOfResource[rNum] -= amount;
	/* For all processes that have already completed or aborted, set canComplete to true, false for 
	 * non terminated processes */
	for(int m = 0; m < processes.length; m++)
	{
		Process p = processes[m];
		if(p.completed || p.isAborted){canComplete[m] = true;}
		else{canComplete[m] = false;}	
	}
	int j;
	boolean newState;
	boolean canTotallyComplete;
	do
	{
		newState = false;
		canTotallyComplete = true;
		for(int i = 0; i < processes.length; i++)
		{
			/* If a process has not completed, check to see if it can run to completion */
			if(!canComplete[i])
			{
				for(j = 0; j < numResources; j++)
				{
					/* If a process needs more than is available, it can not run to completion*/
					if(tempNeed[i][j] > unitsAvailOfResource[j])
					{
						canComplete[i] = false;
						//System.out.println("\tTask " + (i+1) + " can not run to completion");
						break;
					}	
				}
				/* If a process can fulfil its needs for each resource it can run to completion and 
				 * there is a new state */			
				if(j == numResources)
				{
					
					canComplete[i] = true;
					newState = true;
					//System.out.println("\tTask " + (i+1) + " can run to completion.");
					
					for(int y = 0; y < numResources; y++)
					{
						unitsAvailOfResource[y] += tempHolding[i][y];
						tempNeed[i][y] += tempHolding[i][y];
						tempHolding[i][y] = 0;
						
					}
				}
			}	
		}
		/* If all remaining processes can run to completion, the state is safe  */
		for(int n = 0; n < canComplete.length; n++)
		{
			canTotallyComplete = canTotallyComplete && canComplete[n];		
		}
		if(canTotallyComplete)
			return true;
		/* If not all processes can complete and if there is not a new state, return false*/
		else if(!newState)
		{
			return false;
		}
	}while(newState);
	//System.out.println("something is wrong!");
   	return false;
	
	
}	
	
public static void printOptimistic() {
  /* Sum up total wait time and total time overall for output */
  int total_time = 0;
  int total_wait = 0;
  float percent_wait;
  System.out.format("%20s", "FIFO\n");
  for (int i = 0; i < processes.length; i++) {
   /* Only factor in total time and wait time if the process completed successfully 
    * (was not aborted) */
   if (processes[i].completed == true) {
    total_time = total_time + processes[i].end;
    total_wait = total_wait + processes[i].waiting;
   }
   float waitPercent;
   /* If no wait time, set waitPercent to 0 to avoid dividing by 0 in calculation */
   if (processes[i].waiting == 0) {
    waitPercent = 0;
   } else {
    waitPercent = (float)(processes[i].waiting * 100) / (processes[i].end);
   }
   /* If task was aborted, print message */
   if (processes[i].completed == false) {
    System.out.println("Task " + (i + 1) + "\t" + "aborted");
   }
   /* If task completed successfully, print its end time, waiting time, and percent time spent waiting */
   else {
	//System.out.format("%s\t%d\t%2d\t%2d\t", "Task", (i+1), processes[i].end, processes[i].waiting);

    System.out.print("Task " + (i + 1) + "\t" + processes[i].end + "\t" + processes[i].waiting + "   ");
    System.out.format("%2.0f%s", waitPercent, "%");
    System.out.println();
   } //waitPercent = 0;

  }
  /* Calcualte the total percent waiting time */
  percent_wait = Math.round((total_wait * 100.0f) / total_time);

  //System.out.format("%s\t%2d\t%2d\t", "total", total_time, total_wait);
  System.out.print("total " + " \t" + total_time + "\t" + total_wait + "   ");
  System.out.format("%2.0f%s\n", percent_wait, "%");
 }

/* This function is an optimistic resource manager. Satisfy a request if possible,
 * if not, make the task wait. When a release occurs, try to satisfy pending requests in a FIFO manner */
 public static void optimisticManager() {
  blocked = new ArrayList < Process > ();
  time = 0;
  numTerminated = 0;
  abortedCount = 0;
  //System.out.println();
  while (numTerminated < numProcesses) {
   //System.out.println("During " + (time) + " - " + (time+1));


   /* At the start of each cycle, update the units available for 
    * each resource */
   for (int i = 0; i < numResources; i++) {
    resources[i].avail += resources[i].availNext;
    resources[i].availNext = 0;
   }
   /* Check if there is a deadlock */
	if(isDeadlocked())
	{
		
		for(int i = 0; i < processes.length; i++)
		{
			if(!processes[i].completed && !processes[i].isAborted){
				processes[i].isAborted = true;
				//System.out.println("task " + (i+1) + " aborted during cycle " + time + " - " + (time+1)+ " and its resources are available at next cycle ( " + (time+1) + " - " + (time+2) + " ) " );
				numTerminated++;
				/* Keep track of the number of aborted processes -- this will effect the end times of other processes */
				abortedCount++;
				/* Remove aborted process from blocked list */
				blocked.remove(processes[i]);
				/* The aborted process releases all its resources so they are available at the next cycle */
				for(int j = 0; j < numResources; j++)
				{
					
					resources[j].availNext += processes[i].holding[j];
					processes[i].holding[j] = 0;
					
				}
				/* If deadlock still remains after aborting a process, do same steps again to see if another process
				 * needs to be aborted */
				if(isDeadlocked()){continue;}
				else{break;}
			} 
		}
	}
	

   /* Check if any processes are blocked */

   Iterator < Process > it = blocked.iterator();
   while (it.hasNext()) {
    Process p = it.next();
    Activity a = p.currActivity();
    if (a.amount <= resources[a.resourceType - 1].avail) {
     /* Request can be satisfied -- set the process to hold the requested units
      * and update the available units */
     p.holding[a.resourceType - 1] += a.amount;
     resources[a.resourceType - 1].avail -= a.amount;

     /* Increment activity number, remove process from blocked list, set 
      * justUnblocked to true */
     p.activityNum++;
     it.remove();
     p.justUnblocked = true;

    }
    /* If a request cannot be satisfied, it is waiting */
    else {
     p.waiting++;
    }

   }

   /* Loop through the activities for each process that has not
    * completed or aborted. */
   for (int i = 0; i < processes.length; i++) {
    Process p = processes[i];
    if (!p.completed && !p.isAborted) {
     if (blocked.contains(p) || p.justUnblocked == true) {
      continue;

     } else {
      Activity a = p.currActivity();
      if (a.delay > 0 && !p.justComputed) {
       p.totalDelay = a.delay;
       p.computing = true;

      }
      p.justComputed = false;
      if (p.computing) {
       p.timer++;

       if (p.timer == p.totalDelay) {
        p.timer = 0;
        p.computing = false;
        p.justComputed = true;
       }
      }


      if (a.instruction.equals("initiate")) {
       Activity initiate = p.currActivity();
       p.claim[initiate.resourceType - 1] = initiate.amount;
       p.activityNum++;
       numInitiatesPerProcess[i]++;
      }



      /* If the instruction is to request the resource, first check
       * to see if the number of units requested is less than or 
       * equal to the number of units available.  */
      else if (a.instruction.equals("request") && (!p.computing) && (!p.justComputed)) {
       /* If the requested units can be granted, add units requested
        * to units held by the process, and decrement the units available by the units requested.
        * Then increment the activity number to process the next activity.*/
       if (a.amount <= resources[a.resourceType - 1].avail) {
        p.holding[a.resourceType - 1] += a.amount;
        resources[a.resourceType - 1].avail -= a.amount;
        p.activityNum++;

       }
       /* If a request cannot be satisfied, make the task wait
        * by adding it to the blocked queue and increment its 
        * waiting time. When a release occurs, try to satisfy 
        * requests in blocked queue in FIFO manner. */
       else {
        blocked.add(p);
        p.waiting++;
       }
      }
      /* If the instruction is to release a resource, decrement the 
       * amount of units held by the process by the number of units
       * released by the current activity. incremement the number of 
       * resource units available at the next cycle by the number of 
       * units released in the current cycle */
      if (a.instruction.equals("release") && !p.computing && !p.justComputed) {
       p.holding[a.resourceType - 1] -= a.amount;
       resources[a.resourceType - 1].availNext += a.amount;
       p.activityNum++;
      }
      /* If the instruction is to terminate, set the end time for 
       * the process to the current time, and increment the number
       * of terminated processes */
      if (a.instruction.equals("terminate") && !p.computing) {
       /* If there are no aborted tasks, add the number of resources to 
        * end time to account for multiple initiate instructions. Otherwise,
        * decrement the waiting time and set the process end time to the current
        * time. Increment numTerminated and set the completed boolean for the 
        * process to true */
			if (p.justComputed == true)
				p.end = time+1;
			else if(abortedCount > 0){
				p.end = time-1;
				p.waiting--;
			}
			else {
				p.end = time;
			}
			
			numTerminated++;
			p.completed = true;
      }
     }
    }
   }


   /* increment time at end of each cycle*/
   time++;

   /* Set justUnblocked to false for all processes at 
    * the end of the cycle */
   for (int i = 0; i < processes.length; i++) {
    processes[i].justUnblocked = false;
   }
  }
 }





/* This function uses banker's algorithm of Dijkstra to do resource allocation */
public static void bankerManager()
{
	blocked = new ArrayList < Process > ();
	time = 0;
	numTerminated = 0;
	abortedCount = 0;
	System.out.println();
	while (numTerminated < numProcesses)
	{
		//System.out.println();
		//System.out.println("During " + (time) + " - " + (time+1));
		//System.out.println();
	   /* At the start of each cycle, update the units available for 
		* each resource */
	   for (int i = 0; i < numResources; i++)
	   {
		resources[i].avail += resources[i].availNext;
		resources[i].availNext = 0;
	   }
	   
	   /* Check if any processes are blocked */
	   Iterator < Process > it = blocked.iterator();
	 
	   while (it.hasNext())
	   {
			Process p = it.next();
			Activity a = p.currActivity();
			//System.out.println("First check blocked tasks.");
		
			
			/* Check if granting a request leads to a future state that is safe */
			if(!isSafe2(a))
			{	
				//System.out.println("(not safe state)");
				//System.out.println("\tTask " + (p.ID) + " 's request cannot be satisfied. it is waiting.");
				p.waiting++;
				
				
				//resources[a.resourceType-1].avail += p.holding[a.resourceType-1];
				
				p.completed = false;
												
			}
			else
			{
				//System.out.println("\tTask " + (p.ID) + " s request is granted (safe state)");
				p.holding[a.resourceType - 1] += a.amount;
				p.need[a.resourceType-1] -= a.amount;
				resources[a.resourceType - 1].avail -= a.amount;
				
				p.justUnblocked = true;
				it.remove();
				//p.waiting++;
				p.activityNum++;	
				
			}	
			
			
		}

			/* Loop through the activities for each process that has not
		* completed or aborted. */
		for (int i = 0; i < processes.length; i++) 
		{
			Process p = processes[i];
				if (!p.completed && !p.isAborted) 
				{
					if (blocked.contains(p) || p.justUnblocked == true) {
						//p.waiting++;
						continue;
						} 
					else 
					{
						Activity a = p.currActivity();
						if (a.delay > 0 && !p.justComputed)
						{
						   p.totalDelay = a.delay;
						   p.computing = true;
						   //p.waiting++;
						   //p.waiting += a.delay;
						}
						p.justComputed = false;
						if (p.computing)
						{
							//System.out.println("\tTask " + (i+1) + " delayed " +  (p.totalDelay - p.timer));
							p.timer++;
							//p.waiting++;
		  

							if (p.timer == p.totalDelay)
							{
								p.timer = 0;
								p.computing = false;
								p.justComputed = true;
								//p.waiting++;
							}
						}

						if (a.instruction.equals("initiate")) 
						{
							Activity initiate = p.currActivity();
							p.claim[initiate.resourceType - 1] = initiate.amount;
							if(p.claim[initiate.resourceType-1] > resources[initiate.resourceType-1].total)
							{
								//System.out.println("\tTask " + (i+1) + " is aborted");
								p.isAborted = true;
								p.completed = true;
								numTerminated++;
								abortedCount++;	
							}
							else
							{
								//System.out.println("\tTask " + (i+1) + " completes its initiate");
								p.need[initiate.resourceType - 1] = p.claim[initiate.resourceType-1];
								p.activityNum++;
								numInitiatesPerProcess[i]++;					
							}
							
						}

					

					  /* If the instruction is to request the resource, first check
					   * to see if the number of units requested is less than or 
					   * equal to the number of units available.  */
						else if (a.instruction.equals("request") && (!p.computing) && (!p.justComputed)) {
					   /* If the requested units can be granted, add units requested
						* to units held by the process, and decrement the units available by the units requested.
						* Then increment the activity number to process the next activity.*/
							if((a.amount > p.need[a.resourceType-1]) || (a.amount > p.claim[a.resourceType-1]))
							{
				  
								//System.out.println("\tTask " + (i+1) + " requested more than it needs -- abort");
				   
								for(int j = 0; j < numResources; j++)
								{
									resources[j].avail += p.holding[j];
									p.holding[j] = 0;					   
								}
								p.isAborted = true;
								p.completed = true;
								numTerminated++;
								abortedCount++;
								
							}  
							if ((a.amount <= resources[a.resourceType - 1].avail) && (a.amount <= p.need[a.resourceType-1]) && isSafe2(a))
						  
							{
								//System.out.println("\tTask " + (i+1) + " s request for " + a.amount + " is granted (safe state)");
								p.holding[a.resourceType - 1] += a.amount;
								p.need[a.resourceType-1] -= a.amount;
								resources[a.resourceType - 1].avail -= a.amount;
								p.activityNum++;
								
							}
							else{
									
									
									p.waiting++;
									blocked.add(p);
								
									
							}
							
							
			  
			   
						}
						
					  /* If the instruction is to release a resource, decrement the 
					   * amount of units held by the process by the number of units
					   * released by the current activity. incremement the number of 
					   * resource units available at the next cycle by the number of 
					   * units released in the current cycle */
						else if (a.instruction.equals("release") && !p.computing && !p.justComputed)
						{
							p.holding[a.resourceType - 1] -= a.amount;
							resources[a.resourceType - 1].availNext += a.amount;
							p.need[a.resourceType-1] += a.amount;
							p.activityNum++;
							//System.out.println("\tTask " + (i+1) + " releases " + a.amount + "(avail at " + (time+1) + " )");
							
						}
						/* If the instruction is to terminate, set the end time for 
						* the process to the current time, and increment the number
						* of terminated processes */
						 if (a.instruction.equals("terminate") && !p.computing)
						{
					   /* If there are no aborted tasks, add the number of resources to 
						* end time to account for multiple initiate instructions. Otherwise,
						* decrement the waiting time and set the process end time to the current
						* time. Increment numTerminated and set the completed boolean for the 
						* process to true */
							if (p.justComputed == true )
							{
								p.end = time+1; 
								//p.waiting++;
							}
							else if(abortedCount > 0)
							{
								p.end = time;
								//p.waiting--;
							}
							else 
							{
								p.end = time;
							}
							//System.out.println("\tTask " + (i+1) + " terminates at " + p.end);
							numTerminated++;
							p.completed = true;
						}
					}
				}
		}


		/* increment time at end of each cycle*/
		time++;

		/* Set justUnblocked to false for all processes at 
		* the end of the cycle */
	   for (int i = 0; i < processes.length; i++) 
	   {
		processes[i].justUnblocked = false;
	   }
		//printMatrix();
	}
 }
	
	 /* This function checks to see if there is a deadlock in the optimistic
  * resource manager */
 public static boolean isDeadlocked() {
  /* There is a deadlock if all non-terminated processes are in the 
   * blocked list */
  if (numProcesses - numTerminated == blocked.size()) {
   /* Keep track of the resources that will be available the cycle after */
   int[] resourcesNext = new int[numResources];
   for (int i = 0; i < numResources; i++) {
    resourcesNext[i] = resources[i].availNext + resources[i].avail;
   }
   /* If a process in the blocked list can be granted its request, there is no deadlock */
   for (int i = 0; i < blocked.size(); i++) {
    if (blocked.get(i).currActivity().amount <= resourcesNext[blocked.get(i).currActivity().resourceType - 1]) {
     return false;
    }

   }
   /* If request cannot be granted, there is a deadlock */
   return true;
  }
  /* If not all non-terminated processes are in the blocked list there is no deadlock */
  else {
   return false;
  }
 }
	

/* Main function takes input file as a command line argument. */
 public static void main(String[] args) {
  String fileName = null;
  fileName = args[0];
  File input = new File(fileName);
  readInput(input);
  optimisticManager();
  printOptimistic();
  readInput(input);
  bankerManager();
  printBanker();

	
 }


 /* Resource class */
 private static class Resource {
  int ID;
  int total;
  int avail;
  int availNext;

  /* Each resource object has an ID and the number of units for that resource */
  public Resource(int id, int units) {
   ID = id;
   total = units;
   avail = total;
   availNext = 0;
  }
 }

 /* Activity Class */
 private static class Activity {
  String instruction;
  int ID;
  int delay;
  int resourceType;
  int amount;

  /* Each activity object has a instruction (initiate, request, release, terminate),
   * the delay value (not used for initiate; represents the number of cycles between 
   * the completion of the previous activity and the beginning of the current activity) */
  public Activity(String instruction, int ID, int delay, int resourceType, int amount) {
   this.instruction = instruction;
   this.ID = ID;
   this.delay = delay;
   this.resourceType = resourceType;
   this.amount = amount;
  }
 }
 /* Process class */
 private static class Process {
  
  int ID;
  ArrayList < Activity > list;
  int[] claim;
  int[] need;
  int timer = 0;
  int activityNum;
  int[] holding;
  int start = 0;
  int end;
  boolean completed;
  int waiting;
  boolean justUnblocked;
  boolean isAborted;
  int totalDelay;
  boolean computing;
  boolean justComputed;
  /* Each process object has an ID */
  public Process(int ID) {
    this.ID = ID;
    timer = 0;
    claim = new int[numResources];
    list = new ArrayList < Activity > ();
    activityNum = 0;
    holding = new int[numResources];
    completed = false;
    waiting = 0;
    justUnblocked = false;
    end = 0;
    isAborted = false;
    totalDelay = 0;
    computing = false;
    justComputed = false;
    need = new int[numResources];
   }
   /* Function returns the current activity object */
  public Activity currActivity() {
   return list.get(activityNum);

  }

 }
}



