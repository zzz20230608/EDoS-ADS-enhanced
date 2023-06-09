package org.cloudbus.cloudsim.information;

public class ServerInformation {
	/** The  initiation time */
	private double initiationTime;
	
	/** The  queueing time */
	private double queueingTime;
	
	/** The  processing time */
	private double processingTime;
	
	/** The  finish time */
	private double finishingTime;
	
	/** the number of requests served */
	private int requestsServed;
	
	public ServerInformation() {
		initiationTime = 0.0;
		queueingTime = 0.0;
		processingTime = 0.0;
		finishingTime = -1;
		requestsServed = 0;
	}
	
	/**
	 * set the initiation time.
	 * 
	 */
	public void setInitiationTime(double time) {
		initiationTime = time;
	}
	
	/**
	 * get the initiation time.
	 * 
	 */
	public double getInitiationTime() {
		return initiationTime;
	}
	
	/**
	 * update the queueing Time.
	 * 
	 */
	public void updateQueueingTime(double time) {
		queueingTime += time;
	}
	
	/**
	 * get the queueing Time.
	 * 
	 */
	public double getQueueingTime() {
		return queueingTime;
	}
	
	/**
	 * update the processing time.
	 * 
	 */
	public void updateProcessingTime(double time) {
		processingTime += time;
	}
	
	/**
	 * get the processing time.
	 * 
	 */
	public double getProcessingTime() {
		return processingTime;
	}
	
	/**
	 * set the finish time.
	 * 
	 */
	public void setFinishingTime(double time) {
		finishingTime = time;
	}
	
	/**
	 * get the finish time.
	 * 
	 */
	public double getFinishingTime() {
		return finishingTime;
	}
	
	/**
	 * update the requests served.
	 * 
	 */
	public void updateRequestsServed() {
		requestsServed += 1;
	}
	
	/**
	 * get the requests served.
	 * 
	 */
	public int getRequestsServed() {
		return requestsServed;
	}
}
