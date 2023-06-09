package org.cloudbus.cloudsim.database;

public class Record {
	/** The IP number */
	private String IP;
	
	/** The port number */
	private int SP;
	
	/** The status */
	private String Status;
		
	/** The Trust Factor value */
	private double TF;
	
	/** The Last Seen Time */
	private int LastSeen;
	
	/** The requests count */
	private int RequestsCount;
	
	/** The Previous Trust Factor value */
	private double PTF;
	
	/** The Redirected */
	private boolean Redirected;
	
	/** The next allowable time */
	private double NextAllowableTime;
	
	public Record(String IP, int SP, String Status, double TF, int LastSeen, int RequestsCount, double PTF, boolean Redirected, double NextAllowableTime) {
		this.IP = IP;
		this.SP = SP;
		this.Status = Status;
		this.TF = TF;
		this.LastSeen = LastSeen;
		this.RequestsCount = RequestsCount;
		this.PTF = PTF;
		this.Redirected = Redirected;
		this.NextAllowableTime = NextAllowableTime;
	}
	
	/**
	 * set the IP number.
	 * 
	 */
	public void setIP(String IP) {
		this.IP = IP;
	}
	
	/**
	 * get the IP number.
	 * 
	 */
	public String getIP() {
		return IP;
	}
	
	/**
	 * set the port number.
	 * 
	 */
	public void setSP(int SP) {
		this.SP = SP;
	}
	
	/**
	 * get the port number.
	 * 
	 */
	public int getSP() {
		return SP;
	}
	
	/**
	 * set the status.
	 * 
	 */
	public void setStatus(String Status) {
		this.Status = Status;
	}
	
	/**
	 * get the status.
	 * 
	 */
	public String getStatus() {
		return Status;
	}
	
	/**
	 * set the TF.
	 * 
	 */
	public void setTF(double TF) {
		this.TF = TF;
	}
	
	/**
	 * get the TF.
	 * 
	 */
	public double getTF() {
		return TF;
	}
	
	/**
	 * set the Last Seen.
	 * 
	 */
	public void setLastSeen(int LastSeen) {
		this.LastSeen = LastSeen;
	}
	
	/**
	 * get the Last Seen.
	 * 
	 */
	public int getLastSeen() {
		return LastSeen;
	}
	
	/**
	 * set the Requests Count.
	 * 
	 */
	public void setRequestsCount(int RequestsCount) {
		this.RequestsCount = RequestsCount;
	}
	
	/**
	 * get the Requests Count.
	 * 
	 */
	public int getRequestsCount() {
		return RequestsCount;
	}
	
	/**
	 * set the PTF.
	 * 
	 */
	public void setPTF(double PTF) {
		this.PTF = PTF;
	}
	
	/**
	 * get the PTF.
	 * 
	 */
	public double getPTF() {
		return PTF;
	}
	
	/**
	 * set the Redirected.
	 * 
	 */
	public void setRedirected(boolean Redirected) {
		this.Redirected = Redirected;
	}
	
	/**
	 * get the Redirected.
	 * 
	 */
	public boolean getRedirected() {
		return Redirected;
	}
	
	/**
	 * set the Next Allowable Time.
	 * 
	 */
	public void setNextAllowableTime(double NextAllowableTime) {
		this.NextAllowableTime = NextAllowableTime;
	}
	
	/**
	 * get the Next Allowable Time.
	 * 
	 */
	public double getNextAllowableTime() {
		return NextAllowableTime;
	}
}
