package com.example.android.movieapp.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.example.android.movieapp.app.models.Favorite;
import com.example.android.movieapp.app.models.Review;
import com.example.android.movieapp.app.models.Trailer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ProgressDialog loadingEffect;
    private Spinner spinner;
    private SharedPreferences prefs;
    public static ImageAdapter imageAdapter;
    public static ArrayList<Movie> moviesList;
    final String SPINNER_SELECTION = "spinner_selection";
    private static int pageNumber = 1;
    public static String totalPages;
    private LinearLayout pagination;
    private TextView pagesTextView;
    private ImageButton nextPageBtn;
    private ImageButton prevPageBtn;

    private Callbacks mCallbacks = sDummyCallbacks;

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Movie movie) {
        }
        @Override
        public void onListUpdate() {
        }
    };

    public MainActivityFragment() {
        moviesList = new ArrayList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "MainActivity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActiveAndroid.initialize(getActivity());
    }

    @Override
    public void onStart(){
        super.onStart();

        loadingEffect = new ProgressDialog(getActivity());
        loadingEffect.setIndeterminate(true);
        loadingEffect.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingEffect.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int spinnerPosition = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(SPINNER_SELECTION, 0);
        spinner.setSelection(spinnerPosition);

        String sortOrder = getResources().getStringArray(R.array.sort_order_values)[spinnerPosition];

        if(!sortOrder.equals("favorite")){
            updateMovies(sortOrder, pageNumber);
        }
        else{
            pagination.setVisibility(View.GONE);
            fetchFavoriteMoviesFromDb();
        }

    }

    private void updateMovies(String sortOrder, int pageNumber){
        new FetchMoviesTask().execute(sortOrder, Integer.toString(pageNumber));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        imageAdapter = new ImageAdapter(getActivity());
        final GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(imageAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onItemSelected(moviesList.get(position));

//                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
//                detailIntent.putExtra("movieDetails", moviesList.get(position));
//                startActivity(detailIntent);
            }
        });

        spinner = (Spinner) rootView.findViewById(R.id.sort_order_spinner);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sort_order,
                R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        final AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onListUpdate();

                prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putInt(SPINNER_SELECTION, position);
                prefsEditor.commit();

                String sortOrder = parent.getResources().getStringArray(R.array.sort_order_values)[position];

                pageNumber = 1;
                if(!sortOrder.equals("favorite")){
                    moviesList.clear();
                    gridView.removeAllViewsInLayout();
                    updateMovies(sortOrder, pageNumber);
                }
                else{
                    pagination.setVisibility(View.GONE);
                    fetchFavoriteMoviesFromDb();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(listener);
            }
        });

        pagination = (LinearLayout) rootView.findViewById(R.id.pagination);
        nextPageBtn = (ImageButton) rootView.findViewById(R.id.next_page_btn);
        prevPageBtn = (ImageButton) rootView.findViewById(R.id.previous_page_btn);
        pagesTextView = (TextView) rootView.findViewById(R.id.page_number_textView);

        nextPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onListUpdate();
                if(pageNumber < Integer.parseInt(totalPages)) {
                    if(!prevPageBtn.isEnabled()){
                        prevPageBtn.setEnabled(true);
                        prevPageBtn.setVisibility(View.VISIBLE);
                    }
                    pageNumber++;
                    String sortOrder = getResources().getStringArray(R.array.sort_order_values)[spinner.getSelectedItemPosition()];
                    pagesTextView.setText(Integer.toString(pageNumber) + " of " + totalPages);
                    updateMovies(sortOrder, pageNumber);
                }
            }
        });

        prevPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onListUpdate();
                if(pageNumber > 1){
                    if(!nextPageBtn.isEnabled()){
                        nextPageBtn.setEnabled(true);
                        nextPageBtn.setVisibility(View.VISIBLE);
                    }
                    pageNumber--;
                    String sortOrder = getResources().getStringArray(R.array.sort_order_values)[spinner.getSelectedItemPosition()];
                    pagesTextView.setText(Integer.toString(pageNumber) + " of " + totalPages);
                    updateMovies(sortOrder, pageNumber);
                }
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
            String baseUrl = "http://image.tmdb.org/t/p/w" + getResources().getInteger(R.integer.poster_image_size);
            return baseUrl + moviesList.get(position).getPoster();
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


            String postersDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MovieAppPosters";
            String posterName = moviesList.get(position).getPoster();

            File postersDir = new File(postersDirPath);
            if(!postersDir.exists()){
                postersDir.mkdir();
            }

            File imageFile = new File(postersDirPath, posterName);
            if(postersDir.exists() && imageFile.exists()){
//                Log.v("Loading Images: ", "From Memory");
                imageView.setImageDrawable(Drawable.createFromPath(postersDirPath + posterName));
            }
            else{
//                Log.v("Loading Images: ", "From Picasso");
                ImageTarget target = new ImageTarget(imageFile, imageView);
                imageView.setTag(target);

                // Picasso load image from url into Target
                String url = getItem(position);
                Picasso.with(getActivity()).load(url).into(target);

            }

            return imageView;
        }
    }

    private class ImageTarget implements Target{

        File imageFile;
        ImageView imageView;

        public ImageTarget (File imageFile, ImageView imageView){
            this.imageFile = imageFile;
            this.imageView = imageView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            imageView.setImageBitmap(bitmap);
//            Log.v("On Bitmab Loaded: ", imageFile.getName());
            try{
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                outputStream.close();
            }catch(Exception e){
                Log.d("Picasso Target: ", "Failed to save image into target");
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
//            Log.v("FAILED", "FAILED LOADING BITMAP");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}

    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie> >{
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private ArrayList<Movie> getMoviesDataFromJson(String jsonString) throws JSONException{
            ArrayList<Movie> result = new ArrayList<>();
            Movie movie;
            final String TMDB_RESULTS = "results";
            final String TMDB_TOTAL_PAGES = "total_pages";

            JSONObject moviesJson = new JSONObject(jsonString);
            JSONArray moviesJsonArray = moviesJson.getJSONArray(TMDB_RESULTS);
            totalPages = moviesJson.getString(TMDB_TOTAL_PAGES);
//            Log.v("TOTAL PAGES", totalPages);

            for(int i=0; i<moviesJsonArray.length(); i++){
                JSONObject movieObj = moviesJsonArray.getJSONObject(i);
                movie = new Movie();
                movie.setMovieId(movieObj.getString(Movie.MOVIE_ID));
                movie.setPoster(movieObj.getString(Movie.POSTER_PATH));
                movie.setTitle(movieObj.getString(Movie.TITLE));
                movie.setReleaseDate(movie.formatReleaseDate(movieObj.getString(Movie.RELEASE)));
                movie.setRating(movie.formatRating(movieObj.getString(Movie.RATING)));
                movie.setPlot(movieObj.getString(Movie.PLOT));
                movie.setTrailers(new ArrayList<HashMap<String, String>>());
                movie.setReviews(new ArrayList<HashMap<String, String>>());
                result.add(movie);
            }

            return result;
        }

        @Override
        protected void onPreExecute(){
//            Log.v("Loading Effect", "showing");
            loadingEffect.show();
            loadingEffect.setContentView(R.layout.loading_effect);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonString = null;

            try {
                final String BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";
                final String PAGE_PARAM = "page";
                final String APIKEY = BuildConfig.TMDB_API_KEY;

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(APIKEY_PARAM, APIKEY)
                        .appendQueryParameter(PAGE_PARAM, params[1])
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
        protected void onPostExecute(ArrayList<Movie> result){
            if(result != null){
                moviesList.clear();
                for(Movie movie: result) {
                    moviesList.add(movie);
                }

                imageAdapter.notifyDataSetChanged();
                if(loadingEffect != null && loadingEffect.isShowing())loadingEffect.dismiss();

                if(pageNumber == Integer.parseInt(totalPages)){
                    nextPageBtn.setEnabled(false);
                    nextPageBtn.setVisibility(View.GONE);
                }
                if(pageNumber == 1){
                    prevPageBtn.setEnabled(false);
                    prevPageBtn.setVisibility(View.GONE);
                }
                pagesTextView.setText(Integer.toString(pageNumber) + " of " + totalPages);
                if(pagination.getVisibility() == View.GONE)pagination.setVisibility(View.VISIBLE);
            }
            else{
                Toast.makeText(getActivity(), "Network Unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void fetchFavoriteMoviesFromDb(){
        moviesList.clear();
        List<Favorite> favoriteMovies = new Select().from(Favorite.class).execute();
        if(favoriteMovies.size() == 0) {
            Toast.makeText(getActivity(), "Favorite list is empty!", Toast.LENGTH_SHORT).show();
        }
//        Log.v("Favorites size: ", Integer.toString(favoriteMovies.size()));

        for(Favorite favMovie: favoriteMovies){
            Movie movie = new Movie();
            movie.setMovieId(favMovie.movieId);
            movie.setPoster(favMovie.posterPath);
            movie.setTitle(favMovie.title);
            movie.setReleaseDate(favMovie.releaseDate);
            movie.setRating(favMovie.rating);
            movie.setPlot(favMovie.plot);

//            Log.v("fetch Fav Trailers Size", Integer.toString(favMovie.trailers().size()));
            ArrayList<HashMap<String, String>>trailers = new ArrayList<>();
            for(Trailer trailer: favMovie.trailers()){
                HashMap<String, String>hMap = new HashMap<>();
                hMap.put(Movie.TRAILER_NAME, trailer.name);
                hMap.put(Movie.TRAILER_KEY, trailer.key);
                trailers.add(hMap);
            }
            movie.setTrailers(trailers);
//            Log.v("fetch trailers Size", Integer.toString(trailers.size()));

//            Log.v("fetch Fav reviews Size", Integer.toString(favMovie.reviews().size()));
            ArrayList<HashMap<String, String>>reviews = new ArrayList<>();
            for(Review review: favMovie.reviews()){
                HashMap<String, String>hMap = new HashMap<>();
                hMap.put(Movie.REVIEW_CONTENT, review.content);
                hMap.put(Movie.REVIEW_AUTHOR, review.author);
                reviews.add(hMap);
            }
            movie.setReviews(reviews);

            moviesList.add(movie);
        }
        imageAdapter.notifyDataSetChanged();

        if(loadingEffect != null && loadingEffect.isShowing())loadingEffect.dismiss();
    }

    @Override
    public void onPause(){
        super.onDestroy();
        if(loadingEffect != null && loadingEffect.isShowing())loadingEffect.dismiss();
    }

}
