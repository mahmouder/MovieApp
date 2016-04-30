package com.example.android.movieapp.app.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Mahmoud on 4/25/2016.
 */

@Table(name = "trailer")
public class Trailer extends Model {

    @Column(name = "name")
    public String name;

    @Column(name = "key")
    public String key;

    @Column(name = "favoriteFK", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Favorite favorite;

    public Trailer(){
        super();
    }

    public Trailer(String name, String key, Favorite favorite){
        super();
        this.name = name;
        this.key = key;
        this.favorite = favorite;
    }

}
