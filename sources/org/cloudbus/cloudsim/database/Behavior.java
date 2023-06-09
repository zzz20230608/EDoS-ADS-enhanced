package org.cloudbus.cloudsim.database;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

public class Behavior {
	
	private static List<Record> behavior = new ArrayList<Record>();

	/**
	 * insert new user.
	 * 
	 */
	public static Record insert(String IP, int SP, String Status, double TF, int LastSeen, int RequestsCount, double PTF, boolean Redirected, double NextAllowableTime) {
		Record user = new Record(IP, SP, Status, TF, LastSeen, RequestsCount, PTF, Redirected, NextAllowableTime);
		behavior.add(user);
		
		return user;
	}
	
	/**
	 * update new user.
	 * 
	 */
	public static Record update(Record user, String Status, double TF, int LastSeen, int RequestsCount, double PTF, boolean Redirected, double NextAllowableTime) {
		user.setStatus(Status);
		user.setTF(TF);
		user.setLastSeen(LastSeen);
		user.setRequestsCount(RequestsCount);
		user.setPTF(PTF);
		user.setRedirected(Redirected);
		user.setNextAllowableTime(NextAllowableTime);
		
		return user;
	}
	
	/**
	 * select user.
	 * 
	 */
	public static Record select(String IP, int SP) {
		for(Record user : behavior) {
			if(user.getIP().equals(IP) && user.getSP() == SP) {
				return user;
			}
		}
		
		return null;
	}
	
	/**
	 * delete user.
	 * 
	 */
	public static void delete(Record user) {
		behavior.remove(user);
	}
	
	/**
	 * all users are legitimate.
	 * 
	 */
	public static boolean areAllUsersLegitimate() {
		for(Record user : behavior) {
			if(user.getRequestsCount() <= CloudSim.getMRPS() && !user.getRedirected()) {
				return false;
			} else if(user.getRequestsCount() > CloudSim.getMRPS() && user.getTF() <= 0.75) {
				return false;
			} else if(user.getRequestsCount() > CloudSim.getMRPS() && user.getTF() > 0.75 && !user.getRedirected()) {
				return false;
			}
		}
		
		return true;
	}
}
