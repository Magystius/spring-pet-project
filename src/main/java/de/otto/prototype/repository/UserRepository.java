package de.otto.prototype.repository;

import de.otto.prototype.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

	@Query("select u from User u")
	Stream<User> streamAll();

}
