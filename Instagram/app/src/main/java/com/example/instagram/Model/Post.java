package com.example.instagram.Model;

public class Post {
    private String description;
    private String imageUrl;
    private String postId;
    private String publisherDetails;


    public Post()
    {

    }

    public Post(String description, String imageUrl, String postId, String publisherDetails) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.postId = postId;
        this.publisherDetails = publisherDetails;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPublisherDetails() {
        return publisherDetails;
    }

    public void setPublisherDetails(String publisherDetails) {
        this.publisherDetails = publisherDetails;
    }
}
