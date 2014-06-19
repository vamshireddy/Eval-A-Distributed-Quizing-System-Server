package com.example.peerbased;
import java.io.Serializable;


public class Packet implements Serializable{
	static final long serialVersionUID = 42L;
	int seq_no; 			// This will be the packet identifier
	byte type;
	boolean auth_packet;	// This flag will be set, if the packet is used for authentication of student or teacher
	boolean bcast;			// This flag will be set, if the packet is a broadcast packet . Usually set by the sender
	boolean probe_packet;	// Used when the packet is used for probing the status of the android devices (students)
	boolean param_packet;
	boolean leader_req_packet;
	boolean team_selection_packet;
	boolean group_name_selection_packet;
	boolean quizPacket;
	boolean ack;
	byte[] data;			// This holds a serialized object of the class according to the flags set above.

	private Packet()
	{
		// Initialize the fields to defaults
		ack = false;
		type = -1;
		seq_no = 0;
		auth_packet = false;
		bcast = false;
		probe_packet = false;
		data = null;
		param_packet = false;
		leader_req_packet = false;
		team_selection_packet  = false;
		group_name_selection_packet = false;
	}
	public Packet(int seq_no, boolean auth, boolean bcast, boolean probe, byte[] data) {
		this();
		this.seq_no = seq_no;
		this.auth_packet = auth;
		this.bcast = bcast;
		this.probe_packet = probe;
		this.data = data;
	}
	public Packet(int seq_no, boolean auth, boolean bcast, boolean probe, byte[] data, boolean param_pack) {
		this(seq_no, auth, bcast, probe, data);
		this.param_packet = param_pack;
	}
	public Packet(int seq_no, boolean auth, boolean bcast, boolean probe, byte[] data, boolean param_pack, boolean leader_pack)
	{
		this(seq_no, auth, bcast, probe, data, param_pack);
		this.leader_req_packet = leader_pack;
	}
	public Packet(int seq_no, boolean auth, boolean bcast, boolean probe, byte[] data, boolean param_pack, boolean leader_pack, 
				boolean team_Sel, boolean grp_name_req)
	{
		this(seq_no, auth, bcast, probe, data, param_pack, leader_pack);
		team_selection_packet = team_Sel;
		group_name_selection_packet = grp_name_req;
	}
}
