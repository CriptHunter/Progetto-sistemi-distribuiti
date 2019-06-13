package Messages;

public class Message<C> {
    private Header header;
    private int timestamp;
    private C content;

    public Message(Header header, int timestamp, C content) {
        this.header = header;
        this.timestamp = timestamp;
        this.content = content;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public C getContent() {
        return content;
    }

    public void setContent(C content) {
        this.content = content;
    }
}
