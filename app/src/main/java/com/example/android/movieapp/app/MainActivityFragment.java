package com.example.android.movieapp.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public Spinner spinner;
    public ImageAdapter imageAdapter;
    public ArrayList<HashMap<String, String> > moviesList;
    final String POSTER = "poster_path";
    final String PLOT = "overview";
    final String RELEASE = "release_date";
    final String TITLE = "title";
    final String RATING = "vote_average";

    public MainActivityFragment() {
        moviesList = new ArrayList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        int position = spinner.getSelectedItemPosition();
        String sortOrder = getResources().getStringArray(R.array.sort_order_values)[position];
        updateMovies(sortOrder);
    }

    private void updateMovies(String sortOrder){
        new FetchMoviesTask().execute(sortOrder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        imageAdapter = new ImageAdapter(getActivity());
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra("movieDetails", moviesList.get(position));
                startActivity(detailIntent);
            }
        });

        spinner = (Spinner) rootView.findViewById(R.id.sort_order_spinner);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sort_order,
                R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOrder = parent.getResources().getStringArray(R.array.sort_order_values)[position];
                updateMovies(sortOrder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    public class ImageAdapter extends BaseAdapter{
        private Context context;

        public ImageAdapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return moviesList.size();
        }

        @Override
        public String getItem(int position) {
            String baseUrl = "http://image.tmdb.org/t/p/w342";
            return baseUrl + moviesList.get(position).get(POSTER);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // initialize some attributes
                imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (ImageView) convertView;
            }
            // Picasso load image from url into imageView
            String url = getItem(position);
            Picasso.with(context).load(url).into(imageView);

            return imageView;
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>> >{
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private ArrayList<HashMap<String, String>> getMoviesDataFromJson(String jsonString) throws JSONException{
            ArrayList<HashMap<String, String>> result = new ArrayList<>();
            HashMap<String, String> hMap;
            final String TMDB_RESULTS = "results";

            JSONObject moviesJson = new JSONObject(jsonString);
            JSONArray moviesJsonArray = moviesJson.getJSONArray(TMDB_RESULTS);

            for(int i=0; i<moviesJsonArray.length(); i++){
                JSONObject movieObj = moviesJsonArray.getJSONObject(i);
                hMap = new HashMap<>();
                hMap.put(POSTER, movieObj.getString(POSTER));
                hMap.put(TITLE, movieObj.getString(TITLE));
                hMap.put(RATING, movieObj.getString(RATING));
                hMap.put(PLOT, movieObj.getString(PLOT));
                hMap.put(RELEASE, movieObj.getString(RELEASE));
                result.add(hMap);
            }

            return result;
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonString = null;

            try {
                final String BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";
                final String APIKEY = BuildConfig.TMDB_API_KEY;

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(APIKEY_PARAM, APIKEY)
                        .build();

//                Log.v(LOG_TAG, builtUri.toString());

                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonString = buffer.toString();

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(jsonString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result){
            if(result != null){
                moviesList.clear();
                for(HashMap<String, String> movieData: result) {
                    moviesList.add(movieData);
                }
                imageAdapter.notifyDataSetChanged();
            }
            else{
                Toast.makeText(getActivity(), "Network Unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
