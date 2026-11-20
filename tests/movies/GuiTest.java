// AI-generated code
package movies;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Checks the two-tab GUI through its controls without opening a window. */
public final class GuiTest {
    /** Runs the GUI flow and optionally saves an image for visual review. */
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("movie-gui-");
        Path ratingsPath = directory.resolve("my_ratings.csv");
        try {
            RatingStore ratings = RatingStore.load(ratingsPath);
            MovieCatalog catalog = new MovieCatalog(List.of(
                    movie("sample", "Sample Movie", "PG", "Drama", 7.0),
                    movie("another", "Another Drama", "PG", "Drama", 7.5),
                    movie("comedy", "Comedy Film", "R", "Comedy", 8.0),
                    movie("unknown", "Unknown Film", "", "Horror", 6.0)));
            SwingUtilities.invokeAndWait(() -> exerciseControls(catalog, ratings, args));
            check(RatingStore.load(ratingsPath).ratings().get("sample") == 4.25,
                    "GUI rating persists in the shared CSV");
            System.out.println("All GUI checks passed.");
        } finally {
            Files.deleteIfExists(ratingsPath);
            Files.deleteIfExists(directory);
        }
    }

    /** Exercises search, rating validation, saved ratings, and filtered recommendations. */
    private static void exerciseControls(MovieCatalog catalog, RatingStore ratings, String[] args) {
        MovieRecommenderPanel panel = new MovieRecommenderPanel(catalog, ratings);
        JTabbedPane tabs = find(panel, "movieTabs", JTabbedPane.class);
        check(tabs.getTabCount() == 2, "two tabs");
        check(tabs.getTitleAt(0).equals("Recommendations"), "recommendations tab");
        check(tabs.getTitleAt(1).equals("Rate Movies"), "rating tab");

        JButton recommend = find(panel, "giveRecommendations", JButton.class);
        JTable recommendations = find(panel, "recommendationResults", JTable.class);
        recommend.doClick();
        check(recommendations.getRowCount() == 4, "recommendations appear on button click");

        JTextField search = find(panel, "movieSearch", JTextField.class);
        JTable matches = find(panel, "searchResults", JTable.class);
        search.setText("Sample");
        search.postActionEvent();
        check(matches.getRowCount() == 1, "Enter searches movies");
        matches.setRowSelectionInterval(0, 0);

        JTextField personalRating = find(panel, "personalRating", JTextField.class);
        JButton save = find(panel, "saveRating", JButton.class);
        JLabel ratingStatus = find(panel, "ratingStatus", JLabel.class);
        personalRating.setText("6");
        save.doClick();
        check(ratings.ratings().isEmpty(), "invalid GUI rating is not saved");
        check(ratingStatus.getText().contains("0.0 and 5.0"), "GUI explains valid range");
        personalRating.setText("4.25");
        save.doClick();
        check(ratings.ratings().get("sample") == 4.25, "valid GUI rating is saved");
        check(recommendations.getRowCount() == 0, "old recommendations clear after rating");

        search.setText("Comedy");
        find(panel, "searchButton", JButton.class).doClick();
        check(matches.getRowCount() == 1, "Search button finds movies");
        check(matches.getValueAt(0, 0).equals("Comedy Film"), "search displays matching title");

        find(panel, "certificateFilter", JComboBox.class).setSelectedItem("PG");
        recommend.doClick();
        check(recommendations.getRowCount() == 1, "certificate filter excludes R and unknown movies");
        check(recommendations.getValueAt(0, 1).equals("Another Drama"), "rated movie is excluded");
        if (args.length > 0) {
            renderPreview(panel, tabs, 0, Path.of(args[0]));
        }
        if (args.length > 1) {
            renderPreview(panel, tabs, 1, Path.of(args[1]));
        }
    }

    /** Renders one tested tab to a PNG for layout inspection. */
    private static void renderPreview(MovieRecommenderPanel panel, JTabbedPane tabs,
                                      int tabIndex, Path destination) {
        tabs.setSelectedIndex(tabIndex);
        panel.setSize(panel.getPreferredSize());
        layoutChildren(panel);
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.printAll(graphics);
            ImageIO.write(image, "png", destination.toFile());
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not render GUI preview", exception);
        } finally {
            graphics.dispose();
        }
    }

    /** Lays out nested Swing containers before offscreen rendering. */
    private static void layoutChildren(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) {
                layoutChildren(nested);
            }
        }
    }

    /** Finds a named control so tests use the same UI actions as a person. */
    private static <T extends Component> T find(Component root, String name, Class<T> type) {
        if (name.equals(root.getName()) && type.isInstance(root)) {
            return type.cast(root);
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                if (child instanceof Container || name.equals(child.getName())) {
                    try {
                        return find(child, name, type);
                    } catch (AssertionError exception) {
                        // Keep searching siblings until the requested control is found.
                    }
                }
            }
        }
        throw new AssertionError("Missing control: " + name);
    }

    /** Creates small catalog movies used by GUI interaction checks. */
    private static Movie movie(String id, String title, String certificate, String genre, double publicRating) {
        return new Movie(id, title, "2024", "Test Director", certificate, 90,
                List.of(genre), publicRating, 2000);
    }

    /** Stops the GUI check with a useful failure name. */
    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name);
        }
    }
}
