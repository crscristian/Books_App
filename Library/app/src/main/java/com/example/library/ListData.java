package com.example.library;

public class ListData {
    String t_book,n_author,email_user,d_book,url_image;

    public ListData(String t_book, String n_author, String email_user, String d_book, String url_image) {
        this.t_book = t_book;
        this.n_author = n_author;
        this.email_user = email_user;
        this.d_book = d_book;
        this.url_image = url_image;
    }

    public String getT_book(){
        return  t_book;
    }

    public String getN_author(){
        return n_author;
    }
}
