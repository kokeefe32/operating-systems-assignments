Kayli O’Keefe
Lab4: Demand Paging

*** Ensure that the file “random-numbers.txt” is in the same directory as the Paging.java file — MUST INCLUDE .txt EXTENSION ***

To Compile:
type: javac Paging.java


To Execute:
type: java Paging arg1 arg2 arg3 arg4 arg5 arg6


Arguments:
arg1:
	- type = integer
	- value = machine size (in words)
arg2:
	- type = integer
	- value = page size (in words)
arg3:	
	- type = integer
	- value = size of each process 
arg4:
	- type = integer
	- value = job mix  
arg5:
	- type = integer
	- value = number of references for each process
arg6:
	- type = string
	- 3 options — case sensitive:
		-  “lru”
		-  “fifo”
		-  “random”