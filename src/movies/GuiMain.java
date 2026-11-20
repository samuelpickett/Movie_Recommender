// AI-generated code
package movies;

import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts the desktop interface with the same catalog and ratings as the CLI. */
public final class GuiMain {
    private GuiMain() {
    }

    /** Loads local data before opening the two-tab movie window. */
    public static void main(String[] args) {
        Path catalogPath = args.length > 0 ? Path.of(args[0]) : Path.of("data", "movies.csv");
        Path ratingsPath = args.length > 1 ? Path.of(args[1]) : Path.of("data", "my_ratings.csv");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // The standard Swing look and feel remains usable if the system theme is unavailable.
        }

        try {
            MovieCatalog catalog = MovieCatalog.load(catalogPath);
            RatingStore ratings = RatingStore.load(ratingsPath);
            SwingUtilities.invokeLater(() -> showWindow(catalog, ratings));
        } catch (IOException | IllegalArgumentException exception) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Could not open the movie recommender:\n" + exception.getMessage(),
                    "Movie recommender", JOptionPane.ERROR_MESSAGE));
        }
    }

    /** Creates the frame on Swing's event thread and displays the application. */
    private static void showWindow(MovieCatalog catalog, RatingStore ratings) {
        JFrame frame = new JFrame("Movie recommender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MovieRecommenderPanel(catalog, ratings));
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
