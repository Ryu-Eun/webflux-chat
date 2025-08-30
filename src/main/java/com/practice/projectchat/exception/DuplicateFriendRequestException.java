package com.practice.projectchat.exception;

public class DuplicateFriendRequestException extends RuntimeException {
    public DuplicateFriendRequestException(String message) {
        super(message);
    }
}
