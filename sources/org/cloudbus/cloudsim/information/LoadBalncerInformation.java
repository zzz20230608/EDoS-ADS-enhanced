package org.cloudbus.cloudsim.information;

public class LoadBalncerInformation {
	/** The  initiation time */
	private static double initiationTime = 0.0;
	
	/** The  queueing time */
	private static double queueingTime = 0.0;
	
	/** The  processing time */
	private static double processingTime = 0.0;
	
	/** The  finish time */
	private static double finishingTime = -1;
	
	/** the number of requests served */
	private static int requestsServed = 0;
	
	/**
	 * set the initiation time.
	 * 
	 */
	public static void setInitiationTime(double time) {
		initiationTime = time;
	}
	
	/**
	 * get the initiation time.
	 * 
	 */
	public static double getInitiationTime() {
		return initiationTime;
	}
	
	/**
	 * update the queueing Time.
	 * 
	 */
	public static void updateQueueingTime(double time) {
		queueingTime += time;
	}
	
	/**
	 * get the queueing Time.
	 * 
	 */
	public static double getQueueingTime() {
		return queueingTime;
	}
	
	/**
	 * update the processing time.
	 * 
	 */
	public static void updateProcessingTime(double time) {
		processingTime += time;
	}
	
	/**
	 * get the processing time.
	 * 
	 */
	public static double getProcessingTime() {
		return processingTime;
	}
	
	/**
	 * set the finish time.
	 * 
	 */
	public static void setFinishingTime(double time) {
		finishingTime = time;
	}
	
	/**
	 * get the finish time.
	 * 
	 */
	public static double getFinishingTime() {
		return finishingTime;
	}
	
	/**
	 * update the requests served.
	 * 
	 */
	public static void updateRequestsServed() {
		requestsServed += 1;
	}
	
	/**
	 * get the requests served.
	 * 
	 */
	public static int getRequestsServed() {
		return requestsServed;
	}
}
