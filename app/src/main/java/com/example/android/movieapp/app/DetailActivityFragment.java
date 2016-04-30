package com.example.android.movieapp.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.*;
import android.widget.*;
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
public class DetailActivityFragment extends Fragment {
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    Movie movie;
    private ListAdapter trailersListAdapter;
    private ListView trailersListView;
    private ListAdapter reviewsListAdapter;
    private ListView reviewsListView;

    private ShareActionProvider mShareActionProvider;
    private String mTrailerKey;
    private ArrayList<HashMap<String, String>> mTrailers, mReviews;
    private boolean mFavorite;
    private boolean inFavoriteList = true;
    private ProgressDialog loadingEffect;
    final String SPINNER_SELECTION = "spinner_selection";
    private Callbacks mCallbacks;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        if (inFavoriteList) {
            populateListViewWithData(mTrailers, trailersListView, trailersListAdapter,
                    R.id.detail_trailer_listview_header, "Trailers:", "No trailers.", Movie.VIDEOS);
            populateListViewWithData(mReviews, reviewsListView, reviewsListAdapter,
                    R.id.detail_reviews_listview_header, "Reviews:", "No reviews.", Movie.REVIEWS);
        }

        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent shareIntent = createShareTrailerIntent();
        if (shareIntent != null){
            mShareActionProvider.setShareIntent(shareIntent);
        }
//        Log.v("ShareActionProvider", Boolean.toString(shareIntent == null));
    }

    private Intent createShareTrailerIntent() {
//        Log.v("List Size 0", Boolean.toString(mTrailerKey == null));
        if (mTrailerKey != null) {
            String url = "https://www.youtube.com/watch?v=" + mTrailerKey;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            return shareIntent;
        }

        return null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        int spinnerPosition = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(SPINNER_SELECTION, 0);
        String sortOrder = getResources().getStringArray(R.array.sort_order_values)[spinnerPosition];

        if(!sortOrder.equals("favorite")) {
            inFavoriteList = false;
            loadingEffect = new ProgressDialog(getActivity());
            loadingEffect.setIndeterminate(true);
            loadingEffect.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingEffect.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            new FetchMoviesTask(Movie.VIDEOS).execute(movie.getMovieId());
            new FetchMoviesTask(Movie.REVIEWS).execute(movie.getMovieId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

//        Intent detailIntent = getActivity().getIntent();
//        if(detailIntent != null && detailIntent.hasExtra("movieDetails")){
//            movie = (Movie)detailIntent.getSerializableExtra("movieDetails");
        Bundle args = getArguments();
        if(args != null && args.containsKey("movieDetails")){
            movie = (Movie) args.getSerializable("movieDetails");
            mTrailers = movie.getTrailers();
            mReviews = movie.getReviews();
            mFavorite = movie.isFavorite();

            ImageView poster = (ImageView) rootView.findViewById(R.id.detail_poster);
            String postersDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MovieAppPosters";
            String posterName = movie.getPoster();
            poster.setImageDrawable(Drawable.createFromPath(postersDirPath + posterName));

            TextView title = (TextView) rootView.findViewById(R.id.detail_title);
            title.setText(movie.getTitle());

            TextView rating = (TextView) rootView.findViewById(R.id.detail_rating);
            rating.setText(movie.getRating());

            TextView release = (TextView) rootView.findViewById(R.id.detail_release);
            release.setText(movie.getReleaseDate());

            TextView plot = (TextView) rootView.findViewById(R.id.detail_plot);
            plot.setText(movie.getPlot());
        }

        trailersListAdapter = new ListAdapter(inflater, mTrailers,
                R.layout.list_item_trailer, R.id.list_item_trailer_textview, Movie.TRAILER_NAME);
        trailersListView = (ListView) rootView.findViewById(R.id.detail_trailer_listview);
        trailersListView.setAdapter(trailersListAdapter);

        reviewsListAdapter = new ListAdapter(inflater, mReviews,
                R.layout.list_item_review, R.id.list_item_review_textview, Movie.REVIEW_CONTENT);
        reviewsListView = (ListView) rootView.findViewById(R.id.detail_reviews_listview);
        reviewsListView.setAdapter(reviewsListAdapter);

        trailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playTrailer(mTrailers.get(position).get(Movie.TRAILER_KEY));
            }
        });

        reviewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Review")
                        .setMessage(mReviews.get(position).get(Movie.REVIEW_CONTENT) + "\n\n"
                                + mReviews.get(position).get(Movie.REVIEW_AUTHOR) + ",")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
            }
        });

        final ImageButton favoriteBtn = (ImageButton) rootView.findViewById(R.id.favorite_btn);
        if(mFavorite)favoriteBtn.setImageResource(R.drawable.ic_favorite_white_24dp);
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mFavorite){
                    mFavorite = true;
                    movie.setFavorite();
                    favoriteBtn.setImageResource(R.drawable.ic_favorite_white_24dp);
                    Toast.makeText(getActivity(), "Added to favorites", Toast.LENGTH_SHORT).show();
                }
                else{
                    mFavorite = false;
                    movie.removeFavorite();
                    if(inFavoriteList) {
                        Activity activity = getActivity();
                        if (activity instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) activity;
                            mainActivity.removeFavoriteMovie();
                        }
                    }

                    favoriteBtn.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                    Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }

    public class ListAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        int mLayoutId, mTextViewId;
        String mKey;
        ArrayList<HashMap<String, String>> mList;

        public ListAdapter(LayoutInflater inflater, ArrayList<HashMap<String, String>> list,
                           int layoutId, int textViewId, String key){
            mInflater = inflater;
            mList = list;
            mLayoutId = layoutId;
            mTextViewId = textViewId;
            mKey = key;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getItem(int position) {
            if(getCount() != 0) return mList.get(position).get(mKey);
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(mLayoutId, null);
            }

            final TextView listItemTextView = (TextView) convertView.findViewById(mTextViewId);
            String listItemText = getItem(position);
            listItemTextView.setText(listItemText);

            return convertView;
        }
    }

    public void setListViewHeight(ListAdapter listAdapter, ListView listView){
        int paddingHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        int childHeight, totalHeight;

        View listItem = listAdapter.getView(0, null, listView);
        listItem.measure(0, 0);
        childHeight = listItem.getMeasuredHeight();

        totalHeight = paddingHeight + (childHeight * listAdapter.getCount());
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>> > {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private String mOption;

        public FetchMoviesTask(String option){
            mOption = option;
        }

        private ArrayList<HashMap<String, String>> getMoviesDataFromJson(String jsonString) throws JSONException {
            ArrayList<HashMap<String, String>> result = new ArrayList<>();
            HashMap<String, String> hMap;
            final String TMDB_RESULTS = "results";

            JSONObject moviesJson = new JSONObject(jsonString);
            JSONArray moviesJsonArray = moviesJson.getJSONArray(TMDB_RESULTS);

            for(int i=0; i<moviesJsonArray.length(); i++){
                JSONObject movieObj = moviesJsonArray.getJSONObject(i);
                hMap = new HashMap<>();
                if(mOption.equals(Movie.VIDEOS)) {
                    hMap.put(Movie.TRAILER_NAME, movieObj.getString(Movie.TRAILER_NAME));
                    hMap.put(Movie.TRAILER_KEY, movieObj.getString(Movie.TRAILER_KEY));
                }
                else {
                    hMap.put(Movie.REVIEW_AUTHOR, movieObj.getString(Movie.REVIEW_AUTHOR));
                    hMap.put(Movie.REVIEW_CONTENT, movieObj.getString(Movie.REVIEW_CONTENT));
                }
                result.add(hMap);
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
                loadingEffect.show();
                loadingEffect.setContentView(R.layout.loading_effect);
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
                final String MOVIE_ID = params[0];

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(MOVIE_ID)
                        .appendPath(mOption)
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
            ArrayList<HashMap<String, String>> list = new ArrayList<>();

            if(result != null){
                for (HashMap<String, String> listData : result) {
                    list.add(listData);
                }

                if(mOption.equals(Movie.VIDEOS)){
                    mTrailers.clear();
                    mTrailers.addAll(list);
                    movie.setTrailers(mTrailers);
                    populateListViewWithData(mTrailers, trailersListView, trailersListAdapter,
                            R.id.detail_trailer_listview_header, "Trailers:", "No trailers.", Movie.VIDEOS);
                }else{
                    mReviews.clear();
                    mReviews.addAll(list);
                    movie.setReviews(mReviews);
                    populateListViewWithData(mReviews, reviewsListView, reviewsListAdapter,
                            R.id.detail_reviews_listview_header, "Reviews:", "No reviews.", Movie.REVIEWS);
                }

                if(loadingEffect != null && loadingEffect.isShowing())loadingEffect.dismiss();
            }
            else{
                Toast.makeText(getActivity(), "Network Unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateListViewWithData(ArrayList<HashMap<String, String>> list,
                                          ListView listView,
                                          ListAdapter listAdapter,
                                          int headerViewId,
                                          String headerText,
                                          String headerTextNo,
                                          String mOption){

        ViewGroup viewGroup = (ViewGroup) listView.getParent();
        if (list.size() == 0) {
            if(viewGroup != null){
                ((TextView)(viewGroup.findViewById(headerViewId))).setText(headerTextNo);
                viewGroup.removeView(listView);
            }

        } else {
            if(mOption.equals(Movie.VIDEOS)){
                mTrailerKey = list.get(0).get(Movie.TRAILER_KEY);
                if(mShareActionProvider != null){
                    mShareActionProvider.setShareIntent(createShareTrailerIntent());
                }
            }
            if(viewGroup != null){
                ((TextView)(viewGroup.findViewById(headerViewId))).setText(headerText);
            }
        }

        listAdapter.notifyDataSetChanged();
        setListViewHeight(listAdapter, listView);
    }

    private void playTrailer(String id){
        Uri uriLocation = Uri.parse("vnd.youtube:" + id);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uriLocation);
        if(intent.resolveActivity(getActivity().getPackageManager()) != null){
            startActivity(intent);
        }
        else{
            Log.d("Play Trailer", "failed to resolve youtube view intent");
        }

    }

    @Override
    public void onPause(){
        super.onDestroy();
        if(loadingEffect != null && loadingEffect.isShowing())loadingEffect.dismiss();
    }

}
