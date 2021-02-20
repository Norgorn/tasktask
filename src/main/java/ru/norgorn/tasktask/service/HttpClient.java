package ru.norgorn.tasktask.service;

import org.asynchttpclient.*;
import ru.norgorn.tasktask.model.HttpBodyResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class HttpClient implements AutoCloseable {

    private final AsyncHttpClient httpClient;

    public HttpClient() {
        DefaultAsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(true)
                .setMaxRedirects(5)
                .setConnectTimeout(15000)
                .setRequestTimeout(15000)
                .setKeepAlive(false)
                .build();
        httpClient = new DefaultAsyncHttpClient(config);
    }

    public CompletableFuture<HttpBodyResponse> getBodyFromMicrosoft(String url) {
        BoundRequestBuilder builder = prepareGet(url)
                // This requires investigation to determine really required cookies
                .setHeader("Cookie",
                        "MUID=052CDDC9B9ED67F731B0D22DB8C5663F; MUIDB=052CDDC9B9ED67F731B0D22DB8C5663F; " +
                                "_EDGE_S=F=1&SID=0E96578F89CA67F93EB7586B88E26652&mkt=ru-ru; _EDGE_V=1; SRCHD=AF=NOFORM; " +
                                "SRCHUID=V=2&GUID=275BF4F4F86F43729D92DE5127394536&dmnchg=1; " +
                                "SRCHUSR=DOB=20210220&T=1613823654000; _SS=SID=0E96578F89CA67F93EB7586B88E26652; " +
                                "_HPVN=CS" +
                                "=eyJQbiI6eyJDbiI6MSwiU3QiOjAsIlFzIjowLCJQcm9kIjoiUCJ9LCJTYyI6eyJDbiI6MSwiU3QiOjAsIlFzIjowLCJQcm9kIjoiSCJ9LCJReiI6eyJDbiI6MSwiU3QiOjAsIlFzIjowLCJQcm9kIjoiVCJ9LCJBcCI6dHJ1ZSwiTXV0ZSI6dHJ1ZSwiTGFkIjoiMjAyMS0wMi0yMFQwMDowMDowMFoiLCJJb3RkIjowLCJEZnQiOm51bGwsIk12cyI6MCwiRmx0IjowLCJJbXAiOjJ9; SRCHHPGUSR=CW=1728&CH=342&DPR=1.1111111111111112&UTC=180&DM=0&HV=1613823702&WTS=63749420454&BRW=XW&BRH=S&EXLTT=5; ipv6=hit=1613827256548&t=4");
        return executeAndValidateResponse(url, builder);
    }

    public CompletableFuture<HttpBodyResponse> getBody(String url) {
        BoundRequestBuilder builder = prepareGet(url);
        return executeAndValidateResponse(url, builder);
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    private BoundRequestBuilder prepareGet(String url) {
        return httpClient.prepareGet(url)
                // This should be configurable
                .setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:85.0) Gecko/20100101 Firefox/85.0")
                .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .setHeader("Accept-Language", "en-US,en;q=0.5")
                .setHeader("Cache-Control", "max-age=0");
    }

    private CompletableFuture<HttpBodyResponse> executeAndValidateResponse(String url, BoundRequestBuilder builder) {
        return builder.execute().toCompletableFuture()
                .thenApply(response -> {
                    validateResponse(response);
                    return new HttpBodyResponse(url, response.getResponseBody());
                })
                .exceptionally(e -> {
                    Logger.debug("Error: " + e.getMessage());
                    e.printStackTrace();
                    return new HttpBodyResponse(url, "<html></html>");
                });
    }

    private void validateResponse(Response response) {
        if (response.getStatusCode() > 299) { // not a proper check, must be replaced in prod code
            throw new RuntimeException("Could not read page " + response.getUri() + "   " + response.getStatusCode()
                    + "   " + response.getResponseBody());
        }
        // May contain some further validation...
    }
}
