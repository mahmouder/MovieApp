package com.example.android.movieapp.app;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.android.movieapp.app.models.Favorite;
import com.example.android.movieapp.app.models.Review;
import com.example.android.movieapp.app.models.Trailer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mahmoud on 4/25/2016.
 */
public class Movie implements Serializable{
    static final String MOVIE_ID = "id";
    static final String POSTER_PATH = "poster_path";
    static final String TITLE = "title";
    static final String RELEASE = "release_date";
    static final String RATING = "vote_average";
    static final String PLOT = "overview";
    static final String VIDEOS = "videos";
    static final String TRAILER_NAME = "name";
    static final String TRAILER_KEY = "key";
    static final String REVIEWS = "reviews";
    static final String REVIEW_AUTHOR = "author";
    static final String REVIEW_CONTENT = "content";

    private String movieId;
    private String poster;
    private String title;
    private String releaseDate;
    private String rating;
    private String plot;
    private ArrayList<HashMap<String, String>>trailers;
    private ArrayList<HashMap<String, String>>reviews;

    public Movie() {

    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public ArrayList<HashMap<String, String>> getTrailers() {
        return trailers;
    }

    public void setTrailers(ArrayList<HashMap<String, String>> trailers) {
        this.trailers = trailers;
    }

    public ArrayList<HashMap<String, String>> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<HashMap<String, String>> reviews) {
        this.reviews = reviews;
    }

    public String formatReleaseDate(String releaseDate){
        return releaseDate.substring(0, releaseDate.indexOf("-"));
    }

    public String formatRating(String rating){
        return rating + "/10";
    }

    public boolean isFavorite(){
        Favorite movie = new Select()
                .from(Favorite.class)
                .where("movieId = ?", this.getMovieId())
                .executeSingle();
        return (movie != null);
    }

    public void setFavorite(){
        Favorite favoriteMovie = new Favorite();
        favoriteMovie.movieId = this.movieId;
        favoriteMovie.posterPath = this.poster;
        favoriteMovie.title = this.title;
        favoriteMovie.releaseDate = this.releaseDate;
        favoriteMovie.rating = this.rating;
        favoriteMovie.plot = this.plot;
        favoriteMovie.save();

//        Log.v("Fav Trailers Size", Integer.toString(this.trailers.size()));
        ActiveAndroid.beginTransaction();
        try{
            for(int i=0; i<this.trailers.size(); i++){
                Trailer trailer = new Trailer();
                trailer.name = this.trailers.get(i).get(TRAILER_NAME);
                trailer.key = this.trailers.get(i).get(TRAILER_KEY);
                trailer.favorite = favoriteMovie;
                trailer.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        }finally{
            ActiveAndroid.endTransaction();
        }

//        Log.v("Fav Reviews Size", Integer.toString(this.reviews.size()));
        ActiveAndroid.beginTransaction();
        try{
            for(int i=0; i<this.reviews.size(); i++){
                Review review = new Review();
                review.content = this.reviews.get(i).get(REVIEW_CONTENT);
                review.author = this.reviews.get(i).get(REVIEW_AUTHOR);
                review.favorite = favoriteMovie;
                review.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        }finally{
            ActiveAndroid.endTransaction();
        }

    }

    public void removeFavorite(){
        new Delete().from(Favorite.class).where("movieId = ?", this.movieId).execute();
    }

}
