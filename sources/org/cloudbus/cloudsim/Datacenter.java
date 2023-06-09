/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Math.pow;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.database.Behavior;
import org.cloudbus.cloudsim.database.Record;
import org.cloudbus.cloudsim.information.CloudModeInformation;
import org.cloudbus.cloudsim.information.SystemInformation;
import org.cloudbus.cloudsim.information.GeneralInformation;

/**
 * Datacenter class is a CloudResource whose hostList are virtualized. It deals with processing of
 * VM queries (i.e., handling of VMs) instead of processing Cloudlet-related queries. So, even
 * though an AllocPolicy will be instantiated (in the init() method of the superclass, it will not
 * be used, as processing of cloudlets are handled by the CloudletScheduler and processing of
 * VirtualMachines are handled by the VmAllocationPolicy.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Datacenter extends SimEntity {

	/** The characteristics. */
	private DatacenterCharacteristics characteristics;

	/** The regional cis name. */
	private String regionalCisName;

	/** The vm provisioner. */
	private VmAllocationPolicy vmAllocationPolicy;

	/** The last process time. */
	private double lastProcessTime;

	/** The storage list. */
	private List<Storage> storageList;

	/** The vm list. */
	private List<? extends Vm> vmList;

	/** The scheduling interval. */
	private double schedulingInterval;
	
	/** The number of vms used */
	private static long usedVMs = 0;
	
	private static int roundRobinIndex = 0;
	
	private List<Vm> webservers = new ArrayList<Vm>();
	
	private List<SystemInformation> avg = new ArrayList<SystemInformation>();
	
	private List<CloudModeInformation> cloudModeInformation = new ArrayList<CloudModeInformation>();
	
	private List<GeneralInformation> set_1_TF = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> set_2_TF = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> set_3_TF = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> set_4_TF = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> set_5_TF = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> falsePositive = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> falseNegative = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> set_1_reCAPTCHA = new ArrayList<GeneralInformation>();
	
	private List<GeneralInformation> set_2_reCAPTCHA = new ArrayList<GeneralInformation>();
	
	/** The number of Total requests served */
	private static int requestsServed = 0;
	
	/** The number of Total requests response time */
	private static double totalRequestsResponseTime = 0.0;
	
	/** The number of Total request */
	private static int TRC = 0;
	
	/** The number of malicious request */
	private static int MRC = 0;
	
	private static double checkFlashModeInSuspicion;
	
	private static boolean EDoSCheckUtilization = true;
	
	private static boolean EDoSAlgCheckUtilization = true;
	
	public static Record user;
	
	private static double currentUtilization = 0;
	
	private static int VMCount = 0;
	
	/**
	 * Allocates a new PowerDatacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @param characteristics an object of DatacenterCharacteristics
	 * @param storageList a LinkedList of storage elements, for data simulation
	 * @param vmAllocationPolicy the vmAllocationPolicy
	 * @throws Exception This happens when one of the following scenarios occur:
	 *             <ul>
	 *             <li>creating this entity before initializing CloudSim package
	 *             <li>this entity name is <tt>null</tt> or empty
	 *             <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br>
	 *             No PEs mean the Cloudlets can't be processed. A CloudResource must contain one or
	 *             more Machines. A Machine must contain one or more PEs.
	 *             </ul>
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public Datacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name);

		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);

		for (Host host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);
		}

		// If this resource doesn't have any PEs then no useful at all
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}

		// stores id of this class
		getCharacteristics().setId(super.getId());
		
		getCloudModeInformation().add(new CloudModeInformation(CloudSim.clock(), "Normal"));
	}

	/**
	 * Overrides this method when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link #body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void registerOtherEntity() {
		// empty. This should be override by a child class
	}

	/**
	 * Processes events or services that are available for this PowerDatacenter.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		int srcId = -1;

		switch (ev.getTag()) {
		// Resource characteristics inquiry
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), getCharacteristics());
				break;

			// Resource dynamic info inquiry
			case CloudSimTags.RESOURCE_DYNAMICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), 0);
				break;

			case CloudSimTags.RESOURCE_NUM_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int numPE = getCharacteristics().getNumberOfPes();
				sendNow(srcId, ev.getTag(), numPE);
				break;

			case CloudSimTags.RESOURCE_NUM_FREE_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int freePesNumber = getCharacteristics().getNumberOfFreePes();
				sendNow(srcId, ev.getTag(), freePesNumber);
				break;

			// New Cloudlet arrives
			case CloudSimTags.CLOUDLET_SUBMIT:
				processCloudletSubmit(ev, false);
				break;

			// New Cloudlet arrives, but the sender asks for an ack
			case CloudSimTags.CLOUDLET_SUBMIT_ACK:
				processCloudletSubmit(ev, true);
				break;

			// Cancels a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
				break;

			// Pauses a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
				break;

			// Pauses a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
				break;

			// Resumes a previously submitted Cloudlet
			case CloudSimTags.CLOUDLET_RESUME:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
				break;

			// Resumes a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement
			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE_ACK:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
				break;

			// Checks the status of a Cloudlet
			case CloudSimTags.CLOUDLET_STATUS:
				processCloudletStatus(ev);
				break;

			// Ping packet
			case CloudSimTags.INFOPKT_SUBMIT:
				processPingRequest(ev);
				break;

			case CloudSimTags.VM_CREATE:
				processVmCreate(ev, false);
				break;

			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev, true);
				break;

			case CloudSimTags.VM_DESTROY:
				processVmDestroy(ev, false);
				break;

			case CloudSimTags.VM_DESTROY_ACK:
				processVmDestroy(ev, true);
				break;

			case CloudSimTags.VM_MIGRATE:
				processVmMigrate(ev, false);
				break;

			case CloudSimTags.VM_MIGRATE_ACK:
				processVmMigrate(ev, true);
				break;

			case CloudSimTags.VM_DATA_ADD:
				processDataAdd(ev, false);
				break;

			case CloudSimTags.VM_DATA_ADD_ACK:
				processDataAdd(ev, true);
				break;

			case CloudSimTags.VM_DATA_DEL:
				processDataDelete(ev, false);
				break;

			case CloudSimTags.VM_DATA_DEL_ACK:
				processDataDelete(ev, true);
				break;

			case CloudSimTags.VM_DATACENTER_EVENT:
				updateCloudletProcessing();
				checkCloudletCompletion();
				break;

			case CloudSimTags.Finishing_Cloudlet:
				processCloudletFinishing((Cloudlet) ev.getData());
				break;
				
			case CloudSimTags.Check_Average_CPU_Utilization:
				checkAverageCPUUtilizationForScalling();
				if(CloudSim.clock() <= 12 * 60){
					send(getId(), CloudSim.getScaleUpTimer(), CloudSimTags.Check_Average_CPU_Utilization);
				}
				break;
				
			case CloudSimTags.Check_Average_CPU_Utilization_every_second:
				if(CloudSim.clock() <= 12 * 60){
					getAverageCPUUtilization();
					setNewVmResultInf();
					send(getId(), 1, CloudSimTags.Check_Average_CPU_Utilization_every_second);
				}
				break;
				
			case CloudSimTags.Send_to_Cloud_Servers:
				SendTheRequestToTheCloudServers(ev, false);
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process data del.
	 * 
	 * @param ev the ev
	 * @param ack the ack
	 */
	protected void processDataDelete(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] data = (Object[]) ev.getData();
		if (data == null) {
			return;
		}

		String filename = (String) data[0];
		int req_source = ((Integer) data[1]).intValue();
		int tag = -1;

		// check if this file can be deleted (do not delete is right now)
		int msg = deleteFileFromStorage(filename);
		if (msg == DataCloudTags.FILE_DELETE_SUCCESSFUL) {
			tag = DataCloudTags.CTLG_DELETE_MASTER;
		} else { // if an error occured, notify user
			tag = DataCloudTags.FILE_DELETE_MASTER_RESULT;
		}

		if (ack) {
			// send back to sender
			Object pack[] = new Object[2];
			pack[0] = filename;
			pack[1] = Integer.valueOf(msg);

			sendNow(req_source, tag, pack);
		}
	}

	/**
	 * Process data add.
	 * 
	 * @param ev the ev
	 * @param ack the ack
	 */
	protected void processDataAdd(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] pack = (Object[]) ev.getData();
		if (pack == null) {
			return;
		}

		File file = (File) pack[0]; // get the file
		file.setMasterCopy(true); // set the file into a master copy
		int sentFrom = ((Integer) pack[1]).intValue(); // get sender ID

		/******
		 * // DEBUG Log.printLine(super.get_name() + ".addMasterFile(): " + file.getName() +
		 * " from " + CloudSim.getEntityName(sentFrom));
		 *******/

		Object[] data = new Object[3];
		data[0] = file.getName();

		int msg = addFile(file); // add the file

		if (ack) {
			data[1] = Integer.valueOf(-1); // no sender id
			data[2] = Integer.valueOf(msg); // the result of adding a master file
			sendNow(sentFrom, DataCloudTags.FILE_ADD_MASTER_RESULT, data);
		}
	}

	/**
	 * Processes a ping request.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processPingRequest(SimEvent ev) {
		InfoPacket pkt = (InfoPacket) ev.getData();
		pkt.setTag(CloudSimTags.INFOPKT_RETURN);
		pkt.setDestId(pkt.getSrcId());

		// sends back to the sender
		sendNow(pkt.getSrcId(), CloudSimTags.INFOPKT_RETURN, pkt);
	}

	/**
	 * Process the event for an User/Broker who wants to know the status of a Cloudlet. This
	 * PowerDatacenter will then send the status back to the User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletStatus(SimEvent ev) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;
		int status = -1;

		try {
			// if a sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];

			status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId).getCloudletScheduler()
					.getCloudletStatus(cloudletId);
		}

		// if a sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();

				status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
						.getCloudletScheduler().getCloudletStatus(cloudletId);
			} catch (Exception e) {
				Log.printLine(getName() + ": Error in processing CloudSimTags.CLOUDLET_STATUS");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printLine(getName() + ": Error in processing CloudSimTags.CLOUDLET_STATUS");
			Log.printLine(e.getMessage());
			return;
		}

		int[] array = new int[3];
		array[0] = getId();
		array[1] = cloudletId;
		array[2] = status;

		int tag = CloudSimTags.CLOUDLET_STATUS;
		sendNow(userId, tag, array);
	}

	/**
	 * Here all the method related to VM requests will be received and forwarded to the related
	 * method.
	 * 
	 * @param ev the received event
	 * @pre $none
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): Error - an event is null.");
		}
	}

	/**
	 * Process the event for an User/Broker who wants to create a VM in this PowerDatacenter. This
	 * PowerDatacenter will then send the status back to the User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @param ack the ack
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();

		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
		}

		if (result) {
			getVmList().add(vm);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}

			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
					.getAllocatedMipsForVm(vm));
			
			vm.getInformation().setInitiationTime(CloudSim.clock());
			
			webservers.add(vm);
			
			setUsedVMs();
			
			
			
			if((CloudSim.getCloudType() == CloudSimTags.EDoS_Algorithm) && (CloudSim.getCloudMode() != CloudSimTags.Normal_Mode)) {
				setCloudModeToNormalMode();
			}
		}
		

		if((CloudSim.getCloudType() == CloudSimTags.EDoS) && EDoSCheckUtilization && (getVmList().size() > 0)) {
			send(getId(), CloudSim.getScaleUpTimer(), CloudSimTags.Check_Average_CPU_Utilization);
			EDoSCheckUtilization = false;
		}

		if(EDoSAlgCheckUtilization) {
			send(getId(), 1, CloudSimTags.Check_Average_CPU_Utilization_every_second);
			EDoSAlgCheckUtilization = false;
		}
		
	}
	
	/**
	 * Process the event for an User/Broker who wants to destroy a VM previously created in this
	 * PowerDatacenter. This PowerDatacenter may send, upon request, the status back to the
	 * User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @param ack the ack
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		getVmAllocationPolicy().deallocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);			
		}

		getVmList().remove(vm);
	}

	/**
	 * Process the event for an User/Broker who wants to migrate a VM. This PowerDatacenter will
	 * then send the status back to the User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		Object tmp = ev.getData();
		if (!(tmp instanceof Map<?, ?>)) {
			throw new ClassCastException("The data object must be Map<String, Object>");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> migrate = (HashMap<String, Object>) tmp;

		Vm vm = (Vm) migrate.get("vm");
		Host host = (Host) migrate.get("host");

		getVmAllocationPolicy().deallocateHostForVm(vm);
		host.removeMigratingInVm(vm);
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
		if (!result) {
			Log.printLine("[Datacenter.processVmMigrate] VM allocation to the destination host failed");
			System.exit(0);
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
		}

		Log.formatLine(
				"%.2f: Migration of VM #%d to Host #%d is completed",
				CloudSim.clock(),
				vm.getId(),
				host.getId());
		vm.setInMigration(false);
	}

	/**
	 * Processes a Cloudlet based on the event type.
	 * 
	 * @param ev a Sim_event object
	 * @param type event type
	 * @pre ev != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudlet(SimEvent ev, int type) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;

		try { // if the sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];
		}

		// if the sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();
				vmId = cl.getVmId();
			} catch (Exception e) {
				Log.printLine(super.getName() + ": Error in processing Cloudlet");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printLine(super.getName() + ": Error in processing a Cloudlet.");
			Log.printLine(e.getMessage());
			return;
		}

		// begins executing ....
		switch (type) {
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudletCancel(cloudletId, userId, vmId);
				break;

			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudletPause(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudletPause(cloudletId, userId, vmId, true);
				break;

			case CloudSimTags.CLOUDLET_RESUME:
				processCloudletResume(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudletResume(cloudletId, userId, vmId, true);
				break;
			default:
				break;
		}

	}

	/**
	 * Process the event for an User/Broker who wants to move a Cloudlet.
	 * 
	 * @param receivedData information about the migration
	 * @param type event tag
	 * @pre receivedData != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudletMove(int[] receivedData, int type) {
		updateCloudletProcessing();

		int[] array = receivedData;
		int cloudletId = array[0];
		int userId = array[1];
		int vmId = array[2];
		int vmDestId = array[3];
		int destId = array[4];

		// get the cloudlet
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);

		boolean failed = false;
		if (cl == null) {// cloudlet doesn't exist
			failed = true;
		} else {
			// has the cloudlet already finished?
			if (cl.getCloudletStatus() == Cloudlet.SUCCESS) {// if yes, send it back to user
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cloudletId;
				data[2] = 0;
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
				
				cloudletFinishedWebServer(cl);
			}

			// prepare cloudlet for migration
			cl.setVmId(vmDestId);

			// the cloudlet will migrate from one vm to another does the destination VM exist?
			if (destId == getId()) {
				Vm vm = getVmAllocationPolicy().getHost(vmDestId, userId).getVm(vmDestId,userId);
				if (vm == null) {
					failed = true;
				} else {
					// time to transfer the files
					double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
					vm.getCloudletScheduler().cloudletSubmit(cl, fileTransferTime);
				}
			} else {// the cloudlet will migrate from one resource to another
				int tag = ((type == CloudSimTags.CLOUDLET_MOVE_ACK) ? CloudSimTags.CLOUDLET_SUBMIT_ACK
						: CloudSimTags.CLOUDLET_SUBMIT);
				sendNow(destId, tag, cl);
			}
		}

		if (type == CloudSimTags.CLOUDLET_MOVE_ACK) {// send ACK if requested
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (failed) {
				data[2] = 0;
			} else {
				data[2] = 1;
			}
			sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet submission.
	 * 
	 * @param ev a SimEvent object
	 * @param ack an acknowledgement
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		if(CloudSim.getCloudType() == CloudSimTags.EDoS_Algorithm) {
			checkAverageCPUUtilizationForScalling();
			
			// check the cloud mode
			if(CloudSim.getCloudMode() == CloudSimTags.Normal_Mode || CloudSim.getCloudMode() == CloudSimTags.Flash_Overcrowd_Mode) {
				SendTheRequestToTheCloudServers(ev, ack);
	
				if(CloudSim.getTimeBreakScaleUpCpuUtilizationThr() != -1.0) {
					if(CloudSim.getCloudMode() != CloudSimTags.Flash_Overcrowd_Mode) {
						setCloudModeToSuspicionMode();
					}
				} else if(CloudSim.getCloudMode() != CloudSimTags.Normal_Mode) {
					setCloudModeToNormalMode();
				}
			} else if(CloudSim.getCloudMode() == CloudSimTags.Attack_Mode){
				SendTheRequestToTheAttackShell(ev, ack);
			} else {
				SendTheRequestToTheSuspicionShell(ev, ack);
			}
		} else if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
			SendTheRequestToTheCloudServers(ev, ack);
		} else { // DoS
			SendTheRequestToTheCloudServers(ev, ack);
		}
	}

	private void SendTheRequestToTheCloudServers(SimEvent ev, boolean ack) {
		try {
			// gets the Cloudlet object
			Cloudlet cl = (Cloudlet) ev.getData();
			
			// load balancing
			if(CloudSim.getLoadBalancingPolicy() == CloudSimTags.ROUND_ROBIN) {
				cl.setVmId(getVmList().get(roundRobinIndex).getId());
				roundRobinIndex = (roundRobinIndex + 1) % getVmList().size();
			} else if(CloudSim.getLoadBalancingPolicy() == CloudSimTags.LEAST_LOADED) {
				LeastLoadedVM(cl);
			}
			
			// update false negative
			if(!cl.getPass_reCAPTCHA()) {
				if(falseNegative.size() == 0) {
					falseNegative.add(new GeneralInformation(0, 0));
				}
				
				if((int) falseNegative.get(falseNegative.size() - 1).getTime() == (int) CloudSim.clock()) {
					falseNegative.get(falseNegative.size() - 1).incrementValue();
				} else {
					falseNegative.add(new GeneralInformation((int) CloudSim.clock(), 1));
				}
			}
			
			//Log.printLine(CloudSim.clock() + ": Load Balancer: Sending cloudlet (" + cl.getCloudletId() + ") to VM (" + cl.getVmId() + ")");
			
			// checks whether this Cloudlet has finished or not
			if (cl.isFinished()) {
				//Log.printLine(getName() + ": Warning - Cloudlet #" + cl.getCloudletId() + " owned by " + name + " is already completed/finished.");
				Log.printLine("Therefore, it is not being executed again");
				Log.printLine();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					// unique tag = operation tag
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					sendNow(cl.getUserId(), tag, data);
				}

				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
				
				cloudletFinishedWebServer(cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics()
					.getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getVmId();
			Host host = getVmAllocationPolicy().getHost(vmId, userId);
			Vm vm = host.getVm(vmId, userId);
			
			/// new work
			
			
			if((CloudSim.clock() != cl.getLoadbalancerTimingInfo().getDeparturingTime()) && (Math.abs(CloudSim.clock() - cl.getLoadbalancerTimingInfo().getDeparturingTime()) > 0.0000005) && (CloudSim.getCloudType() != CloudSimTags.EDoS_Algorithm))  {
				Log.printLine(" ######## ERROR #########");
			}
			
			cl.getWebServersTimingInfo().setArrivingTime(CloudSim.clock());
			cl.getWebServersTimingInfo().setProcessingTime(Random_Genrator.expon(1.0/CloudSim.getWebServersServiceRate()));
			
			if(vm.getNextAvailableTime() > CloudSim.clock()) {  // busy
				cl.getWebServersTimingInfo().setQueueingTime(vm.getNextAvailableTime() - CloudSim.clock());
				cl.getWebServersTimingInfo().setDeparturingTime(vm.getNextAvailableTime() + cl.getWebServersTimingInfo().getProcessingTime());
			} else {
				cl.getWebServersTimingInfo().setDeparturingTime(CloudSim.clock() + cl.getWebServersTimingInfo().getProcessingTime());
				
				vm.setTimeOfLastBusy(CloudSim.clock());
			}
			
			vm.getInformation().updateRequestsServed();
			vm.getInformation().updateQueueingTime(cl.getWebServersTimingInfo().getQueueingTime());

			if(cl.getWebServersTimingInfo().getDeparturingTime() > vm.getNextAvailableTime()) {
				vm.setNextAvailableTime(cl.getWebServersTimingInfo().getDeparturingTime());
			}
			
			send(getId(), cl.getWebServersTimingInfo().getDeparturingTime() - CloudSim.clock(), CloudSimTags.Finishing_Cloudlet, cl);
			
			
			
			// time to transfer the files
			double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);

			// if this cloudlet is in the exec queue
			if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
				estimatedFinishTime += fileTransferTime;
				//send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
			}

			if (ack) {
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cl.getCloudletId();
				data[2] = CloudSimTags.TRUE;

				// unique tag = operation tag
				int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
				sendNow(cl.getUserId(), tag, data);
			}
					
			if(CloudSim.getCloudType() == CloudSimTags.EDoS_Algorithm) {
				checkAverageCPUUtilizationForScalling();
			}
		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}

	}
			
	private void cloudletFinishedWebServer(Cloudlet cl) {
		int userId = cl.getUserId();
		int vmId = cl.getVmId();
		Host host = getVmAllocationPolicy().getHost(vmId, userId);
		Vm vm = host.getVm(vmId, userId);
		
		vm.getInformation().updateProcessingTime(cl.getWebServersTimingInfo().getProcessingTime());
		vm.getInformation().setFinishingTime(CloudSim.clock());
		
		if((CloudSim.clock() != cl.getWebServersTimingInfo().getDeparturingTime()) && (Math.abs(CloudSim.clock() - cl.getWebServersTimingInfo().getDeparturingTime()) > 0.0000005))  {
			Log.printLine(" ######## ERROR #########");
		}
		
		if(vm.getNextAvailableTime() > CloudSim.clock()) {	
			vm.setTimeOfLastBusy(CloudSim.clock());
		}
		
		
	}
	
	private void SendTheRequestToTheSuspicionShell(SimEvent ev, boolean ack) {
		// gets the Cloudlet object
		Cloudlet cl = (Cloudlet) ev.getData();
		
		user = Behavior.select(cl.getIP(), cl.getSP());
		
		cl.getMitigationTimingInfo().setArrivingTime(CloudSim.clock());
		
		if(cl.getRequestType() == 0) { // TCP session connection request
			Behavior.delete(user);
		} else if(cl.getPass_reCAPTCHA() && user != null && user.getNextAllowableTime() > CloudSim.clock()) {
			// assume that request did not send
		} else {
			boolean sendToServers = false;
			boolean checkNextCloudMode = true;
			 
			user = InsertorUpdateRPSValue(user, cl.getIP(), cl.getSP());
			
			TRC = TRC + 1;
						
			if(user.getRequestsCount() > CloudSim.getMRPS()) {
				if(user.getTF() > 0.75) {
					if(user.getRedirected()) {
						sendToServers = true;
					} else {
						cl.getMitigationTimingInfo().setProcessingTime(CloudSim.getRedirectionOverhead());
						
						if(cl.getPass_reCAPTCHA()) {
							user = Behavior.update(user, user.getStatus(), user.getTF(), user.getLastSeen(), user.getRequestsCount(), user.getPTF(), true, CloudSim.clock() + CloudSim.getRedirectionOverhead());
							sendToServers = true;
						} else {
							MRC = MRC + 1;
						}
					}
				} else {
					user = updateBehaviorTable(user, cl.getPass_reCAPTCHA());
					
					cl.getMitigationTimingInfo().setProcessingTime(CloudSim.getreCAPTCHAOverhead());
					
					if(cl.getPass_reCAPTCHA()) {
						sendToServers = true;
						
						// count reCAPTCHA
						if(user.getSP() == 0) {
							if(set_1_reCAPTCHA.size() == 0) {
								set_1_reCAPTCHA.add(new GeneralInformation(0, 0));
							}
							
							set_1_reCAPTCHA.add(new GeneralInformation(CloudSim.clock(), set_1_reCAPTCHA.get(set_1_reCAPTCHA.size() - 1).getValue() + 1));
						} else if(user.getSP() == 100) {
							if(set_2_reCAPTCHA.size() == 0) {
								set_2_reCAPTCHA.add(new GeneralInformation(0, 0));
							}
							
							set_2_reCAPTCHA.add(new GeneralInformation(CloudSim.clock(), set_2_reCAPTCHA.get(set_2_reCAPTCHA.size() - 1).getValue() + 1));
						}
					} else {
						MRC = MRC + 1;
						
						if(user.getStatus().equals("Malicious") && user.getTF() < 0.25) {
							setCloudModeToAttackMode();
							checkNextCloudMode = false;
						}
					}
				}
			} else {
				if(user.getRedirected()) {
					sendToServers = true;
				} else {
					cl.getMitigationTimingInfo().setProcessingTime(CloudSim.getRedirectionOverhead());
					
					if(cl.getPass_reCAPTCHA()) {
						user = Behavior.update(user, user.getStatus(), user.getTF(), user.getLastSeen(), user.getRequestsCount(), user.getPTF(), true, CloudSim.clock() + CloudSim.getRedirectionOverhead());
						sendToServers = true;
					} else {
						MRC = MRC + 1;
					}
				}
			}
			
			cl.getMitigationTimingInfo().setDeparturingTime(cl.getMitigationTimingInfo().getArrivingTime() + cl.getMitigationTimingInfo().getProcessingTime());
			
			if(sendToServers) {
				send(getId(), cl.getMitigationTimingInfo().getDeparturingTime() - CloudSim.clock(), CloudSimTags.Send_to_Cloud_Servers, cl);
			} else if(cl.getPass_reCAPTCHA()) {
				// update false positive
				if(falsePositive.size() == 0) {
					falsePositive.add(new GeneralInformation(0, 0));
				}
				
				if(falsePositive.get(falsePositive.size() - 1).getTime() == (int) CloudSim.clock()) {
					falsePositive.get(falsePositive.size() - 1).incrementValue();
				} else {
					falsePositive.add(new GeneralInformation((int) CloudSim.clock(), falsePositive.get(falsePositive.size() - 1).getValue() + 1));
				}
			}
			
			if(checkNextCloudMode) {
				checkAverageCPUUtilizationForScalling();
				
				//(CloudSim.getTimeBreakScaleUpCpuUtilizationThr() == -1.0) || (CloudSim.clock() - CloudSim.getSuspicionTimerStarts() >= CloudSim.getSuspicionTimer())
				if(CloudSim.clock() - CloudSim.getSuspicionTimerStarts() >= CloudSim.getSuspicionTimer()) {
					if(1.0*MRC/TRC > 0.08) {
						setCloudModeToAttackMode();
					} else {
						if(currentUtilization > CloudSim.getScaleUpCpuUtilizationThr()) {
							setCloudModeToFlashOverCrowdMode();
						} else {
							setCloudModeToNormalMode();
						}
					}
				} if(CloudSim.clock() - checkFlashModeInSuspicion >= 0) {
					if(Behavior.areAllUsersLegitimate()) { // all clients are legitimate
						setCloudModeToFlashOverCrowdMode();
					} else {
						checkFlashModeInSuspicion = checkFlashModeInSuspicion + 60;
					}
				}
			}
		}
	}
	
	private void SendTheRequestToTheAttackShell(SimEvent ev, boolean ack) {
		// gets the Cloudlet object
		Cloudlet cl = (Cloudlet) ev.getData();
				
		user = Behavior.select(cl.getIP(), cl.getSP());
				
		cl.getMitigationTimingInfo().setArrivingTime(CloudSim.clock());
				
		if(cl.getRequestType() == 0) { // TCP session connection request
			Behavior.delete(user);
		} else if(cl.getPass_reCAPTCHA() && user != null && user.getNextAllowableTime() > CloudSim.clock()) {
			// assume that request did not send
		} else {
			boolean sendToServers = false;
					 
			user = InsertorUpdateRPSValue(user, cl.getIP(), cl.getSP());
					
			TRC = TRC + 1;
			
			if(user.getRedirected()) {
				if(user.getRequestsCount() > getAllowableRPS(user.getTF())) {
					user = updateBehaviorTable(user, cl.getPass_reCAPTCHA());
					
					cl.getMitigationTimingInfo().setProcessingTime(CloudSim.getreCAPTCHAOverhead());
					
					if(cl.getPass_reCAPTCHA()) {
						sendToServers = true;
						
						// count reCAPTCHA
						if(user.getSP() == 0) {
							if(set_1_reCAPTCHA.size() == 0) {
								set_1_reCAPTCHA.add(new GeneralInformation(0, 0));
							}
							
							set_1_reCAPTCHA.add(new GeneralInformation(CloudSim.clock(), set_1_reCAPTCHA.get(set_1_reCAPTCHA.size() - 1).getValue() + 1));
						} else if(user.getSP() == 100) {
							if(set_2_reCAPTCHA.size() == 0) {
								set_2_reCAPTCHA.add(new GeneralInformation(0, 0));
							}
							
							set_2_reCAPTCHA.add(new GeneralInformation(CloudSim.clock(), set_2_reCAPTCHA.get(set_2_reCAPTCHA.size() - 1).getValue() + 1));
						}
					} else {
						MRC = MRC + 1;
					}
				} else {
					sendToServers = true;
				}
			} else {
				if(user.getRequestsCount() > CloudSim.getMRPS()) {
					MRC = MRC + 1;
				} else {
					cl.getMitigationTimingInfo().setProcessingTime(CloudSim.getRedirectionOverhead());
					
					if(cl.getPass_reCAPTCHA()) {
						user = Behavior.update(user, user.getStatus(), user.getTF(), user.getLastSeen(), user.getRequestsCount(), user.getPTF(), true, CloudSim.clock() + CloudSim.getRedirectionOverhead());
						sendToServers = true;
					} else {
						MRC = MRC + 1;
					}
				}
			}
			
			cl.getMitigationTimingInfo().setDeparturingTime(cl.getMitigationTimingInfo().getArrivingTime() + cl.getMitigationTimingInfo().getProcessingTime());
					
			if(sendToServers) {
				send(getId(), cl.getMitigationTimingInfo().getDeparturingTime() - CloudSim.clock(), CloudSimTags.Send_to_Cloud_Servers, cl);
			} else if(cl.getPass_reCAPTCHA()) {
				// update false positive
				if(falsePositive.size() == 0) {
					falsePositive.add(new GeneralInformation(0, 0));
				}
				
				if(falsePositive.get(falsePositive.size() - 1).getTime() == (int) CloudSim.clock()) {
					falsePositive.get(falsePositive.size() - 1).incrementValue();
				} else {
					falsePositive.add(new GeneralInformation((int) CloudSim.clock(), falsePositive.get(falsePositive.size() - 1).getValue() + 1));
				}
			}
					
			if(CloudSim.clock() - CloudSim.getAttackTimerStarts() >= CloudSim.getAttackDuration()) {
				if(1.0*MRC/TRC > 0.08) { // restart attack mode with new attack-duration
					setCloudModeToAttackMode();
				} else {
					checkAverageCPUUtilizationForScalling();
					
					if(CloudSim.getTimeBreakScaleUpCpuUtilizationThr() == -1.0) {
						setCloudModeToNormalMode();
					} else {
						setCloudModeToSuspicionMode();
					}
				}
			}
			
			
		}
	}

	private void setCloudModeToNormalMode() {
		CloudSim.setCloudMode(CloudSimTags.Normal_Mode);
		Log.printLine("*****************************************************");
		Log.printLine("******************** Normal Mode ********************");
		Log.printLine("*****************************************************");
		
		getCloudModeInformation().add(new CloudModeInformation(CloudSim.clock(), "Normal"));
	}
	
	private void setCloudModeToSuspicionMode() {
		TRC = 0;
		MRC = 0;
		
		CloudSim.setSuspicionTimerStarts(CloudSim.clock());
		
		checkFlashModeInSuspicion = CloudSim.clock() + 60;
		
		CloudSim.setCloudMode(CloudSimTags.Suspicion_Mode);
		Log.printLine("********************************************************");
		Log.printLine("******************** Suspicion Mode ********************");
		Log.printLine("********************************************************");
		
		getCloudModeInformation().add(new CloudModeInformation(CloudSim.clock(), "Suspicion"));
	}
	
	private void setCloudModeToFlashOverCrowdMode() {
		CloudSim.setCloudMode(CloudSimTags.Flash_Overcrowd_Mode);
		Log.printLine("**************************************************************");
		Log.printLine("******************** Flash Overcrowd Mode ********************");
		Log.printLine("**************************************************************");
		

		double averageUtilization = currentUtilization;
		
		Log.printLine(CloudSim.clock() + ": Scaling Up .... CPU Utilization is " + averageUtilization + "%");	
		sendNow(getVmList().get(0).getUserId(), CloudSimTags.CLOUD_SCALE_UP, getId());
		
		getCloudModeInformation().add(new CloudModeInformation(CloudSim.clock(), "Flash Overcrowd"));
	}
	
	private void setCloudModeToAttackMode() {
		CloudSim.setAttackDuration(1.0*MRC/TRC);
		CloudSim.setAttackTimerStarts(CloudSim.clock());
		
		TRC = 0;
		MRC = 0;
		
		CloudSim.setCloudMode(CloudSimTags.Attack_Mode);
		Log.printLine("*****************************************************");
		Log.printLine("******************** Attack Mode ********************");
		Log.printLine("*****************************************************");
		
		getCloudModeInformation().add(new CloudModeInformation(CloudSim.clock(), "Attack"));
	}
	
	private Record InsertorUpdateRPSValue(Record user, String IP, int SP) {		
		if(user != null) {
			int nextRequestsCount = user.getRequestsCount() + 1;
			
			if((int) CloudSim.clock() != user.getLastSeen()) {
				nextRequestsCount = 1;
			}
			
			user = Behavior.update(user, user.getStatus(), user.getTF(), (int) CloudSim.clock(), nextRequestsCount, user.getPTF(), user.getRedirected(), user.getNextAllowableTime());
		} else {
			user = Behavior.insert(IP, SP, "null", 0.5, (int) CloudSim.clock(), 1, 0, false, 0);
			
			if(SP >= 0 &&  SP < 100) {
				if(set_1_TF.size() == 0) {
					set_1_TF.add(new GeneralInformation(CloudSim.clock(), 0.5));
				}
			} else if(SP >= 100 &&  SP < 120) {
				if(set_2_TF.size() == 0) {
					set_2_TF.add(new GeneralInformation(CloudSim.clock(), 0.5));
				}
			} else if(SP >= 120 &&  SP < 220) {
				if(set_3_TF.size() == 0) {
					set_3_TF.add(new GeneralInformation(CloudSim.clock(), 0.5));
				}
			} else if(SP >= 220 &&  SP < 245) {
				if(set_4_TF.size() == 0) {
					set_4_TF.add(new GeneralInformation(CloudSim.clock(), 0.5));
				}
			} else if(SP >= 245 &&  SP < 255) {
				if(set_5_TF.size() == 0) {
					set_5_TF.add(new GeneralInformation(CloudSim.clock(), 0.5));
				}
			}
		}
		
		return user;
	}
		
	private Record updateBehaviorTable(Record user, boolean Pass_reCAPTCHA) {
		String nextStatus = getNextStatus(user.getStatus(), Pass_reCAPTCHA);
		double newPreviousTF = getPreviousTF(user.getStatus(), Pass_reCAPTCHA, user.getPTF());	
		double nextTF = getNextTF(user.getStatus(), user.getTF(), Pass_reCAPTCHA, newPreviousTF);
		
		user = Behavior.update(user, nextStatus, nextTF, user.getLastSeen(), user.getRequestsCount(), newPreviousTF, user.getRedirected(), CloudSim.clock() + CloudSim.getreCAPTCHAOverhead());
		
		if(user.getSP() >= 0 &&  user.getSP() < 100) {
			if(set_1_TF.get(set_1_TF.size() - 1).getValue() < nextTF) {
				set_1_TF.add(new GeneralInformation(CloudSim.clock(), nextTF));
			}
		} else if(user.getSP() >= 100 &&  user.getSP() < 120) {
			if(set_2_TF.get(set_2_TF.size() - 1).getValue() < nextTF) {
				set_2_TF.add(new GeneralInformation(CloudSim.clock(), nextTF));
			}
		} else if(user.getSP() >= 120 &&  user.getSP() < 220) {
			if(set_3_TF.get(set_3_TF.size() - 1).getValue() > nextTF) {
				set_3_TF.add(new GeneralInformation(CloudSim.clock(), nextTF));
			}
		} else if(user.getSP() >= 220 &&  user.getSP() < 245) {
			if(set_4_TF.get(set_4_TF.size() - 1).getValue() > nextTF) {
				set_4_TF.add(new GeneralInformation(CloudSim.clock(), nextTF));
			}
		} else if(user.getSP() >= 245 &&  user.getSP() < 255) {
			if(set_5_TF.get(set_5_TF.size() - 1).getValue() > nextTF) {
				set_5_TF.add(new GeneralInformation(CloudSim.clock(), nextTF));
			}
		}
		return user;
	}
	
	private int getAllowableRPS(double TF) {
		 int RPSAllows = (int) (4*0.111129*pow(81.0, TF));
		 
		 if(RPSAllows < 1) {
			 RPSAllows = 1;
		 }
		 
		 return RPSAllows;
	}		
	
	private String getNextStatus(String currentState, boolean Pass_reCAPTCHA) {
		String nextStatusValue = null;
		
		if(currentState.equals("null") && Pass_reCAPTCHA) {
			nextStatusValue = "Legitimate";
		} else if(currentState.equals("null") && !Pass_reCAPTCHA) {
			nextStatusValue = "Suspicious";
		} else if(currentState.equals("Suspicious") && Pass_reCAPTCHA) {
			nextStatusValue = "Legitimate";
		} else if(currentState.equals("Suspicious") && !Pass_reCAPTCHA) {
			nextStatusValue = "Malicious";
		} else if(currentState.equals("Malicious") && Pass_reCAPTCHA) {
			nextStatusValue = "Legitimate";
		} else if(currentState.equals("Malicious") && !Pass_reCAPTCHA) {
			nextStatusValue = "Malicious";
		} else if(currentState.equals("Legitimate") && Pass_reCAPTCHA) {
			nextStatusValue = "Legitimate";
		} else if(currentState.equals("Legitimate") && !Pass_reCAPTCHA) {
			nextStatusValue = "Suspicious";
		}
		
		return nextStatusValue;
	}
	
	private double getNextTF(String currentState, double currentTF, boolean Pass_reCAPTCHA, double previousTF) {
		double nextTFValue = 0.0;
		if(currentState.equals("null") && Pass_reCAPTCHA) {
			nextTFValue = currentTF + CloudSim.getTFCoefficientOfVariation() * 0.01;
		} else if(currentState.equals("null") && !Pass_reCAPTCHA) {
			nextTFValue = currentTF - CloudSim.getTFCoefficientOfVariation() * 0.02;
		} else if(currentState.equals("Suspicious") && Pass_reCAPTCHA) {
			nextTFValue = currentTF + CloudSim.getTFCoefficientOfVariation() * 0.01;
		} else if(currentState.equals("Suspicious") && !Pass_reCAPTCHA) {
			nextTFValue = currentTF - CloudSim.getTFCoefficientOfVariation() * 0.03;
		} else if(currentState.equals("Malicious") && Pass_reCAPTCHA) {
			nextTFValue = currentTF + CloudSim.getTFCoefficientOfVariation() * 0.007;
		} else if(currentState.equals("Malicious") && !Pass_reCAPTCHA) {
			nextTFValue = currentTF - CloudSim.getTFCoefficientOfVariation() * previousTF;
		} else if(currentState.equals("Legitimate") && Pass_reCAPTCHA) {
			nextTFValue = currentTF + CloudSim.getTFCoefficientOfVariation() * previousTF;
		} else if(currentState.equals("Legitimate") && !Pass_reCAPTCHA) {
			nextTFValue = currentTF - CloudSim.getTFCoefficientOfVariation() * 0.02;
		}
		
		if(nextTFValue < 0.0) {
			nextTFValue =  0.0;
		} else if(nextTFValue > 1.0) {
			nextTFValue = 1.0;
		}
		
		return nextTFValue;
	}
	
	private double getPreviousTF(String currentState, boolean Pass_reCAPTCHA, double previousTF) {
		if((currentState.equals("null") && Pass_reCAPTCHA) || (currentState.equals("Suspicious") && Pass_reCAPTCHA) || (currentState.equals("Malicious") && Pass_reCAPTCHA)) {
			previousTF = 0.01;
		} else if(currentState.equals("Suspicious") && !Pass_reCAPTCHA) {
			previousTF = 0.03;
		} else if(currentState.equals("Malicious") && !Pass_reCAPTCHA) {
			previousTF = 2 * previousTF;
		} else if(currentState.equals("Legitimate") && Pass_reCAPTCHA) {
			previousTF = 2 * previousTF;
		}
		
		return previousTF;
	}
	
	private void LeastLoadedVM(Cloudlet cl) {
		double minFinishingTime = Double.MAX_VALUE, finishingTime;
		int vmId = 0, firstVm = 1, minConnectionCount = 1, connectionCount;
		
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			
			for (Vm vm : host.getVmList()) {
				finishingTime = vm.getNextAvailableTime();
				connectionCount = vm.getCloudletScheduler().runningCloudlets();
					
				if(firstVm == 1) {
					minFinishingTime = finishingTime;
					minConnectionCount = connectionCount;
					vmId = vm.getId();
						
					firstVm = 0;
				} else {
					if(finishingTime < minFinishingTime) {
						minFinishingTime = finishingTime;
						minConnectionCount = connectionCount;
						vmId = vm.getId();
					} else if(finishingTime == minFinishingTime && connectionCount < minConnectionCount) {
						minConnectionCount = connectionCount;
						vmId = vm.getId();
					}
				}
			}
		}
		
		cl.setVmId(vmId);
	}	
	
	/**
	 * Predict file transfer time.
	 * 
	 * @param requiredFiles the required files
	 * @return the double
	 */
	protected double predictFileTransferTime(List<String> requiredFiles) {
		double time = 0.0;

		Iterator<String> iter = requiredFiles.iterator();
		while (iter.hasNext()) {
			String fileName = iter.next();
			for (int i = 0; i < getStorageList().size(); i++) {
				Storage tempStorage = getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				if (tempFile != null) {
					time += tempFile.getSize() / tempStorage.getMaxTransferRate();
					break;
				}
			}
		}
		return time;
	}

	/**
	 * Processes a Cloudlet resume request.
	 * 
	 * @param cloudletId resuming cloudlet ID
	 * @param userId ID of the cloudlet's owner
	 * @param ack $true if an ack is requested after operation
	 * @param vmId the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletResume(int cloudletId, int userId, int vmId, boolean ack) {
		double eventTime = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletResume(cloudletId);

		boolean status = false;
		if (eventTime > 0.0) { // if this cloudlet is in the exec queue
			status = true;
			if (eventTime > CloudSim.clock()) {
				schedule(getId(), eventTime, CloudSimTags.VM_DATACENTER_EVENT);
			}
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_RESUME_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet pause request.
	 * 
	 * @param cloudletId resuming cloudlet ID
	 * @param userId ID of the cloudlet's owner
	 * @param ack $true if an ack is requested after operation
	 * @param vmId the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletPause(int cloudletId, int userId, int vmId, boolean ack) {
		boolean status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletPause(cloudletId);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_PAUSE_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet cancel request.
	 * 
	 * @param cloudletId resuming cloudlet ID
	 * @param userId ID of the cloudlet's owner
	 * @param vmId the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletCancel(int cloudletId, int userId, int vmId) {
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);
		sendNow(userId, CloudSimTags.CLOUDLET_CANCEL, cl);
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void updateCloudletProcessing() {
		// if some time passed since last processing
		// R: for term is to allow loop at simulation start. Otherwise, one initial
		// simulation step is skipped and schedulers are not properly initialized
		if (CloudSim.clock() < 0.111 || CloudSim.clock() > getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
			List<? extends Host> list = getVmAllocationPolicy().getHostList();
			double smallerTime = Double.MAX_VALUE;
			// for each host...
			for (int i = 0; i < list.size(); i++) {
				Host host = list.get(i);
				// inform VMs to update processing
				double time = host.updateVmsProcessing(CloudSim.clock());
				// what time do we expect that the next cloudlet will finish?
				if (time < smallerTime) {
					smallerTime = time;
				}
			}
			//Ahmad
			// gurantees a minimal interval before scheduling the event
			//smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01
			if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents()) {
				smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents();
			}
		
			if ((smallerTime != Double.MAX_VALUE) && (smallerTime - CloudSim.clock() != 0.0) ) {
				schedule(getId(), (smallerTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
			}
			setLastProcessTime(CloudSim.clock());
		}
		
	}
	
	protected void processCloudletFinishing(Cloudlet cl) {
		int userId = cl.getUserId();
		int vmId = cl.getVmId();
		Host host = getVmAllocationPolicy().getHost(vmId, userId);
		Vm vm = host.getVm(vmId, userId);
		vm.getCloudletScheduler().finishedCloudlet(cl);
		
		sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
		
		cloudletFinishedWebServer(cl);
		
		if(cl.getPass_reCAPTCHA()) {
			updateTotalRequestsServed();
			updateTotalRequestsResponseTime(cl.getWebServersTimingInfo().getDeparturingTime() - cl.getLoadbalancerTimingInfo().getArrivingTime());
		}
		
		
		if(CloudSim.getCloudType() == CloudSimTags.EDoS_Algorithm) {
			checkAverageCPUUtilizationForScalling();
		}
	}

	/**
	 * Verifies if some cloudlet inside this PowerDatacenter already finished. If yes, send it to
	 * the User/Broker
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
						
						cloudletFinishedWebServer(cl);
						
						if(cl.getPass_reCAPTCHA()) {
							updateTotalRequestsServed();
							updateTotalRequestsResponseTime(cl.getWebServersTimingInfo().getDeparturingTime() - cl.getLoadbalancerTimingInfo().getArrivingTime());
						}
					}
				}
			}
		}
	}

	// Ahmad
	/**
	 * calculate the average CPU utilization for all VMs
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void getAverageCPUUtilization() {
		double averageUtilization = 0, totalHostUtilization = 0;
		int totalVmCount = 0;
		
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
			
		// for each host...
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			
			totalHostUtilization += host.getTotalCPUUtilization(CloudSim.clock());
			totalVmCount += host.getVmList().size();
		}

		
		if(totalVmCount > 0){
			averageUtilization = totalHostUtilization/totalVmCount;
		} else {
			averageUtilization = 0;
		}
		
		currentUtilization = averageUtilization;
		VMCount = totalVmCount;
	}
	
	protected void checkAverageCPUUtilizationForScalling() {
		double averageUtilization = currentUtilization;
		long totalVmCount = (long) VMCount;

		if(totalVmCount > 0) {
			if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
				if((averageUtilization > CloudSim.getScaleUpCpuUtilizationThr()) && (totalVmCount < CloudSim.getVmsCountUpperThr())) {
					Log.printLine(CloudSim.clock() + ": Scaling Up .... CPU Utilization is " + averageUtilization + "%");	
					sendNow(getVmList().get(0).getUserId(), CloudSimTags.CLOUD_SCALE_UP, getId());	
				} else if(averageUtilization < CloudSim.getScaleDownCpuUtilizationThr()) {
					if(totalVmCount > CloudSim.getVmsCountLowerThr()){
						Vm vm = getFreeVM();
						if(vm != null){
							Log.printLine(CloudSim.clock() + ": Scaling Down .... CPU Utilization is " + averageUtilization + "%");
							sendNow(vm.getUserId(), CloudSimTags.CLOUD_SCALE_DOWN, vm);
						}
					}
				}
			} else {
				checkForScallingUp(averageUtilization, totalVmCount);	
				checkForScallingDown(averageUtilization, totalVmCount);
			}
		}
	}
	
	// Ahmad
	/**
	 * check the cpu utilization for scalling up
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void checkForScallingUp(double averageUtilization, long VmCount) {
		if(averageUtilization > CloudSim.getScaleUpCpuUtilizationThr()) {
			if(CloudSim.getTimeBreakScaleUpCpuUtilizationThr() == -1.0){
				CloudSim.setTimeBreakScaleUpCpuUtilizationThr(CloudSim.clock());
			}else {
				if(CloudSim.clock() - CloudSim.getTimeBreakScaleUpCpuUtilizationThr() >= CloudSim.getScaleUpTimer()){
					if(VmCount < CloudSim.getVmsCountUpperThr()){
						if(CloudSim.getCloudType() == CloudSimTags.EDoS) {
							Log.printLine(CloudSim.clock() + ": Scaling Up .... CPU Utilization is " + averageUtilization + "%");
								
							sendNow(getVmList().get(0).getUserId(), CloudSimTags.CLOUD_SCALE_UP, getId());
								
							CloudSim.setTimeBreakScaleUpCpuUtilizationThr(CloudSim.clock());	
						}
					}
				}
			}
			CloudSim.setTimeBreakScaleUpCpuUtilizationLowerThr(-1.0);
		} else {
			if(averageUtilization > CloudSim.getScaleUpLowercpuUtilizationThr() && CloudSim.getTimeBreakScaleUpCpuUtilizationThr() != -1.0) {
				if(CloudSim.getTimeBreakScaleUpCpuUtilizationLowerThr() == -1.0){
					CloudSim.setTimeBreakScaleUpCpuUtilizationLowerThr(CloudSim.clock());
				}else{
					if(CloudSim.clock() - CloudSim.getTimeBreakScaleUpCpuUtilizationLowerThr() >= CloudSim.getScaleUpLowerTimer()){
						CloudSim.setTimeBreakScaleUpCpuUtilizationThr(-1.0);
						CloudSim.setTimeBreakScaleUpCpuUtilizationLowerThr(-1.0);
					}
				}
			} else {
				CloudSim.setTimeBreakScaleUpCpuUtilizationThr(-1.0);
				CloudSim.setTimeBreakScaleUpCpuUtilizationLowerThr(-1.0);
			}
		}
	}
	
	// Ahmad
	/**
	 * check the cpu utilization for scalling down
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void checkForScallingDown(double averageUtilization, long VmCount) {
		if(averageUtilization < CloudSim.getScaleDownCpuUtilizationThr()){
			if(CloudSim.getTimeBreakScaleDownCpuUtilizationThr() == -1.0){
				CloudSim.setTimeBreakScaleDownCpuUtilizationThr(CloudSim.clock());
				//send(getId(), CloudSim.getScaleDownTimer(), CloudSimTags.VM_DATACENTER_EVENT);
			}else{
				if(CloudSim.clock() - CloudSim.getTimeBreakScaleDownCpuUtilizationThr() >= CloudSim.getScaleDownTimer()){
					if(VmCount > CloudSim.getVmsCountLowerThr()){
						Vm vm = getFreeVM();
						if(vm != null){
							Log.printLine(CloudSim.clock() + ": Scaling Down .... CPU Utilization is " + averageUtilization + "%");
		
							sendNow(vm.getUserId(), CloudSimTags.CLOUD_SCALE_DOWN, vm);
							
							CloudSim.setTimeBreakScaleDownCpuUtilizationThr(CloudSim.clock());
							//send(getId(), CloudSim.getScaleDownTimer(), CloudSimTags.VM_DATACENTER_EVENT);
						}
					}
				}
			}
			CloudSim.setTimeBreakScaleDownCpuUtilizationUpperThr(-1.0);
		}else{
			if(averageUtilization < CloudSim.getScaleDownUppercpuUtilizationThr() && CloudSim.getTimeBreakScaleDownCpuUtilizationThr() != -1.0){
				if(CloudSim.getTimeBreakScaleDownCpuUtilizationUpperThr() == -1.0){
					CloudSim.setTimeBreakScaleDownCpuUtilizationUpperThr(CloudSim.clock());
					//send(getId(), CloudSim.getScaleDownUpperTimer(), CloudSimTags.VM_DATACENTER_EVENT);
				}else{
					if(CloudSim.clock() - CloudSim.getTimeBreakScaleDownCpuUtilizationUpperThr() >= CloudSim.getScaleDownUpperTimer()){
						CloudSim.setTimeBreakScaleDownCpuUtilizationThr(-1.0);
					}
				}
			}else{
				CloudSim.setTimeBreakScaleDownCpuUtilizationThr(-1.0);
				CloudSim.setTimeBreakScaleDownCpuUtilizationUpperThr(-1.0);
			}
		}
	}	
	
	// Ahmad
	private Vm getFreeVM() {
		int done = 0;
		Vm vmToRemove = null;
		
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			
			for (Vm vm : host.getVmList()) {					
				if(vm.getCloudletScheduler().runningCloudlets() == 0 && CloudSim.clock() >= vm.getNextAvailableTime()) {
					vmToRemove = vm;
					done = 1;
					break;
				}
			}
			
			if(done == 1) {
				break;
			}
		}
			
		return vmToRemove;
	}	
	/**
	 * Adds a file into the resource's storage before the experiment starts. If the file is a master
	 * file, then it will be registered to the RC when the experiment begins.
	 * 
	 * @param file a DataCloud file
	 * @return a tag number denoting whether this operation is a success or not
	 */
	public int addFile(File file) {
		if (file == null) {
			return DataCloudTags.FILE_ADD_ERROR_EMPTY;
		}

		if (contains(file.getName())) {
			return DataCloudTags.FILE_ADD_ERROR_EXIST_READ_ONLY;
		}

		// check storage space first
		if (getStorageList().size() <= 0) {
			return DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;
		}

		Storage tempStorage = null;
		int msg = DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			if (tempStorage.getAvailableSpace() >= file.getSize()) {
				tempStorage.addFile(file);
				msg = DataCloudTags.FILE_ADD_SUCCESSFUL;
				break;
			}
		}

		return msg;
	}

	/**
	 * Checks whether the resource has the given file.
	 * 
	 * @param file a file to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(File file) {
		if (file == null) {
			return false;
		}
		return contains(file.getName());
	}

	/**
	 * Checks whether the resource has the given file.
	 * 
	 * @param fileName a file name to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}

		Iterator<Storage> it = getStorageList().iterator();
		Storage storage = null;
		boolean result = false;

		while (it.hasNext()) {
			storage = it.next();
			if (storage.contains(fileName)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Deletes the file from the storage. Also, check whether it is possible to delete the file from
	 * the storage.
	 * 
	 * @param fileName the name of the file to be deleted
	 * @return the error message
	 */
	private int deleteFileFromStorage(String fileName) {
		Storage tempStorage = null;
		File tempFile = null;
		int msg = DataCloudTags.FILE_DELETE_ERROR;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			tempFile = tempStorage.getFile(fileName);
			tempStorage.deleteFile(fileName, tempFile);
			msg = DataCloudTags.FILE_DELETE_SUCCESSFUL;
		} // end for

		return msg;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		// this resource should register to regional GIS.
		// However, if not specified, then register to system GIS (the
		// default CloudInformationService) entity.
		int gisID = CloudSim.getEntityId(regionalCisName);
		if (gisID == -1) {
			gisID = CloudSim.getCloudInfoServiceEntityId();
		}

		// send the registration to GIS
		sendNow(gisID, CloudSimTags.REGISTER_RESOURCE, getId());
		// Below method is for a child class to override
		registerOtherEntity();
	}

	/**
	 * Gets the host list.
	 * 
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Host> List<T> getHostList() {
		return (List<T>) getCharacteristics().getHostList();
	}

	/**
	 * Gets the characteristics.
	 * 
	 * @return the characteristics
	 */
	protected DatacenterCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**
	 * Sets the characteristics.
	 * 
	 * @param characteristics the new characteristics
	 */
	protected void setCharacteristics(DatacenterCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	/**
	 * Gets the regional cis name.
	 * 
	 * @return the regional cis name
	 */
	protected String getRegionalCisName() {
		return regionalCisName;
	}

	/**
	 * Sets the regional cis name.
	 * 
	 * @param regionalCisName the new regional cis name
	 */
	protected void setRegionalCisName(String regionalCisName) {
		this.regionalCisName = regionalCisName;
	}

	/**
	 * Gets the vm allocation policy.
	 * 
	 * @return the vm allocation policy
	 */
	public VmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}

	/**
	 * Sets the vm allocation policy.
	 * 
	 * @param vmAllocationPolicy the new vm allocation policy
	 */
	protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = vmAllocationPolicy;
	}

	/**
	 * Gets the last process time.
	 * 
	 * @return the last process time
	 */
	protected double getLastProcessTime() {
		return lastProcessTime;
	}

	/**
	 * Sets the last process time.
	 * 
	 * @param lastProcessTime the new last process time
	 */
	protected void setLastProcessTime(double lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}

	/**
	 * Gets the storage list.
	 * 
	 * @return the storage list
	 */
	protected List<Storage> getStorageList() {
		return storageList;
	}

	/**
	 * Sets the storage list.
	 * 
	 * @param storageList the new storage list
	 */
	protected void setStorageList(List<Storage> storageList) {
		this.storageList = storageList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the scheduling interval.
	 * 
	 * @return the scheduling interval
	 */
	protected double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the scheduling interval.
	 * 
	 * @param schedulingInterval the new scheduling interval
	 */
	protected void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

	// Ahmad
	/**
	 * Returns number of vms used in the simulation.
	 * @return the number of vms used in the simulation.
	 */
	public long getUsedVMs() {
	    return usedVMs;
	}

	public void setUsedVMs() {
		if(getVmList().size() > usedVMs){
			usedVMs = getVmList().size();
		}
	}
	
	/**
	 * Returns total cost of vms.
	 * @return the total cost of vms.
	 */
	public double getCostOfUsedVMs() {
	    return getUsedVMs()*getCharacteristics().getCostPerVm();
	}
	
	public List<Vm> getWebServersInformation() {
		return webservers;
	}
	
	public void setNewVmResultInf() {
		if(CloudSim.clock() > 0) {	
			double averageUtilization = currentUtilization;
			int totalVmCount = (int) VMCount;
			double rt = 0;
			
			if(getTotalRequestsServed() > 0) {
				rt = getTotalRequestsResponseTime()/getTotalRequestsServed();
			}
			
			getAverageVmResultInformation().add(new SystemInformation((int) CloudSim.clock(), totalVmCount, averageUtilization, rt, getTotalRequestsServed()));
			
			resetTotalRequestsServed();
			resetTotalRequestsResponseTime();
		}
	}
	
	public List<SystemInformation> getAverageVmResultInformation() {
		return avg;
	}
	
	public List<CloudModeInformation> getCloudModeInformation() {
		return cloudModeInformation;
	}
	
	
	public List<List<GeneralInformation>> getAllTrustFactorInformation() {
		List<List<GeneralInformation>> allTF = new ArrayList<List<GeneralInformation>> ();
		
		allTF.add(set_1_TF);
		allTF.add(set_2_TF);
		allTF.add(set_3_TF);
		allTF.add(set_4_TF);
		allTF.add(set_5_TF);
		
		return allTF;
	}
	
	public List<List<GeneralInformation>> getAllreCARTCHAInformation() {
		List<List<GeneralInformation>> allreCAPTCHA = new ArrayList<List<GeneralInformation>> ();
		
		allreCAPTCHA.add(set_1_reCAPTCHA);
		allreCAPTCHA.add(set_2_reCAPTCHA);
		
		return allreCAPTCHA;
	}
	
	public List<GeneralInformation> getFalseNegative() {
		return falseNegative;
	}
	
	public List<GeneralInformation> getFalsePositive() {
		return falsePositive;
	}
	
	public void updateTotalRequestsServed() {
		requestsServed = requestsServed + 1;
	}
	
	public int getTotalRequestsServed() {
		return requestsServed;
	}
	
	public void resetTotalRequestsServed() {
		requestsServed = 0;
	}
	
	public void updateTotalRequestsResponseTime(double res) {
		totalRequestsResponseTime = totalRequestsResponseTime + (res * 1000);
	}
	
	public double getTotalRequestsResponseTime() {
		return totalRequestsResponseTime;
	}
	
	public void resetTotalRequestsResponseTime() {
		totalRequestsResponseTime = 0;
	}
}
