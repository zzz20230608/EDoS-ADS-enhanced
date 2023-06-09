/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package EDoS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.excel.Excel;
import org.cloudbus.cloudsim.information.CloudModeInformation;
import org.cloudbus.cloudsim.information.LoadBalncerInformation;
import org.cloudbus.cloudsim.information.ServerInformation;
import org.cloudbus.cloudsim.information.SystemInformation;
import org.cloudbus.cloudsim.information.GeneralInformation;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example showing how to create
 * scalable simulations.
 */
public class EDoS {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	private static List<Vm> webservers;
	
	private static List<SystemInformation> avg;
	
	private static List<CloudModeInformation> cloudModeInformation;
	
	private static List<List<GeneralInformation>> allTF;
	
	private static List<List<GeneralInformation>> allreCAPTCHA;

	private static int portNum = 0;
	private static int number = 0;
	
	private static List<Vm> createVM(int userId, int vms) {

		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//VM Parameters
		// Important : if you change any of these parameter don't forget to change the same parameter in datacenterbrocker (vmScallingUp function)
		long size = 1000000; //image size (MB)
		int ram = 2048; //vm memory (MB)
		int mips = 1000;
		long bw = 10000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name

		//create VMs
		Vm[] vm = new Vm[vms];

		for(int i=0;i<vms;i++) {
			vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			list.add(vm[i]);
		}

		return list;
	}


	public static List<Cloudlet> createCloudlet(int off, int userId, int cloudlets){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//////////////////////// sender information ////////////////////////
		String IP;
		int sourcePort = 0;
		boolean pass_reCAPTCHA = true;
		int requestType; // 0 -- TCP session connection request
						 // 1 -- HTTP Web page request
		////////////////////////sender information ////////////////////////
		
		//cloudlet parameters
		long length = 10;
		long fileSize = 30; // in byte
		long outputSize = 30; // in byte
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];
		
		for(int i = 0 + (off*cloudlets); i<cloudlets + (off*cloudlets); i++) {
			if(portNum >= 0 &&  portNum < 100) { // set 1 ( CRPS = 1)
				sourcePort = portNum;
				pass_reCAPTCHA = true;				
				portNum++;
			} else if(portNum >= 100 &&  portNum < 120) { // set 2 ( CRPS = 5)
				sourcePort = portNum;
				pass_reCAPTCHA = true;
				
				number++;
				if(number == 5) {
					portNum++;
					number = 0;
				}
			} else if(portNum >= 120 &&  portNum < 220) { // set 3 ( CRPS = 1)
				sourcePort = portNum;
				pass_reCAPTCHA = false;
				portNum++;
			} else if(portNum >= 220 &&  portNum < 245) { // set 2 ( CRPS = 4)
				sourcePort = portNum;
				pass_reCAPTCHA = false;
				
				number++;
				if(number == 4) {
					portNum++;
					number = 0;
				}
			} else if(portNum >= 245 &&  portNum < 255) { // set 2 ( CRPS = 10)
				sourcePort = portNum;
				pass_reCAPTCHA = false;
				
				number++;
				if(number == 10) {
					portNum++;
					number = 0;
				}
			}
			
			IP = "10.10.10.1";
			requestType = 1;
			
			cloudlet[i - (off*cloudlets)] = new Cloudlet(IP, sourcePort, pass_reCAPTCHA, requestType, i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i - (off*cloudlets)].setUserId(userId);
			list.add(cloudlet[i - (off*cloudlets)]);
		}

		portNum = 0;
		number = 0;
		
