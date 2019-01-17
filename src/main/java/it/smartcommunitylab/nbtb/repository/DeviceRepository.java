package it.smartcommunitylab.nbtb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.nbtb.model.Device;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {
	@Query(value="{nbAe:?0, nbMsIsdn:?1}")
	Device findByNbMsIsdn(String ae, String msIsdn);
	
	@Query(value="{tbTenantId:?0, tbId:?1}")
	Device findByTbId(String tenantId, String deviceId);
	
}
