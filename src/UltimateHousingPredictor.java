import java.io.Serializable;
import java.util.*;

/**
 * Ultimate House Data Model - Combines best features of both versions
 * Serializable for persistence + ML-ready features
 */
public class UltimateHousingPredictor {
    
    // Enhanced enums for ML with serialization
    public enum LocationType implements Serializable {
        DOWNTOWN, SUBURB, RURAL, UPTOWN, BEACHSIDE, METROPOLITAN
    }
    
    public enum FurnishingState implements Serializable {
        FURNISHED, UNFURNISHED, SEMI_FURNISHED
    }
    
    public enum KitchenType implements Serializable {
        OPEN_KITCHEN, CLOSED_KITCHEN
    }
    
    /**
     * Ultimate House class - Serializable + ML-ready
     */
    static class House implements Serializable {
        private static final long serialVersionUID = 1L;
        
        // Core features
        private double squareFootage;
        private int bedrooms;
        private int bathrooms;
        private int age;
        private double neighborhood; // 1-5 scale
        private int parkingSpaces; // 0-10
        private LocationType locationType;
        private FurnishingState furnishingState;
        private KitchenType kitchenType;
        private double price;
        
        // Constructors
        public House() {}
        
        public House(double squareFootage, int bedrooms, int bathrooms, int age,
                    double neighborhood, int parkingSpaces, LocationType locationType,
                    FurnishingState furnishingState, KitchenType kitchenType, double price) {
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
        
        // Getters and Setters
        public double getSquareFootage() { return squareFootage; }
        public void setSquareFootage(double squareFootage) { this.squareFootage = squareFootage; }
        
        public int getBedrooms() { return bedrooms; }
        public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }
        
        public int getBathrooms() { return bathrooms; }
        public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public double getNeighborhood() { return neighborhood; }
        public void setNeighborhood(double neighborhood) { this.neighborhood = neighborhood; }
        
        public int getParkingSpaces() { return parkingSpaces; }
        public void setParkingSpaces(int parkingSpaces) { this.parkingSpaces = parkingSpaces; }
        
        public LocationType getLocationType() { return locationType; }
        public void setLocationType(LocationType locationType) { this.locationType = locationType; }
        
        public FurnishingState getFurnishingState() { return furnishingState; }
        public void setFurnishingState(FurnishingState furnishingState) { this.furnishingState = furnishingState; }
        
        public KitchenType getKitchenType() { return kitchenType; }
        public void setKitchenType(KitchenType kitchenType) { this.kitchenType = kitchenType; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        @Override
        public String toString() {
            return String.format(
                "House[sqft=%.0f, beds=%d, baths=%d, age=%d, neighborhood=%.1f, parking=%d, location=%s, furnishing=%s, kitchen=%s, price=$%,.0f]",
                squareFootage, bedrooms, bathrooms, age, neighborhood, parkingSpaces, 
                locationType, furnishingState, kitchenType, price
            );
        }
        
        // Convert to numerical array for ML (all features in one method)
        public double[] toFeatureArray() {
            return new double[] {
                squareFootage,
                bedrooms,
                bathrooms,
                age,
                neighborhood,
                parkingSpaces,
                locationTypeToNumber(locationType),
                furnishingStateToNumber(furnishingState),
                kitchenTypeToNumber(kitchenType)
            };
        }
        
        private double locationTypeToNumber(LocationType location) {
            switch (location) {
                case RURAL: return 1.0;
                case SUBURB: return 2.0;
                case UPTOWN: return 3.0;
                case DOWNTOWN: return 4.0;
                case METROPOLITAN: return 5.0;
                case BEACHSIDE: return 6.0;
                default: return 3.0;
            }
        }
        
        private double furnishingStateToNumber(FurnishingState furnishing) {
            switch (furnishing) {
                case UNFURNISHED: return 1.0;
                case SEMI_FURNISHED: return 2.0;
                case FURNISHED: return 3.0;
                default: return 2.0;
            }
        }
        
        private double kitchenTypeToNumber(KitchenType kitchen) {
            return kitchen == KitchenType.OPEN_KITCHEN ? 1.0 : 0.0;
        }
    }
    
    // ML Prediction System (simplified version)
    static class LinearRegressionModel {
        double intercept;
        double[] coefficients;
        
