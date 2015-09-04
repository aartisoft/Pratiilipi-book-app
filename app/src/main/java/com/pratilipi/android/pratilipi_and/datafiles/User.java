package com.pratilipi.android.pratilipi_and.datafiles;

import java.io.Serializable;

/**
 * Created by Rahul Ranjan on 9/2/2015.
 */
public class User implements Serializable {

    private String displayName;
    private String email;
    private int contentsInShelf;
    private boolean isLoggedIn;
    private long tokenExpiry;
    private String profileImageUrl;

    public int getContentsInShelf() {
        return contentsInShelf;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public long getTokenExpiry() {
        return tokenExpiry;
    }

    public void setContentsInShelf(int contentsInShelf) {
        this.contentsInShelf = contentsInShelf;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setTokenExpiry(long tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
}
