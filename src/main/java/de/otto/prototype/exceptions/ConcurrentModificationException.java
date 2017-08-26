package de.otto.prototype.exceptions;

public class ConcurrentModificationException extends RuntimeException {
	public ConcurrentModificationException(String message) {
		super(message);
	}
}
