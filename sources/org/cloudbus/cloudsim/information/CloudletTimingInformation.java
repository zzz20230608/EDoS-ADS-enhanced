package org.cloudbus.cloudsim.information;

public class CloudletTimingInformation {
	/** The  arriving time */
	private double arrivingTime;
	
	/** The  queueing time */
	private double queueingTime;
	
	/** The  processing time */
	private double processingTime;
	
	/** The  departure time */
	private double departuringTime;
	
	public CloudletTimingInformation() {
		arrivingTime = -1;
		queueingTime = 0.0;
		processingTime = 0.0;
		departuringTime = -1;
	}
	
	/**
	 * set the arriving time.
	 * 
	 */
	public void setArrivingTime(double time) {
		arrivingTime = time;
	}
	
	/**
	 * get the arriving time.
	 * 
	 */
	public double getArrivingTime() {
		return arrivingTime;
	}
	
	/**
	 * set the queueing Time.
	 * 
	 */
	public void setQueueingTime(double time) {
		queueingTime = time;
	}
	
	/**
	 * get the queueing Time.
	 * 
	 */
	public double getQueueingTime() {
		return queueingTime;
	}
	
	/**
	 * set the processing time.
	 * 
	 */
	public void setProcessingTime(double time) {
		processingTime = time;
	}
	
	/**
	 * get the processing time.
	 * 
	 */
	public double getProcessingTime() {
		return processingTime;
	}
	
	/**
	 * set the departure time.
	 * 
	 */
	public void setDeparturingTime(double time) {
		departuringTime = time;
	}
	
	/**
	 * get the departure time.
	 * 
	 */
	public double getDeparturingTime() {
		return departuringTime;
	}
}
