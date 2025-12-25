import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.Socket;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BCNode {

	ArrayList<Block> chain;
	final int N = 5; // number of leading 0's required in hash for PoW

	ArrayList<ObjectOutputStream> oosList; 

	List<Socket> socketList;

	public BCNode(int port, List<Integer> remotePorts)
			throws UnknownHostException, IOException, ClassNotFoundException {

		chain = new ArrayList<Block>();

		oosList = new ArrayList<ObjectOutputStream>();

		socketList = new ArrayList<Socket>();

		if (remotePorts.size() == 0) {

			chain.addFirst(new Block());
		} else {
			for (Integer pNum : remotePorts) {

				Socket sock = new Socket("localhost", pNum);

				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream()); 

				chain = (ArrayList<Block>) ois.readObject();

				Thread t = new Thread(new ReadHandler(this, ois, oos));
				t.start();

				socketList.add(sock);

				oosList.add(oos);

			}

		}
		

		Thread connectionT = new Thread(new ConnectionHandler(this, port)); // Last Step, set up a connectionHandler for
																			// port
		connectionT.start();

	}

	public synchronized void addBlock(Block b) throws CorruptedBlockException, IOException {
		// System.out.println("---Add Block Method Called---");

		// To decide if String has N leading 0's we make a string with N leading 0s called prefixZeros
		String prefixZeros = new String(new char[N]).replace('\0', '0');

		String prevHash = chain.getLast().getHash();

		b.setPreviousHash(prevHash);
		String newHash = b.calculateBlockHash();

		while (!newHash.substring(0, N).equals(prefixZeros)) { // use string comparison to see if the hash's first N
																// characters match this prefixZeros, loop and mine
																// until it does match,

			b.setNonce(b.getNonce() + 1);
			newHash = b.calculateBlockHash();

		} 
			
		b.setHash(newHash);

		validateBlock(b); 

	}

	public synchronized void validateBlock(Block b) throws CorruptedBlockException, IOException {

		// System.out.println("---Chain Validation Method Called---");

		
		for (int i = 0; i < (chain.size() - 1); i++) {

			if (!chain.get(i).getHash().equals(chain.get(i + 1).getPreviousHash())) { // compare current blocks hash to
																						// next block in chain prev's
																						// hash
				throw new CorruptedBlockException(
						"Corrupted Block found in chain during validation\n" + chain.get(i + 1).toString());
			}
		}

		
		if (chain.getLast().getHash().equals(b.getPreviousHash())) {
			chain.add(b);
			shareBlock(b);
		}

	}

	public synchronized void oosListModify(ObjectOutputStream oos, boolean add) throws IOException { // Do not want
																										// changes made
																										// to oosList
																										// while we are
																										// removing/adding
		if (add) {
			// System.out.println("Adding oos to list");
			oos.writeObject(chain);
			oosList.add(oos);
		} else {
			// System.out.println("Removing oos from list");
			oosList.remove(oos);
		}

	}
	/*
	 * shareBlock is not synchronized because it is only called within validateBlock, 
	 * which is Synchronized
	 */
	public void shareBlock(Block b) throws IOException {

		for (ObjectOutputStream oos : oosList) { 
			try {
				oos.writeObject(b);
				oos.reset();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	@Override
	  public String toString() { String out = "Chain: \n"; for (Block bn : chain) {
	  out += bn.toString() + "\n"; }
	  
	  return out;
	  
	  }
	 

	public static void main(String[] args)
			throws CorruptedBlockException, UnknownHostException, IOException, ClassNotFoundException {
		Scanner keyScan = new Scanner(System.in);

		// Grab my port number on which to start this node
		System.out.print("Enter port to start (on current IP): ");
		int myPort = keyScan.nextInt();

		// Need to get what other Nodes to connect to
		System.out.print("Enter remote ports (current IP is assumed): ");
		keyScan.nextLine(); // skip the NL at the end of the previous scan int
		String line = keyScan.nextLine();
		List<Integer> remotePorts = new ArrayList<Integer>();
		if (line != "") {
			String[] splitLine = line.split(" ");
			for (int i = 0; i < splitLine.length; i++) {
				remotePorts.add(Integer.parseInt(splitLine[i]));
			}
		}
		
		BCNode n = new BCNode(myPort, remotePorts);

		String ip = "";
		try {
			ip = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Node started on " + ip + ": " + myPort);

		// Node command line interface
		while (true) {
			System.out.println("\nNODE on port: " + myPort);
			System.out.println("1. Display Node's blockchain");
			System.out.println("2. Create/mine new Block");
			System.out.println("3. Kill Node");
			System.out.print("Enter option: ");
			int in = keyScan.nextInt();

			if (in == 1) {
				System.out.println(n); // I did my own toString method so I could see the data in each block

			} else if (in == 2) {
				// Grab the information to put in the block
				System.out.print("Enter information for new Block: ");
				String blockInfo = keyScan.next();
				Block b = new Block(blockInfo); // when block is added from user interface, mine, if we get block from
												// socket, it is already mined,
				n.addBlock(b);

			} else if (in == 3) {
				// Take down the whole virtual machine (and all the threads)
				keyScan.close();
				System.exit(0); // takes down VM and all threads in VM
			}
		}
	}

}
