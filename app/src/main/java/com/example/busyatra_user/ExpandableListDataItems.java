package com.example.busyatra_user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataItems {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableDetailList = new HashMap<String, List<String>>();

        // As we are populating List of fruits, vegetables and nuts, using them here
        // We can modify them as per our choice.
        // And also choice of fruits/vegetables/nuts can be changed
        List<String> Q1 = new ArrayList<String>();
        Q1.add("You can view your travel history and payments in your profile page.");


        List<String> Q2 = new ArrayList<String>();
        Q2.add("You can use payment though razor pay.");




        // Fruits are grouped under Fruits Items. Similarly the rest two are under
        // Vegetable Items and Nuts Items respectively.
        // i.e. expandableDetailList object is used to map the group header strings to
        // their respective children using an ArrayList of Strings.
        expandableDetailList.put("How can I view my travel history?", Q1);
        expandableDetailList.put("How to do payments?", Q2);
        return expandableDetailList;
    }
}