        LinearRegressionModel(double intercept, double[] coefficients) {
            this.intercept = intercept;
            this.coefficients = coefficients;
        }
        
        double predict(double[] features) {
            double prediction = intercept;
            for (int i = 0; i < features.length; i++) {
                prediction += coefficients[i] * features[i];
            }
            return prediction;
        }
    }
    
    static class HousingMLSystem {
        private LinearRegressionModel model;
        private List<House> trainingData;
        
        public HousingMLSystem(List<House> trainingData) {
            this.trainingData = trainingData;
            trainModel();
        }
        
        private void trainModel() {
            // Convert houses to feature arrays and prices
            double[][] features = new double[trainingData.size()][];
            double[] prices = new double[trainingData.size()];
            
            for (int i = 0; i < trainingData.size(); i++) {
                features[i] = trainingData.get(i).toFeatureArray();
                prices[i] = trainingData.get(i).getPrice();
            }
            
            // Simple linear regression training
            this.model = trainLinearRegression(features, prices);
        }
        
        private LinearRegressionModel trainLinearRegression(double[][] features, double[] prices) {
            int n = features.length;
            int numFeatures = features[0].length;
            
            // Calculate means
            double[] featureMeans = new double[numFeatures];
            for (int j = 0; j < numFeatures; j++) {
                double sum = 0;
                for (int i = 0; i < n; i++) {
                    sum += features[i][j];
                }
                featureMeans[j] = sum / n;
            }
            
            double meanPrice = Arrays.stream(prices).average().orElse(0);
            
            // Calculate coefficients
            double[] coefficients = new double[numFeatures];
            for (int j = 0; j < numFeatures; j++) {
                double numerator = 0;
                double denominator = 0;
                
                for (int i = 0; i < n; i++) {
                    numerator += (features[i][j] - featureMeans[j]) * (prices[i] - meanPrice);
                    denominator += (features[i][j] - featureMeans[j]) * (features[i][j] - featureMeans[j]);
                }
                
                coefficients[j] = denominator != 0 ? numerator / denominator : 0;
            }
            
            // Calculate intercept
            double intercept = meanPrice;
            for (int j = 0; j < numFeatures; j++) {
                intercept -= coefficients[j] * featureMeans[j];
            }
            
            return new LinearRegressionModel(intercept, coefficients);
        }
        
        public double predictPrice(House house) {
            return model.predict(house.toFeatureArray());
        }
        
        public void displayModelInfo() {
            System.out.println("\n=== Trained Model Information ===");
            System.out.printf("Intercept: $%,.2f%n", model.intercept);
            String[] featureNames = {
                "Square Footage", "Bedrooms", "Bathrooms", "Age", 
                "Neighborhood", "Parking Spaces", "Location", "Furnishing", "Kitchen Type"
            };
            
            for (int i = 0; i < featureNames.length; i++) {
                System.out.printf("%-15s: %+,.2f (price impact per unit)%n", 
                    featureNames[i], model.coefficients[i]);
            }
        }
    }
    
    // Sample data generator
    static class SampleDataGenerator {
        public static List<House> generateSampleHouses() {
            List<House> houses = new ArrayList<>();
            Random random = new Random(42);
            
            // Generate 20 sample houses
            for (int i = 0; i < 20; i++) {
                double sqft = 1000 + random.nextInt(2000);
                int beds = 1 + random.nextInt(4);
                int baths = Math.max(1, beds - random.nextInt(1));
                int age = random.nextInt(30);
                double neighborhood = 1 + random.nextInt(4) + random.nextDouble();
                neighborhood = Math.round(neighborhood * 10.0) / 10.0; // Round to 1 decimal
                int parking = random.nextInt(4);
                
                // Random enums
                LocationType[] locations = LocationType.values();
                FurnishingState[] furnishings = FurnishingState.values();
                KitchenType[] kitchens = KitchenType.values();
                
                LocationType location = locations[random.nextInt(locations.length)];
                FurnishingState furnishing = furnishings[random.nextInt(furnishings.length)];
                KitchenType kitchen = kitchens[random.nextInt(kitchens.length)];
                
                // Calculate realistic price
                double price = calculateRealisticPrice(sqft, beds, baths, age, neighborhood, 
                                                     parking, location, furnishing, kitchen);
                
                houses.add(new House(sqft, beds, baths, age, neighborhood, parking, 
                                   location, furnishing, kitchen, price));
            }
            
            return houses;
        }
        
