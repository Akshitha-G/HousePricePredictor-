import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Utility tool to create custom housing datasets
 * Can be run standalone to generate CSV files for testing
 */
public class DatasetGenerator {
    
    // Enum for location types to match main system
    public enum LocationType {
        DOWNTOWN, SUBURB, RURAL, UPTOWN, BEACHSIDE, METROPOLITAN
    }
    
    // Enum for furnishing states to match main system
    public enum FurnishingState {
        FURNISHED, UNFURNISHED, SEMI_FURNISHED
    }
    
    // Enum for kitchen types to match main system
    public enum KitchenType {
        OPEN_KITCHEN, CLOSED_KITCHEN
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Enhanced Housing Dataset Generator ===\n");
        
        System.out.print("Enter number of samples to generate (100-10000): ");
        int numSamples = scanner.nextInt();
        
        if (numSamples < 100 || numSamples > 10000) {
            System.out.println("Invalid number. Using default: 1000");
            numSamples = 1000;
        }
        
        System.out.print("Enter output filename (e.g., housing_data.csv): ");
        scanner.nextLine(); // consume newline
        String filename = scanner.nextLine();
        
        if (filename.isEmpty()) {
            filename = "enhanced_housing_data_" + System.currentTimeMillis() + ".csv";
        }
        
        System.out.println("\nGenerating " + numSamples + " enhanced samples...");
        
        List<EnhancedHouse> houses = generateEnhancedData(numSamples);
        
        try {
            saveEnhancedDataset(houses, filename);
            System.out.println("✓ Enhanced dataset saved to: " + filename);
            
            // Display statistics
            displayEnhancedStatistics(houses);
            
            // Display sample records
            System.out.println("\nSample records:");
            for (int i = 0; i < Math.min(5, houses.size()); i++) {
                System.out.println("  " + (i+1) + ". " + houses.get(i));
            }
            
        } catch (IOException e) {
            System.err.println("Error saving dataset: " + e.getMessage());
        }
        
        scanner.close();
    }
    
    /**
     * Generate enhanced realistic housing data with all features
     */
    private static List<EnhancedHouse> generateEnhancedData(int numSamples) {
        List<EnhancedHouse> houses = new ArrayList<>();
        Random random = new Random(42); // Fixed seed for reproducible results
        
        for (int i = 0; i < numSamples; i++) {
            // Generate correlated features
            
            // Bedrooms follow a realistic distribution (1-6)
            int bedrooms = generateSkewedInt(random, 3, 3, 1, 6);
            
            // Bathrooms correlate with bedrooms
            int bathrooms = Math.max(1, bedrooms - random.nextInt(2));
            
            // Area correlates with bedrooms (500-5000 sq ft)
            double baseArea = 600 + (bedrooms - 1) * 400;
            double area = baseArea + random.nextGaussian() * 200;
            area = Math.max(500, Math.min(5000, area));
            
            // Location with realistic distribution
            LocationType location = generateLocationType(random);
            
            // Age (0-50 years, skewed towards newer)
            int age = (int) Math.abs(random.nextGaussian() * 10);
            age = Math.min(50, age);
            
            // Neighborhood quality (1-5) correlates with location
            double neighborhood = generateNeighborhoodQuality(location, random);
            
            // Furnishing state
            FurnishingState furnishing = generateFurnishingState(random);
            
            // Kitchen type (correlates with modern houses)
            KitchenType kitchen = generateKitchenType(age, random);
            
            // Parking correlates with size and location
            int parking = generateParkingSpaces(bedrooms, location, random);
            
            // Calculate realistic price using enhanced formula
            double price = calculateEnhancedPrice(
                area, bedrooms, bathrooms, age, neighborhood,
                parking, location, furnishing, kitchen, random
            );
            
            houses.add(new EnhancedHouse(
                (int)Math.round(area),                    // Round to whole number
                bedrooms,
                bathrooms,
                age,
                neighborhood,
                parking,
                location,
                furnishing,
                kitchen,
                (double)Math.round(price)                   // Round to whole number
            ));
        }
        
        return houses;
    }
    
