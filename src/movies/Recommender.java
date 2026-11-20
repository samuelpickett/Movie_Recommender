// AI-generated code
package movies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ranks unwatched catalog movies using the person's genre ratings. */
public final class Recommender {
    private final MovieCatalog catalog;

    /** Uses one catalog for any set of personal ratings, including a future GUI. */
    public Recommender(MovieCatalog catalog) {
        this.catalog = catalog;
    }

    /** Returns up to limit eligible movies, or diverse public favorites for a new user. */
    public List<Result> recommend(Map<String, Double> personalRatings, Integer maximumRuntime,
                                String maximumCertificate, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        if (maximumRuntime != null && maximumRuntime <= 0) {
            throw new IllegalArgumentException("Maximum runtime must be positive");
        }
        int certificateLimit = maximumCertificate == null ? -1 : certificateRank(maximumCertificate);
        if (maximumCertificate != null && certificateLimit < 0) {
            throw new IllegalArgumentException("Use G, PG, PG-13, R, or NC-17");
        }

        Map<String, Double> genreTotals = new HashMap<>();
        Map<String, Integer> genreCounts = new HashMap<>();
        for (Map.Entry<String, Double> entry : personalRatings.entrySet()) {
            Movie watched = catalog.get(entry.getKey());
            if (watched == null) {
                continue;
            }
            for (String genre : watched.genres()) {
                genreTotals.merge(genre, entry.getValue() - 3.0, Double::sum);
                genreCounts.merge(genre, 1, Integer::sum);
            }
        }

        List<Result> results = new ArrayList<>();
        for (Movie movie : catalog.movies()) {
            if (personalRatings.containsKey(movie.id()) || !eligible(movie, maximumRuntime, certificateLimit)) {
                continue;
            }
            double score = 0;
            for (String genre : movie.genres()) {
                // Two neutral observations prevent one rating from setting a strong genre preference.
                score += genreTotals.getOrDefault(genre, 0.0) / (genreCounts.getOrDefault(genre, 0) + 2.0);
            }
            // The square root rewards several matching genres without giving every long genre list full weight.
            results.add(new Result(movie, score / Math.sqrt(movie.genres().size())));
        }
        results.sort(Recommender::compare);
        if (genreCounts.isEmpty()) {
            return diverseStarterList(results, limit);
        }
        return results.subList(0, Math.min(limit, results.size()));
    }

    /** Applies optional runtime and US certificate limits before scoring. */
    private static boolean eligible(Movie movie, Integer maximumRuntime, int certificateLimit) {
        if (maximumRuntime != null && movie.runtimeMinutes() > maximumRuntime) {
            return false;
        }
        int movieCertificate = certificateRank(movie.certificate());
        return certificateLimit < 0 || (movieCertificate >= 0 && movieCertificate <= certificateLimit);
    }

    /** Orders equal personal scores by public rating, votes, title, then ID. */
    private static int compare(Result left, Result right) {
        int order = Double.compare(right.score(), left.score());
        if (order == 0) {
            order = Double.compare(right.movie().publicRating(), left.movie().publicRating());
        }
        if (order == 0) {
            order = Long.compare(right.movie().votes(), left.movie().votes());
        }
        if (order == 0) {
            order = String.CASE_INSENSITIVE_ORDER.compare(left.movie().title(), right.movie().title());
        }
        return order == 0 ? left.movie().id().compareTo(right.movie().id()) : order;
    }

    /** Chooses high-rated movies across primary genres when there is no history. */
    private static List<Result> diverseStarterList(List<Result> sorted, int limit) {
        List<Result> chosen = new ArrayList<>();
        Set<String> primaryGenres = new HashSet<>();
        Set<String> chosenIds = new HashSet<>();
        for (Result result : sorted) {
            if (result.movie().votes() >= 1000 && primaryGenres.add(result.movie().genres().get(0))) {
                chosen.add(result);
                chosenIds.add(result.movie().id());
                if (chosen.size() == limit) {
                    return chosen;
                }
            }
        }
        for (Result result : sorted) {
            if (chosenIds.add(result.movie().id())) {
                chosen.add(result);
                if (chosen.size() == limit) {
                    break;
                }
            }
        }
        return chosen;
    }

    /** Gives the ordering of recognized US certificates; unknown labels return -1. */
    private static int certificateRank(String certificate) {
        return switch (certificate) {
            case "G" -> 0;
            case "PG" -> 1;
            case "PG-13" -> 2;
            case "R" -> 3;
            case "NC-17" -> 4;
            default -> -1;
        };
    }

    /** Pairs a movie with its personal genre score. */
    public record Result(Movie movie, double score) {
    }
}
