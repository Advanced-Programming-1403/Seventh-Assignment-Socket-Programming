package Shared;

import java.io.Serializable;

/**
 * Represents structured data exchanged between client and server.
 * Supports multiple message types including login, chat, and file operations.
 * Can be serialized for socket transmission.
 */
public class Message implements Serializable {
    // Message type constants
    public static final int TYPE_LOGIN = 0;
    public static final int TYPE_CHAT = 1;
    public static final int TYPE_FILE_UPLOAD = 2;
    public static final int TYPE_FILE_DOWNLOAD = 3;
    public static final int TYPE_FILE_LIST = 4;
    public static final int TYPE_COMMAND = 5;
    public static final int TYPE_STATUS = 6;

    private int type;
    private String sender;
    private String content;
    private byte[] fileData;
    private String additionalInfo;

    public Message() {
        // Default constructor for serialization
    }

    public Message(int type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    // Factory methods for common message types
    public static Message createLoginMessage(String username, String password) {
        return new Message(TYPE_LOGIN, username, password);
    }

    public static Message createChatMessage(String sender, String message) {
        return new Message(TYPE_CHAT, sender, message);
    }

    public static Message createFileRequest(String filename) {
        return new Message(TYPE_FILE_DOWNLOAD, null, filename);
    }

    // Getters and setters
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", fileData=" + (fileData != null ? fileData.length + " bytes" : "null") +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}