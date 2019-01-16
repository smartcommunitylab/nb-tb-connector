package it.smartcommunitylab.nbtb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.nbtb.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

}
