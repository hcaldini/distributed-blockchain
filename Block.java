import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Block implements Serializable { 

	// HEADER INFORMATION
	private long timestamp;
	private String previousHash;
	private String hash;
	private int nonce;
	// DATA
	private String data; 

	// default constructor - Genesis Block
	public Block() {
		this.timestamp = new Date().getTime();
		this.nonce = 0;
		this.data = "Genesis Block";

		this.previousHash = "";
		this.calculateBlockHash();
	}

	public Block(String data) {
		this.timestamp = new Date().getTime();
		this.nonce = 0;
		this.data = data;
		this.previousHash = "";
	}

	public String calculateBlockHash() {

		String instanceVarData = "" + previousHash + Long.toString(timestamp) + Integer.toString(nonce) + data;

		try {
			MessageDigest myDigest = MessageDigest.getInstance("SHA-256");

			byte[] hashBytes = myDigest.digest(instanceVarData.getBytes("UTF-8"));

			StringBuffer buffer = new StringBuffer();
			for (byte b : hashBytes) {
				buffer.append(String.format("%02x", b));
			}
			this.hash = buffer.toString();
			return hash;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";

	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	@Override
	public String toString() {
		
		return "Block{" + timestamp + ", " + data + "}";
	}

}
