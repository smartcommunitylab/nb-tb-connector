package it.smartcommunitylab.nbtb.model;

import org.springframework.data.annotation.Id;

public class Device {
	@Id
	private String id;
	private String name;
	private String type;
	private String tbId;
	private String tbTenantId;
	private String tbCredentialsId;
	private String tbCredentialsType;
	private String nbAe;
	private String nbMsIsdn;
	
	public String getTbTenantId() {
		return tbTenantId;
	}
	public void setTbTenantId(String tenantId) {
		this.tbTenantId = tenantId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTbCredentialsId() {
		return tbCredentialsId;
	}
	public void setTbCredentialsId(String tbCredentialsId) {
		this.tbCredentialsId = tbCredentialsId;
	}
	public String getTbCredentialsType() {
		return tbCredentialsType;
	}
	public void setTbCredentialsType(String tbCredentialsType) {
		this.tbCredentialsType = tbCredentialsType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTbId() {
		return tbId;
	}
	public void setTbId(String tbId) {
		this.tbId = tbId;
	}
	public String getNbAe() {
		return nbAe;
	}
	public void setNbAe(String nbAe) {
		this.nbAe = nbAe;
	}
	public String getNbMsIsdn() {
		return nbMsIsdn;
	}
	public void setNbMsIsdn(String nbMsIsdn) {
		this.nbMsIsdn = nbMsIsdn;
	}
	
}
