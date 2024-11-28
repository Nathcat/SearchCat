package com.nathcat.searchcat.index;

import com.nathcat.searchcat.crawler.Crawler;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Provides an efficient way of search the database of indexed pages.
 */
public class SearchIndex {
    /**
     * The file containing the index data
     */
    public static final String INDEX_FILE = "Assets/index.bin";

    private HashMap<String, String> index;
    private HashMap<String, Crawler.Page> pages;

    public SearchIndex() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(INDEX_FILE));
            ArrayList<Object> l = (ArrayList<Object>) ois.readObject();
            ois.close();

            index = (HashMap<String, String>) l.get(0);
            pages = (HashMap<String, Crawler.Page>) l.get(1);
        }
        catch (IOException | ClassNotFoundException e) {
            index = new HashMap<>();
            pages = new HashMap<>();
            save();
        }
    }

    /**
     * Index a new page into the search index
     * @param page The page to index
     */
    public void indexPage(Crawler.Page page) {
        HashSet<String> searchTerms = new HashSet<>();

        searchTerms.addAll(List.of(page.getTitle().split("\\s+")));
        searchTerms.addAll(List.of(page.getDescription().split("\\s+")));
        searchTerms.addAll(page.keywords);
        searchTerms.addAll(page.content);

        searchTerms.forEach((String s) -> index.put(s, page.url));
        pages.put(page.url, page);

        save();
    }

    /**
     * Search the index for a query
     * @param query The query
     * @return An array of pages which match this query
     */
    public Crawler.Page[] search(String query) {
        HashSet<Crawler.Page> results = new HashSet<>();

        for (String word : query.split("\\s+")) {
            results.add(
                    pages.get(
                            index.get(word)
                    )
            );
        }

        return results.toArray(new Crawler.Page[0]);
    }

    /**
     * Save the contents of the index to the <code>INDEX_FILE</code>
     */
    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INDEX_FILE));
            ArrayList<Object> l = new ArrayList<>();
            l.add(index);
            l.add(pages);
            oos.writeObject(l);
            oos.flush();
            oos.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
