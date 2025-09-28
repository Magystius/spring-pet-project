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

	/**
	 * Generates an order-independent ETag for a list of hashable entities.
	 * The ETag remains consistent regardless of the order of items in the list,
	 * as long as the same set of items is present.
	 * 
	 * Performance optimizations:
	 * - Sorts ETags to ensure order independence
	 * - Uses String.join() for efficient concatenation
	 * - Handles empty lists gracefully
	 * 
	 * @param data List of hashable entities
	 * @return MultiValueMap containing the ETag header
	 */
	MultiValueMap<String, String> getETagHeader(final List<? extends Hashable> data) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		
		if (data.isEmpty()) {
			// Handle empty list case - use a consistent empty marker
			headers.add(ETAG, sha256().newHasher().putString("EMPTY_LIST", UTF_8).hash().toString());
			return headers;
		}
		
		// Sort individual ETags to ensure order independence
		// Use String.join() for better performance than reduce() with concatenation
		final String combinedETags = String.join(",", 
			data.stream()
				.map(Hashable::getETag)
				.sorted() // Key optimization: sort ETags before combining
				.toArray(String[]::new));
		
		final HashCode hashCode = sha256().newHasher().putString(combinedETags, UTF_8).hash();
		headers.add(ETAG, hashCode.toString());
		return headers;
	}
}
