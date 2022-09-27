package com.elexlab.mydisk.pojo;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    private String name;
    private List<String> phoneNumbers = new ArrayList<>();

    public Contact() {
    }

    public Contact(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
