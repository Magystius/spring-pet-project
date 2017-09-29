package de.otto.prototype.controller.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidationRepresentation<T> {

	private final T data;

	private final List<ValidationEntryRepresentation> errors;

	@java.beans.ConstructorProperties({"data", "errors"})
	ValidationRepresentation(T data, List<ValidationEntryRepresentation> errors) {
		this.data = data;
		this.errors = errors;
	}

	public static <T> ValidationRepresentationBuilder<T> builder() {
		return new ValidationRepresentationBuilder<T>();
	}

	public T getData() {
		return this.data;
	}

	public List<ValidationEntryRepresentation> getErrors() {
		return this.errors;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof ValidationRepresentation)) return false;
		final ValidationRepresentation other = (ValidationRepresentation) o;
		final Object this$data = this.getData();
		final Object other$data = other.getData();
		if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
		final Object this$errors = this.getErrors();
		final Object other$errors = other.getErrors();
		return this$errors == null ? other$errors == null : this$errors.equals(other$errors);
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $data = this.getData();
		result = result * PRIME + ($data == null ? 43 : $data.hashCode());
		final Object $errors = this.getErrors();
		result = result * PRIME + ($errors == null ? 43 : $errors.hashCode());
		return result;
	}

	public String toString() {
		return "ValidationRepresentation(data=" + this.getData() + ", errors=" + this.getErrors() + ")";
	}

	public static class ValidationRepresentationBuilder<T> {
		private T data;
		private ArrayList<ValidationEntryRepresentation> errors;

		ValidationRepresentationBuilder() {
		}

		public ValidationRepresentation.ValidationRepresentationBuilder<T> data(T data) {
			this.data = data;
			return this;
		}

		public ValidationRepresentation.ValidationRepresentationBuilder<T> error(ValidationEntryRepresentation error) {
			if (this.errors == null) this.errors = new ArrayList<ValidationEntryRepresentation>();
			this.errors.add(error);
			return this;
		}

		public ValidationRepresentation.ValidationRepresentationBuilder<T> errors(Collection<? extends ValidationEntryRepresentation> errors) {
			if (this.errors == null) this.errors = new ArrayList<ValidationEntryRepresentation>();
			this.errors.addAll(errors);
			return this;
		}

		public ValidationRepresentation.ValidationRepresentationBuilder<T> clearErrors() {
			if (this.errors != null)
				this.errors.clear();

			return this;
		}

		public ValidationRepresentation<T> build() {
			List<ValidationEntryRepresentation> errors;
			switch (this.errors == null ? 0 : this.errors.size()) {
				case 0:
					errors = java.util.Collections.emptyList();
					break;
				case 1:
					errors = java.util.Collections.singletonList(this.errors.get(0));
					break;
				default:
					errors = java.util.Collections.unmodifiableList(new ArrayList<ValidationEntryRepresentation>(this.errors));
			}

			return new ValidationRepresentation<T>(data, errors);
		}

		public String toString() {
			return "ValidationRepresentation.ValidationRepresentationBuilder(data=" + this.data + ", errors=" + this.errors + ")";
		}
	}
}
