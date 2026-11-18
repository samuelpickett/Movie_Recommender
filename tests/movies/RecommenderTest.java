// AI-generated code
package movies;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Focused checks for catalog parsing, personalized ranking, and saved ratings. */
public final class RecommenderTest {
    /** Runs all checks without an external test framework. */
    public static void main(String[] args) throws Exception {
        parsesQuotedCsv();
        ranksAndFiltersMovies();
        prefersMultipleMatchingGenres();
        breaksTiesByPublicRatingAndVotes();
        offersDiverseStarterMovies();
        persistsPersonalRatings();
        System.out.println("All recommender checks passed.");
    }

    /** Confirms a movie sharing two liked genres outranks one sharing only one. */
    private static void prefersMultipleMatchingGenres() {
        MovieCatalog catalog = new MovieCatalog(List.of(
                new Movie("watched", "Liked", "2024", "Test Director", "R", 90,
                        List.of("Drama", "Horror"), 7.0, 100),
                new Movie("both", "Both", "2024", "Test Director", "R", 90,
                        List.of("Drama", "Horror"), 7.0, 100),
                movie("one", "One", "R", 90, "Drama", 9.0, 100)));
        List<Recommender.Result> results = new Recommender(catalog)
                .recommend(Map.of("watched", 5.0), null, null, 2);
        check(results.get(0).movie().id().equals("both"), "multiple matching genres matter more than public rating");
    }

    /** Confirms quoted commas, escaped quotes, and line breaks remain in their fields. */
    private static void parsesQuotedCsv() throws IOException {
        Path file = Files.createTempFile("movies-csv-", ".csv");
        try {
            Files.writeString(file, "a,b\n\"A, B\",\"say \"\"hi\"\"\nnext\"\n", StandardCharsets.UTF_8);
            List<List<String>> rows = Csv.read(file);
            check(rows.size() == 2, "CSV record count");
            check(rows.get(1).equals(List.of("A, B", "say \"hi\"\nnext")), "quoted CSV fields");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /** Confirms positive and negative genre signals, watched exclusion, and limits. */
    private static void ranksAndFiltersMovies() {
        MovieCatalog catalog = new MovieCatalog(List.of(
                movie("watched-action", "Watched Action", "PG-13", 100, "Action", 6.0, 100),
                movie("watched-horror", "Watched Horror", "R", 90, "Horror", 6.0, 100),
                movie("action", "Action Pick", "PG-13", 95, "Action", 7.0, 100),
                movie("horror", "Horror Pick", "PG", 85, "Horror", 9.0, 100),
                movie("long", "Long Action", "PG", 150, "Action", 8.0, 100),
                movie("unknown", "Unknown Action", "", 80, "Action", 9.0, 100)));
        Recommender recommender = new Recommender(catalog);
        Map<String, Double> ratings = Map.of("watched-action", 5.0, "watched-horror", 1.0);
        List<Recommender.Result> all = recommender.recommend(ratings, null, null, 10);
        check(all.get(0).movie().id().equals("unknown"), "liked genres outrank disliked genres");
        check(all.get(all.size() - 1).movie().id().equals("horror"), "disliked genres rank lower");
        check(all.stream().noneMatch(result -> result.movie().id().startsWith("watched")), "watched exclusion");

        List<Recommender.Result> filtered = recommender.recommend(ratings, 100, "PG-13", 10);
        check(filtered.size() == 2, "runtime and certificate filtering");
        check(filtered.get(0).movie().id().equals("action"), "valid US certificate retained");
        check(filtered.stream().noneMatch(result -> result.movie().id().equals("unknown")),
                "unknown certificate excluded with an active limit");
    }

    /** Confirms the agreed public rating and vote-count tie breakers. */
    private static void breaksTiesByPublicRatingAndVotes() {
        MovieCatalog catalog = new MovieCatalog(List.of(
                movie("watched", "Watched Drama", "PG", 90, "Drama", 6.0, 1000),
                movie("low", "Low Public", "PG", 90, "Drama", 7.0, 1000),
                movie("few", "High Public Few Votes", "PG", 90, "Drama", 8.0, 100),
                movie("many", "High Public Many Votes", "PG", 90, "Drama", 8.0, 500)));
        List<Recommender.Result> results = new Recommender(catalog).recommend(Map.of("watched", 3.0), null, null, 3);
        check(results.get(0).movie().id().equals("many"), "vote count breaks public rating ties");
        check(results.get(1).movie().id().equals("few"), "higher public rating breaks personal score ties");
    }

    /** Confirms a new user sees more than one primary genre. */
    private static void offersDiverseStarterMovies() {
        MovieCatalog catalog = new MovieCatalog(List.of(
                movie("a1", "Action 1", "PG", 90, "Action", 9.0, 2000),
                movie("a2", "Action 2", "PG", 90, "Action", 8.5, 2000),
                movie("c1", "Comedy 1", "PG", 90, "Comedy", 8.0, 2000)));
        List<Recommender.Result> results = new Recommender(catalog).recommend(Map.of(), null, null, 2);
        check(results.size() == 2, "starter result count");
        check(results.get(0).movie().id().equals("a1") && results.get(1).movie().id().equals("c1"),
                "starter genre diversity");
    }

    /** Confirms ratings survive reload and edits replace the previous value. */
    private static void persistsPersonalRatings() throws IOException {
        Path directory = Files.createTempDirectory("movie-ratings-");
        Path file = directory.resolve("my_ratings.csv");
        try {
            RatingStore ratings = RatingStore.load(file);
            ratings.rate("movie-one", 4.5);
            ratings.rate("movie-one", 2.0);
            check(RatingStore.load(file).ratings().get("movie-one") == 2.0, "rating save and replacement");
            try {
                ratings.rate("movie-two", 4.2);
                throw new AssertionError("invalid rating was accepted");
            } catch (IllegalArgumentException expected) {
                check(!ratings.ratings().containsKey("movie-two"), "invalid rating leaves file unchanged");
            }
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    /** Constructs one movie with a single genre for readable test scenarios. */
    private static Movie movie(String id, String title, String certificate, int runtime,
                               String genre, double publicRating, long votes) {
        return new Movie(id, title, "2024", "Test Director", certificate, runtime,
                List.of(genre), publicRating, votes);
    }

    /** Stops the checks with a named failure. */
    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name);
        }
    }
}
