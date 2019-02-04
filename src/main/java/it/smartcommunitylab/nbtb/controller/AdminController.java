package it.smartcommunitylab.nbtb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.nbtb.core.DataManager;

@RestController
public class AdminController {
	private static final transient Logger logger = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private DataManager dataManager;
	
	@GetMapping(value = "/admin/init")
	public void initDataset() throws Exception {
		logger.info("initDataset: get TB user");
		dataManager.storeTbUser();
	}
	
	@GetMapping(value = "/admin/tb/device/refresh")
	public void alignTbDevices() throws Exception {
		logger.info("alignTbDevices: align TB devices");
		dataManager.storeTbDevices();
	}
}