        private static double calculateRealisticPrice(double sqft, int beds, int baths, int age,
                                                     double neighborhood, int parking, 
                                                     LocationType location, FurnishingState furnishing,
                                                     KitchenType kitchen) {
            double basePrice = sqft * 150; // $150 per sq ft
            
            // Feature adjustments
            basePrice += beds * 25000;
            basePrice += baths * 20000;
            basePrice += parking * 15000;
            basePrice += (neighborhood - 2.5) * 50000;
            
            // Enum adjustments
            switch (location) {
                case BEACHSIDE: basePrice *= 1.4; break;
                case UPTOWN: basePrice *= 1.3; break;
                case DOWNTOWN: basePrice *= 1.2; break;
                case METROPOLITAN: basePrice *= 1.1; break;
                case RURAL: basePrice *= 0.8; break;
            }
            
            switch (furnishing) {
                case FURNISHED: basePrice += 30000; break;
                case SEMI_FURNISHED: basePrice += 15000; break;
            }
            
            if (kitchen == KitchenType.OPEN_KITCHEN) {
                basePrice += 20000;
            }
            
            // Age depreciation
            basePrice *= (1 - (age * 0.01));
            
            return Math.max(100000, basePrice);
        }
    }
    
    // Interactive console with numbered menus
    static class InteractiveConsole {
        private Scanner scanner;
        private HousingMLSystem mlSystem;
        
        public InteractiveConsole(HousingMLSystem mlSystem) {
            this.scanner = new Scanner(System.in);
            this.mlSystem = mlSystem;
        }
        
