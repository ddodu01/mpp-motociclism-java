package ro.mpp2024;

public class Request {
    private RequestType type;
    private Object data;

    // Constructor gol necesar pentru Jackson
    public Request() {}

    public Request(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    @Override
    public String toString() {
        return "Request{type=" + type + ", data=" + data + "}";
    }
}