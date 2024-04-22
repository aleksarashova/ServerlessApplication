package org.example.functions.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import org.example.functions.models.Movie;
import org.example.functions.models.Review;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

public class SearchMovieFunction {

    private static final EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("MoviePU");
    }

    public HttpResponseMessage run(HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        context.getLogger().info("Searching for movies.");
        String searchString = request.getBody().orElse(null);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            Query query;
            if (searchString != null && !searchString.isEmpty()) {
                query = entityManager.createQuery("SELECT m FROM Movie m WHERE m.title LIKE :searchString");
                query.setParameter("searchString", "%" + searchString + "%");
            } else {
                query = entityManager.createQuery("SELECT m FROM Movie m");
            }
            List<Movie> movies = query.getResultList();
            for (Movie movie : movies) {
                movie.setReviews(fetchReviewsForMovie(movie, entityManager));
                movie.setAverageRating(calculateAverageRatingForMovie(movie, entityManager));
            }

            return request.createResponseBuilder(HttpStatus.OK).body(movies).build();
        } catch (Exception e) {
            context.getLogger().severe("Failed to search for movies: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            entityManager.close();
        }
    }

    private List<Review> fetchReviewsForMovie(Movie movie, EntityManager entityManager) {
        Query query = entityManager.createQuery("SELECT r FROM Review r WHERE r.movie = :movie");
        query.setParameter("movie", movie);
        return query.getResultList();
    }

    private double calculateAverageRatingForMovie(Movie movie, EntityManager entityManager) {
        Query query = entityManager.createQuery("SELECT AVG(r.rating) FROM Review r WHERE r.movie = :movie");
        query.setParameter("movie", movie);
        Double averageRating = (Double) query.getSingleResult();
        return averageRating != null ? averageRating : 0.0;
    }
}