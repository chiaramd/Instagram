package com.example.instagram.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Collections;


@ParseClassName("Post")
public class Post extends ParseObject {
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_USER = "user";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_LIKED_BY = "likedBy";

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public Integer getLikes() {
        return getInt(KEY_LIKES);
    }

    public void setLikes(Integer likes) {
        put(KEY_LIKES, likes);
    }

    public void add(String username) {
        addAllUnique(KEY_LIKED_BY, Collections.singletonList(username));
    }

    public void remove(String username) {
        removeAll(KEY_LIKED_BY, Collections.singletonList(username));
    }

    public boolean isLikedBy(String username) {
        boolean result;
        try {
            result = getList(KEY_LIKED_BY).contains(username);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    // this is good pattern for developing android apps
    // keeps it within the Post context
    public static class Query extends ParseQuery<Post> {
        public Query() {
            super(Post.class);
        }

        public Query getTop() {
            setLimit(3);
            orderByDescending("createdAt");
            return this;
        }

        public Query withUser() {
            include("user");
            include("likedBy");
            return this;
        }

        public Query getNext(int offset) {
            setLimit(3);
            orderByDescending("createdAt");
            setSkip(offset);
            return this;
        }

        public Query getUserPosts() {
            whereEqualTo(KEY_USER, ParseUser.getCurrentUser());
            return this;
        }
    }
}
