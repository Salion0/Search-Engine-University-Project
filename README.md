# SEARCH ENGINE
## Introduction
The project revolves around the building of a Search Engine. the first phase carried out in this project is the indexing phase, which allows to construct the necessary data structures for the second phase, the query processing phase.
Here the user queries are processed and the most relevant documents for each query are returned to the user.

## Program execution
To execute the program, the project has to be compiled using JDK-21 (https://www.oracle.com/it/java/technologies/downloads/#java21) and Maven (https://maven.apache.org/download.cgi). The path for the  file where the **main** is located is the following: src\main\java\it\unipi\mircv\CommandLineInterface.java.

Commands to download, compile and run the program:
1.   git clone https://github.com/Salion0/Search-Engine-University-Project
2.   cd Search-Engine-University-Project
3.   mvn clean package -Dmaven.test.skip\=true
4.   java -jar target/SearchEngine-1.0.0-SNAPSHOT-jar-with-dependencies.jar  

## Program Functionalities
The program is able to carry out the following procedures:
*   Indexing of a given collection
*   Process queries once the index data structure is built

Both of them can be executed with different options that can be set by the user:
*   CompressedReading:
    *   TRUE: read the collection file in tar.gz format
    *   FALSE: read the collection file in tsv format
*   StopWord removal:
    *   TRUE: removes the stopwords
    *   FALSE: doesn't remove stopwords
*   Stemming:
    *   TRUE: Stemming is carried out on words
    *   FALSE: Stemming isn't taken into account
*   Compression:
    *   TRUE: enables compression
    *   FALSE: compression isn't used
*   ScoreType:
    *   BM25: use BM25 as the score function
    *   TFIDF: use TFIDF as the score function
*   QueryProcessType:
    *   DISJUNCTIVE_DAAT: process queries in Disjunctive mode
    *   CONJUNCTIVE_DAAT: process queries in Conjunctive mode
    *   DISJUNCTIVE_MAXSCORE: process queries in Disjunctive mode using MaxScore optimization


## Command Line Interface
The system provides to the user a simple command line interface through which he/she can interact with the Search Engine. When it's launched, a list of commands is shown to the user together with the possible parameters to set:

*   **settings (-c)** --> shows the settings and allows the user to change them according to their preferences. So this command is the one to use to change the flags for the Search Engine
*   **index [file_name]** --> create a new index on [file_name] collection. The collection file must be inside the program folder. The program will remove the previous index if present. Here the flag for the CompressedReading option can be set.
*   **query** --> after this command is entered the program will wait for a query in the next line. The system then returns the most 10 relevant document
*   **exit** --> shut down the program
*   **help** --> prints the explanaitions of the commands
