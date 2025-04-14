package com.modakdev.arcinema_image_search.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

@Component
public class UrlFetchClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UrlFetchClient(@Value("${flask.base-url}")String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public Object getUrlResponse(Object requestData)
    {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestData, httpHeaders);
        String requestUrl = baseUrl + "/get-url";
        ResponseEntity<Object> response = restTemplate.postForEntity(requestUrl, requestEntity, Object.class);
        return response.getBody();
    }
    public Object getNameResponse(Object requestData)
    {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestData, httpHeaders);
        String requestUrl = baseUrl + "/get-name";
        ResponseEntity<Object> response = restTemplate.postForEntity(requestUrl, requestEntity, Object.class);
        return response.getBody();
    }
}
