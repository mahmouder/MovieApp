package com.example.android.movieapp.app.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * Created by Mahmoud on 4/25/2016.
 */

@Table(name = "favorite")
public class Favorite extends Model{

    @Column(name = "movieId", unique = true)
    public String movieId;

    @Column(name = "posterPath")
    public String posterPath;

    @Column(name = "title")
    public String title;

    @Column(name = "releaseDate")
    public String releaseDate;

    @Column(name = "rating")
    public String rating;

    @Column(name = "plot")
    public String plot;

    public Favorite(){
        super();
    }

    public List<Trailer> trailers() {
        return getMany(Trailer.class, "favoriteFK");
    }

    public List<Review> reviews() {
        return getMany(Review.class, "favoriteFK");
    }

}
