package com.modakdev.arcinema_image_search.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String movieName;

    private String url;

    // Default constructor (required by JPA)
    public Trailer() {
    }

    // Constructor with fields (optional, but often useful)
    public Trailer(String movieName, String url) {
        this.movieName = movieName;
        this.url = url;
    }

    // Getters and setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Trailer{" +
                "id=" + id +
                ", movieName='" + movieName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}