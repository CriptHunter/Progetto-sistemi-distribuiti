package Messages;

public class Message<C> {
    private Header header;
    private long timestamp;
    private C content;

    public Message(Header header, long timestamp, C content) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public C getContent() {
        return content;
    }

    public void setContent(C content) {
        this.content = content;
    }
}
