Hi,

In order to run both applications; please use the following run formats outlined below.
There is also validation to check whether the received arguments are correct. The default parameters is the loopback address 127.0.0.1 and port 8080.

javac SoundClient.java
javac SoundServer.java


--------------------------------------------------------------
Running
--------------------------------------------------------------
Client:
java SoundClient <ip-address> <port>
java SoundClient <ip-address>
java SoundClient <port>
java SoundClient

Server:
java SoundServer <port>
java SoundServer