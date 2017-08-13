package de.js_labs.gaminggroups.elements;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by janik on 08.08.2017.
 */

public class User{

    private String[] groupIDs;
    private Invite[] invites;

    public User() {
    }

    public User(String[] groupIDs, Invite[] invites) {
        this.groupIDs = groupIDs;
        this.invites = invites;
    }

    public String[] getGroupIDs() {
        return groupIDs;
    }

    public void setGroupIDs(String[] groupIDs) {
        this.groupIDs = groupIDs;
    }

    public Invite[] getInvites() {
        return invites;
    }

    public void setInvites(Invite[] invites) {
        this.invites = invites;
    }
}
