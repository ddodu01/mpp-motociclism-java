package ro.mpp2024.rest;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestClientTest {

    // Interceptor care afiseaza request-urile si response-urile
    static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request,
                                            byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            System.out.println("===========================================");
            System.out.println("[REQUEST]  " + request.getMethod() + " " + request.getURI());
            if (body.length > 0) {
                System.out.println("[BODY]     " + new String(body, StandardCharsets.UTF_8));
            }

            ClientHttpResponse response = execution.execute(request, body);

            System.out.println("[RESPONSE] Status: " + response.getStatusCode());
            System.out.println("===========================================");
            return response;
        }
    }

    public static void main(String[] args) {
        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:8090")
                .requestInterceptor(new LoggingInterceptor())
                .build();

        // 1. GET toate cursele
        System.out.println("\n--- TEST 1: GET toate cursele ---");
        String curse = client.get()
                .uri("/api/curse")
                .retrieve()
                .body(String.class);
        System.out.println("[DATA] " + curse);

        // 2. GET cursa dupa id
        System.out.println("\n--- TEST 2: GET cursa cu id=1 ---");
        String cursa = client.get()
                .uri("/api/curse/1")
                .retrieve()
                .body(String.class);
        System.out.println("[DATA] " + cursa);

        // 3. GET filtrare dupa capacitate
        System.out.println("\n--- TEST 3: GET filtrare capacitate=250 ---");
        String filtrate = client.get()
                .uri("/api/curse/filter?capacitate=250")
                .retrieve()
                .body(String.class);
        System.out.println("[DATA] " + filtrate);

        // 4. POST creare cursa noua
        System.out.println("\n--- TEST 4: POST creare cursa noua ---");
        String cursaNoua = client.post()
                .uri("/api/curse")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("{\"capacitateMotor\": 999}")
                .retrieve()
                .body(String.class);
        System.out.println("[DATA] " + cursaNoua);

        // Extragem id-ul creat
        int startIndex = cursaNoua.indexOf("\"id\":") + 5;
        int endIndex = cursaNoua.indexOf(",", startIndex);
        long idNou = Long.parseLong(cursaNoua.substring(startIndex, endIndex).trim());

        // 5. PUT modificare cursa
        System.out.println("\n--- TEST 5: PUT modificare cursa id=" + idNou + " ---");
        String cursaModificata = client.put()
                .uri("/api/curse/" + idNou)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body("{\"capacitateMotor\": 1000}")
                .retrieve()
                .body(String.class);
        System.out.println("[DATA] " + cursaModificata);

        // 6. DELETE stergere cursa
        System.out.println("\n--- TEST 6: DELETE cursa id=" + idNou + " ---");
        client.delete()
                .uri("/api/curse/" + idNou)
                .retrieve()
                .toBodilessEntity();
        System.out.println("[DATA] Cursa stearsa cu succes!");
    }
}