package de.otto.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.HashFunction;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;

import static com.google.common.hash.Hashing.sha256;
import static org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE;

@Document
@Value
@Builder(toBuilder = true)
public class User implements Identifiable<String> {

    public static final int AGE_YOUNGEST = 18;
    public static final int AGE_OLDEST = 150;

    @Id
    @NotNull(groups = Existing.class, message = "error.id.existing")
    @Null(groups = New.class, message = "error.id.new")
    private final String id;

    @NotEmpty(message = "error.name.empty")
    @Size(min = 3, max = 30, message = "error.name.range")
    private final String firstName;

    @Size(min = 3, max = 30, message = "error.name.range")
    private final String secondName;

    @NotEmpty(message = "error.name.empty")
    @Size(min = 3, max = 30, message = "error.name.range")
    private final String lastName;

    @NotNull(message = "error.age.empty")
    @Min(value = AGE_YOUNGEST, message = "error.age.young")
    @Max(value = AGE_OLDEST, message = "error.age.old")
    private final int age;

    private final boolean vip;

    @Valid
    private final Login login;

    @SafeHtml(whitelistType = NONE, message = "error.bio.invalid")
    private final String bio;

    @JsonIgnore
    public String getETag() {
        HashFunction hashFunction = sha256();
        return hashFunction.newHasher()
                .putObject(this, UserFunnel.INSTANCE)
                .hash().toString();
    }

    public interface Existing extends Default {
    }

    public interface New extends Default {
    }
}
