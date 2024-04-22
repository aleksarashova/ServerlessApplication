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
import org.example.functions.models.Review;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.Optional;

public class AddReviewFunction {

    private static final EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("MoviePU"); // Name of your persistence unit in persistence.xml
    }

    @FunctionName("AddReview")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request to add a review.");

        String requestBody = request.getBody().orElse(null);
        if (requestBody == null || requestBody.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Request body is empty or missing.")
                    .build();
        }

        Review review = parseReviewFromJson(requestBody);
        if (review == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Failed to parse JSON data.")
                    .build();
        }

        try {
            saveReviewToDatabase(review);
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .body("Review added successfully.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Failed to save review to database: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save review to database.")
                    .build();
        }
    }

    private Review parseReviewFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Review.class);
    }

    private void saveReviewToDatabase(Review review) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(review);
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
