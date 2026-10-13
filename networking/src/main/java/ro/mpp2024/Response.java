package ro.mpp2024;

public class Response {
    private ResponseType type;
    private Object data;

    // Constructor gol necesar pentru Jackson
    public Response() {}

    public Response(ResponseType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public ResponseType getType() { return type; }
    public void setType(ResponseType type) { this.type = type; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    @Override
    public String toString() {
        return "Response{type=" + type + ", data=" + data + "}";
    }
}