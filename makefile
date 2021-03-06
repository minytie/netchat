all: 
	javac p2p.java
	javac Command.java
	javac PeerTopology.java
	javac NetworkUtil.java
	javac ParseFile.java
	javac TcpSocket.java
	javac TcpSocketController.java

clean:
	rm -f p2p.class
	rm -f Command.class
	rm -f PeerTopology*.class
	rm -f NetworkUtil.class
	rm -f ParseFile.class
	rm -f TcpSocket.class
	rm -f TcpSocketController.class

upload:
	scp -r * mxt497@eecslab-10.case.edu:~/p2p
	scp -r * mxt497@eecslab-11.case.edu:~/p2p
	scp -r * mxt497@eecslab-12.case.edu:~/p2p
	scp -r * mxt497@eecslab-13.case.edu:~/p2p
	scp -r * mxt497@eecslab-14.case.edu:~/p2p
	scp -r * mxt497@eecslab-15.case.edu:~/p2p
