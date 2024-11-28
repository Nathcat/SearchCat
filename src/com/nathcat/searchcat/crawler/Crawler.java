package com.nathcat.searchcat.crawler;

import com.nathcat.searchcat.index.SearchIndex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class Crawler extends Thread {
    /**
     * Represents a page visited by the crawler
     */
    public static class Page {
        /**
         * The URL of the page
         */
        public final String url;

        /**
         * The page's document tree
         */
        private final Document doc;

        /**
         * The links within the page processed into a Queue structure
         */
        private final Queue<String> links;

        public final HashSet<String> content = new HashSet<>();
        public final HashSet<String> keywords = new HashSet<>();
        private String description;
        private String title;

        /**
         * Process a new page from the given URL
         * @param url The URL of the page
         * @throws IOException Thrown if JSoup fails to retrieve the page for whatever reason
         */
        public Page(String url) throws IOException {
            this.url = url;

            // Get the document and get each link from it
            this.doc = Jsoup.connect(url).get();
            List<String> l = this.doc.select("a").eachAttr("href");
            links = new ArrayBlockingQueue<>(l.size(), true, l);

            // Process text elements
            this.doc.select("li, p, h6, h5, h4, h3, h2, h1").forEach(element -> {
                if (element.hasText()) {
                    // Get the text from the element and split it by whitespace, i.e. split into array of words,
                    // and add this to the words list.
                    content.addAll(List.of(element.text().split("\\s+")));
                }
            });

            // Process meta tags
            this.doc.select("meta").forEach(element -> {
               switch (element.attr("name")) {
                   case "keywords" -> keywords.addAll(List.of(element.attr("content").split(",\\s*")));
                   case "description" -> description = element.attr("content");
               }
            });

            // Process title tags
            this.doc.select("title").forEach(element -> title = element.text());
        }

        /**
         * Get the next link in the page
         * @return The next link in the page to be processed, or null if there are no more links
         */
        public String getNextLink() {
            try {
                return links.remove();
            }
            catch (NoSuchElementException e) {
                return null;
            }
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * The starting URL for the crawler to process
     */
    private final String startURL;
    private final Stack<Page> state;
    public SearchIndex searchIndex;

    /**
     * Sets up the crawler
     * @param startURL The URL the crawler should start at
     * @throws IOException Thrown if JSoup fails to access <code>startURL</code>
     */
    public Crawler(String startURL, SearchIndex searchIndex) throws IOException {
        this.startURL = startURL;
        this.searchIndex = searchIndex;
        this.state = new Stack<>();

        this.state.push(new Page(this.startURL));
    }

    @Override
    public void run() {
        Page currentPage;

        while ((currentPage = state.pop()) != null) {
            String link;
            while ((link = currentPage.getNextLink()) != null) {
                try {
                    state.push(new Page(link));

                } catch (IOException e) {
                    System.err.println("Crawler " + getId() + ": Failed to access page " + link);
                }
            }
        }
    }
}
