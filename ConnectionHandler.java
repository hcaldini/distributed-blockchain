
/**
 * 
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

	ServerSocket ss;

	BCNode bc;

	public ConnectionHandler(BCNode bc, int port) {
		try {
			ss = new ServerSocket(port);
			this.bc = bc;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		while (true) {
			// System.out.println("Waiting for a call");
			Socket s;
			ObjectInputStream ois;
			ObjectOutputStream oos;
			try {
				s = ss.accept();
				// System.out.println("Accepted");

				ois = new ObjectInputStream(s.getInputStream());
				oos = new ObjectOutputStream(s.getOutputStream());
				bc.oosListModify(oos, true); 

				
				Thread t = new Thread(new ReadHandler(bc, ois, oos));
				t.start();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
