import java.io.InputStream;
import java.sql.Blob;
import java.util.Date;

/**
 * Created by Igor Pavinich on 04.12.2017.
 */
public class Message {
    private String sender;
    private String receiver;
    private long date;
    private String message;
    private String type;
    private InputStream sound;
    private InputStream file;

    public Message() {
    }

    public Message(String sender, String receiver, long date, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public InputStream getSound() {
        return sound;
    }

    public void setSound(InputStream sound) {
        this.sound = sound;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", date=" + date +
                ", message='" + message + '\'' +
                '}';
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
