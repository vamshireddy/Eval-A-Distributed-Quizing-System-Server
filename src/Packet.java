import java.io.Serializable;


public class Packet implements Serializable{
	static final long serialVersionUID = 42L;
	int seq_no; 			// This will be the packet identifier
	int ack_no;				// This will only be set when the packet is used as acknowledgement.
							// When this field is set, all the other fields should not be set or used
	boolean auth_packet;	// This flag will be set, if the packet is used for authentication of student or teacher
	boolean bcast;			// This flag will be set, if the packet is a broadcast packet . Usually set by the sender
	boolean probe_packet;	// Used when the packet is used for probing the status of the android devices (students)
	byte[] data;			// This holds a serialized object of the class according to the flags set above.

	private Packet()
	{
		// Initialize the fields to defaults
		seq_no = 0;
		auth_packet = false;
		bcast = false;
		probe_packet = false;
		data = null;
	}
	public Packet(int seq_no, boolean auth, boolean bcast, boolean probe) {
		this.seq_no = seq_no;
		this.auth_packet = auth;
		this.bcast = bcast;
		this.probe_packet = probe;
	}
	public Packet(int ack_no)
	{
		// This constructor is used for creating the Ack packet
		this();
		this.ack_no = ack_no;
	}
}
