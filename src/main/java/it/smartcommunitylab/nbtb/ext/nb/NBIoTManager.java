package it.smartcommunitylab.nbtb.ext.nb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.nbtb.utils.HTTPUtils;
import it.smartcommunitylab.nbtb.utils.Utils;

@Component
public class NBIoTManager {
	private static final transient Logger logger = LoggerFactory.getLogger(NBIoTManager.class);
	
	@Value("${nb.endpoint}")
	private String endpoint;

	@Value("${nb.auth.username}")
	private String user;

	@Value("${nb.auth.password}")
	private String password;
	
	@Value("${nb.callback}")
	private String callbackEndpoint;
	
	public String addSubscription(String ae, String msIsdn) throws Exception {
		String address = endpoint + "onem2m/" + ae + "/" + msIsdn + "/inbox";
		BufferedReader buf = new BufferedReader(
				new InputStreamReader(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("subscription_req.json"), "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = buf.readLine()) != null) {
			sb.append(line).append("\n");
		}
		
		String subscriptionId = Utils.getUUID();
		String callBackUrl = callbackEndpoint + subscriptionId;
		String contentString = sb.toString();
		contentString = contentString.replace("{{name}}", subscriptionId);
		contentString = contentString.replace("{{url}}", callBackUrl);
		
		Map<String, String> headers = new HashMap<>();
		headers.put("X-M2M-RI", String.valueOf(System.currentTimeMillis()));
		headers.put("X-M2M-Origin", ae + "_prod");
		headers.put("Content-Type", "application/vnd.onem2m-res+json;ty=23");
		headers.put("Accept", "application/json");
		
		HTTPUtils.post(address, contentString, null, null, user, password, headers);
		if(logger.isInfoEnabled()) {
			logger.info(String.format("addSubscription:%s - %s - %s", ae, msIsdn, subscriptionId));
		}
		return subscriptionId;
	}

}
