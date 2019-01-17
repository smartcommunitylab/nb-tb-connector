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

	@PostMapping(value = "/api/nb/{tbCredentialsId}")
	public void storeData(
			@RequestBody String nbData,
			@PathVariable String tbCredentialsId,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		JsonNode rootNode = dataManager.getJsonNode(nbData);
		String sur = rootNode.get("m2m:sgn").get("sur").asText();
		String[] strings = sur.split("/");
		String ae = strings[1];
		String msIsdn = strings[2];
		Device device = deviceRepository.findByNbMsIsdn(ae, msIsdn);
		if(device == null) {
			logger.warn(String.format("device not found: %s - %s", ae, msIsdn));
			throw new EntityNotFoundException("device not found");
		}
		if(!device.getTbCredentialsId().equals(tbCredentialsId)) {
			logger.warn(String.format("credentials not verified: %s - %s - %s", ae, msIsdn, tbCredentialsId));
			throw new EntityNotFoundException("credentials not verified");
		}
		dataManager.sendTelemetry(device, rootNode);
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
