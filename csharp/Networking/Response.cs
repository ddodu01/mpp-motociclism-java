namespace Networking;

public class Response
{
    public ResponseType Type { get; set; }
    public object? Data { get; set; }

    public Response() { }

    public Response(ResponseType type, object? data = null)
    {
        Type = type;
        Data = data;
    }

    public override string ToString() => $"Response{{ Type={Type}, Data={Data} }}";
}