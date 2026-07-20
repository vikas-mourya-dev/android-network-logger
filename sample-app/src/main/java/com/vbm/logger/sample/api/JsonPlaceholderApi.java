package com.vbm.logger.sample.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface JsonPlaceholderApi {

    @GET("posts/1")
    Call<Post> getPost();

    @POST("posts")
    Call<Post> createPost(@Body Post post);

    @GET("posts/999999")
    Call<Post> getMissingPost();

    class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
