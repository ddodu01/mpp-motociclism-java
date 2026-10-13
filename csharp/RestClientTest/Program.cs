using System.Text;

var handler = new LoggingHandler(new HttpClientHandler());
var client = new HttpClient(handler) { BaseAddress = new Uri("http://localhost:8090") };
client.DefaultRequestHeaders.Add("Accept", "application/json");

// 1. GET toate cursele
Console.WriteLine("\n--- TEST 1: GET toate cursele ---");
var response1 = await client.GetAsync("/api/curse");
string data1 = await response1.Content.ReadAsStringAsync();
Console.WriteLine($"[DATA] {data1}");

// 2. GET cursa dupa id
Console.WriteLine("\n--- TEST 2: GET cursa cu id=1 ---");
var response2 = await client.GetAsync("/api/curse/1");
string data2 = await response2.Content.ReadAsStringAsync();
Console.WriteLine($"[DATA] {data2}");

// 3. GET filtrare dupa capacitate
Console.WriteLine("\n--- TEST 3: GET filtrare capacitate=250 ---");
var response3 = await client.GetAsync("/api/curse/filter?capacitate=250");
string data3 = await response3.Content.ReadAsStringAsync();
Console.WriteLine($"[DATA] {data3}");

// 4. POST creare cursa noua
Console.WriteLine("\n--- TEST 4: POST creare cursa noua ---");
var content4 = new StringContent("{\"capacitateMotor\": 999}", Encoding.UTF8, "application/json");
var response4 = await client.PostAsync("/api/curse", content4);
string data4 = await response4.Content.ReadAsStringAsync();
Console.WriteLine($"[DATA] {data4}");

// Extragem id-ul creat
int startIndex = data4.IndexOf("\"id\":") + 5;
int endIndex = data4.IndexOf(",", startIndex);
long idNou = long.Parse(data4.Substring(startIndex, endIndex - startIndex).Trim());

// 5. PUT modificare cursa
Console.WriteLine($"\n--- TEST 5: PUT modificare cursa id={idNou} ---");
var content5 = new StringContent("{\"capacitateMotor\": 1000}", Encoding.UTF8, "application/json");
var response5 = await client.PutAsync($"/api/curse/{idNou}", content5);
string data5 = await response5.Content.ReadAsStringAsync();
Console.WriteLine($"[DATA] {data5}");

// 6. DELETE stergere cursa
Console.WriteLine($"\n--- TEST 6: DELETE cursa id={idNou} ---");
await client.DeleteAsync($"/api/curse/{idNou}");
Console.WriteLine("[DATA] Cursa stearsa cu succes!");

// Clasa interceptor
public class LoggingHandler : DelegatingHandler
{
    public LoggingHandler(HttpMessageHandler innerHandler) : base(innerHandler) { }

    protected override async Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request, CancellationToken cancellationToken)
    {
        Console.WriteLine("===========================================");
        Console.WriteLine($"[REQUEST]  {request.Method} {request.RequestUri}");
        if (request.Content != null)
        {
            string body = await request.Content.ReadAsStringAsync();
            Console.WriteLine($"[BODY]     {body}");
        }

        HttpResponseMessage response = await base.SendAsync(request, cancellationToken);

        Console.WriteLine($"[RESPONSE] Status: {(int)response.StatusCode} {response.StatusCode}");
        Console.WriteLine("===========================================");
        return response;
    }
}