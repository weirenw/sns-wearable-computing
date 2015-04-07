package edu.gatech.cc.cs7470.facecard.Model;

import com.google.android.gms.plus.model.people.Person;

/**
 * Created by miseonpark on 3/3/15.
 */
public class Profile {

    private Bluetooth bluetoothInfo;
    private String name;
    private String tagline;
    private String organization;
    private String profile_picture_url;
    private String profile_cover_url;

    private String phone;
    private String email;
    private String website;

    private String google_link;
    private String facebook_link;
    private String linkedIn_link;

    public Profile(Person person, String email){

//        bluetoothInfo = new Bluetooth();
        name = person.getDisplayName();
        tagline = person.getTagline();
        if(person.getOrganizations()!=null)
            organization = person.getOrganizations().get(0).getName();
        else
            organization = "organization placeholder";
        profile_picture_url = "";
        if(person.hasImage()){
            if(person.getImage().hasUrl()){
                profile_picture_url = person.getImage().getUrl();
            }
        }
        profile_cover_url = "";
        if(person.hasCover()){
            if(person.getCover().hasCoverPhoto()) {
                profile_cover_url = person.getCover().getCoverPhoto().getUrl();
            }
        }

        this.email = email;

        google_link = person.getUrl();
        //TODO
        facebook_link = "";
        linkedIn_link = "";
    }

    public Bluetooth getBluetoothInfo() {
        return bluetoothInfo;
    }

    public void setBluetoothInfo(Bluetooth bluetoothInfo) {
        this.bluetoothInfo = bluetoothInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getProfile_picture_url() {
        return profile_picture_url;
    }

    public void setProfile_picture_url(String profile_picture_url) {
        this.profile_picture_url = profile_picture_url;
    }

    public String getProfile_cover_url() {
        return profile_cover_url;
    }

    public void setProfile_cover_url(String profile_cover_url) {
        this.profile_cover_url = profile_cover_url;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getGoogle_link() {
        return google_link;
    }

    public void setGoogle_link(String google_link) {
        this.google_link = google_link;
    }

    public String getFacebook_link() {
        return facebook_link;
    }

    public void setFacebook_link(String facebook_link) {
        this.facebook_link = facebook_link;
    }

    public String getLinkedIn_link() {
        return linkedIn_link;
    }

    public void setLinkedIn_link(String linkedIn_link) {
        this.linkedIn_link = linkedIn_link;
    }

}
