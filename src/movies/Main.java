// AI-generated code
package movies;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/** Provides the first command line interface for one person's recommendations. */
public final class Main {
    private final MovieCatalog catalog;
    private final RatingStore ratingStore;
    private final Recommender recommender;
    private final Scanner input;

    /** Connects the reusable catalog and recommender to console input. */
    private Main(MovieCatalog catalog, RatingStore ratingStore, Scanner input) {
        this.catalog = catalog;
        this.ratingStore = ratingStore;
        this.recommender = new Recommender(catalog);
        this.input = input;
    }

    /** Loads the local CSV files and starts the menu. */
    public static void main(String[] args) {
        Path catalogPath = args.length > 0 ? Path.of(args[0]) : Path.of("data", "movies.csv");
        Path ratingsPath = args.length > 1 ? Path.of(args[1]) : Path.of("data", "my_ratings.csv");
        try {
            MovieCatalog catalog = MovieCatalog.load(catalogPath);
            RatingStore ratings = RatingStore.load(ratingsPath);
            System.out.printf("Loaded %,d movies. Your ratings: %,d.%n", catalog.movies().size(), ratings.ratings().size());
            new Main(catalog, ratings, new Scanner(System.in)).run();
        } catch (IOException | IllegalArgumentException exception) {
            System.err.println("Could not start: " + exception.getMessage());
            System.exit(1);
        }
    }

    /** Repeats the menu until the user exits or input ends. */
    private void run() {
        while (true) {
            System.out.println("\n1. Find and rate a movie\n2. Get recommendations\n3. View your ratings\n4. Exit");
            String choice = ask("Choose 1-4: ");
            if (choice == null || choice.equals("4")) {
                return;
            }
            switch (choice) {
                case "1" -> findAndRate();
                case "2" -> showRecommendations();
                case "3" -> showRatings();
                default -> System.out.println("Enter a number from 1 to 4.");
            }
        }
    }

    /** Searches titles and records a personal rating for the chosen movie. */
    private void findAndRate() {
        String query = ask("Part of a movie title: ");
        if (query == null || query.isBlank()) {
            return;
        }
        List<Movie> matches = catalog.search(query, 20);
        if (matches.isEmpty()) {
            System.out.println("No matching movies in this catalog.");
            return;
        }
        for (int index = 0; index < matches.size(); index++) {
            Movie movie = matches.get(index);
            System.out.printf(Locale.ROOT, "%2d. %s | %s | public %.1f/10%n",
                    index + 1, movie.title(), displayDirector(movie), movie.publicRating());
        }
        while (true) {
            String choice = ask("Movie number to rate (Enter to cancel): ");
            if (choice == null || choice.isBlank()) {
                return;
            }
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index < 0 || index >= matches.size()) {
                    throw new NumberFormatException();
                }
                Movie selected = matches.get(index);
                String rating = ask("Your rating, 0.5 to 5.0 in half-star steps: ");
                if (rating == null || rating.isBlank()) {
                    return;
                }
                ratingStore.rate(selected.id(), Double.parseDouble(rating));
                System.out.println("Saved your rating for " + selected.title() + ".");
                return;
            } catch (NumberFormatException exception) {
                System.out.println("Enter a listed movie number and a numeric rating.");
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
                return;
            } catch (IOException exception) {
                System.err.println("Could not save your rating: " + exception.getMessage());
                return;
            }
        }
    }

    /** Asks for optional limits and prints up to ten unwatched movies. */
    private void showRecommendations() {
        Integer runtime = askRuntime();
        String certificate = askCertificate();
        Map<String, Double> ratings = ratingStore.ratings();
        List<Recommender.Result> results = recommender.recommend(ratings, runtime, certificate, 10);
        if (results.isEmpty()) {
            System.out.println("No unwatched movies meet those limits.");
            return;
        }
        if (ratings.isEmpty()) {
            System.out.println("Starter picks across genres. Rate a few movies to personalize the next list.");
        }
        if (certificate != null) {
            System.out.println("Movies without a recognized US certificate are excluded by this limit.");
        }
        for (int index = 0; index < results.size(); index++) {
            Recommender.Result result = results.get(index);
            Movie movie = result.movie();
            String contentRating = movie.certificate().isBlank() ? "unknown certificate" : movie.certificate();
            System.out.printf(Locale.ROOT, "%2d. %s | %s%n", index + 1, movie.title(), displayDirector(movie));
            System.out.printf(Locale.ROOT, "    %s | %d min | public %.1f/10 | %s | match %+.2f%n",
                    contentRating, movie.runtimeMinutes(), movie.publicRating(),
                    String.join(", ", movie.genres()), result.score());
        }
    }

    /** Lists saved personal ratings by title and highlights missing catalog IDs. */
    private void showRatings() {
        Map<String, Double> ratings = ratingStore.ratings();
        if (ratings.isEmpty()) {
            System.out.println("No personal ratings yet. Find a movie to begin.");
            return;
        }
        List<Map.Entry<String, Double>> entries = new ArrayList<>(ratings.entrySet());
        entries.sort(Comparator.comparing(entry -> {
            Movie movie = catalog.get(entry.getKey());
            return movie == null ? entry.getKey() : movie.title();
        }, String.CASE_INSENSITIVE_ORDER));
        for (Map.Entry<String, Double> entry : entries) {
            Movie movie = catalog.get(entry.getKey());
            String title = movie == null ? "Missing from catalog: " + entry.getKey() : movie.title();
            System.out.printf(Locale.ROOT, "%s | Your Rating: %.1f/5%n", title, entry.getValue());
        }
    }

    /** Reads a positive minute limit, or no limit for a blank answer. */
    private Integer askRuntime() {
        while (true) {
            String value = ask("Maximum runtime in minutes (Enter for no limit): ");
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                int minutes = Integer.parseInt(value);
                if (minutes > 0) {
                    return minutes;
                }
            } catch (NumberFormatException exception) {
                // The common message below covers non-numeric and non-positive input.
            }
            System.out.println("Enter a positive whole number of minutes.");
        }
    }

    /** Reads a US certificate ceiling, or no limit for a blank answer. */
    private String askCertificate() {
        while (true) {
            String value = ask("Highest US certificate G/PG/PG-13/R/NC-17 (Enter for no limit): ");
            if (value == null || value.isBlank()) {
                return null;
            }
            String normalized = value.toUpperCase(Locale.ROOT);
            if (List.of("G", "PG", "PG-13", "R", "NC-17").contains(normalized)) {
                return normalized;
            }
            System.out.println("Choose G, PG, PG-13, R, or NC-17.");
        }
    }

    /** Returns trimmed console input or null when input has ended. */
    private String ask(String prompt) {
        System.out.print(prompt);
        return input.hasNextLine() ? input.nextLine().trim() : null;
    }

    /** Displays a missing director without inventing one. */
    private static String displayDirector(Movie movie) {
        return movie.director().isBlank() ? "director unknown" : movie.director();
    }
}
