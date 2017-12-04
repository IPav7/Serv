import java.util.Date;

/**
 * Created by Igor Pavinich on 04.12.2017.
 */
public class Message {
    private String sender;
    private String receiver;
    private long date;
    private String message;

    public Message() {
    }

    public Message(String sender, String receiver, long date, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
        this.message = message;
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
