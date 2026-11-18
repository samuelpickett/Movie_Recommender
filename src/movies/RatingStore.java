// AI-generated code
package movies;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Keeps one person's ratings in a separate CSV file. */
public final class RatingStore {
    private static final List<String> HEADER = List.of("MovieId", "PersonalStars");
    private final Path path;
    private final Map<String, Double> ratings;

    /** Starts a rating store from validated entries already in memory. */
    private RatingStore(Path path, Map<String, Double> ratings) {
        this.path = path;
        this.ratings = new HashMap<>(ratings);
    }

    /** Loads saved ratings, or starts empty when no ratings file exists. */
    public static RatingStore load(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new RatingStore(path, Map.of());
        }
        List<List<String>> rows = Csv.read(path);
        if (rows.isEmpty() || !rows.get(0).equals(HEADER)) {
            throw new IOException("Unexpected ratings header in " + path);
        }
        Map<String, Double> ratings = new HashMap<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            try {
                if (row.size() != 2) {
                    throw new IllegalArgumentException("Expected 2 columns");
                }
                double stars = Double.parseDouble(row.get(1));
                validate(stars);
                if (ratings.putIfAbsent(row.get(0), stars) != null) {
                    throw new IllegalArgumentException("Duplicate movie ID");
                }
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid ratings row " + (index + 1) + ": " + exception.getMessage(), exception);
            }
        }
        return new RatingStore(path, ratings);
    }

    /** Saves or replaces one personal rating after checking the five-star scale. */
    public void rate(String movieId, double stars) throws IOException {
        validate(stars);
        Double previous = ratings.put(movieId, stars);
        try {
            save();
        } catch (IOException exception) {
            if (previous == null) {
                ratings.remove(movieId);
            } else {
                ratings.put(movieId, previous);
            }
            throw exception;
        }
    }

    /** Returns a snapshot so callers cannot change ratings without saving. */
    public Map<String, Double> ratings() {
        return Map.copyOf(ratings);
    }

    /** Writes the entire small personal file, replacing it atomically when supported. */
    private void save() throws IOException {
        Path directory = path.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "ratings-", ".csv");
        try {
            StringBuilder csv = new StringBuilder("MovieId,PersonalStars\n");
            for (Map.Entry<String, Double> entry : new TreeMap<>(ratings).entrySet()) {
                csv.append(entry.getKey()).append(',')
                        .append(String.format(Locale.ROOT, "%.1f", entry.getValue())).append('\n');
            }
            Files.writeString(temporary, csv.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** Rejects ratings outside 0.5–5.0 or between half-star steps. */
    private static void validate(double stars) {
        if (!Double.isFinite(stars) || stars < 0.5 || stars > 5.0 || stars * 2 != Math.rint(stars * 2)) {
            throw new IllegalArgumentException("Rating must be 0.5 to 5.0 in half-star steps");
        }
    }
}
