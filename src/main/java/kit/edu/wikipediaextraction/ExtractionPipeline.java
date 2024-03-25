package kit.edu.wikipediaextraction;


import kit.edu.wikipediaextraction.database.section.SectionService;
import kit.edu.wikipediaextraction.extraction.ExtractionService;
import kit.edu.wikipediaextraction.model.WikipediaSection;
import kit.edu.wikipediaextraction.scraper.ScraperService;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;


@Service
@EnableScheduling
public class ExtractionPipeline {
    private final static int NUMBER_OF_RANDOM_ARTICLES = 40;
    private final SectionService sectionService;
    private final ScraperService scraperService;
    private final ExtractionService extractionService;

    public ExtractionPipeline(
            SectionService sectionService,
            ScraperService scraperService,
            ExtractionService extractionService
    ) {
        this.sectionService = sectionService;
        this.scraperService = scraperService;
        this.extractionService = extractionService;
        this.startExtraction();
    }

    /**
     * Starts the extraction from sections of wikipedia articles
     */
    private void startExtraction() {
        // scrape wikipedia articles
        Collection<Document> articleDocs = new ArrayList<>();
        Collection<Document> randomWikipediaArticles = this.scraperService.extractRandomArticles(NUMBER_OF_RANDOM_ARTICLES);
        Collection<Document> vitalWikipediaArticles = this.scraperService.extractAllVitalArticles();

        articleDocs.addAll(randomWikipediaArticles);
        articleDocs.addAll(vitalWikipediaArticles);

        // extract sections from articles
        Collection<WikipediaSection> sections = this.extractionService.extractSectionsFromArticles(articleDocs);

        // clear tables
        this.sectionService.clearTables();

        // store sections in database
        this.sectionService.storeSections(sections.stream().toList());
    }
}
