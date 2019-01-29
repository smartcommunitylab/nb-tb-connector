package it.smartcommunitylab.nbtb.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import it.smartcommunitylab.nbtb.core.DataManager;
import it.smartcommunitylab.nbtb.exception.EntityNotFoundException;
import it.smartcommunitylab.nbtb.model.Device;
import it.smartcommunitylab.nbtb.repository.DeviceRepository;
import it.smartcommunitylab.nbtb.utils.Utils;

@RestController
public class NBIoTController {
	private static final transient Logger logger = LoggerFactory.getLogger(NBIoTController.class);
	
	@Autowired
	private DataManager dataManager;
	
	@Autowired
	private DeviceRepository deviceRepository;

	@PostMapping(value = "/api/nb/{nbSubscriptionId}")
	public void storeData(
			@RequestBody String nbData,
			@PathVariable String nbSubscriptionId,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		try {
			JsonNode rootNode = dataManager.getJsonNode(nbData);
			String sur = rootNode.get("m2m:sgn").get("sur").asText();
			String[] strings = sur.split("/");
			String ae = strings[2];
			String msIsdn = strings[3];
			Device device = deviceRepository.findByNbMsIsdn(ae, msIsdn);
			if(device == null) {
				String error = String.format("device not found: %s - %s", ae, msIsdn); 
				logger.warn(error);
				throw new EntityNotFoundException(error);
			}
			if(!device.getNbSubscriptionId().equals(nbSubscriptionId)) {
				String error = String.format("subscription not verified: %s - %s - %s", ae, msIsdn, nbSubscriptionId);
				logger.warn(error);
				throw new EntityNotFoundException(error);
			}
			dataManager.sendTelemetry(device, rootNode);			
		} catch (Exception e) {
			if(e instanceof EntityNotFoundException) {
				throw e;
			} else {
				logger.error(String.format("storeData error:%s", e.getMessage()));
				throw e;
			}
		}
	}
	
	@ExceptionHandler({EntityNotFoundException.class})
	@ResponseStatus(value=HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String,String> handleEntityNotFoundError(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage());
		return Utils.handleError(exception);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Map<String,String> handleGenericError(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage());
		return Utils.handleError(exception);
	}		

}
