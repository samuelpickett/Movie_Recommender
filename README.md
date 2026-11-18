# Movie recommender

A personal Java command line recommender that learns genre preferences from movies you rate. The catalog is stored in CSV. Your ratings are saved separately in `data/my_ratings.csv`.

## Run

Requires a Java 17 or newer JDK. From PowerShell:

```powershell
cd C:\Users\samue\CSE-381\Movie_Recommender
.\run.ps1
```

The script compiles the source and opens a menu to find and rate movies, request ten recommendations, or view your ratings. Type part of a movie title to find it. Your ratings use 0.5 to 5 stars in half-star steps. Rating a movie again replaces your earlier rating.

When requesting recommendations, press Enter to skip either optional limit. With no age limit, movies with unknown or non-US certificates remain eligible. With a US certificate limit, only G, PG, PG-13, R, and NC-17 certificates at or below that limit are eligible. Movies already rated are excluded.

The personal score combines your genre preferences. A 3-star rating is neutral; higher ratings raise the scores of shared genres, and lower ratings reduce them. Two neutral observations smooth each genre so one rating has limited influence. Matching several genres helps more than matching one. Ties use the catalog's public IMDb rating, then vote count, title, and ID. With no personal ratings, the program shows a varied starter list of publicly well-rated movies.

## Catalog

`data/movies.csv` is a compact catalog derived from [Aman Barthwal's IMDb Movies Data on Kaggle](https://www.kaggle.com/datasets/amanbarthwal/imdb-movies-data). It retains title, source year, director, certificate, runtime, genres, public rating, and votes. Some source years are visibly inconsistent with the movie titles, so the interface displays the director instead of the year. The supplied source does not include IMDb or TMDB IDs. The preparation script assigns stable IDs based on title, year, and director; it rejects duplicate identities. If any of those three fields changes in a later source version, the movie's ID changes and its saved personal rating will need to be matched again.

To rebuild the catalog from a downloaded copy of the source CSV:

```powershell
.\prepare-catalog.ps1 -SourceCsv 'C:\path\to\imdb-movies-dataset.csv'
```

The script skips rows missing the title, genre, valid runtime, public rating, or vote count. The original certificate is retained without guessing a US equivalent. This catalog is a snapshot; it does not fetch live movie data.

## Project layout

- `src/movies`: catalog loading, personal rating storage, scoring, and command line interface
- `data/movies.csv`: prepared movie catalog
- `data/my_ratings.csv`: your personal ratings, ignored by Git
- `tests/movies`: focused checks for CSV parsing, ranking, limits, and saved ratings
