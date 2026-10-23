package com.agendasvisuales.pictogram.integration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.agendasvisuales.shared.error.ArasaacUnavailableException;

@Component
public class ArasaacClient {

	private static final String LANGUAGE = "es";

	private final RestClient restClient;

	public ArasaacClient(

			@Value("${app.arasaac.base-url}") String baseUrl,

			@Value("${app.arasaac.connect-timeout}") Duration connectTimeout,

			@Value("${app.arasaac.read-timeout}") Duration readTimeout) {

		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

		requestFactory.setReadTimeout(readTimeout);

		this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
	}

	public List<ArasaacPictogramResponse> search(String term) {

		if (term == null || term.isBlank()) {
			return List.of();
		}

		try {

			List<ArasaacPictogramResponse> response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/pictograms/{language}/search/{searchText}").build(LANGUAGE,
							term.trim()))
					.retrieve().body(new ParameterizedTypeReference<List<ArasaacPictogramResponse>>() {
					});

			return response != null ? response : List.of();

		} catch (HttpClientErrorException.NotFound exception) {

			return List.of();
		} catch (RestClientException exception) {

			throw new ArasaacUnavailableException("No fue posible comunicarse con ARASAAC.", exception);
		}
	}
}