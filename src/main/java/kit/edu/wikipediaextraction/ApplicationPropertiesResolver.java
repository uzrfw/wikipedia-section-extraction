package kit.edu.wikipediaextraction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationPropertiesResolver {
    private final boolean isProduction;
    private final String SQLiteURL;

    private static final String PRODUCTION_PROFILE_IDENTIFIER = "prod";

    public ApplicationPropertiesResolver(
            @Value("${spring.profiles.active}") String springProfile,
            @Value("${spring.data.sqlite}") String databaseURL
    ) {
        System.out.println("Use: " + springProfile + " profile");

        this.isProduction = springProfile.equals(PRODUCTION_PROFILE_IDENTIFIER);
        this.SQLiteURL = databaseURL;
    }

    public String getSQLiteURL() {
        return SQLiteURL;
    }

    public boolean isProduction() {
        return isProduction;
    }
}
