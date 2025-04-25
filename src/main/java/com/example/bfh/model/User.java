package com.example.bfh.model;

import java.util.List;

public class User {
    private int id;
    private String name;
    private List<Integer> follows;

    public User() {}

    public User(int id, String name, List<Integer> follows) {
        this.id = id;
        this.name = name;
        this.follows = follows;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getFollows() {
        return follows;
    }

    public void setFollows(List<Integer> follows) {
        this.follows = follows;
    }
}
