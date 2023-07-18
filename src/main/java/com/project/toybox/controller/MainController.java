package com.project.toybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.toybox.httpclient.TMDb;
import com.project.toybox.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@PropertySource("classpath:application-information.properties")
public class MainController {
    @Autowired
    SignService signService;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Value("${tMDbApiKey:tMDbApiKey}")
    private String tMDbApiKey;

    @Value("${tMDbToken:tMDbToken}")
    private String tMDbToken;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("/")
    public String main(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/n";
        }

        String name = signService.userName(principal.getName());

        JsonNode movie = TMDb.getMovie(tMDbApiKey, tMDbToken);

        List<Map<String, String>> movieList = new ArrayList<>();
        for (int i = 0; i < movie.get("results").size(); i++) {
            Map<String, String> movieMap = new HashMap<>();
            movieMap.put("movieImage", movie.get("results").get(i).get("poster_path").asText());
            movieMap.put("movieTitle", movie.get("results").get(i).get("title").asText());
            movieMap.put("movieOverview", movie.get("results").get(i).get("overview").asText());
            movieList.add(movieMap);
        }

        model.addAttribute("name", name);
        model.addAttribute("movieList", movieList);
        model.addAttribute("movieBanner", movie.get("results").get(0).get("backdrop_path").asText());

        return "Main";
    }

    // 로그인 전 메인 페이지
    @GetMapping("/n")
    public String nmain(Model model) {
        JsonNode movie = TMDb.getMovie(tMDbApiKey, tMDbToken);

        List<Map<String, String>> movieList = new ArrayList<>();
        for (int i = 0; i < movie.get("results").size(); i++) {
            Map<String, String> movieMap = new HashMap<>();
            movieMap.put("movieImage", movie.get("results").get(i).get("poster_path").asText());
            movieMap.put("movieTitle", movie.get("results").get(i).get("title").asText());
            movieMap.put("movieOverview", movie.get("results").get(i).get("overview").asText());
            movieList.add(movieMap);
        }

        model.addAttribute("movieList", movieList);
        model.addAttribute("movieBanner", movie.get("results").get(0).get("backdrop_path").asText());

        return "Main";
    }
}
