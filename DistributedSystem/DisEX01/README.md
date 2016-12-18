## Introduction to RPC

### RPC
Remote Procedure Call: In distributed computing a remote procedure call (RPC) is when a computer program causes a procedure (subroutine) to execute in another address space (commonly on another computer on a shared network), which is coded as if it were a normal (local) procedure call, without the programmer explicitly coding the details for the remote interaction.

### Requirements
Design a distributed application which generates and dispalys current time in several machines, and 
* One server object/procedure/node generates the current time and displays it on local machines;
* Other two/more client objects/procedures/nodes get tiem to be displayed from the server, and display it in digital mode/per second or analogue mode/per minute.

Also handle
* The communication delay between client and server;
* Only authorized clients could get time from server.

### For Windows
Folder DateClient and DateServer are on windows 10, VS community 2015.
