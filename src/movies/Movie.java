// AI-generated code
package movies;

import java.util.List;

/** Holds the catalog fields used to filter and recommend one movie. */
public record Movie(
        String id,
        String title,
        String year,
        String director,
        String certificate,
        int runtimeMinutes,
        List<String> genres,
        double publicRating,
        long votes) {

    /** Makes the genre list immutable so scoring sees consistent catalog data. */
    public Movie {
        genres = List.copyOf(genres);
    }
}
