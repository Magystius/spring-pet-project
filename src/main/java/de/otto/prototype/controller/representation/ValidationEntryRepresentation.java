package de.otto.prototype.controller.representation;

public class ValidationEntryRepresentation {

	private final String attribute;

	private final String errorMessage;

	@java.beans.ConstructorProperties({"attribute", "errorMessage"})
	ValidationEntryRepresentation(String attribute, String errorMessage) {
		this.attribute = attribute;
		this.errorMessage = errorMessage;
	}

	public static ValidationEntryRepresentationBuilder builder() {
		return new ValidationEntryRepresentationBuilder();
	}

	public String getAttribute() {
		return this.attribute;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof ValidationEntryRepresentation)) return false;
		final ValidationEntryRepresentation other = (ValidationEntryRepresentation) o;
		final Object this$attribute = this.getAttribute();
		final Object other$attribute = other.getAttribute();
		if (this$attribute == null ? other$attribute != null : !this$attribute.equals(other$attribute)) return false;
		final Object this$errorMessage = this.getErrorMessage();
		final Object other$errorMessage = other.getErrorMessage();
		return this$errorMessage == null ? other$errorMessage == null : this$errorMessage.equals(other$errorMessage);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $attribute = this.getAttribute();
		result = result * PRIME + ($attribute == null ? 43 : $attribute.hashCode());
		final Object $errorMessage = this.getErrorMessage();
		result = result * PRIME + ($errorMessage == null ? 43 : $errorMessage.hashCode());
		return result;
	}

	public String toString() {
		return "ValidationEntryRepresentation(attribute=" + this.getAttribute() + ", errorMessage=" + this.getErrorMessage() + ")";
	}

	public static class ValidationEntryRepresentationBuilder {
		private String attribute;
		private String errorMessage;

		ValidationEntryRepresentationBuilder() {
		}

		public ValidationEntryRepresentation.ValidationEntryRepresentationBuilder attribute(String attribute) {
			this.attribute = attribute;
			return this;
		}

		public ValidationEntryRepresentation.ValidationEntryRepresentationBuilder errorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public ValidationEntryRepresentation build() {
			return new ValidationEntryRepresentation(attribute, errorMessage);
		}

		public String toString() {
			return "ValidationEntryRepresentation.ValidationEntryRepresentationBuilder(attribute=" + this.attribute + ", errorMessage=" + this.errorMessage + ")";
		}
	}
}
