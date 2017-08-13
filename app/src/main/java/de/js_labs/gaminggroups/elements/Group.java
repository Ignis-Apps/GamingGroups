package de.js_labs.gaminggroups.elements;

import java.util.ArrayList;

/**
 * Created by janik on 08.08.2017.
 */

public class Group {
    public String name;
    public String motto;
    public String description;

    public ArrayList<String> users;
    public ArrayList<String> admins;
    public ArrayList<String> onlineMembers;

    public Group(){

    }

    public Group(String name, String motto, String describtion){
        this.name = name;
        this.motto = motto;
        this.description = describtion;
    }

    public Group(String name, String motto, String describtion, ArrayList<String> users, ArrayList<String> admins, ArrayList<String> onlineMembers){
        this.name = name;
        this.motto = motto;
        this.description = describtion;
        this.users = users;
        this.admins = admins;
        this.onlineMembers = onlineMembers;
    }
}
