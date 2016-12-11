package com.kirylshreyter.training.hotel.webbalancer.controller;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kirylshreyter.training.hotel.web.cache.CacheMethods;

@RestController
@RequestMapping("/")
public class CommonController {

	@Inject
	private CacheMethods cacheMethods;

	private Integer getRandomServer() {
		Integer i = ThreadLocalRandom.current().nextInt(1, 3 + 1);
		return i;
	}

	@RequestMapping(value = "/{entityTree}", method = RequestMethod.GET)
	private ResponseEntity<List<Object>> getAllEntities(@PathVariable String entityTree) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet request = null;
		List<Object> myObject = null;
		String temportyUrl = "http://127.0.0.1:808" + getRandomServer() + "/%s";
		try {
			String targetUrl = String.format(temportyUrl, entityTree);
			request = new HttpGet(targetUrl);
			HttpResponse response = httpClient.execute(request);
			ObjectMapper objectMapper = new ObjectMapper();
			myObject = (List<Object>) objectMapper.readValue(response.getEntity().getContent(), Object.class);
		} catch (Exception ex) {
			return new ResponseEntity<List<Object>>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<List<Object>>(myObject, HttpStatus.OK);

	}

	@RequestMapping(value = "/{entityTree}/{entityId}", method = RequestMethod.GET)
	private ResponseEntity<Object> getEntity(@PathVariable String entityTree, @PathVariable Long entityId) {
		Object entityFromCache = cacheMethods.getEntityFromCache(entityTree + entityId);
		if (entityFromCache == null) {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = null;
			Object myObject = null;
			String temportyUrl = "http://127.0.0.1:808" + getRandomServer() + "/%s/%s";
			try {
				String targetUrl = String.format(temportyUrl, entityTree, entityId.toString());
				request = new HttpGet(targetUrl);
				HttpResponse response = httpClient.execute(request);
				ObjectMapper objectMapper = new ObjectMapper();
				myObject = objectMapper.readValue(response.getEntity().getContent(), Object.class);
			} catch (Exception ex) {
				return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
			}
			cacheMethods.putEntityInCache(entityTree + entityId, myObject);
			System.out.println("Object returned from database");
			return new ResponseEntity<Object>(myObject, HttpStatus.OK);
		} else {
			System.out.println("Object returned from cache");
			return new ResponseEntity<Object>(entityFromCache, HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/{entityTree}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	private ResponseEntity<Long> createEntity(@RequestBody String data, @PathVariable String entityTree) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = null;
		Long myObject = null;
		String temportyUrl = "http://127.0.0.1:808" + getRandomServer() + "/%s";
		try {
			String targetUrl = String.format(temportyUrl, entityTree);
			request = new HttpPost(targetUrl);
			ByteArrayEntity bae = new ByteArrayEntity(data.getBytes());
			request.addHeader("Content-Type", "application/json; charset=UTF-8");
			request.setEntity(bae);
			HttpResponse response = httpClient.execute(request);
			ObjectMapper objectMapper = new ObjectMapper();
			myObject = objectMapper.readValue(response.getEntity().getContent(), Long.class);
		} catch (Exception ex) {
			ex.printStackTrace();
			// return new ResponseEntity<Long>(Response);
		}
		Object entityFromCacheToModify = cacheMethods.getEntityFromCache(entityTree + myObject);
		if (entityFromCacheToModify != null) {
			cacheMethods.deleteFromCache(entityTree + myObject);
			System.out.println("Object was modified and deleted from cache.");
		}
		return new ResponseEntity<Long>(myObject, HttpStatus.CREATED);
	}

}