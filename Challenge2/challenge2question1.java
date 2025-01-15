package challenge2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class challenge2question1 {
	
	public static void main(String[] args) {
		Product[] ProductList = {
				new Product("Laptop", 999.99, 5),
				new Product("Smartphone", 499.99, 10),
				new Product("Tablet", 299.99, 0),
				new Product("Smartwatch", 199.99, 3)
		};
		
		System.out.println("1. Total value of all products in stock: " + totalInventoryValue(ProductList));
		System.out.println("2. The most expensive product is: " + findMostExpensive(ProductList));
		System.out.println("3. Check if the product named \"Headphones\" is in stock: " + checkInStock(ProductList, "Headphones"));
		
		Product[] list = sortProduct(ProductList, "ascending", "price");
		System.out.println("\n4.1 Sort products ascending order by price:");
		for (Product product : list) {
			System.out.println(product.toString());
		}
		
		list = sortProduct(ProductList, "ascending", "quantity");
		System.out.println("\n4.2 Sort products ascending order by quantity:");
		for (Product product : list) {
			System.out.println(product.toString());
		}
		
		list = sortProduct(ProductList, "descending", "price");
		System.out.println("\n4.3 Sort products descending order by price:");
		for (Product product : list) {
			System.out.println(product.toString());
		}
		
		list = sortProduct(ProductList, "descending", "quantity");
		System.out.println("\n4.4 Sort products descending order by quantity:");
		for (Product product : list) {
			System.out.println(product.toString());
		}
	}

	//function returns total value of all products in stock
	public static double totalInventoryValue(Product[] listProduct) {
		double total = 0;
		for (Product product : listProduct) {
			if(product.quantity > 0) {
			total += (product.price * product.quantity);
			}
		}
		return total;
	}
	
	//function returns name of product with the highest price
	public static String findMostExpensive(Product[] listProduct) {
		String name = "";
		double maxprice = 0;
		for (Product product : listProduct) {
			if(maxprice < product.price) {
			name = product.name;
			}
		}
		return name;
	}
	
	//Function returns true or false depending on whether the product name exists in the inventory
	public static boolean checkInStock(Product[] listProduct, String name) {
		boolean result = false;
		for (Product product : listProduct) {
			if(name == product.name) {
			result = true;
			break;
			}
		}
		return result;
	}
	
	// Function to sort products in ascending or descending order by price or quantity
	public static Product[] sortProduct(Product[] listProduct, String option, String orderby){
		Product[] sortedList = Arrays.copyOf(listProduct, listProduct.length);
		switch (option) {
		case "ascending":
			switch (orderby) {
			case "price":
				Arrays.sort(sortedList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
				break;
			case "quantity":
				Arrays.sort(sortedList, (p1, p2) -> Integer.compare(p1.getQuantity(), p2.getQuantity()));
				break;
			}
			break;
		case "descending":
			switch (orderby) {
			case "price":
				Arrays.sort(sortedList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
				break;
			case "quantity":
				Arrays.sort(sortedList, (p1, p2) -> Integer.compare(p2.getQuantity(), p1.getQuantity()));
				break;
			}
			break;
		}
		return sortedList;
	}
	
	public static class Product{
		private String name;
		private double price;
		private int quantity;
		public Product(String name, double price, int quantity) {
			super();
			this.name = name;
			this.price = price;
			this.quantity = quantity;
		}
		public Product() {
			super();
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
		public int getQuantity() {
			return quantity;
		}
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
		@Override
		public String toString() {
			return "name: " + name + ", price: " + price + ", quantity: " + quantity;
		}
	}
	
}
