package it.smartcommunitylab.nbtb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.nbtb.model.Customer;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

}
