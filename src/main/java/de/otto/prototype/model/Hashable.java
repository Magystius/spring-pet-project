package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Hashable {

	@JsonIgnore
	String getETag();
}
