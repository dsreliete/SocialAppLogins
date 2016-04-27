package com.socialapp.login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by eliete on 4/16/16.
 */
public class Person implements Serializable {

    private String gender;
    private String name;
    private String email;
    private String profilePicture;
    private String idFacebook;
    private String location;
    private String birthday;
    private String cover;

    public Person() {
    }

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setIdFacebook(String idFacebook) {
        this.idFacebook = idFacebook;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getIdFacebook() {
        return idFacebook;
    }

    public String getLocation() {
        return location;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public static Person getPersonFomJson(JSONObject jsonObject) {
        Person person = new Person();
        try {
            if (jsonObject.has("id")){
                String id = jsonObject.getString("id");
                person.setIdFacebook(id);

                StringBuilder sb = new StringBuilder(("https://graph.facebook.com/"));
                sb.append(id);
                sb.append("/picture?width=200&height=200");

                person.setProfilePicture(sb.toString());
            }

            if (jsonObject.has("name"))
                person.setName(jsonObject.getString("name"));

            if (jsonObject.has("email"))
                person.setEmail(jsonObject.getString("email"));

            if (jsonObject.has("gender"))
                person.setGender(jsonObject.getString("gender"));

            if (jsonObject.has("birthday"))
                person.setBirthday(jsonObject.getString("birthday"));

            if (jsonObject.has("location"))
                person.setLocation(jsonObject.getJSONObject("location").getString("name"));

            if (jsonObject.has("cover")){
                String coverId = jsonObject.getJSONObject("cover").getString("id");
                StringBuilder sb = new StringBuilder(("https://graph.facebook.com/"));
                sb.append(coverId);
                sb.append("/picture");
                person.setCover(sb.toString());

            }

        } catch (JSONException e){
            e.printStackTrace();
        }
        return person;
    }

    @Override
    public String toString() {
        return "Person{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
