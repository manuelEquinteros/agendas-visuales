package com.agendasvisuales.pictogram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.agendasvisuales.pictogram.api.dto.PictogramaCandidate;
import com.agendasvisuales.pictogram.application.PictogramService;
import com.agendasvisuales.pictogram.integration.ArasaacClient;
import com.agendasvisuales.pictogram.integration.ArasaacMapper;
import com.sun.net.httpserver.HttpServer;

class PictogramServiceTest {

	private HttpServer server;

	private PictogramService pictogramService;

	@BeforeEach
	void setUp() throws IOException {

		server = HttpServer.create(new InetSocketAddress(0), 0);

		server.createContext("/v1/pictograms/es/search/desayunar", exchange -> {

			String response = """
					[
					  {
					    "_id": 4625,
					    "keywords": [
					      {
					        "keyword": "desayunar"
					      }
					    ]
					  },
					  {
					    "_id": 7012,
					    "keywords": [
					      {
					        "keyword": "desayunar"
					      }
					    ]
					  }
					]
					""";

			byte[] body = response.getBytes(StandardCharsets.UTF_8);

			exchange.getResponseHeaders().add("Content-Type", "application/json");

			exchange.sendResponseHeaders(200, body.length);

			try (OutputStream outputStream = exchange.getResponseBody()) {

				outputStream.write(body);
			}
		});

		server.start();

		String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/v1";

		ArasaacClient arasaacClient = new ArasaacClient(baseUrl, Duration.ofSeconds(1), Duration.ofSeconds(2));

		ArasaacMapper arasaacMapper = new ArasaacMapper();

		pictogramService = new PictogramService(arasaacClient, arasaacMapper);
	}

	@AfterEach
	void tearDown() {

		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void shouldSearchAndMapPictograms() {

		List<PictogramaCandidate> result = pictogramService.search("desayunar");

		assertEquals(2, result.size());

		PictogramaCandidate first = result.getFirst();

		assertEquals(4625L, first.idArasaac());

		assertEquals("desayunar", first.descripcion());

		assertEquals("https://static.arasaac.org/pictograms/4625/4625_500.png", first.urlImagen());
	}

	@Test
	void shouldReturnEmptyListForBlankSearch() {

		List<PictogramaCandidate> result = pictogramService.search("   ");

		assertTrue(result.isEmpty());
	}
}