    /**
     * Generate location type with realistic distribution
     */
    private static LocationType generateLocationType(Random random) {
        double prob = random.nextDouble();
        if (prob < 0.35) return LocationType.SUBURB;      // 35%
        else if (prob < 0.55) return LocationType.METROPOLITAN; // 20%
        else if (prob < 0.70) return LocationType.DOWNTOWN;     // 15%
        else if (prob < 0.82) return LocationType.UPTOWN;       // 12%
        else if (prob < 0.92) return LocationType.BEACHSIDE;    // 10%
        else return LocationType.RURAL;                         // 8%
    }
    
    /**
     * Generate neighborhood quality based on location
     */
    private static double generateNeighborhoodQuality(LocationType location, Random random) {
        double baseQuality;
        switch (location) {
            case BEACHSIDE: baseQuality = 4.5; break;
            case UPTOWN: baseQuality = 4.2; break;
            case DOWNTOWN: baseQuality = 3.8; break;
            case METROPOLITAN: baseQuality = 3.5; break;
            case SUBURB: baseQuality = 3.2; break;
            case RURAL: baseQuality = 2.8; break;
            default: baseQuality = 3.0;
        }
        
        // Add some variation
        double variation = (random.nextGaussian() * 0.5);
        double quality = baseQuality + variation;
        return Math.max(1.0, Math.min(5.0, Math.round(quality * 10.0) / 10.0)); // Round to 1 decimal
    }
    
    /**
     * Generate furnishing state
     */
    private static FurnishingState generateFurnishingState(Random random) {
        double prob = random.nextDouble();
        if (prob < 0.4) return FurnishingState.UNFURNISHED;     // 40%
        else if (prob < 0.75) return FurnishingState.SEMI_FURNISHED; // 35%
        else return FurnishingState.FURNISHED;                  // 25%
    }
    
    /**
     * Generate kitchen type (newer houses more likely to have open kitchen)
     */
    private static KitchenType generateKitchenType(int age, Random random) {
        // Newer houses (<= 10 years) more likely to have open kitchen
        if (age <= 10) {
            return random.nextDouble() < 0.7 ? KitchenType.OPEN_KITCHEN : KitchenType.CLOSED_KITCHEN;
        } else {
            return random.nextDouble() < 0.3 ? KitchenType.OPEN_KITCHEN : KitchenType.CLOSED_KITCHEN;
        }
    }
    
    /**
     * Generate parking spaces based on bedrooms and location
     */
    private static int generateParkingSpaces(int bedrooms, LocationType location, Random random) {
        int baseParking;
        
        // Base parking based on bedrooms
        if (bedrooms <= 2) baseParking = 1;
        else if (bedrooms <= 4) baseParking = 2;
        else baseParking = 3;
        
        // Adjust based on location (downtown has less parking)
        switch (location) {
            case DOWNTOWN: baseParking = Math.max(0, baseParking - 1); break;
            case METROPOLITAN: baseParking = Math.max(0, baseParking - 1); break;
            case RURAL: baseParking += 1; break;
            case SUBURB: baseParking += 1; break;
        }
        
        // Add some random variation
        int variation = random.nextInt(2); // 0 or 1
        int parking = baseParking + variation;
        
        return Math.max(0, Math.min(10, parking));
    }
    
