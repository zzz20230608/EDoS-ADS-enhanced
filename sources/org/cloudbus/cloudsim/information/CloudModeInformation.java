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
public class CloudModeInformation {

	/** The time. */
	private double time;

	/** The mode */
	private String mode;

	/**
	 * Instantiates a new cloud mode information.
	 * 
	 * @param time the time
	 * @param mode
	 */
	public CloudModeInformation(double time, String mode) {
		setTime(time);
		setMode(mode);
	}

	/**
	 * Sets the time.
	 * 
	 * @param time the new time
	 */
	protected void setTime(double time) {
		this.time = time;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Sets the mode.
	 * 
	 * @param mode
	 */
	protected void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Gets the mode.
	 * 
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}
}
