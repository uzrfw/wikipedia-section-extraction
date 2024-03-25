package kit.edu.wikipediaextraction.database;

import kit.edu.wikipediaextraction.ApplicationPropertiesResolver;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;

@Service
public class DatabaseService {
    private final static String JDBC_CLASS_NAME = "org.sqlite.JDBC";
    private final static String INSERT_INTO_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private final static String SELECT_QUERY = "SELECT * FROM %s";
    private final static String DELETE_QUERY = "DELETE FROM %s";
    private final static String VALUES_PLACEHOLDER = "?,";
    private final static String IS_FIRST_SECTION = "isFirstSection";
    private Connection databaseConnection;
    private final String SQLiteURL;

    public DatabaseService(
            ApplicationPropertiesResolver applicationPropertiesResolver
    ) {
        String databaseName;
        if (applicationPropertiesResolver.isProduction()) {
            databaseName = DatabaseConstants.DATABASE_NAME;
        } else {
            databaseName = DatabaseConstants.TEST_DATABASE_NAME;
        }
        this.SQLiteURL = applicationPropertiesResolver.getSQLiteURL();
        this.connect(databaseName);
    }


    /**
     * Connect with database
     *
     * @param databaseName name of database
     */
    private void connect(String databaseName) {
        try {
            Class.forName(JDBC_CLASS_NAME);
            String url = SQLiteURL + databaseName;
            this.databaseConnection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Close database connection
     */
    private void disconnect() {
        try {
            if (this.databaseConnection != null) {
                this.databaseConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Inserts values to table
     *
     * @param tableName name of table
     * @param columns   columns of table
     * @param values    values of table
     */
    public void insertData(
            String tableName,
            String columns,
            List<DatabaseEntry> values
    ) {
        try {
            String valuesPlaceholder = VALUES_PLACEHOLDER.repeat(values.size());
            String insertQuery = String.format(INSERT_INTO_QUERY, tableName, columns, valuesPlaceholder.substring(0, valuesPlaceholder.length() - 1));

            try (PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertQuery)) {
                for (int i = 0; i < values.size(); i++) {
                    switch (values.get(i).type()) {
                        case TEXT -> preparedStatement.setString(i + 1, values.get(i).value());
                        case INT -> preparedStatement.setInt(i + 1, Integer.parseInt(values.get(i).value()));
                        case BOOLEAN ->
                                preparedStatement.setBoolean(i + 1, Boolean.parseBoolean(values.get(i).value()));
                    }
                }
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get content of table
     *
     * @param tableName name of table
     */
    public void getTable(String tableName) {
        try {
            String query = String.format(SELECT_QUERY, tableName);
            Statement statement = this.databaseConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String data = resultSet.getString(IS_FIRST_SECTION);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Clears table
     *
     * @param tableName name of table
     */
    public void clearTable(String tableName) {
        try {
            String query = String.format(DELETE_QUERY, tableName);
            Statement statement = this.databaseConnection.createStatement();

            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
