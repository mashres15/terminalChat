# terminalChat
Maniz Shrestha CS410 Project
Readme
File Roster
There are two java files for this project: ChatServer.java and MultiThreadChatClient.java. The
ChatServer has two classes ChatServer and ClientThread. ChatServer implements the main
method that runs the server at a specified port number. The client thread implements threads
for all the client that are willing to connect given that the maximum client limit is not exceeded.
The MultiThreadChatClient runs the client and it uses thread to give the chat client the ability to
type as well as receive message simultaneously.
There is also Makefile that is used to compile the program.
Running the program
 Using makefile
o Type make all in bash to compile the program.
o Type make clean to delete the complied files.
 Running the Server
o Type java ChatServer portNumber
For e.g. ChatServer 9715
 Running the Client
o Type java MultiThreadChatClient address portNumber
For e.g. java MultiThreadChatClient cluster.earlham.edu 9715
Functionality
*** List of commands ***
/USER NAME : Change username. E.g. /USER Carl changes name to Carl
/POST MESSAGE : Send Message to all users. e.g. /POST hi
/WHO : List of users"
/QUIT : Exit chat
@USER MESSAGE : Send private message to USER. For e.g. @Maniz Private msg.
Note - Command starts with /. By default, text without / are treated as message, i.e. text: hello
is treated as /POST hello.