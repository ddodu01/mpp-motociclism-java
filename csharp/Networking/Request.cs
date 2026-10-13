namespace Networking;

public class Request
{
    public RequestType Type { get; set; }
    public object? Data { get; set; }

    public Request() { }

    public Request(RequestType type, object? data = null)
    {
        Type = type;
        Data = data;
    }

    public override string ToString() => $"Request{{ Type={Type}, Data={Data} }}";
}