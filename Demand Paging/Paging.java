import java.io.*;
import java.util.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
public class Paging
{
	//static int DEBUG;
	static Scanner reader = null;
	static int QUANTUM = 3;
	static int machineSize;		// machine size
	static int pageSize;		// page size
	static int processSize;		// process size
	static int jobMix;			// job mix number
	static int numReferences;	// number of references per process
	static String algo; 		// page replacement algorithm
	static int numProcesses;	// total number of processes
	static int numCompletedP;
	static FrameTableEntry[] frameTable;
	static ArrayList<Process> processList = new ArrayList<Process>();
	static int timer;
	static double runningAvg;

public static void main(String[] args)
{
	machineSize = Integer.parseInt(args[0]);
	pageSize = Integer.parseInt(args[1]);
	processSize = Integer.parseInt(args[2]);
	jobMix = Integer.parseInt(args[3]);
	numReferences = Integer.parseInt(args[4]);
	algo = args[5];
	//DEBUG = Integer.parseInt(args[6]);

	printInput();
	
	try{
	reader = new Scanner(new File("random-numbers.txt"));
	}catch(FileNotFoundException e){
		System.out.println("Not found");
	}	
	
	if(jobMix == 1)
	{
		processList.add(new Process(1, 1.0, 0.0, 0.0));
		
	}
	else if(jobMix == 2)
	{
		processList.add(new Process(1, 1.0, 0.0, 0.0));
		processList.add(new Process(2, 1.0, 0.0, 0.0));
		processList.add(new Process(3, 1.0, 0.0, 0.0));
		processList.add(new Process(4, 1.0, 0.0, 0.0));
		
	}
	else if(jobMix == 3)
	{
		processList.add(new Process(1, 0.0, 0.0, 0.0));
		processList.add(new Process(2, 0.0, 0.0, 0.0));
		processList.add(new Process(3, 0.0, 0.0, 0.0));
		processList.add(new Process(4, 0.0, 0.0, 0.0));
		
	}
	else if(jobMix == 4)
	{
		processList.add(new Process(1, 0.75, 0.25, 0.0));
		processList.add(new Process(2, 0.75, 0.0, 0.25));
		processList.add(new Process(3, 0.75, 0.125, 0.125));
		processList.add(new Process(4, 0.5, 0.125, 0.125));
		
	}
	numProcesses = processList.size();
	numCompletedP = 0;
	timer = 0;
	int q = 0;
	int numFrames = machineSize/pageSize;
	frameTable = new FrameTableEntry[numFrames];
	int numFreeFrames = numFrames;
	
	while(numProcesses != numCompletedP)
	{
		for(int i = 0; i < processList.size(); i++)
		{
			Process p = processList.get(i);
			for(int j = 0; j < QUANTUM; j++)
			{
				timer++;
				if(p.N != 0)
				{
					//System.out.print(p.ID + " references word " + p.currWord + " (page " + p.pageNum + " )  at time " + timer + ": ");
					int frame = frameTableContainsPage(p, p.pageNum);
						
					if((frame == -1) && !isFull()) // means that entry is not in frame table
					{
						p.numFaults++;
							
						//System.out.println("Fault, using free frame " + addToFrameTable(p, p.pageNum ) + "\n");
						addToFrameTable(p, p.pageNum );
						
						numFreeFrames--;
						//frameTable(p, p.pageNum).setArrivalTime(timer);
						//p.setArrivalTime(timer);
					}
					else if(frame != -1)
					{
						//System.out.println("Hit in frame " + frame);
						frameTable[frame].timeOfLastReference = timer;
						
					}	
							
							
					else 
					{	
						
						int frameToEvict = -1;

						if(algo.equals("lru"))
						{
							frameToEvict = LRU();
							//System.out.println("Fault, Evicting page " +frameTable[frameToEvict].pageNum + " from frame " + frameToEvict); 
							
						}
						else if(algo.equals("fifo"))
						{
							frameToEvict = FIFO();
							//System.out.println("Fault, Evicting page " + frameTable[frameToEvict].pageNum + " from frame " + frameToEvict); 
							
						}
						
						else if(algo.equals("random"))
						{
							frameToEvict = RANDOM();
							//System.out.println("Fault, Evicting page " + frameTable[frameToEvict].pageNum + " from frame " + frameToEvict); 

						}
						replaceEntry(p, p.pageNum, frameToEvict);
					
					}
				}	
				p.N--;
				p.setNextWord();
				if(p.N == 0)
				{
					numCompletedP++;
					break;
				}	
					
			}
		}
	}
	System.out.println();
	displayOutput();	
}

	

public static int addToFrameTable(Process proc, int page)
{
	for(int i = frameTable.length-1; i>=0; i--)
	{
		if(frameTable[i] == null){
			frameTable[i] = new FrameTableEntry(proc, page);
			frameTable[i].arrivalTime = timer;
			frameTable[i].resStart = timer;
			frameTable[i].setArrivalTime(timer);
			return i;
			
		}
	}
	return -1;

}
	
public static void replaceEntry(Process proc, int page, int frame)
{
	
	FrameTableEntry old = frameTable[frame];
	
	old.p.resTime += (timer - old.arrivalTime);
	old.resEnd = timer;
	old.p.numEvictions++;
	frameTable[frame] = new FrameTableEntry(proc, page);
	frameTable[frame].p.numFaults++;
	frameTable[frame].resStart = timer;
	frameTable[frame].setResStart(timer);
	frameTable[frame].arrivalTime = timer;

}
public static boolean isFull()
{
	for(int i = 0; i < frameTable.length; i++)
	{
		if(frameTable[i] == null)
			return false;
		
	}
	return true;
	
}
public static int frameTableContainsPage(Process proc, int pageNum)
{
	for(int i = 0; i < frameTable.length; i++)
	{
		if(frameTable[i] == null){continue;}
		else
		{
			if((frameTable[i].p == proc) && (frameTable[i].pageNum == pageNum))
			{
				
				return i;
			}
		}	
	}
	return -1;

}
	

public static int FIFO()
{
	int entryToEvict = -1;
	int latestRef = Integer.MAX_VALUE;
	for(int i = 0; i < frameTable.length; i++)
	{
		if(frameTable[i].arrivalTime < latestRef)
		{
			latestRef = frameTable[i].arrivalTime;
			entryToEvict = i;
		}	
	}
	
	return entryToEvict;
	
	
	
}

public static int RANDOM()
{
	int r = reader.nextInt();
	return r % frameTable.length;
	
}
public static int LRU()
{
	int entryToEvict = -1;
	int earliestRef = Integer.MAX_VALUE;
	
	
	for(int i = 0; i < frameTable.length; i++)
	{
		if(frameTable[i].timeOfLastReference < earliestRef)
		{
			earliestRef = frameTable[i].timeOfLastReference;
			entryToEvict = i;
			
		}	
	}
	
	return entryToEvict;
}

public static void displayOutput()
{
	int faultTot = 0;
	int evictTot = 0;
	double indivRes = 0.0;
	runningAvg = 0.0;
	for(int i = 0; i < processList.size(); i++)
	{
		Process p = processList.get(i);
		//double indivRes = 0.0;
		if(p.numEvictions == 0)
		{
			System.out.println("Process " + p.ID + " had " + p.numFaults + " faults. With no evictions, average residency is undefined.\n" );
			faultTot += p.numFaults;
			
			
			
		}

		else
		
		{
			indivRes = (double)p.resTime / p.numEvictions;
			System.out.println("Process " + p.ID + " had " + p.numFaults + " faults, with " + indivRes + " average residency.");

			evictTot += p.numEvictions;
			faultTot += p.numFaults;
			runningAvg += p.resTime;
			

		}
	}
	if(evictTot == 0){
		
		System.out.println("\nThe total number of faults is " + faultTot + " faults. average residency is undefined. \n");

	}
	else
	{
		runningAvg = (double)(runningAvg / evictTot) ;
		System.out.println("\nThe total number of faults is " + faultTot + " and the overall average residency is " + runningAvg + "\n");
	}
	
	
	
	
}

	
public static void printInput()
{
	System.out.println("\nThe machine size is " + machineSize);
	System.out.println("The page size is " + pageSize);
	System.out.println("The process size is " + processSize);
	System.out.println("The job mix number is " + jobMix);
	System.out.println("The number of references per process is " + numReferences);
	System.out.println("The replacement algorithm is " + algo + "\n");	
	
}
private static class Process
{
	public double A;
	public double B;
	public double C;
	public int ID;
	public int currWord;
	public int nextWord;
	public int N;
	public int S;
	public int pageNum;
	public int numFaults = 0;
	public int numEvictions;
	public int resTime;
	public int TOTAL_RESIDENCY;

	
	public Process(int ID, double A, double B, double C){
		this.ID = ID;
		this.A = A;
		this.B = B;
		this.C = C;
		this.S = processSize;
		N = numReferences;
		numFaults = 0;
		numEvictions = 0;
		resTime = 0;
		currWord = (111 * this.ID) % this.S;
		pageNum = currWord/pageSize;
	}
	

