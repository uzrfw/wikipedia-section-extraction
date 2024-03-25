package kit.edu.wikipediaextraction.scraper;

import kit.edu.wikipediaextraction.utils.ConnectionConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Service for retrieving article URLs
 */
@Service
public class ScraperService {
    private final static String WIKIPEDIA_URL = "https://en.wikipedia.org";
    private final static String WIKIPEDIA_RANDOM_URL = "https://en.wikipedia.org/wiki/Special:Random";
    private final static String WIKIPEDIA_LIST_OF_VITAL_ARTICLE_LISTS_URL = "https://en.wikipedia.org/wiki/Wikipedia:Vital_articles/Level/5";
    private final static String WIKI_TABLE_HTML_CLASS_SELECTOR = ".wikitable";
    private final static String WIKIPEDIA_MW_CONTENT_TEXT_HTML_SELECTOR = "#mw-content-text";
    private final static String HTML_LINK_ELEMENT_SELECTOR = "a";
    private final static String HTML_ATTRIBUTE_HREF = "href";
    private final static String COLON = ":";

    public ScraperService() {
    }

    /**
     * Extracts random articles from wikipedia
     *
     * @param numberOfArticles number of random articles
     * @return article documents
     */
    public Collection<Document> extractRandomArticles(
            int numberOfArticles
    ) {
        Collection<Document> docs = new ArrayList<>();

        for (int i = 0; i < numberOfArticles; i++) {
            // try to request random URL
            try {
                URL randomWikipediaURL = new URL(WIKIPEDIA_RANDOM_URL);
                URLConnection connection = randomWikipediaURL.openConnection();
                connection.setRequestProperty(ConnectionConstants.USER_AGENT, ConnectionConstants.STANDARD_USER_AGENT);

                InputStream inputStream = connection.getInputStream();
                Document doc = Jsoup.parse(inputStream, ConnectionConstants.CHARSET_NAME, randomWikipediaURL.toString());
                docs.add(doc);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        return docs;
    }


    /**
     * Extracts all vital article documents from wikipedia
     *
     * @return article documents
     */
    public Collection<Document> extractAllVitalArticles() {
        Collection<Document> docs = new ArrayList<>();
        Collection<String> vitalArticleURLs = this.getAllVitalArticleURLs();
        int counter = 0;

        for (String articleURL : vitalArticleURLs) {
            if (counter >= 0 && counter < 9000) {
                try {
                    URL vitalArticleURL = new URL(articleURL);
                    URLConnection connection = vitalArticleURL.openConnection();
                    connection.setRequestProperty(ConnectionConstants.USER_AGENT, ConnectionConstants.STANDARD_USER_AGENT);
                    connection.setReadTimeout(10000);

                    InputStream inputStream = connection.getInputStream();
                    Document doc = Jsoup.parse(inputStream, ConnectionConstants.CHARSET_NAME, vitalArticleURL.toString());
                    docs.add(doc);
                    counter++;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        return docs;
    }


    /**
     * Gets all vital article URLs
     *
     * @return vital article URLs
     */
    private Collection<String> getAllVitalArticleURLs() {
        Collection<String> URLs = new ArrayList<>();
        Collection<String> pageURLrls = new ArrayList<>();

        // get page URLs of all vital article list pages
        try {
            URL randomWikipediaURL = new URL(WIKIPEDIA_LIST_OF_VITAL_ARTICLE_LISTS_URL);
            URLConnection connection = randomWikipediaURL.openConnection();
            connection.setRequestProperty(ConnectionConstants.USER_AGENT, ConnectionConstants.STANDARD_USER_AGENT);

            InputStream inputStream = connection.getInputStream();
            Document doc = Jsoup.parse(inputStream, ConnectionConstants.CHARSET_NAME, randomWikipediaURL.toString());
            Element wikiTableElement = doc.selectFirst(WIKI_TABLE_HTML_CLASS_SELECTOR);

            if (wikiTableElement != null) {
                Elements wikiTableLinkElements = wikiTableElement.select(HTML_LINK_ELEMENT_SELECTOR);

                for (Element linkElement : wikiTableLinkElements) {
                    pageURLrls.add(linkElement.attr(HTML_ATTRIBUTE_HREF));
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


        for (String pageURL : pageURLrls) {
            Collection<String> pageVitalArticleURLs = this.getVitalArticleURLsFromPage(pageURL);
            URLs.addAll(pageVitalArticleURLs);
        }
        return URLs;
    }


    /**
     * Gets vital article URLs from selected page
     *
     * @param pageURL URL of the selected page
     * @return list of vital article URLs
     */
    private Collection<String> getVitalArticleURLsFromPage(
            String pageURL
    ) {
        Collection<String> URLs = new ArrayList<>();

        try {
            URL randomWikipediaURL = new URL(WIKIPEDIA_URL + pageURL);
            URLConnection connection = randomWikipediaURL.openConnection();
            connection.setRequestProperty(ConnectionConstants.USER_AGENT, ConnectionConstants.STANDARD_USER_AGENT);

            InputStream inputStream = connection.getInputStream();
            Document doc = Jsoup.parse(inputStream, ConnectionConstants.CHARSET_NAME, randomWikipediaURL.toString());
            Element wikipediaMainText = doc.selectFirst(WIKIPEDIA_MW_CONTENT_TEXT_HTML_SELECTOR);

            if (wikipediaMainText != null) {
                Elements linkElements = wikipediaMainText.select(HTML_LINK_ELEMENT_SELECTOR);

                for (Element linkElement : linkElements) {
                    String href = linkElement.attr(HTML_ATTRIBUTE_HREF);

                    // Colon is only used for internal links, not for articles
                    if (!href.contains(COLON)) {
                        URLs.add(WIKIPEDIA_URL + href);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return URLs.stream().toList().subList(0, 100);
    }
}
