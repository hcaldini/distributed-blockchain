
/**
 * 
 */

/**
 *
 * 
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ReadHandler implements Runnable {

	ObjectInputStream ois; 
	ObjectOutputStream oos; 
	BCNode bc;

	public ReadHandler(BCNode bc, ObjectInputStream ois, ObjectOutputStream oos) {

		this.ois = ois;
		this.oos = oos;
		this.bc = bc;

	}

	@Override
	public void run() {

		try {
			while (true) {

				Block b = (Block) ois.readObject(); 
				
				bc.validateBlock(b);

			}
		} catch (ClassNotFoundException | IOException | CorruptedBlockException e) {

			try {
				bc.oosListModify(oos, false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

}
