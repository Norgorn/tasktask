package ru.norgorn.tasktask.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.norgorn.tasktask.model.HttpBodyResponse;
import ru.norgorn.tasktask.model.LibInfo;
import ru.norgorn.tasktask.model.PageInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HtmlParseService {

    public List<String> parseSearchResult(String body) {
        Document document = Jsoup.parse(body);
        return document.select("li.b_algo").stream()
                .map(e -> e
                        // there should be checks for nulls
                        .select(".b_title").first()
                        .select("h2").first()
                        .select("a").first()
                        .attr("href")
                ).collect(Collectors.toList());
    }

    public PageInfo parsePage(HttpBodyResponse response) {
        String url = response.getRequestedUrl();
        String body = response.getBody();
        Document document = Jsoup.parse(body);
        List<LibInfo> libs = document.select("script").stream()
                // this is definitely not the only way to link some JS lib and should be expanded before any real usage
                .map(e -> e.attr("src").trim())
                .filter(s -> s.length() > 0)
                .map(src -> new LibInfo(src, Optional.empty()))
                .collect(Collectors.toList());
        return new PageInfo(url, libs);
    }
}
