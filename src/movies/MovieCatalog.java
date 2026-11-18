// AI-generated code
package movies;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Loads the prepared catalog and supports title searches. */
public final class MovieCatalog {
    private static final List<String> HEADER = List.of(
            "Id", "Title", "Year", "Director", "Certificate", "RuntimeMinutes", "Genres", "PublicRating", "Votes");

    private final List<Movie> movies;
    private final Map<String, Movie> byId;

    /** Indexes movies by their persistent IDs and rejects duplicate IDs. */
    public MovieCatalog(List<Movie> movies) {
        this.movies = List.copyOf(movies);
        this.byId = new HashMap<>();
        for (Movie movie : movies) {
            if (byId.putIfAbsent(movie.id(), movie) != null) {
                throw new IllegalArgumentException("Duplicate movie ID: " + movie.id());
            }
        }
    }

    /** Loads the normalized catalog, reporting the row of malformed input. */
    public static MovieCatalog load(Path path) throws IOException {
        List<List<String>> rows = Csv.read(path);
        if (rows.isEmpty() || !rows.get(0).equals(HEADER)) {
            throw new IOException("Unexpected catalog header in " + path);
        }
        List<Movie> movies = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            try {
                if (row.size() != HEADER.size()) {
                    throw new IllegalArgumentException("Expected " + HEADER.size() + " columns");
                }
                List<String> genres = List.of(row.get(6).split("\\|"));
                movies.add(new Movie(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                        Integer.parseInt(row.get(5)), genres, Double.parseDouble(row.get(7)),
                        Long.parseLong(row.get(8))));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid catalog row " + (index + 1) + ": " + exception.getMessage(), exception);
            }
        }
        return new MovieCatalog(movies);
    }

    /** Returns up to limit movies whose titles contain the query. */
    public List<Movie> search(String query, int limit) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty() || limit <= 0) {
            return List.of();
        }
        List<Movie> matches = new ArrayList<>();
        for (Movie movie : movies) {
            if (movie.title().toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(movie);
                if (matches.size() == limit) {
                    break;
                }
            }
        }
        return matches;
    }

    /** Finds a movie by the ID stored in the user's ratings file. */
    public Movie get(String id) {
        return byId.get(id);
    }

    /** Returns the complete, immutable movie list for recommendation. */
    public List<Movie> movies() {
        return movies;
    }
}
