package de.otto.prototype.controller;

import com.google.common.hash.HashCode;
import de.otto.prototype.model.Hashable;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.hash.Hashing.sha256;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpHeaders.ETAG;

public abstract class BaseController {

	List<Link> determineLinks(final Identifiable data, List<? extends Identifiable> listOfAllData, Class relativePath) {
		final List<Link> links = new ArrayList<>();
		links.add(linkTo(relativePath).slash(data).withSelfRel());
		links.add(linkTo(relativePath).slash(listOfAllData.get(0)).withRel("start"));
		int indexOfUser = listOfAllData.indexOf(data);
		if (indexOfUser > 0)
			links.add(linkTo(relativePath).slash(listOfAllData.get(indexOfUser - 1)).withRel("prev"));
		if (indexOfUser < (listOfAllData.size() - 1))
			links.add(linkTo(relativePath).slash(listOfAllData.get(indexOfUser + 1)).withRel("next"));

		return links;
	}

	MultiValueMap<String, String> getETagHeader(final Hashable data) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(ETAG, data.getETag());
		return headers;
	}

	//TODO: do we really need this extends?
	MultiValueMap<String, String> getETagHeader(final List<? extends Hashable> data) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		final String combinedETags = data.stream().map(Hashable::getETag).reduce("", (eTag1, eTag2) -> eTag1 + "," + eTag2);
		final HashCode hashCode = sha256().newHasher().putString(combinedETags, UTF_8).hash();
		headers.add(ETAG, hashCode.toString());
		return headers;
	}
}
