package com.example.android.movieapp.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements Callbacks{

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(findViewById(R.id.movie_detail_container) != null){
            mTwoPane = true;
        }

    }

    @Override
    public void onItemSelected(Movie movie) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putSerializable("movieDetails", movie);
            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, detailActivityFragment).commit();

        }else{
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra("movieDetails", movie);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onListUpdate() {
        if(mTwoPane){
            DetailActivityFragment detailActivityFragment = (DetailActivityFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.movie_detail_container);
            if(detailActivityFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(detailActivityFragment).commit();
            }
        }
    }

    public void removeFavoriteMovie() {
        if (mTwoPane) {
            onListUpdate();
            MainActivityFragment mainActivityFragment = (MainActivityFragment) getSupportFragmentManager()
                    .findFragmentByTag("MainActivityFragment");
//            Log.v("MainActivityFragment", Boolean.toString(mainActivityFragment == null));
            if(mainActivityFragment != null) {
                mainActivityFragment.fetchFavoriteMoviesFromDb();
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
