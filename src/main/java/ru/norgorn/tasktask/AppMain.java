package ru.norgorn.tasktask;

import ru.norgorn.tasktask.model.LibInfo;
import ru.norgorn.tasktask.model.LibStatistics;
import ru.norgorn.tasktask.model.PageInfo;
import ru.norgorn.tasktask.service.HtmlParseService;
import ru.norgorn.tasktask.service.HttpClient;
import ru.norgorn.tasktask.service.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AppMain implements AutoCloseable {

    HttpClient httpClient = new HttpClient();
    HtmlParseService parseService = new HtmlParseService();

    public static void main(String[] args) {
        try (AppMain app = new AppMain()) {
            app.run();
        }
    }

    public void run() {
        String input = readInput();
        run(input);
    }

    public void run(String input) {
        Logger.debug("Input: " + input);
        Logger.debug("Now waiting for search results...");
        List<String> urls = getSearchResults(input);
        Logger.debug("Got search results, now waiting for pages...");
        List<PageInfo> pages = getPages(urls);
        List<LibStatistics> topLibs = findTopLibs(pages, 5);
        topLibs.forEach(s -> System.out.println(s.getCount() + " of " + s.getLib().getUrl()));
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String readInput() {
        System.out.println("Please, enter search phrase");
        try (Scanner in = new Scanner(System.in)) {
            return in.nextLine();
        }
    }

    List<String> getSearchResults(String input) {
        String url;
        try {
            url = "https://www.bing.com/search?q=" + URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // never happens
            throw new RuntimeException(e);
        }
        return httpClient.getBodyFromMicrosoft(url)
                .thenApply(b -> parseService.parseSearchResult(b.getBody()))
                .join();
    }

    List<PageInfo> getPages(List<String> urls) {
        return urls.stream()
                .map(httpClient::getBody)
                .map(f -> f.thenApply(parseService::parsePage))
                .collect(Collectors.toList()).stream() // Collect to guarantee eager calls
                .map(CompletableFuture::join).collect(Collectors.toList());
    }

    Collection<LibStatistics> collectLibStatistics(List<PageInfo> pages) {
        return pages.stream()
                .flatMap(p -> p.getLibs().stream())
                .collect(Collectors.toMap(LibInfo::getUrl, LibStatistics::new,
                        (s1, s2) -> {
                            s1.setCount(s1.getCount() + s2.getCount());
                            return s1;
                        })
                ).values();
    }

    List<LibStatistics> findTopLibs(List<PageInfo> pages, int count) {
        Collection<LibStatistics> statistics = collectLibStatistics(pages);
        return findTopLibs(statistics, count);
    }

    List<LibStatistics> findTopLibs(Collection<LibStatistics> statistics, int count) {
        return statistics.stream()
                .sorted(Comparator.comparingInt(LibStatistics::getCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
