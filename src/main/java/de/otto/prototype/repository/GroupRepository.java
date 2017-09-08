package de.otto.prototype.repository;

import de.otto.prototype.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

	@Query("{}")
	Stream<Group> streamAll();
}