    /**
     * Calculate realistic house price based on all enhanced features
     */
    private static double calculateEnhancedPrice(
            double area, int bedrooms, int bathrooms, int age, double neighborhood,
            int parking, LocationType location, FurnishingState furnishing, 
            KitchenType kitchen, Random random) {
        
        // Base price per square foot
        double basePricePerSqFt = 150.0;
        
        // Location multipliers
        double locationMultiplier = getLocationMultiplier(location);
        
        // Feature values
        double bedroomValue = bedrooms * 25000;
        double bathroomValue = bathrooms * 20000;
        double parkingValue = parking * 15000;
        double neighborhoodValue = (neighborhood - 3.0) * 30000; // Base 3.0
        
        // Furnishing bonuses
        double furnishingBonus = 0;
        switch (furnishing) {
            case FURNISHED: furnishingBonus = 40000; break;
            case SEMI_FURNISHED: furnishingBonus = 15000; break;
            case UNFURNISHED: furnishingBonus = 0; break;
        }
        
        // Kitchen bonus
        double kitchenBonus = (kitchen == KitchenType.OPEN_KITCHEN) ? 20000 : 0;
        
        // Age depreciation (1.5% per year)
        double ageDepreciation = age * 0.015;
        
        // Calculate base price
        double basePrice = (area * basePricePerSqFt) + bedroomValue + bathroomValue + 
                          parkingValue + neighborhoodValue + furnishingBonus + kitchenBonus;
        
        // Apply location multiplier
        double price = basePrice * locationMultiplier;
        
        // Apply age depreciation
        price *= (1.0 - ageDepreciation);
        
        // Add realistic market noise (±12%)
        double noise = 0.88 + (random.nextDouble() * 0.24); // 0.88 to 1.12
        price *= noise;
        
        return Math.max(80000, price); // Minimum price $80,000
    }
    
    /**
     * Get location-based price multiplier
     */
    private static double getLocationMultiplier(LocationType location) {
        switch (location) {
            case BEACHSIDE: return 1.8;
            case UPTOWN: return 1.5;
            case DOWNTOWN: return 1.4;
            case METROPOLITAN: return 1.3;
            case SUBURB: return 1.0;
            case RURAL: return 0.7;
            default: return 1.0;
        }
    }
    
    /**
     * Generate integer with skewed distribution
     */
    private static int generateSkewedInt(Random random, int mode, int mean, int min, int max) {
        double value = mean + random.nextGaussian() * 1.2;
        int result = (int) Math.round(value);
        return Math.max(min, Math.min(max, result));
    }
    
