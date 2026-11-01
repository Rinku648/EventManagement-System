package com.eventmanagement;

import com.eventmanagement.util.PasswordUtil;

public class TestHash {
    public static void main(String[] args) {
        System.out.println(PasswordUtil.hash("admin123"));
    }
}
