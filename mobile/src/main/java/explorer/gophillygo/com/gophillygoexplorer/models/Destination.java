package explorer.gophillygo.com.gophillygoexplorer.models;

import java.net.URL;

/**
 * Created by kat on 5/12/17.
 */

public class Destination {

    private Integer id;

    private String city;
    private String description;
    private String zip;
    private URL image;
    private boolean published;
    private Integer priority;

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

    public URL getImage() {
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

    public URL getWide_image() {
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

    private String state;
    private URL wide_image;
    private String address;
    private URL website_url;
    private String name;
    private Location location;


    // TODO: ignoring point and extent


    public Destination() {
        // default constructor
    }

}
