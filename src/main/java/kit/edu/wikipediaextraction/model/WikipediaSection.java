package kit.edu.wikipediaextraction.model;

import java.util.Collection;

public record WikipediaSection(
        String articleID,
        String articleHeadline,

        String sectionID,
        String rawArticleText,
        String cleanedArticleText,
        String sectionHeadline,
        boolean isFirstSection,

        Collection<String> wikipediaReferences,
        Collection<String> categories
) {
}
