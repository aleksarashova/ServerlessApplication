package org.example.functions.functions;

import com.google.gson.Gson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.example.functions.models.Movie;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Optional;

public class AddFilmFunction {

    private static final EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("MoviePU");
    }

    @FunctionName("CreateMovie")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request to create a movie.");

        String requestBody = request.getBody().orElse(null);
        if (requestBody == null || requestBody.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Request body is empty or missing.")
                    .build();
        }

        Movie movie = parseMovieFromJson(requestBody);
        if (movie == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Failed to parse JSON data.")
                    .build();
        }

        try {
            saveMovieToDatabase(movie);
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .body("Movie created successfully.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Failed to save movie to database: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save movie to database.")
                    .build();
        }
    }

    private Movie parseMovieFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Movie.class);
    }

    private void saveMovieToDatabase(Movie movie) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(movie);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }
}