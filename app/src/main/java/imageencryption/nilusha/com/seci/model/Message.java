package imageencryption.nilusha.com.seci.model;


public class Message {
    private String id;
    private String from;
   // private String subject;
    private String message;
    private String timestamp;
    //private String picture;
    private boolean isImportant;
    private boolean isRead;
    private int color = -1;
    private int count=0;

    public Message(int count,String id,String from, String message,String timestamp, boolean isImportant, boolean isRead,int color) {
        this.count=count;
        this.id=id;
        this.from=from;
        //this.subject=subject;
        this.message=message;
        this.timestamp=timestamp;
       // this.picture=picture;
        this.isImportant=isImportant;
        this.isRead=isRead;
        this.color=color;
    }
    public int getCount() {
        return count;
    }

    public void setCount(int id) {
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

//    public String getSubject() {
//        return subject;
//    }

//    public void setSubject(String subject) {
//        this.subject = subject;
//    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

//    public String getPicture() {
//        return picture;
//    }
//
//    public void setPicture(String picture) {
//        this.picture = picture;
//    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
