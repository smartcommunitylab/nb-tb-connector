package it.smartcommunitylab.nbtb.core;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.smartcommunitylab.nbtb.ext.nb.NBIoTManager;
import it.smartcommunitylab.nbtb.ext.tb.ThingsBoardManager;
import it.smartcommunitylab.nbtb.model.Customer;
import it.smartcommunitylab.nbtb.model.Device;
import it.smartcommunitylab.nbtb.model.User;
import it.smartcommunitylab.nbtb.repository.CustomerRepository;
import it.smartcommunitylab.nbtb.repository.DeviceRepository;
import it.smartcommunitylab.nbtb.repository.UserRepository;
import it.smartcommunitylab.nbtb.utils.Utils;

@Component
public class DataManager {
	private static final transient Logger logger = LoggerFactory.getLogger(DataManager.class);
	
	@Autowired
	private ThingsBoardManager tbManager;
	
	@Autowired
	private NBIoTManager nbIoTManager; 
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	private ObjectMapper mapper = null;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	
	@PostConstruct
	public void init() throws Exception {
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}
	
	public JsonNode getJsonNode(String json) throws IOException {
		JsonNode jsonNode = mapper.readTree(json);
		return jsonNode;
	}
	
	public void sendTelemetry(Device device, JsonNode rootNode) {
		try {
			String ae = device.getNbAe();
			String msIsdn = device.getNbMsIsdn();
			// check if exists in tb
			if(Utils.isNotEmpty(device.getTbTenantId()) &&
					Utils.isNotEmpty(device.getTbId())) {
				//send telemetry
				JsonNode objectNode = convertUplink(rootNode);
				long timestamp = objectNode.get("timestamp").asLong();
				tbManager.sendTelemetry(device, objectNode, timestamp);
				//TODO move log to debug level
				if(logger.isInfoEnabled()) {
					logger.info(String.format("sendTelemetry - sent data to device: %s / %s", ae, msIsdn));
				}				
			} else {
				if(logger.isInfoEnabled()) {
					logger.info(String.format("sendTelemetry - device not connected to TB: %s / %s", ae, msIsdn));
				}
			}
		} catch (Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info(String.format("sendTelemetry exception:%s", e.getMessage()));
			}
		}
	}
	
	private JsonNode convertUplink(JsonNode rootNode) {
		String con =  rootNode.get("m2m:sgn").get("nev").get("rep").get("m2m:cin").get("con").asText();
		String sur = rootNode.get("m2m:sgn").get("sur").asText();
		String ct = rootNode.get("m2m:sgn").get("nev").get("rep").get("m2m:cin").get("ct").asText();
		Date date = new Date();
		try {
			date = sdf.parse(ct);
		} catch (ParseException e) {
			if(logger.isInfoEnabled()) {
				logger.info("convertUplink - error in parsing ct: %s", sur);
			}			
		}
		ObjectNode uplinkNode = mapper.createObjectNode();
		uplinkNode.put("sur", sur);
		uplinkNode.put("ct", ct);
		uplinkNode.put("con", con);
		uplinkNode.put("timestamp", date.getTime());
		return uplinkNode;
	}

	public String getTbTenantId() {
		List<User> list = userRepository.findAll();
		if(list.size() > 0) {
			return list.get(0).getTbTenantId();
		}
		return null;
	}
	
	public void storeTbUser() throws Exception {
		User user = tbManager.getUser();
		Optional<User> userOpt = userRepository.findById(user.getTbId());
		if(userOpt.isPresent()) {
			User userDb = userOpt.get();
			userDb.setTbTenantId(user.getTbTenantId());
			userDb.setTbEmail(user.getTbEmail());
			userDb.setTbName(user.getTbName());
			userRepository.save(userDb);
		} else {
			userRepository.deleteAll();
			userRepository.save(user);
		}
	}
	
	public void storeTbCustomers() throws Exception {
		List<Customer> customers = tbManager.getCustomers();
		for (Customer customer : customers) {
			Optional<Customer> optional = customerRepository.findById(customer.getId());
			if(!optional.isPresent()) {
				customerRepository.save(customer);
			}
		}
	}
	
	public void storeTbDevices() throws Exception {
		List<Device> devices = tbManager.getDevicesByTenant();
		for (Device device : devices) {
			Device deviceDb = deviceRepository.findByTbId(device.getTbTenantId(), device.getTbId());
			if(deviceDb == null) {
				deviceDb = deviceRepository.save(device);
			} else {
				deviceDb.setName(device.getName());
				deviceDb.setType(device.getType());
				deviceDb.setTbCredentialsId(device.getTbCredentialsId());
				deviceDb.setTbCredentialsType(device.getTbCredentialsType());
				deviceRepository.save(deviceDb);
			}
			if(Utils.isNotEmpty(deviceDb.getNbAe()) && Utils.isNotEmpty(deviceDb.getNbMsIsdn()) &&
					Utils.isEmpty(deviceDb.getNbSubscriptionId())) {
				//add subscription
				try {
					String subscriptionId = nbIoTManager.addSubscription(deviceDb.getNbAe(), deviceDb.getNbMsIsdn());
					deviceDb.setNbSubscriptionId(subscriptionId);
					deviceRepository.save(deviceDb);
				} catch (Exception e) {
					logger.error(String.format("storeTbDevices error in add subscription:%s", e.getMessage()));
				}
			}
		}
	}
	
	@Scheduled(cron = "${cronexp}")
	public void refreshTbDevices() {
		if(logger.isInfoEnabled()) {
			logger.info("refreshTbDevices started");
		}
		try {
			storeTbDevices();
		} catch (Exception e) {
			logger.error(String.format("refreshTbDevices exception:%s", e.getMessage()));
		}
	}

}
