package com.alemdianov.api.services;


import com.alemdianov.api.utils.RestTemplateResponseErrorHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class BaseService {
    final public  static String BASE_URL = "https://petstore.swagger.io/v2";

    protected RestTemplate restTemplate;

    public BaseService() {
        this.restTemplate = createTemplate();
    }

    private static RestTemplate createTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        restTemplate.getMessageConverters().add(0, converter);
        return restTemplate;
    }

    public static HttpEntity getDefaultEntity() {
        return new HttpEntity(defaultHeaders());
    }

    public static HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_key", "special-key");
        headers.set("Accept", "application/json");
        return headers;
    }

    public static HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_key", "special-key");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    public static HttpHeaders mediaHaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_key", "special-key");
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }
}