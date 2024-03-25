package kit.edu.wikipediaextraction.database.section;

public class SectionConstants {
    public final static String TABLE_WIKIPEDIA_SECTIONS = "wikipedia_sections";
    public final static String TABLE_WIKIPEDIA_REFERENCES = "wikipedia_references";
    public final static String TABLE_WIKIPEDIA_CATEGORIES = "wikipedia_categories";

    public final static String COLUMNS_WIKIPEDIA_SECTIONS = "sectionID, articleID, articleHeadline, rawArticleText, cleanedArticleText, sectionHeadline, isFirstSection";
    public final static String COLUMNS_WIKIPEDIA_REFERENCES = "sectionID, referenceText";
    public final static String COLUMNS_WIKIPEDIA_CATEGORIES = "sectionID, categoryText";

}
