package StudyNotes.Interview.MoniePoint;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiUseProblem {
    public static void main(String[] args) throws IOException {
        int barcode = 74002314;
        int result = getDiscountedPrice(barcode);
        System.out.println("Discounted price is : "+result);
    }

    private static int getDiscountedPrice(int barcode) throws IOException {
        String url = "https://jsonmock.hackerrank.com/api/inventory?barcode=" + barcode;
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");
        System.out.println("\nconnection: "+connection);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        System.out.println("\nBufferedReader stream: "+bufferedReader);

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while((line= bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        System.out.println("\nStringBuilder : "+stringBuilder);

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());
        System.out.println("\nJsonObject : "+jsonObject);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        System.out.println("\njsonArray : "+jsonArray);

        if (jsonObject.isEmpty()) {
            return -1;
        }
        JSONObject object = jsonArray.getJSONObject(0);
        double price = object.getDouble("price");
        double discount = object.getDouble("discount");
        double discountedPrice = price - price * (discount/100.0);
        System.out.println("\nPrice:"+ price + " Discount:"+ discount + " DiscountedPrice:"+discountedPrice);
        return (int) Math.round(discountedPrice);
    }
}
