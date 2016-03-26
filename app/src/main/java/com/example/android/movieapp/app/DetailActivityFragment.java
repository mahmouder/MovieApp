package com.example.android.movieapp.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private static final String baseUrl = "http://image.tmdb.org/t/p/w342";
    private static final String POSTER = "poster_path";
    private static final String PLOT = "overview";
    private static final String RELEASE = "release_date";
    private static final String TITLE = "title";
    private static final String RATING = "vote_average";
    HashMap<String, String> movieDetails;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent detailIntent = getActivity().getIntent();
        if(detailIntent != null && detailIntent.hasExtra("movieDetails")){
            movieDetails = (HashMap<String, String>)detailIntent.getSerializableExtra("movieDetails");

            ImageView poster = (ImageView) rootView.findViewById(R.id.detail_poster);
            String url = baseUrl + movieDetails.get(POSTER);
            Picasso.with(getActivity()).load(url).into(poster);

            TextView title = (TextView) rootView.findViewById(R.id.detail_title);
            title.setText(movieDetails.get(TITLE));

            TextView rating = (TextView) rootView.findViewById(R.id.detail_rating);
            rating.append(movieDetails.get(RATING));

            TextView release = (TextView) rootView.findViewById(R.id.detail_release);
            release.append(movieDetails.get(RELEASE));

            TextView plot = (TextView) rootView.findViewById(R.id.detail_plot);
            plot.append(movieDetails.get(PLOT));

        }

        return rootView;
    }
}
