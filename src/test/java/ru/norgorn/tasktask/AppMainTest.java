package ru.norgorn.tasktask;

import org.junit.Before;
import org.junit.Test;
import ru.norgorn.tasktask.model.HttpBodyResponse;
import ru.norgorn.tasktask.model.LibInfo;
import ru.norgorn.tasktask.model.LibStatistics;
import ru.norgorn.tasktask.model.PageInfo;
import ru.norgorn.tasktask.service.HttpClient;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AppMainTest {

    // THis is not a full test, but a mere surface-level demonstration of an approach

    AppMain sut;

    HttpClient clientMock;

    @Before
    public void setUp() {
        clientMock = mock(HttpClient.class);
        when(clientMock.getBodyFromMicrosoft(anyString()))
                .thenReturn(CompletableFuture.completedFuture(new HttpBodyResponse("", MICROSOFT_BODY)));

        sut = new AppMain();
        sut.httpClient = clientMock;
    }

    @Test
    public void getSearchResults() {
        String expectedInput = "input_123";
        List<String> expectedResults = Arrays.asList("url_1", "url_2");

        List<String> actualResults = sut.getSearchResults(expectedInput);

        assertEquals(expectedResults, actualResults);
    }

    @Test
    public void findTopLibs() {
        List<PageInfo> pages = Arrays.asList(
                new PageInfo("", Arrays.asList(new LibInfo("url1"), new LibInfo("url2"), new LibInfo("url3"))),
                new PageInfo("", Arrays.asList(new LibInfo("url2"), new LibInfo("url3"))),
                new PageInfo("", Arrays.asList(new LibInfo("url3")))
        );

        List<LibStatistics> actualTop = sut.findTopLibs(pages, 2);

        assertEquals(2, actualTop.size());
        assertEquals(3, actualTop.get(0).getCount());
        assertEquals("url3", actualTop.get(0).getLib().getUrl());
        assertEquals(2, actualTop.get(1).getCount());
        assertEquals("url2", actualTop.get(1).getLib().getUrl());

    }

    private static final String MICROSOFT_BODY = "<html>" +
            "<body> " +
            "<li> Menu item </li>" +
            "<li class='b_algo'> <div class='b_title'> <h2><a href='url_1'>PAGE_1</a></h2> </div> </li>" +
            "<li class='b_algo'> <div class='b_title'> <h2><a href='url_2'>PAGE_2</a></h2> </div> </li>" +
            "<div> Some footer! </div>" +
            "</body>" +
            "</html>";
}
