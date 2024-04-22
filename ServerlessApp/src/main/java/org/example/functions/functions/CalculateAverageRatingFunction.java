package org.example.functions.functions;

import com.microsoft.azure.functions.ExecutionContext;

import javax.persistence.*;

public class CalculateAverageRatingFunction {

    private static final EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("MoviePU");
    }

    public void run(final ExecutionContext context) {
        context.getLogger().info("Calculating and updating average rating for movies.");

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            double averageRating = calculateAverageRating(entityManager);
            updateAverageRatingInDatabase(averageRating, entityManager);

            context.getLogger().info("Average rating calculated and saved: " + averageRating);
        } catch (Exception e) {
            context.getLogger().severe("Failed to calculate and save average rating: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    private double calculateAverageRating(EntityManager entityManager) {
        Query query = entityManager.createQuery("SELECT AVG(r.rating) FROM Review r");
        return (Double) query.getSingleResult();
    }

    private void updateAverageRatingInDatabase(double averageRating, EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Query updateQuery = entityManager.createQuery("UPDATE MovieStatistics ms SET ms.averageRating = :averageRating");
            updateQuery.setParameter("averageRating", averageRating);
            updateQuery.executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}