package com.scorchedcode.JMWLib.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    private String name, id, college, company, city, highschool, interests, job, relationship,
        description, avatarurl;
    public User(JSONObject data) {
        name = data.getString("name");
        id = data.getString("id");
        try {
            college = data.getJSONObject("profile").getString("college");
        }
        catch (JSONException e) {
            college = "Not applicable";
        }
        try {
            city = data.getJSONObject("profile").getString("currentCity");
        }
        catch (JSONException e) {
            city = "Not applicable";
        }
        try {
            company = data.getJSONObject("profile").getString("company");
        }
        catch (JSONException e) {
            company = "Not applicable";
        }
        try{
            highschool = data.getJSONObject("profile").getString("highSchool");
        }
        catch (JSONException e) {
            highschool = "Not applicable";
        }
        try{
            interests = data.getJSONObject("profile").getString("interests");
        }
        catch (JSONException e) {
            interests = "Not applicable";
        }
        try{
            job = data.getJSONObject("profile").getString("job");
        }
        catch (JSONException e) {
            job = "Not applicable";
        }
        try{
            relationship = data.getJSONObject("profile").getString("relationshipStatus");
        }
        catch (JSONException e) {
            relationship = "Not applicable";
        }
        try{
            description = data.getJSONObject("profile").getString("text");
        }
        catch (JSONException e) {
            description = "Not applicable";
        }
        try {
            avatarurl = "https://mewe.com/" + data.getJSONObject("_links").getJSONObject("avatar").getString("href").replaceAll("\\{imageSize}", "170x170");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCollege() {
        return college;
    }

    public String getCompany() {
        return company;
    }

    public String getCity() {
        return city;
    }

    public String getHighschool() {
        return highschool;
    }

    public String getInterests() {
        return interests;
    }

    public String getJob() {
        return job;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatarUrl() {
        return avatarurl;
    }
}
