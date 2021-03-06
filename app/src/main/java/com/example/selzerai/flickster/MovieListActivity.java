package com.example.selzerai.flickster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.selzerai.flickster.models.Config;
import com.example.selzerai.flickster.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MovieListActivity extends AppCompatActivity {


    //url for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    //parameter name for the  API KEY
    public final static String API_KEY_PARAM = "api_key";

    public final static String TAG = "MovieListActivity";

    AsyncHttpClient client;

    // the url of the image
    String imageBaseUrl;

    // the size of the poster
    String posterSize;

    // the lsit of current movies
    ArrayList<Movie> movies;

    // the recycler view
    RecyclerView rvMovies;

    //the adapter wired to recycler view
    MovieAdapter adapter;

    // image config
    Config config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        //initializes objects
        client = new AsyncHttpClient();
        movies = new ArrayList<>();
        adapter = new MovieAdapter(movies);

        // resolves recycler view
        rvMovies = findViewById(R.id.rvMovies);
        // connects layout manager to adapter
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        getConfiguration();

    }

    // gets the list of now playing movies from the API

    private void getNowPlaying(){
        String url = API_BASE_URL +"/movie/now_playing";
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        client.get(url,params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    // iterates through list and creates movie objects
                    for (int i = 0; i < results.length(); i++){
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);

                        // notify adapter that row was changed
                        adapter.notifyItemInserted(movies.size() -1);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true );

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpoint", throwable, true);
            }
        });

    }
    //gets the image url and poster size if congigured
    private void getConfiguration(){
        String url = API_BASE_URL +"/configuration";
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    config = new Config(response);
                    Log.i(TAG,
                            String.format("Loaded configuration with imageBaseUrl %s and posterSize %s ",
                                    config.getImageBaseUrl(),
                                    config.getPosterSize()));
                    adapter.setConfig(config);
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("Failed parsing configuration", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed getting configuration", throwable, true);
            }
        });
    }

    //handle errors, log and alert error

    private void logError (String message, Throwable error, boolean alertUser){
      Log.e(TAG, message,error );
      if(alertUser){
          Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
      }
    }


}
