using System.Net.Sockets;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Networking;

// Converter care transforma null in 0 pentru long
public class NullableLongConverter : JsonConverter<long>
{
    public override long Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
    {
        if (reader.TokenType == JsonTokenType.Null)
            return 0;
        return reader.GetInt64();
    }

    public override void Write(Utf8JsonWriter writer, long value, JsonSerializerOptions options)
    {
        writer.WriteNumberValue(value);
    }
}

public static class JsonUtils
{
    public static readonly JsonSerializerOptions Options = new()
    {
        PropertyNameCaseInsensitive = true,
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        Converters =
        {
            new JsonStringEnumConverter(),
            new NullableLongConverter()
        }
    };

    public static void SendJson(NetworkStream stream, object obj)
    {
        string json = JsonSerializer.Serialize(obj, Options);
        byte[] data = Encoding.UTF8.GetBytes(json + "\n");
        stream.Write(data, 0, data.Length);
        stream.Flush();
    }

    public static string ReceiveJson(StreamReader reader)
    {
        return reader.ReadLine() ?? throw new MotociclismException("Connection closed");
    }

    public static T? ParseData<T>(object? data)
    {
        if (data is JsonElement el)
            return JsonSerializer.Deserialize<T>(el.GetRawText(), Options);
        return default;
    }
}