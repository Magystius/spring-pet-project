package de.otto.prototype.repository;

import de.otto.prototype.model.Group;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends ReactiveMongoRepository<Group, String> {

}