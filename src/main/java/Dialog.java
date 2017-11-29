/**
 * Created by Igor Pavinich on 27.11.2017.
 */
public class Dialog {
    private int sender;
    private int receiver;
    private String lastMessage;

    public Dialog(int sender, int receiver, String lastMessage) {
        this.sender = sender;
        this.receiver = receiver;
        this.lastMessage = lastMessage;
    }

    @Override
    public String toString() {
        return "Dialog{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                ", lastMessage='" + lastMessage + '\'' +
                '}';
    }
}