        public void start() {
            System.out.println("🏠 ULTIMATE HOUSING PRICE PREDICTOR");
            System.out.println("===================================\n");
            
            while (true) {
                System.out.println("\n--- Main Menu ---");
                System.out.println("1. Predict House Price");
                System.out.println("2. Show Model Details");
                System.out.println("3. Exit");
                System.out.print("Choose option: ");
                
                int choice = getIntInput();
                
                switch (choice) {
                    case 1:
                        predictHousePrice();
                        break;
                    case 2:
                        mlSystem.displayModelInfo();
                        break;
                    case 3:
                        System.out.println("Thank you for using Ultimate Housing Predictor!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
        
        private void predictHousePrice() {
            try {
                System.out.println("\nEnter House Details:");
                System.out.println("====================");
                
                System.out.print("Square Footage: ");
                double sqft = getDoubleInput();
                
                System.out.print("Bedrooms: ");
                int beds = getIntInput();
                
                System.out.print("Bathrooms: ");
                int baths = getIntInput();
                
                System.out.print("Age (years): ");
                int age = getIntInput();
                
                System.out.print("Neighborhood Quality (1.0-5.0): ");
                double neighborhood = getDoubleInput();
                
                System.out.print("Parking Spaces (0-10): ");
                int parking = getIntInput();
                
                // Location Type Selection
                LocationType location = selectLocationType();
                
                // Furnishing State Selection
                FurnishingState furnishing = selectFurnishingState();
                
                // Kitchen Type Selection
                KitchenType kitchen = selectKitchenType();
                
                // Create house object
                House house = new House(sqft, beds, baths, age, neighborhood, parking, 
                                      location, furnishing, kitchen, 0); // price 0 for prediction
                
                // Get prediction
                double predictedPrice = mlSystem.predictPrice(house);
                
                // Display results
                System.out.println("\n PREDICTION RESULT");
                System.out.println("====================");
                System.out.printf("Predicted Price: $%,.2f%n", predictedPrice);
                System.out.println("\nHouse Features:");
                System.out.println("  - Square Footage: " + sqft + " sq ft");
                System.out.println("  - Bedrooms: " + beds);
                System.out.println("  - Bathrooms: " + baths);
                System.out.println("  - Age: " + age + " years");
                System.out.println("  - Neighborhood Quality: " + neighborhood + "/5");
                System.out.println("  - Parking Spaces: " + parking);
                System.out.println("  - Location Type: " + location);
                System.out.println("  - Furnishing State: " + furnishing);
                System.out.println("  - Kitchen Type: " + kitchen);
                
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please ensure you enter valid values!");
            }
        }
        
        private LocationType selectLocationType() {
            System.out.println("\n Location Type Options:");
            System.out.println("   1. Downtown");
            System.out.println("   2. Suburb");
            System.out.println("   3. Rural");
            System.out.println("   4. Uptown");
            System.out.println("   5. Beachside");
            System.out.println("   6. Metropolitan City");
            System.out.print("Select location type (1-6): ");
            
            int choice = getIntInput();
            while (choice < 1 || choice > 6) {
                System.out.print("Invalid choice. Please select (1-6): ");
                choice = getIntInput();
            }
            
            LocationType selectedLocation = null;
            switch (choice) {
                case 1: selectedLocation = LocationType.DOWNTOWN; break;
                case 2: selectedLocation = LocationType.SUBURB; break;
                case 3: selectedLocation = LocationType.RURAL; break;
                case 4: selectedLocation = LocationType.UPTOWN; break;
                case 5: selectedLocation = LocationType.BEACHSIDE; break;
                case 6: selectedLocation = LocationType.METROPOLITAN; break;
            }
            
            System.out.println(" Location type selected: " + selectedLocation);
            return selectedLocation;
        }
        
        private FurnishingState selectFurnishingState() {
            System.out.println("\n  Furnishing State Options:");
            System.out.println("   1. Furnished");
            System.out.println("   2. Unfurnished");
            System.out.println("   3. Semi-Furnished");
            System.out.print("Select furnishing state (1-3): ");
            
            int choice = getIntInput();
            while (choice < 1 || choice > 3) {
                System.out.print("Invalid choice. Please select (1-3): ");
                choice = getIntInput();
            }
            
            FurnishingState selectedFurnishing = null;
            switch (choice) {
                case 1: selectedFurnishing = FurnishingState.FURNISHED; break;
                case 2: selectedFurnishing = FurnishingState.UNFURNISHED; break;
                case 3: selectedFurnishing = FurnishingState.SEMI_FURNISHED; break;
            }
            
            System.out.println(" Furnishing state selected: " + selectedFurnishing);
            return selectedFurnishing;
        }
        
        private KitchenType selectKitchenType() {
            System.out.println("\n Kitchen Type Options:");
            System.out.println("   1. Open Kitchen");
            System.out.println("   2. Closed Kitchen");
            System.out.print("Select kitchen type (1-2): ");
            
            int choice = getIntInput();
            while (choice < 1 || choice > 2) {
                System.out.print("Invalid choice. Please select (1-2): ");
                choice = getIntInput();
            }
            
            KitchenType selectedKitchen = (choice == 1) ? KitchenType.OPEN_KITCHEN : KitchenType.CLOSED_KITCHEN;
            
            System.out.println(" Kitchen type selected: " + selectedKitchen);
            return selectedKitchen;
        }
        
        private int getIntInput() {
            while (true) {
                try {
                    return Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.print("Please enter a valid number: ");
                }
            }
        }
        
        private double getDoubleInput() {
            while (true) {
                try {
                    return Double.parseDouble(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.print("Please enter a valid number: ");
                }
            }
        }
    }
    // Main method - How to run in VS Code
    public static void main(String[] args) {
        System.out.println("🚀 Starting Ultimate Housing Price Predictor...\n");
        
        // Step 1: Generate sample training data
        System.out.println("📊 Generating training data...");
        List<House> trainingData = SampleDataGenerator.generateSampleHouses();
        
        // Display sample data
        System.out.println("Sample Training Data (first 3 houses):");
        for (int i = 0; i < Math.min(3, trainingData.size()); i++) {
            System.out.println("  " + trainingData.get(i));
        }
        
        // Step 2: Train ML model
        System.out.println("\n🤖 Training machine learning model...");
        HousingMLSystem mlSystem = new HousingMLSystem(trainingData);
        
        // Step 3: Show model info
        mlSystem.displayModelInfo();
        
        // Step 4: Start interactive console
        System.out.println("\n🎯 Starting interactive prediction console...");
        InteractiveConsole console = new InteractiveConsole(mlSystem);
        console.start();
    }
}