package com.practice.projectchat.exception;

public class FriendRequestAlreadyHandledException extends RuntimeException {
    public FriendRequestAlreadyHandledException(String message) {
        super(message);
    }
}
