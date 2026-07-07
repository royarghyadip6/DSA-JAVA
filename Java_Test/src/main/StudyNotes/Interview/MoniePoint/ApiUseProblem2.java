package StudyNotes.Interview.MoniePoint;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiUseProblem2 {

    public static void main(String[] args) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://fakestoreapi.com/products"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            String json = response.body();

            // Find first object from JSON array
            int start = json.indexOf("{");
            int end = json.indexOf("},") + 1;

            String firstProduct = json.substring(start, end);

            System.out.println("First Product:");
            System.out.println(firstProduct);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}