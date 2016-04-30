package com.example.android.movieapp.app.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Mahmoud on 4/25/2016.
 */

@Table(name = "review")
public class Review extends Model {

    @Column(name = "content")
    public String content;

    @Column(name = "author")
    public String author;

    @Column(name = "favoriteFK", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Favorite favorite;

    public Review(){
        super();
    }

    public Review(String content, String author, Favorite favorite){
        super();
        this.content = content;
        this.author = author;
        this.favorite = favorite;
    }

}
