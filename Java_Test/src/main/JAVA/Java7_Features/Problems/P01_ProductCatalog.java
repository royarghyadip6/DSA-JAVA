package Java7_Features.Problems;

import lombok.*;

import java.util.*;

/*
Problem 1: The "Resilient Product Catalog" (Comparable)
Scenario: You are building a Product entity for an e-commerce microservice. The "natural" order of products is critical for the UI.

The Task:
Implement Comparable<Product> in a class with String name, double price, and int stockQuantity.

Primary Sort: Sort by price (Ascending).
Secondary Sort: If prices are identical, sort by stockQuantity (Descending—show items with more stock first).
The "Senior" Twist: Your implementation must be null-safe. If a product name is null, it should be treated as the "lowest" value.

Why this is a 5-year exp question: It tests if you know how to handle "tie-breakers" and if you're defensive against NullPointerException, which is a common production bug.
* */
public class P01_ProductCatalog {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
                new Product("Laptop" , 1999.99, 15, "Electronics"),
                new Product(null, 999.50, 50, "Electronics"),
                new Product("Mechanical Keyboard", 150.00, 120, "Accessories"),
                new Product("Gaming Mouse", 85.00, 200, "Accessories"),
                new Product("Leather Office Chair", 250.00, 15, "Furniture"),
                new Product("Leather Jacket", 250.00, 20, "Cloths")
        );

        Collections.sort(products);
        products.forEach(System.out::println);
    }
}

@AllArgsConstructor
@NoArgsConstructor
@ToString
class Product implements Comparable<Product> {
    @Getter @Setter String name;
    @Getter @Setter double price;
    @Getter @Setter int stockQuantity;
    @Getter @Setter String category;

    @Override
    public int compareTo(Product other) {
        // 1. Basic Null Check for the object itself
        if (other == null) return 1;

        // 2. Primary Sort: Price (Ascending)
        int priceCompare = Double.compare(this.price, other.price);
        if (priceCompare != 0) return priceCompare;

        // 3. Secondary Sort: Stock (Descending)
        int stockCompare = Integer.compare(other.stockQuantity, this.stockQuantity);
        if (stockCompare != 0) return stockCompare;

        // 4. Final Sort: Name (Nulls as Lowest Value)
        // Objects.compare takes: (Element1, Element2, The Strategy)
        return Objects.compare(
                this.name,
                other.name,
                Comparator.nullsFirst(String::compareTo)
        );
    }

}