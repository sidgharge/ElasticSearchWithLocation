package com.bridgelabz.location.util;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RestCallUtililty {
	public JsonNode getResponse(String url) throws JsonProcessingException, IOException {
		
		ResteasyClient restCall = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = restCall.target(url);

		Response response = target.request().accept(MediaType.APPLICATION_JSON).get();

		String responseString = response.readEntity(String.class);

		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode responseJson = null;

		responseJson = mapper.readTree(responseString);
		
		restCall.close();
		
		return responseJson;
	}
}