		return list;
	}
	
	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		//int rates[] = {0, 400, 1200, 2000, 2800, 3600, 4400, 5200, 6000};
		
		//int attackRates[] = {0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000, 4400, 4800, 5200, 5600, 6000, 6400, 6800, 7200, 7600, 8000};
		//int vmcounts[] =    {7, 11,  17,  21,   27,   31,   37,   41,   47,   51,   57,   61,   67,   71,   77,   81,   87,   91,   97,   101,  107};
		//int EDoS-Shield[] = {6, 11,  16,  21,   26,   31,   36,   41,   46,   51,   56,   61,   66,   71,   76,   81,   86,   91,   96,   101,  106};
					// 
			CloudSim.setAttackRequestsRate(0);
			//CloudSim.setInitialVMsCount(7);
			
			Log.printLine("Starting EDOS ...");
	
			try {
				// First step: Initialize the CloudSim package. It should be called
				// before creating any entities.
				int num_user = 1;   // number of grid users
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = false;  // mean trace events
	
				// Initialize the CloudSim library
				CloudSim.init(num_user, calendar, trace_flag);
	
				// Second step: Create Datacenters
				//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
				@SuppressWarnings("unused")
				Datacenter datacenter0 = createDatacenter("Datacenter_0");
	
				//Third step: Create Broker
				DatacenterBroker broker = createBroker();
				int brokerId = broker.getId();
	
				//Fourth step: Create VMs and Cloudlets and send them to broker
				vmlist = createVM(brokerId, CloudSim.getInitialVMsCount()); //creating 3 vms
				cloudletList = createCloudlet(0, brokerId, CloudSim.getIncomingRequestsRate()); // creating 40 cloudlets
	
				broker.submitVmList(vmlist);
				broker.submitCloudletList(cloudletList);
	
				// Fifth step: Starts the simulation
				CloudSim.startSimulation();
	
				// Final step: Print results when simulation is over
				List<Cloudlet> newList = broker.getCloudletReceivedList();
	
				List<GeneralInformation> falseNegative;
				
				List<GeneralInformation> falsePositive;
				
				//long numberOfUsedInstances = broker.getNumberOfUsedInstances();
				//double costOfUsedInstances = broker.getCostOfUsedInstances();
				webservers = broker.getWebServersInformation();
				avg = broker.getAverageVmResultInformation();
				cloudModeInformation = broker.getCloudModeInformation();
				allTF = broker.getAllTrustFactorInformation();
				allreCAPTCHA = broker.getAllreCAPTCHAInformation();
				falseNegative = broker.getFalseNegative();
				falsePositive = broker.getFalsePositive();
				
				CloudSim.stopSimulation();
	
				printCloudModeInformation();
				printTrustFactorInformation();
				printreCAPTCHAInformation();
				printFalseNegativeInformation(falseNegative);
				printFalsePositiveInformation(falsePositive);
				printMainInformation(newList);
				printWebServersInformation();
				printLoadBalancerInformation();
				printCloudletList(newList);
				printInformationOfAverageUtilizatio(avg);
				Log.printLine("EDOS finished!");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.printLine("The simulation has been terminated due to an unexpected error");
			}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 200*1000;//1200;

		// 3. Create PEs and add these into the list.
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		//Another list, for a dual-core machine
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));

		List<Pe> peList3 = new ArrayList<Pe>();

		peList3.add(new Pe(0, new PeProvisionerSimple(mips)));
		
		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 200*2048;//2048; //host memory (MB)
		long storage = 200*1000000;//1000000; //host storage
		int bw = 200*10000;//10000;
		
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); // This is our first machine

		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		); // Second machine


		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList3,
    				new VmSchedulerTimeShared(peList3)
    			)
    		); // Third machine
		
		
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double costPerVm = 1500;        // the cost of using Vm in this resource
		double cost = 3.0;              // the cost of using processing in this resource (cost of processing per second)
		double costPerMem = 0.00;		// the cost of using memory in this resource
		double costPerStorage = 0.00;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource per byte
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, costPerVm, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = CloudSim.getLegitimateRequestsRate();
		String Data[][] = new String[size+11][16];
		
		Data[0][0] = "Cloudlet ID";
		Data[0][1] = "STATUS";
		Data[0][2] = "DataCenter ID";
		Data[0][3] = "VM ID";
		Data[0][4] = "LB Arrival Time";
		Data[0][5] = "LB Queueing Time";
		Data[0][6] = "LB Processing Time";
		Data[0][7] = "LB Departure Time";
		Data[0][8] = "MT Arrival Time";
		Data[0][9] = "MT Queueing Time";
		Data[0][10] = "MT Processing Time";
		Data[0][11] = "MT Departure Time";
		Data[0][12] = "WS Arrival Time";
		Data[0][13] = "WS Queueing Time";
		Data[0][14] = "WS Processing Time";
		Data[0][15] = "WS Departure Time";
		
		Cloudlet cloudlet;
		
		int counter = 1;
		for (int i = list.size() - size; i < list.size(); i++) {
			cloudlet = list.get(i);
			
			Data[counter][0] = "" + cloudlet.getCloudletId();
			
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Data[counter][1] = "SUCCESS";
				Data[counter][2] = "" + cloudlet.getResourceId();
				Data[counter][3] = "" + cloudlet.getVmId();
				Data[counter][4] = "" + cloudlet.getLoadbalancerTimingInfo().getArrivingTime();
				Data[counter][5] = "" + cloudlet.getLoadbalancerTimingInfo().getQueueingTime();
				Data[counter][6] = "" + cloudlet.getLoadbalancerTimingInfo().getProcessingTime();
				Data[counter][7] = "" + cloudlet.getLoadbalancerTimingInfo().getDeparturingTime();
				Data[counter][8] = "" + cloudlet.getMitigationTimingInfo().getArrivingTime();
				Data[counter][9] = "" + cloudlet.getMitigationTimingInfo().getQueueingTime();
				Data[counter][10] = "" + cloudlet.getMitigationTimingInfo().getProcessingTime();
				Data[counter][11] = "" + cloudlet.getMitigationTimingInfo().getDeparturingTime();
				Data[counter][12] = "" + cloudlet.getWebServersTimingInfo().getArrivingTime();
				Data[counter][13] = "" + cloudlet.getWebServersTimingInfo().getQueueingTime();
				Data[counter][14] = "" + cloudlet.getWebServersTimingInfo().getProcessingTime();
				Data[counter][15] = "" + cloudlet.getWebServersTimingInfo().getDeparturingTime();
			}
			else{
				Data[counter][1] = "FAILURE";
				Data[counter][2] = "####";
				Data[counter][3] = "####";
				Data[counter][4] = "####";
				Data[counter][5] = "####";
				Data[counter][6] = "####";
				Data[counter][7] = "####";
				Data[counter][8] = "####";
				Data[counter][9] = "####";
				Data[counter][10] = "####";
				Data[counter][11] = "####";
				Data[counter][12] = "####";
				Data[counter][13] = "####";
				Data[counter][14] = "####";
				Data[counter][15] = "####";
			}
			counter ++;			
		}
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS){
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "CloudLets/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the cloud Mode Information data
	 * @param list  list of cloud Mode Information
	 */
	private static void printCloudModeInformation() {
		String Data[][] = new String[cloudModeInformation.size() + 1][2];
		
		Data[0][0] = "Time";
		Data[0][1] = "Cloud Mode";
		
		for(int i = 0; i < cloudModeInformation.size(); i++) {
			CloudModeInformation cloudMode = cloudModeInformation.get(i);
			
			Data[i + 1][0] = "" + cloudMode.getTime();
			Data[i + 1][1] = cloudMode.getMode();
		}
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "Cloud Mode Information/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the false negative Information data
	 * @param list  list of false negative Information
	 */
	private static void printFalseNegativeInformation(List<GeneralInformation> falseNegative) {
		String Data[][] = new String[falseNegative.size() + 1][2];
		
		Data[0][0] = "Time";
		Data[0][1] = "False Negative";
		
		for(int i = 0; i < falseNegative.size(); i++) {
			GeneralInformation fn = falseNegative.get(i);
			
			Data[i + 1][0] = "" + fn.getTime();
			Data[i + 1][1] = "" + fn.getValue();
		}
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = "False Negative, " + CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "Users Information/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the false Positive Information data
	 * @param list  list of false Positive Information
	 */
	private static void printFalsePositiveInformation(List<GeneralInformation> falsePositive) {
		String Data[][] = new String[falsePositive.size() + 1][2];
		
		Data[0][0] = "Time";
		Data[0][1] = "False Positive";
		
		for(int i = 0; i < falsePositive.size(); i++) {
			GeneralInformation fn = falsePositive.get(i);
			
			Data[i + 1][0] = "" + fn.getTime();
			Data[i + 1][1] = "" + fn.getValue();
		}
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = "False Positive, " + CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "Users Information/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the trust factor Information data
	 * @param list of trust factor Information
	 */
	private static void printTrustFactorInformation() {
		String Data[][] = new String[100][14];
		
		for(int i = 0; i < allTF.size(); i++) {
			List<GeneralInformation> tfInfo = allTF.get(i);
			
			Data[0][0 + (3 * i)] = "Time";
			Data[0][1 + (3 * i)] = "Set " + i + " TF";
			
			for(int j = 0; j < tfInfo.size(); j++) {
				Data[j + 1][0 + (3 * i)] = "" + tfInfo.get(j).getTime();
				Data[j + 1][1 + (3 * i)] = "" + tfInfo.get(j).getValue();
			}
		}
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "Trust Factor Information/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the reCAPTCHA Information data
	 * @param list of reCAPTCHA Information
	 */
	private static void printreCAPTCHAInformation() {
		String Data[][] = new String[100][5];
		
		for(int i = 0; i < allreCAPTCHA.size(); i++) {
			List<GeneralInformation> reCAPTCHAInfo = allreCAPTCHA.get(i);
			
			Data[0][0 + (3 * i)] = "Time";
			Data[0][1 + (3 * i)] = "Set " + i + " reCAPTCHA";
			
			for(int j = 0; j < reCAPTCHAInfo.size(); j++) {
				Data[j + 1][0 + (3 * i)] = "" + reCAPTCHAInfo.get(j).getTime();
				Data[j + 1][1 + (3 * i)] = "" + reCAPTCHAInfo.get(j).getValue();
			}
		}
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "reCAPTCHA Information/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the Vm data
	 * @param list  list of VmResultInformation
	 */
	private static void printWebServersInformation() {
		String Data[][] = new String[15*webservers.size() + 3][2];
		
		double totalUtilization = 0.0;
		double totalThrougput = 0.0;
		double totalResponseTime = 0.0;
		
		for(int i = 0; i < webservers.size(); i++) {
			ServerInformation vmInfo = webservers.get(i).getInformation();
			
			Data[0 + (15 * i)][0] = "Web Server: " + (i + 1);
			
			Data[2 + (15 * i)][0] = "Total request uses this Web Server";
			Data[2 + (15 * i)][1] = "" + vmInfo.getRequestsServed();
			
			Data[3 + (15 * i)][0] = "Average delay in queue (sec)";
			Data[3 + (15 * i)][1] = "" + (vmInfo.getQueueingTime() / vmInfo.getRequestsServed());
			
			Data[4 + (15 * i)][0] = "Average number of requests in queue";
			Data[4 + (15 * i)][1] = "" + (vmInfo.getQueueingTime() / (vmInfo.getFinishingTime() - vmInfo.getInitiationTime()));

			Data[5 + (15 * i)][0] = "Average utilization";
			Data[5 + (15 * i)][1] = "" + ((vmInfo.getProcessingTime() / (vmInfo.getFinishingTime() - vmInfo.getInitiationTime()))*100);
			
			totalUtilization += ((vmInfo.getProcessingTime() / (vmInfo.getFinishingTime() - vmInfo.getInitiationTime()))*100);
			
			Data[6 + (15 * i)][0] = "Average Response Time (sec)";
			Data[6 + (15 * i)][1] = "" + ((vmInfo.getQueueingTime() / vmInfo.getRequestsServed()) + (1.0/CloudSim.getWebServersServiceRate()));
			
			totalResponseTime += ((vmInfo.getQueueingTime() / vmInfo.getRequestsServed()) + (1.0/CloudSim.getWebServersServiceRate()));
			
			Data[7 + (15 * i)][0] = "Average Througput";
			Data[7 + (15 * i)][1] = "" + ((vmInfo.getProcessingTime() / (vmInfo.getFinishingTime() - vmInfo.getInitiationTime()))*CloudSim.getWebServersServiceRate());
			
			totalThrougput +=  ((vmInfo.getProcessingTime() / (vmInfo.getFinishingTime() - vmInfo.getInitiationTime()))*CloudSim.getWebServersServiceRate());
			
			Data[9 + (15 * i)][0] = "Mean Computing utilization";
			Data[9 + (15 * i)][1] = "" + ((CloudSim.getIncomingRequestsRate() / (webservers.size() * CloudSim.getWebServersServiceRate())) * 100);
			
			Data[10 + (15 * i)][0] = "Mean Response Time (sec)";
			Data[10 + (15 * i)][1] = "" + (1 / (CloudSim.getWebServersServiceRate() - ((1.0 * CloudSim.getIncomingRequestsRate()) / webservers.size())));
			
			Data[12 + (15 * i)][0] = "Time simulation ended (sec)";
			Data[12 + (15 * i)][1] = "" + vmInfo.getFinishingTime();
			
			Data[13 + (15 * i)][0] = "----------------------------";
			Data[13 + (15 * i)][1] = "----------------------------";
			Data[14 + (15 * i)][0] = "----------------------------";
			Data[14 + (15 * i)][1] = "----------------------------";
		}
		
		Data[15*webservers.size()][0] = "Mean Total Servers Utilization";
		Data[15*webservers.size()][1] = "" + (totalUtilization / webservers.size());
		
		Data[15*webservers.size() + 1][0] = "Mean Total Servers Response Time (sec)";
		Data[15*webservers.size() + 1][1] = "" + (totalResponseTime / webservers.size());
		
		Data[15*webservers.size() + 2][0] = "Mean Total Servers Througput";
		Data[15*webservers.size() + 2][1] = "" + totalThrougput;        
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS){
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "Web Servers Information/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}

	private static void printLoadBalancerInformation() {
		String Data[][] = new String[13][2];
		
		Data[0][0] = "The Load Balncer";
		
		Data[2][0] = "Total request uses this LoadBalncer";
		Data[2][1] = "" + LoadBalncerInformation.getRequestsServed();
		
		Data[3][0] = "Average delay in queue (sec)";
		Data[3][1] = "" + (LoadBalncerInformation.getQueueingTime() / LoadBalncerInformation.getRequestsServed());
		
		Data[4][0] = "Average number of requests in queue";
		Data[4][1] = "" + (LoadBalncerInformation.getQueueingTime() / LoadBalncerInformation.getFinishingTime());

		Data[5][0] = "Average utilization";
		Data[5][1] = "" + ((LoadBalncerInformation.getProcessingTime() / LoadBalncerInformation.getFinishingTime())*100);
		
		Data[6][0] = "Average Response Time (sec)";
		Data[6][1] = "" + ((LoadBalncerInformation.getQueueingTime() / LoadBalncerInformation.getRequestsServed()) + (1.0/CloudSim.getLoadBalancerServiceRate()));
		
		Data[7][0] = "Average Througput";
		Data[7][1] = "" + ((LoadBalncerInformation.getProcessingTime() / LoadBalncerInformation.getFinishingTime())*CloudSim.getLoadBalancerServiceRate());
		
		Data[9][0] = "Mean Computing utilization";
		Data[9][1] = "" + ((CloudSim.getIncomingRequestsRate() / (1.0 * CloudSim.getLoadBalancerServiceRate())) * 100);
		
		Data[10][0] = "Mean Response Time (sec)";
		Data[10][1] = "" + (1 / (CloudSim.getLoadBalancerServiceRate() - (CloudSim.getIncomingRequestsRate() / 1.0)));
		
		Data[12][0] = "Time simulation ended (sec)";
		Data[12][1] = "" + LoadBalncerInformation.getFinishingTime();
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS){
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "Load Balancer Information/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printMainInformation(List<Cloudlet> list) {
		double systemResponseTime = 0;
		double utilization = 0;
		double throughput = 0;
		
		String Data[][] = new String[7][2];
		
		Cloudlet cloudlet;
		
		for (int i = 0; i < list.size(); i++) {
			cloudlet = list.get(i);
			
			systemResponseTime += cloudlet.getWebServersTimingInfo().getDeparturingTime() - cloudlet.getLoadbalancerTimingInfo().getArrivingTime();			
		}
		
		for(int i = 0; i < webservers.size(); i++) {
			ServerInformation vmInfo = webservers.get(i).getInformation();
			
			utilization += (vmInfo.getProcessingTime() / (vmInfo.getFinishingTime() - vmInfo.getInitiationTime()))*100;
			throughput +=  (vmInfo.getRequestsServed() / (vmInfo.getFinishingTime() - vmInfo.getInitiationTime()));
		}
		
		systemResponseTime /= list.size();
		
		
		utilization /= webservers.size();
					
		Data[0][0] = "Response Time (ms)";
		Data[0][1] = "" + (systemResponseTime * 1000);
		
		Data[3][0] = "Utilization (%)";
		Data[3][1] = "" + utilization;
		
		Data[5][0] = "Througput";
		Data[5][1] = "" + throughput;
		
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS){
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "MainInformation/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
	
	/**
	 * Prints the average cpu utilization
	 * @param list  list of VmResultInformation
	 */
	private static void printInformationOfAverageUtilizatio(List<SystemInformation> avg) {
		int size = avg.size();
		int count = 0;
		int fileSize = avg.size() < 65536 ? avg.size() + 1 : 65536;
		String Data[][] = new String[fileSize][7];
		
		Data[0][0] = "Time (sec.)";
		Data[0][1] = "CPU Utilization %";
		Data[0][2] = "Response Time (ms)";
		Data[0][3] = "Throughput";
		Data[0][5] = "Time (sec.)";
		Data[0][6] = "Required Instances";
		
		count = 1;
		for (int i = 0; i < size; i++) {
			if(i == 0 || (Double.parseDouble(Data[count - 1][1]) != avg.get(i).getUtilization())) {
				Data[count][0] = "" + avg.get(i).getTime();
				Data[count][1] = "" + avg.get(i).getUtilization();
				Data[count][2] = "" + avg.get(i).getResponseTime();
				Data[count][3] = "" + avg.get(i).getThroughput();
				count++;
				
				if(count > 65535) {
					break;
				}
			}
		}
		
		count = 1;
		
		for (int i = 0; i < size; i++) {
			if(i == 0 || (Double.parseDouble(Data[count - 1][6]) != avg.get(i).getVMCount())) {
				Data[count][5] = String.valueOf(avg.get(i).getTime());
				Data[count][6] = String.valueOf(avg.get(i).getVMCount());
				count++;
				
				if(count > 65535) {
					break;
				}
			}
		}
		
		String cloudType;
		if(CloudSim.getCloudType() == CloudSimTags.DoS) {
			cloudType = "DoS";
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS){
			cloudType = "EDoS";
		} else {
			cloudType = "EDoS_Metigation";
		}
		
		String loadBalancerType = CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED ? "Least Loaded" : "Round Robin";
		
		String name = CloudSim.getIncomingRequestsRate() + " Req per sec, " + CloudSim.getInitialVMsCount() + ", " + loadBalancerType;
		
		String FileName = "output/" + cloudType +"/" + "UtilizationInstancesCount/" + name + ".xls";
		String SheetName = "Time Sharing";
		Excel.ExcelWrite(FileName, SheetName, Data);
	}
}
