package com.example.instagram;

import android.app.Application;

import com.example.instagram.model.Comment;
import com.example.instagram.model.Post;
import com.parse.Parse;
import com.parse.ParseObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // for troubleshooting
        // TODO - remove for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        // register ParseUser as a subclass to tell Parse this is a custom Parse model
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Comment.class);

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("instaChiara")
                .clientKey("Rome2012") // set explicitly unless clientKey is explicitly configured on Parse server
                .clientBuilder(builder)
                .server("http://chiaramd-fbu-instagram.herokuapp.com/parse")
                .build();

        Parse.initialize(configuration);

        // New test creation of object
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();
    }
}