    /**
     * Save enhanced dataset to CSV file
     */
    private static void saveEnhancedDataset(List<EnhancedHouse> houses, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write enhanced header
            writer.write("squareFootage,bedrooms,bathrooms,age,neighborhood,parkingSpaces,locationType,furnishingState,kitchenType,price");
            writer.newLine();
            
            // Write data
            for (EnhancedHouse house : houses) {
                String line = String.format("%d,%d,%d,%d,%.1f,%d,%s,%s,%s,%d",
                    house.getSquareFootage(),
                    house.getBedrooms(),
                    house.getBathrooms(),
                    house.getAge(),
                    house.getNeighborhood(),
                    house.getParkingSpaces(),
                    house.getLocationType(),
                    house.getFurnishingState(),
                    house.getKitchenType(),
                    (int)house.getPrice()
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
    /**
     * Display enhanced dataset statistics
     */
    private static void displayEnhancedStatistics(List<EnhancedHouse> houses) {
        System.out.println("\n=== Enhanced Dataset Statistics ===");
        
        // Price statistics
        double minPrice = houses.stream().mapToDouble(EnhancedHouse::getPrice).min().orElse(0);
        double maxPrice = houses.stream().mapToDouble(EnhancedHouse::getPrice).max().orElse(0);
        double avgPrice = houses.stream().mapToDouble(EnhancedHouse::getPrice).average().orElse(0);
        
        System.out.println("\nPrice Range:");
        System.out.printf("  Min: $%,.0f\n", minPrice);
        System.out.printf("  Max: $%,.0f\n", maxPrice);
        System.out.printf("  Avg: $%,.0f\n", avgPrice);
        
        // Area statistics
        double minArea = houses.stream().mapToDouble(EnhancedHouse::getSquareFootage).min().orElse(0);
        double maxArea = houses.stream().mapToDouble(EnhancedHouse::getSquareFootage).max().orElse(0);
        double avgArea = houses.stream().mapToDouble(EnhancedHouse::getSquareFootage).average().orElse(0);
        
        System.out.println("\nArea Range:");
        System.out.printf("  Min: %.0f sq ft\n", minArea);
        System.out.printf("  Max: %.0f sq ft\n", maxArea);
        System.out.printf("  Avg: %.0f sq ft\n", avgArea);
        
        // Feature distributions
        System.out.println("\nBedroom Distribution:");
        for (int i = 1; i <= 6; i++) {
            int finalI = i;
            long count = houses.stream().filter(h -> h.getBedrooms() == finalI).count();
            if (count > 0) {
                System.out.printf("  %d bedrooms: %d houses (%.1f%%)\n", 
                    i, count, (count * 100.0 / houses.size()));
            }
        }
        
        System.out.println("\nLocation Distribution:");
        for (LocationType loc : LocationType.values()) {
            long count = houses.stream().filter(h -> h.getLocationType() == loc).count();
            if (count > 0) {
                System.out.printf("  %-15s: %d houses (%.1f%%)\n", 
                    loc, count, (count * 100.0 / houses.size()));
            }
        }
        
        System.out.println("\nFurnishing Distribution:");
        for (FurnishingState furn : FurnishingState.values()) {
            long count = houses.stream().filter(h -> h.getFurnishingState() == furn).count();
            if (count > 0) {
                System.out.printf("  %-15s: %d houses (%.1f%%)\n", 
                    furn, count, (count * 100.0 / houses.size()));
            }
        }
        
        System.out.println("\nKitchen Type Distribution:");
        for (KitchenType kitchen : KitchenType.values()) {
            long count = houses.stream().filter(h -> h.getKitchenType() == kitchen).count();
            if (count > 0) {
                System.out.printf("  %-15s: %d houses (%.1f%%)\n", 
                    kitchen, count, (count * 100.0 / houses.size()));
            }
        }
        
        System.out.println("\nParking Spaces Distribution:");
        for (int i = 0; i <= 5; i++) {
            int finalI = i;
            long count = houses.stream().filter(h -> h.getParkingSpaces() == finalI).count();
            if (count > 0) {
                System.out.printf("  %d spaces: %d houses (%.1f%%)\n", 
                    i, count, (count * 100.0 / houses.size()));
            }
        }
    }
    
    /**
     * Quick method to generate and save dataset programmatically
     */
    public static void generateAndSave(int numSamples, String filename) throws IOException {
        List<EnhancedHouse> houses = generateEnhancedData(numSamples);
        saveEnhancedDataset(houses, filename);
        System.out.println("Generated " + numSamples + " enhanced samples and saved to " + filename);
    }
}

/**
 * Enhanced House class matching the main system's features
 */
class EnhancedHouse {
    private int squareFootage;
    private int bedrooms;
    private int bathrooms;
    private int age;
    private double neighborhood;
    private int parkingSpaces;
    private DatasetGenerator.LocationType locationType;
    private DatasetGenerator.FurnishingState furnishingState;
    private DatasetGenerator.KitchenType kitchenType;
    private double price;

    public EnhancedHouse(int squareFootage, int bedrooms, int bathrooms, int age,
                        double neighborhood, int parkingSpaces, 
                        DatasetGenerator.LocationType locationType,
                        DatasetGenerator.FurnishingState furnishingState,
                        DatasetGenerator.KitchenType kitchenType, double price) {
        this.squareFootage = squareFootage;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.age = age;
        this.neighborhood = neighborhood;
        this.parkingSpaces = parkingSpaces;
        this.locationType = locationType;
        this.furnishingState = furnishingState;
        this.kitchenType = kitchenType;
        this.price = price;
    }

    // Getters
    public int getSquareFootage() { return squareFootage; }
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public int getAge() { return age; }
    public double getNeighborhood() { return neighborhood; }
    public int getParkingSpaces() { return parkingSpaces; }
    public DatasetGenerator.LocationType getLocationType() { return locationType; }
    public DatasetGenerator.FurnishingState getFurnishingState() { return furnishingState; }
    public DatasetGenerator.KitchenType getKitchenType() { return kitchenType; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return String.format("%d sqft, %dbd/%dbth, %d yrs, %.1f neigh, %d park, %s, %s, %s, $%,.0f",
                squareFootage, bedrooms, bathrooms, age, neighborhood, parkingSpaces,
                locationType, furnishingState, kitchenType, price);
    }
}
