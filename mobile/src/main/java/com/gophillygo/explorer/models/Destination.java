package com.gophillygo.explorer.models;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kat on 5/12/17.
 */


@IgnoreExtraProperties
public class Destination {

    private Integer id;

    private String name;
    private String description;

    private String address;
    private String city;
    private String state;
    private String zip;

    private String image;
    private String wide_image;

    private URL website_url;

    private Location location;

    private boolean published;
    private Integer priority;


    public Destination() {
        // default constructor
    }

    // setters
    public void setId(Integer id) { this.id = id; }

    public void setWebsite_url(String url) {
        try {
            this.website_url = new URL(url);
        } catch (MalformedURLException e) {
            Log.e("Destination", e.toString());
            // TODO: send to firebase crash logs
        }
    }

    // getters
    public Integer getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getDescription() {
        return description;
    }

    public String getZip() {
        return zip;
    }

    public String getImage() {
        return image;
    }

    public boolean isPublished() {
        return published;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getState() {
        return state;
    }

    public String getWide_image() {
        return wide_image;
    }

    public String getAddress() {
        return address;
    }

    public URL getWebsite_url() {
        return website_url;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

}
