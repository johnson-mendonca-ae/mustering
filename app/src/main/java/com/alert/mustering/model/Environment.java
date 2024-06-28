package com.alert.mustering.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Objects;

public class Environment implements Serializable {
    @Expose
    private String name;
    @Expose
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Environment that)) return false;
        return getName().equals(that.getName()) && Objects.equals(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUrl());
    }

    @Override
    public String toString() {
        return "Environment{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
