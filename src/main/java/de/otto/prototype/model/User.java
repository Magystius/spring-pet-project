package de.otto.prototype.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String firstName;
	private String lastName;
	private int age;

	private String street;
	private String houseNumber;
	private String postalCode;
	private String city;
	private String country;
	private String planet;

	private boolean vip;
	private String login;
	private String mail;
	private String password;
}
