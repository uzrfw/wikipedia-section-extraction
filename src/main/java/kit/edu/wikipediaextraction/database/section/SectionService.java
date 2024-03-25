package kit.edu.wikipediaextraction.database.section;

import kit.edu.wikipediaextraction.database.DatabaseEntry;
import kit.edu.wikipediaextraction.database.DatabaseEntryType;
import kit.edu.wikipediaextraction.database.DatabaseService;
import kit.edu.wikipediaextraction.model.WikipediaSection;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SectionService {
    private final DatabaseService databaseService;
    private final Set<String> sectionTitleSet;

    public SectionService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.sectionTitleSet = new HashSet<>();
    }

    /**
     * Clear all tables
     */
    public void clearTables() {
        this.databaseService.clearTable(SectionConstants.TABLE_WIKIPEDIA_SECTIONS);
        this.databaseService.clearTable(SectionConstants.TABLE_WIKIPEDIA_REFERENCES);
        this.databaseService.clearTable(SectionConstants.TABLE_WIKIPEDIA_CATEGORIES);
    }

    /**
     * Stores section in database
     *
     * @param sections list of sections
     */
    public void storeSections(
            List<WikipediaSection> sections
    ) {
        if (!this.sectionTitleSet.contains(sections.get(0).articleHeadline())) {
            for (WikipediaSection section : sections) {
                this.insertWikipediaSections(section);
                this.insertWikipediaReferences(section);
                this.insertWikipediaCategories(section);
            }
            this.sectionTitleSet.add(sections.get(0).articleHeadline());
        }
    }

    /**
     * Stores wikipedia sections in database
     *
     * @param section wikipedia section
     */
    private void insertWikipediaSections(WikipediaSection section) {
        List<DatabaseEntry> entriesSections = new ArrayList<>();
        entriesSections.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.sectionID()));
        entriesSections.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.articleID()));
        entriesSections.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.articleHeadline()));
        entriesSections.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.rawArticleText()));
        entriesSections.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.cleanedArticleText()));
        entriesSections.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.sectionHeadline()));
        entriesSections.add(new DatabaseEntry(DatabaseEntryType.BOOLEAN, String.valueOf(section.isFirstSection())));

        this.databaseService.insertData(
                SectionConstants.TABLE_WIKIPEDIA_SECTIONS,
                SectionConstants.COLUMNS_WIKIPEDIA_SECTIONS,
                entriesSections
        );
    }

    /**
     * Stores wikipedia references in database
     *
     * @param section wikipedia section
     */
    private void insertWikipediaReferences(WikipediaSection section) {
        for (String reference : section.wikipediaReferences()) {
            List<DatabaseEntry> entriesReferences = new ArrayList<>();
            entriesReferences.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.sectionID()));
            entriesReferences.add(new DatabaseEntry(DatabaseEntryType.TEXT, reference));

            this.databaseService.insertData(
                    SectionConstants.TABLE_WIKIPEDIA_REFERENCES,
                    SectionConstants.COLUMNS_WIKIPEDIA_REFERENCES,
                    entriesReferences
            );
        }
    }

    /**
     * Stores wikipedia categories in database
     *
     * @param section wikipedia section
     */
    private void insertWikipediaCategories(WikipediaSection section) {
        for (String category : section.categories()) {
            List<DatabaseEntry> entriesCategories = new ArrayList<>();
            entriesCategories.add(new DatabaseEntry(DatabaseEntryType.TEXT, section.sectionID()));
            entriesCategories.add(new DatabaseEntry(DatabaseEntryType.TEXT, category));

            this.databaseService.insertData(
                    SectionConstants.TABLE_WIKIPEDIA_CATEGORIES,
                    SectionConstants.COLUMNS_WIKIPEDIA_CATEGORIES,
                    entriesCategories
            );
        }
    }
}
