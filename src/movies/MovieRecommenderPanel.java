// AI-generated code
package movies;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Displays recommendations and personal rating controls in two tabs. */
public final class MovieRecommenderPanel extends JPanel {
    private static final Color ERROR_COLOR = new Color(160, 35, 35);
    private static final Color SUCCESS_COLOR = new Color(35, 110, 55);
    private static final Color NORMAL_COLOR = new Color(55, 55, 55);

    private final MovieCatalog catalog;
    private final RatingStore ratingStore;
    private final Recommender recommender;
    private final JTextField runtimeField = new JTextField(6);
    private final JComboBox<String> certificateBox = new JComboBox<>(
            new String[] {"Any", "G", "PG", "PG-13", "R", "NC-17"});
    private final JButton recommendButton = new JButton("Give recommendations");
    private final JLabel recommendationStatus = new JLabel("Choose filters, then give recommendations.");
    private final DefaultTableModel recommendationRows = readOnlyRows(
            "#", "Movie", "Director", "Genres", "Certificate", "Min", "IMDb");
    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Search");
    private final JLabel searchStatus = new JLabel("Search by movie title to find something you have watched.");
    private final DefaultTableModel searchRows = readOnlyRows(
            "Movie", "Director", "Genres", "Certificate", "Min", "IMDb", "Your rating");
    private final JTable searchTable = new JTable(searchRows);
    private final JTextField ratingField = new JTextField(7);
    private final JButton saveRatingButton = new JButton("Save rating");
    private final JLabel ratingStatus = new JLabel("Select a movie above to rate it.");
    private List<Movie> searchMatches = new ArrayList<>();

    /** Builds the two tabs around the already loaded catalog and personal ratings. */
    public MovieRecommenderPanel(MovieCatalog catalog, RatingStore ratingStore) {
        super(new BorderLayout(0, 12));
        this.catalog = catalog;
        this.ratingStore = ratingStore;
        this.recommender = new Recommender(catalog);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(1050, 700));

