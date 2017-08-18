package de.otto.prototype.repository;

import de.otto.prototype.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

	@Query("{}")
	Stream<User> streamAll();
}
