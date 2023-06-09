package org.cloudbus.cloudsim.information;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2011, The University of Melbourne, Australia
 */

// Ahmad


/**
 * The Class VmResultInformation.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.1.2
 */
public class SystemInformation {

	/** The time. */
	private int time;

	/** The VM count. */
	private int vmCount;

	/** The utilization */
	private double utilization;

	/** The response Time */
	private double responseTime;
	
	/** The throughput */
	private double throughput;

	/**
	 * Instantiates a new vm result information.
	 * 
	 * @param time the time
	 * @param request count
	 * @param utilization
	 */
	public SystemInformation(int time, int vmCount, double utilization, double responseTime, double throughput) {
		setTime(time);
		setVMCount(vmCount);
		setUtilization(utilization);
		setResponseTime(responseTime);
		setThroughput(throughput);
	}

	/**
	 * Sets the time.
	 * 
	 * @param time the new time
	 */
	protected void setTime(int time) {
		this.time = time;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Sets the request count.
	 * 
	 * @param requestCount the new request count
	 */
	protected void setVMCount(int vmCount) {
		this.vmCount = vmCount;
	}

	/**
	 * Gets the request count.
	 * 
	 * @return the request count
	 */
	public long getVMCount() {
		return vmCount;
	}

	/**
	 * Sets the utilization.
	 * 
	 * @param utilization the new utilization
	 */
	protected void setUtilization(double utilization) {
		this.utilization = utilization;
	}

	/**
	 * Gets the utilization.
	 * 
	 * @return the utilization
	 */
	public double getUtilization() {
		return utilization;
	}
	
	protected void setResponseTime(double responseTime) {
		this.responseTime = responseTime;
	}
	
	public double getResponseTime() {
		return responseTime;
	}
	
	protected void setThroughput(double throughput) {
		this.throughput = throughput;
	}
	
	public double getThroughput() {
		return throughput;
	}
}