	public void setNextWord()
	{
		int r = reader.nextInt();
		double y = r / (Integer.MAX_VALUE + 1d);
		if(y < A)
			nextWord = (currWord + 1) % S;	
		else if(y < (A+B))
			nextWord = (currWord - 5 + S) % S;
		else if(y < (A+B+C))
			nextWord = (currWord + 4) % S;
		else{
			nextWord = reader.nextInt();
			//if(DEBUG == 1)System.out.println(this.ID + " uses random number: " + nextWord);
			nextWord = nextWord % S;
			
		}	
		
		currWord = nextWord;	
		this.pageNum = currWord / pageSize;	
		//if(DEBUG ==1)
			//System.out.println(this.ID + " uses random number: " + r);

	}
	
	
	
}

private static class FrameTableEntry
{
	Process p = null;
	int pageNum;
	int timeOfLastReference;
	int arrivalTime;
	int resStart;
	int resEnd;

	public FrameTableEntry(Process p, int pageNum)
	{
		this.p = p;
		this.pageNum = pageNum;
		this.timeOfLastReference = timer;	
		this.arrivalTime = arrivalTime;
		this.resStart = arrivalTime;
	}
	
	

	public void setArrivalTime(int arrivalTime)
	{
		this.arrivalTime = arrivalTime;
		
	}
	
	public int getResEnd()
	{
		return this.resEnd;
		
	}
	
	public void setResEnd(int end)
	{
		this.resEnd = end;
		
	}
	public void setResStart(int start)
	{
		this.resStart = start;
		
	}

	
	public int getResStart()
	{
		return this.resStart;
	}
	
	
	
	
	
	
}

	







}
