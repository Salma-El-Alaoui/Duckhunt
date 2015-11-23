


### Compile
javac *.java

### Run
The agent can be run in two different modes:

1. Server - act as the judge by sending predefined observations one at a time and asking the client to respond 

2. Client - get observations from standard input and output actions to standard output (this is the default mode)

The server and client can be run in separate terminals and communicate through pipes.
 
* Terminal 1:
 ```
 java Main verbose server < player2server > server2player
 ```

* Terminal 2:
 ```
 java Main verbose > player2server < server2player
 ```
