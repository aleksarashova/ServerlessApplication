package org.example.functions.models;

import org.hibernate.mapping.PrimaryKey;

import javax.persistence.*;
import java.util.List;

@Entity
public class Movie {
    @Id
    private Long id;
    private String title;
    private int year;
    private String genre;
    private String description;
    private String director;
    private String actors;

    @OneToMany(mappedBy = "movie")
    private List<Review> reviews;

    @OneToOne(mappedBy = "movie", cascade = CascadeType.ALL)
    private MovieStatistics statistics;

    public Movie(String title, int year, String genre, String description, String director, String actors) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.description = description;
        this.director = director;
        this.actors = actors;
    }

    public Movie() {

    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public MovieStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(MovieStatistics statistics) {
        this.statistics = statistics;
    }

    public double getAverageRating() {
        if (statistics != null) {
            return statistics.getAverageRating();
        }
        return 0.0;
    }

    public void setAverageRating(double averageRating) {
        if (statistics == null) {
            statistics = new MovieStatistics();
        }
        statistics.setAverageRating(averageRating);
    }
}