        JLabel heading = new JLabel("Movie recommender");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("movieTabs");
        tabs.addTab("Recommendations", buildRecommendationsTab());
        tabs.addTab("Rate Movies", buildRatingTab());
        add(tabs, BorderLayout.CENTER);
    }

    /** Creates the filters, action button, and ranked recommendation table. */
    private JPanel buildRecommendationsTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        runtimeField.setName("maximumRuntime");
        certificateBox.setName("certificateFilter");
        recommendButton.setName("giveRecommendations");
        controls.add(new JLabel("Maximum runtime (minutes):"));
        controls.add(runtimeField);
        controls.add(new JLabel("Highest US rating:"));
        controls.add(certificateBox);
        controls.add(recommendButton);
        recommendButton.addActionListener(event -> showRecommendations());

        JPanel top = new JPanel(new BorderLayout(0, 8));
        recommendationStatus.setName("recommendationStatus");
        top.add(controls, BorderLayout.NORTH);
        top.add(recommendationStatus, BorderLayout.SOUTH);
        tab.add(top, BorderLayout.NORTH);

        JTable table = new JTable(recommendationRows);
        table.setName("recommendationResults");
        configureTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(55);
        tab.add(new JScrollPane(table), BorderLayout.CENTER);
        return tab;
    }

    /** Creates the title search, matching movies, and personal rating editor. */
    private JPanel buildRatingTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel searchControls = new JPanel(new BorderLayout(8, 4));
        searchControls.add(new JLabel("Search movies by title:"), BorderLayout.NORTH);
        searchField.setName("movieSearch");
        searchButton.setName("searchButton");
        searchControls.add(searchField, BorderLayout.CENTER);
        searchControls.add(searchButton, BorderLayout.EAST);
        searchField.addActionListener(event -> searchMovies());
        searchButton.addActionListener(event -> searchMovies());

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.add(searchControls, BorderLayout.NORTH);
        top.add(searchStatus, BorderLayout.SOUTH);
        tab.add(top, BorderLayout.NORTH);

        configureTable(searchTable);
        searchTable.setName("searchResults");
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        searchTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        searchTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        searchTable.getColumnModel().getColumn(5).setPreferredWidth(55);
        searchTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                selectMovie();
            }
        });
        tab.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        JPanel ratingControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ratingField.setName("personalRating");
        saveRatingButton.setName("saveRating");
        saveRatingButton.setEnabled(false);
        saveRatingButton.addActionListener(event -> saveRating());
        ratingField.addActionListener(event -> saveRating());
        ratingControls.add(new JLabel("Your rating (0.0–5.0):"));
        ratingControls.add(ratingField);
        ratingControls.add(saveRatingButton);
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        ratingStatus.setName("ratingStatus");
        bottom.add(ratingControls, BorderLayout.NORTH);
        bottom.add(ratingStatus, BorderLayout.SOUTH);
        tab.add(bottom, BorderLayout.SOUTH);
        return tab;
    }

    /** Searches on Enter or button click and clears any earlier selection. */
    private void searchMovies() {
        searchRows.setRowCount(0);
        searchMatches = catalog.search(searchField.getText(), 20);
        ratingField.setText("");
        saveRatingButton.setEnabled(false);
        if (searchField.getText().isBlank()) {
            setStatus(searchStatus, "Enter part of a movie title.", true);
        } else if (searchMatches.isEmpty()) {
            setStatus(searchStatus, "No matching movies were found.", true);
        } else {
            setStatus(searchStatus, "Showing " + searchMatches.size() + " matches. Select one to rate it.", false);
        }
        Map<String, Double> ratings = ratingStore.ratings();
        for (Movie movie : searchMatches) {
            Double personalRating = ratings.get(movie.id());
            searchRows.addRow(new Object[] {movie.title(), displayDirector(movie),
                    String.join(", ", movie.genres()), displayCertificate(movie), movie.runtimeMinutes(),
                    formatPublicRating(movie), personalRating == null ? "" : personalRating.toString()});
        }
        setStatus(ratingStatus, "Select a movie above to rate it.", false);
    }

    /** Shows an existing rating for the selected movie and enables editing it. */
    private void selectMovie() {
        int selectedRow = searchTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= searchMatches.size()) {
            saveRatingButton.setEnabled(false);
            return;
        }
        Movie movie = searchMatches.get(selectedRow);
        Double previous = ratingStore.ratings().get(movie.id());
        ratingField.setText(previous == null ? "" : previous.toString());
        saveRatingButton.setEnabled(true);
        setStatus(ratingStatus, "Rating " + movie.title() + ".", false);
    }

    /** Validates and persists the selected movie's rating without leaving this tab. */
    private void saveRating() {
        int selectedRow = searchTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= searchMatches.size()) {
            setStatus(ratingStatus, "Select a movie before saving a rating.", true);
            return;
        }
        Movie movie = searchMatches.get(selectedRow);
        try {
            double stars = Double.parseDouble(ratingField.getText().trim());
            ratingStore.rate(movie.id(), stars);
            searchRows.setValueAt(Double.toString(stars), selectedRow, 6);
            recommendationRows.setRowCount(0);
            setStatus(recommendationStatus, "Ratings changed. Click Give recommendations to refresh.", false);
            ratingStatus.setForeground(SUCCESS_COLOR);
            ratingStatus.setText("Saved your rating for " + movie.title() + ".");
        } catch (NumberFormatException exception) {
            setStatus(ratingStatus, "Enter a numeric rating from 0.0 to 5.0.", true);
        } catch (IllegalArgumentException exception) {
            setStatus(ratingStatus, exception.getMessage(), true);
        } catch (IOException exception) {
            setStatus(ratingStatus, "Could not save your rating: " + exception.getMessage(), true);
        }
    }

    /** Applies optional limits and displays ten fresh recommendations. */
    private void showRecommendations() {
        Integer maximumRuntime = null;
        String runtimeText = runtimeField.getText().trim();
        if (!runtimeText.isEmpty()) {
            try {
                maximumRuntime = Integer.parseInt(runtimeText);
                if (maximumRuntime <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                setStatus(recommendationStatus, "Enter a positive whole number of minutes.", true);
                return;
            }
        }
        String selectedCertificate = (String) certificateBox.getSelectedItem();
        String maximumCertificate = "Any".equals(selectedCertificate) ? null : selectedCertificate;
        Map<String, Double> ratings = ratingStore.ratings();
        List<Recommender.Result> results = recommender.recommend(ratings, maximumRuntime, maximumCertificate, 10);
        recommendationRows.setRowCount(0);
        for (int index = 0; index < results.size(); index++) {
            Movie movie = results.get(index).movie();
            recommendationRows.addRow(new Object[] {index + 1, movie.title(), displayDirector(movie),
                    String.join(", ", movie.genres()), displayCertificate(movie), movie.runtimeMinutes(),
                    formatPublicRating(movie)});
        }
        if (results.isEmpty()) {
            setStatus(recommendationStatus, "No unwatched movies meet these limits.", true);
        } else if (ratings.isEmpty()) {
            setStatus(recommendationStatus, "Starter picks across genres. Rate movies to personalize this list.", false);
        } else if (maximumCertificate != null) {
            setStatus(recommendationStatus,
                    "Showing " + results.size() + " movies. Unknown US certificates are excluded.", false);
        } else {
            setStatus(recommendationStatus, "Showing " + results.size() + " recommendations.", false);
        }
    }

    /** Uses a compact, consistent table style while preserving recommendation order. */
    private static void configureTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
    }

    /** Creates a table model whose rows can only be changed by the application. */
    private static DefaultTableModel readOnlyRows(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Provides a readable placeholder for missing directors. */
    private static String displayDirector(Movie movie) {
        return movie.director().isBlank() ? "Director unknown" : movie.director();
    }

    /** Keeps source certificates visible without claiming unknown ones are US ratings. */
    private static String displayCertificate(Movie movie) {
        return movie.certificate().isBlank() ? "Unknown" : movie.certificate();
    }

    /** Shows the catalog's public 10-point rating with one decimal place. */
    private static String formatPublicRating(Movie movie) {
        return String.format(Locale.ROOT, "%.1f/10", movie.publicRating());
    }

    /** Displays feedback beside its action and highlights errors. */
    private static void setStatus(JLabel label, String message, boolean error) {
        label.setForeground(error ? ERROR_COLOR : NORMAL_COLOR);
        label.setText(message);
    }
}
