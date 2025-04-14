package com.rhwr.coshop;

public class Member {
    public String userId;

    public Member() {} // nécessaire pour Firebase

    public Member(String userId) {
        this.userId = userId;
    }
}
