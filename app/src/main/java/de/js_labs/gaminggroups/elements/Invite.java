package de.js_labs.gaminggroups.elements;

/**
 * Created by janik on 11.08.2017.
 */

public class Invite {
    public String groupsID;
    public String userInvitedID;

    public Invite(String groupsID, String userInvitedID) {
        this.groupsID = groupsID;
        this.userInvitedID = userInvitedID;
    }
}
