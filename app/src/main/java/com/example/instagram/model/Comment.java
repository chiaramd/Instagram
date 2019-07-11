package com.example.instagram.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {
    private static final String KEY_CONTENT = "content";
    private static final String KEY_USER = "user";

    public String getContent() {
        return getString(KEY_CONTENT);
    }

    public void setContent(String content) {
        put(KEY_CONTENT, content);
    }

    public void setPost(Post post) {
        put("post", post);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public static class Query extends ParseQuery<Comment> {
        public Query() {
            super(Comment.class);
        }

        public Query getComments(Post post) {
            include("user");
            whereEqualTo("post", post);
            return this;
        }
    }
}
