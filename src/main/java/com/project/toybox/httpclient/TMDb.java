package com.project.toybox.httpclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class TMDb {
    public static JsonNode getMovie(String tMDbApiKey, String tMDbToken) {
        final String requestUrl = "https://api.themoviedb.org/3/movie/now_playing?api_key=" + tMDbApiKey + "&language=ko";

        // https://api.themoviedb.org/3/movie/550?api_key=8e9f8da21bc5cbc0ed4fa5e7940f2eee

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet get = new HttpGet(requestUrl); // GET 방식

        JsonNode returnNode = null;

        try {
            get.addHeader("accept", "application/json");
            get.addHeader("Authorization", "Bearer " + tMDbToken);
            final HttpResponse response = client.execute(get);

            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnNode;
    }
}
