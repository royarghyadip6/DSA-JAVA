package StudyNotes.Interview.MoniePoint;

import lombok.Data;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

public class ApiUseProblem3 {

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://fakestoreapi.com/products";
        Product[] products = restTemplate.getForObject(url, Product[].class);

        if (products != null && products.length > 0) {
            System.out.println("First RestTemplate: "+products[0]);
            System.out.println("Tenth RestTemplate: "+products[9]);
            System.out.println("Tenth product Price: "+products[9].getPrice());
        }

        Product[] response = WebClient.create()
                .get()
                .uri("https://fakestoreapi.com/products")
                .retrieve()
                .bodyToMono(Product[].class)
                .block();

        if (response != null && response.length > 0) {
            System.out.println("First WebClient: "+response[0]);
            System.out.println("Tenth WebClient: "+response[9].toString());
            System.out.println("Tenth product Price: "+response[9].getPrice());

        }

        assert response != null;
        for (Product product : response) {
            ProductDto discountedProduct = getDisountProduct(product);
            System.out.println(discountedProduct);
        }


    }

    private static ProductDto getDisountProduct(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.id);
        productDto.setTitle(product.getTitle());
        productDto.setPrice(product.getPrice());
        productDto.setDiscount(getDiscount(product.getPrice()));
        productDto.setDiscountPrice(product.getPrice() - getDiscount(product.getPrice())/100.0);
        productDto.setCategory(product.getCategory());

        return productDto;
    }

    private static Double getDiscount(Double price) {
        double discount;
        if (price >= 1 && price < 999) {
            discount = 1.0;
        } else if (price >= 1000 && price < 9999) {
            discount = 2.5;
        } else if (price >= 10000 && price < 99999) {
            discount = 5.0;
        } else {
            discount = 10.0;
        }
        return discount;
    }


    @Data
    static class Product {
        private Long id;
        private String title;
        private Double price;
        private String category;
    }

    @Data
    static class ProductDto {
        private Long id;
        private String title;
        private Double price;
        private Double discount;
        private Double discountPrice;
        private String category;
    }
}