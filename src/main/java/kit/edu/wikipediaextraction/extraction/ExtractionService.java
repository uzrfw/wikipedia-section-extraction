package kit.edu.wikipediaextraction.extraction;

import kit.edu.wikipediaextraction.model.WikipediaSection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ExtractionService {
    private final static String WIKIPEDIA_MW_CONTENT_TEXT_HTML_SELECTOR = "#mw-content-text";
    private final static String WIKIPEDIA_MW_HEADLINE_HTML_SELECTOR = ".mw-headline";
    private final static String WIKIPEDIA_MW_EMPTY_CLASS_HTML_SELECTOR = "mw-empty-elt";
    private final static String WIKIPEDIA_LINKS = "#mw-normal-catlinks";
    private final static String WIKIPEDIA_URL_PART = "/wiki";
    private final static String WIKIPEDIA_SECTION_REFERENCES = "References";
    private final static String WIKIPEDIA_CATEGORIES = "Categories";
    private final static String HTML_LINK_ELEMENT_SELECTOR = "a";
    private final static String HTML_ATTRIBUTE_HREF = "href";
    private final static String HTML_TAG_TITLE = "title";
    private final static String HTML_TAG_HEADLINE = "h2";
    private final static String HTML_TAG_PARAGRAPH = "p";
    private final static int LENGTH_OF_WIKIPEDIA_IN_TITLE = 12;

    public ExtractionService() {
    }

    /**
     * Extracts all sections from the wikipedia articles
     *
     * @param docs wikipedia articles
     * @return list of extracted sections
     */
    public Collection<WikipediaSection> extractSectionsFromArticles(
            Collection<Document> docs
    ) {
        Collection<WikipediaSection> sections = new ArrayList<>();

        for (Document doc : docs) {
            sections.addAll(this.extractSectionsFromArticle(doc));
        }

        return sections;
    }

    /**
     * Extracts all sections of a wikipedia article
     *
     * @param doc wikipedia article
     * @return list of extracted sections
     */
    private Collection<WikipediaSection> extractSectionsFromArticle(
            Document doc
    ) {
        Collection<WikipediaSection> sections = new ArrayList<>();

        // Extract article information
        UUID articleID = UUID.randomUUID();
        String articleTitle = doc.selectFirst(HTML_TAG_TITLE).text();
        String shortArticleTitle = articleTitle.substring(0, articleTitle.length() - LENGTH_OF_WIKIPEDIA_IN_TITLE);
        Collection<String> categories = this.extractWikipediaCategories(doc);


        // Extract section information
        Element contentText = doc.selectFirst(WIKIPEDIA_MW_CONTENT_TEXT_HTML_SELECTOR);

        if (contentText != null && contentText.children().first() != null) {
            Elements textElements = contentText.children().first().children();

            String lastHeadline = "";
            boolean headlineFound = false;
            boolean stop = false;

            for (Element textElement : textElements) {
                if (stop) {
                    break;
                }

                String htmlTag = textElement.tagName();
                switch (htmlTag) {
                    case HTML_TAG_HEADLINE -> {
                        lastHeadline = textElement.selectFirst(WIKIPEDIA_MW_HEADLINE_HTML_SELECTOR).text();
                        headlineFound = true;

                        if (lastHeadline.equals(WIKIPEDIA_SECTION_REFERENCES)) {
                            // End of article content is reached
                            stop = true;
                        }
                    }

                    case HTML_TAG_PARAGRAPH -> {
                        // Each non-empty paragraph creates a new section
                        if (!textElement.classNames().contains(WIKIPEDIA_MW_EMPTY_CLASS_HTML_SELECTOR)) {
                            String rawArticleText = textElement.text();
                            String cleanedArticleText = this.cleanParagraphText(textElement.text());
                            Collection<String> wikipediaReferences = this.extractWikipediaReferences(textElement);
                            UUID sectionID = UUID.randomUUID();

                            WikipediaSection section = new WikipediaSection(
                                    articleID.toString(),
                                    shortArticleTitle,
                                    sectionID.toString(),
                                    rawArticleText,
                                    cleanedArticleText,
                                    lastHeadline,
                                    !headlineFound,
                                    wikipediaReferences,
                                    categories
                            );
                            sections.add(section);
                        }
                    }
                }
            }
        }
        return sections;
    }

    /**
     * Extracts the categories of a wikipedia article
     *
     * @param doc wikipedia article document
     * @return list of categories
     */
    private Collection<String> extractWikipediaCategories(
            Document doc
    ) {
        Collection<String> categories = new ArrayList<>();
        Element wikipediaLinkBox = doc.selectFirst(WIKIPEDIA_LINKS);

        if (wikipediaLinkBox != null) {
            Elements wikipediaLinks = wikipediaLinkBox.select(HTML_LINK_ELEMENT_SELECTOR);
            for (Element wikipediaLink : wikipediaLinks) {
                String linkText = wikipediaLink.text();
                if (!linkText.equals(WIKIPEDIA_CATEGORIES)) {
                    categories.add(linkText);
                }
            }
        }

        return categories;
    }

    /**
     * Extracts the references in the paragraph
     *
     * @param textElement paragraph element
     * @return list of references
     */
    private Collection<String> extractWikipediaReferences(
            Element textElement
    ) {
        Collection<String> wikipediaReferences = new ArrayList<>();
        Elements linksToOtherArticles = textElement.select(HTML_LINK_ELEMENT_SELECTOR);

        for (Element link : linksToOtherArticles) {
            String href = link.attr(HTML_ATTRIBUTE_HREF);
            if (href.contains(WIKIPEDIA_URL_PART)) {
                wikipediaReferences.add(href);
            }
        }

        return wikipediaReferences;
    }

    /**
     * Cleans the paragraph text from brackets
     *
     * @param text paragraph text
     * @return cleaned text
     */
    private String cleanParagraphText(String text) {
        return this.cleanText(this.removeBrackets(text));
    }

    /**
     * Removes all brackets and their content
     *
     * @param text paragraph text
     * @return cleaned text
     */
    private String removeBrackets(String text) {
        StringBuilder result = new StringBuilder();
        int bracketLevel = 0;

        for (char c : text.toCharArray()) {
            if (c == '(' || c == '[') {
                bracketLevel++;
            } else if (c == ')' || c == ']') {
                bracketLevel--;
            } else if (bracketLevel == 0) {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Cleans text
     *
     * @param text paragraph text
     * @return cleaned text
     */
    private String cleanText(String text) {
        String regex = "[^a-zA-Z0-9.,\\-!\\?\\s]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        return matcher.replaceAll("");
    }
}
