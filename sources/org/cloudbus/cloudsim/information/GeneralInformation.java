package org.cloudbus.cloudsim.information;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2011, The University of Melbourne, Australia
 */

// Ahmad

public class GeneralInformation {

	/** The time. */
	private double time;

	/** The value */
	private double value;

	/**
	 * Instantiates a new value information.
	 * 
	 * @param time the time
	 * @param value
	 */
	public GeneralInformation(double time, double value) {
		setTime(time);
		setTF(value);
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
	 * Sets the value.
	 * 
	 * @param value
	 */
	protected void setTF(double value) {
		this.value = value;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 * 
	 * @param value
	 */
	public void incrementValue() {
		value = value + 1;
	}
}
