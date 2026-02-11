import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Java Web Server for Housing Price Prediction
 * Serves HTML interface and handles prediction requests
 */
public class HousingPriceWebServer {
    private static UltimateHousingPredictor.HousingMLSystem mlSystem;
    private static boolean isTrained = false;
    
    public static void main(String[] args) throws IOException {
        // Initialize the ML system with sample data
        System.out.println(" Starting Housing Price Prediction Web Server...");
        initializeMLSystem();
        
        // Create HTTP server on port 8080
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Serve the web interface
        server.createContext("/", new WebPageHandler());
        
        // API endpoints
        server.createContext("/api/train", new TrainHandler());
        server.createContext("/api/predict", new PredictHandler());
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/evaluate", new EvaluateHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println(" Web server started on http://localhost:8080");
        System.out.println(" ML System: " + (isTrained ? "TRAINED" : "NOT TRAINED"));
    }
    
    private static void initializeMLSystem() {
        try {
            System.out.println(" Initializing ML system with sample data...");
            List<UltimateHousingPredictor.House> trainingData = UltimateHousingPredictor.SampleDataGenerator.generateSampleHouses();
            mlSystem = new UltimateHousingPredictor.HousingMLSystem(trainingData);
            isTrained = true;
            System.out.println("  ML system initialized with " + trainingData.size() + " samples");
        } catch (Exception e) {
            System.err.println(" Error initializing ML system: " + e.getMessage());
        }
    }
    
    // Handler for serving the web page
    static class WebPageHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            String response = getHTMLPage();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
        
        private String getHTMLPage() {
            return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Housing Price Prediction System</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            padding: 20px;
                        }
                        
                        .container {
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                        
                        .header {
                            text-align: center;
                            color: white;
                            margin-bottom: 30px;
                        }
                        
                        .header h1 {
                            font-size: 2.5em;
                            margin-bottom: 10px;
                        }
                        
                        .header p {
                            font-size: 1.1em;
                            opacity: 0.9;
                        }
                        
                        .cards {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
                            gap: 20px;
                            margin-bottom: 20px;
                        }
                        
                        .card {
                            background: white;
                            border-radius: 15px;
                            padding: 25px;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                        }
                        
                        .card h2 {
                            color: #667eea;
                            margin-bottom: 20px;
                            font-size: 1.5em;
                        }
                        
                        .form-group {
                            margin-bottom: 15px;
                        }
                        
                        .form-group label {
                            display: block;
                            margin-bottom: 5px;
                            color: #333;
                            font-weight: 500;
                        }
                        
                        .form-group input,
                        .form-group select {
                            width: 100%;
                            padding: 10px;
                            border: 2px solid #e0e0e0;
                            border-radius: 8px;
                            font-size: 14px;
                            transition: border-color 0.3s;
                        }
                        
                        .form-group input:focus,
                        .form-group select:focus {
                            outline: none;
                            border-color: #667eea;
                        }
                        
                        .btn {
                            width: 100%;
                            padding: 12px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            border: none;
                            border-radius: 8px;
                            font-size: 16px;
                            font-weight: 600;
                            cursor: pointer;
                            transition: transform 0.2s;
                        }
                        
                        .btn:hover {
                            transform: translateY(-2px);
                        }
                        
                        .btn:disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                        }
                        
                        .btn-secondary {
                            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                        }
                        
                        .result {
                            margin-top: 20px;
                            padding: 15px;
                            background: #f0f4ff;
                            border-radius: 8px;
                            border-left: 4px solid #667eea;
                        }
                        
                        .result h3 {
                            color: #667eea;
                            margin-bottom: 10px;
                        }
                        
                        .price-display {
                            font-size: 2em;
                            color: #667eea;
                            font-weight: bold;
                            text-align: center;
                            margin: 15px 0;
                        }
                        
                        .metric {
                            display: flex;
                            justify-content: space-between;
                            padding: 8px 0;
                            border-bottom: 1px solid #e0e0e0;
                        }
                        
                        .metric:last-child {
                            border-bottom: none;
                        }
                        
                        .metric-label {
                            font-weight: 500;
                            color: #555;
                        }
                        
                        .metric-value {
                            color: #667eea;
                            font-weight: 600;
                        }
                        
                        .status {
                            display: inline-block;
                            padding: 5px 15px;
                            border-radius: 20px;
                            font-size: 14px;
                            font-weight: 500;
                        }
                        
                        .status.trained {
                            background: #d4edda;
                            color: #155724;
                        }
                        
                        .status.not-trained {
                            background: #f8d7da;
                            color: #721c24;
                        }
                        
                        .loading {
                            text-align: center;
                            padding: 20px;
                            color: #667eea;
                        }
                        
                        .error {
                            background: #f8d7da;
                            color: #721c24;
                            padding: 15px;
                            border-radius: 8px;
                            margin-top: 15px;
                        }
                        
                        .success {
                            background: #d4edda;
                            color: #155724;
                            padding: 15px;
                            border-radius: 8px;
                            margin-top: 15px;
                        }
                        
                        .grid-2 {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 15px;
                        }
                        
                        @media (max-width: 768px) {
                            .grid-2 {
                                grid-template-columns: 1fr;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1> Housing Price Prediction</h1>
                            <p>Machine Learning-Based Real Estate Valuation System</p>
                            <div style="margin-top: 15px;">
                                <span id="modelStatus" class="status not-trained">Model Not Trained</span>
                            </div>
                        </div>
                        
                        <div class="cards">
                            <!-- Training Card -->
                            <div class="card">
                                <h2> Model Status</h2>
                                <div class="form-group">
                                    <p style="color: #666; margin-bottom: 15px;">
                                        The model is automatically trained with sample data when the server starts.
                                        You can retrain with new data if needed.
                                    </p>
                                </div>
                                
                                <div class="form-group">
                                    <label>Training Samples:</label>
                                    <input type="number" id="numSamples" value="20" min="10" max="1000">
                                </div>
                                
                                <button class="btn" onclick="trainModel()">Retrain Model</button>
                                
                                <div id="trainResult"></div>
                            </div>
                            
                            <!-- Prediction Card -->
                            <div class="card">
                                <h2> Predict Price</h2>
                                
                                <div class="grid-2">
                                    <div class="form-group">
                                        <label>Square Footage:</label>
                                        <input type="number" id="squareFootage" value="1800" min="100" max="10000">
                                    </div>
                                    
                                    <div class="form-group">
                                        <label>Bedrooms:</label>
                                        <input type="number" id="bedrooms" value="3" min="1" max="10">
                                    </div>
                                </div>
                                
                                <div class="grid-2">
                                    <div class="form-group">
                                        <label>Bathrooms:</label>
                                        <input type="number" id="bathrooms" value="2" min="1" max="10">
                                    </div>
                                    
                                    <div class="form-group">
                                        <label>Parking Spaces:</label>
                                        <input type="number" id="parkingSpaces" value="2" min="0" max="10">
                                    </div>
                                </div>
                                
                                <div class="form-group">
                                    <label>Age (years):</label>
                                    <input type="number" id="age" value="10" min="0" max="100">
                                </div>
                                
                                <div class="form-group">
                                    <label>Neighborhood Quality (1-5):</label>
                                    <input type="number" id="neighborhood" value="4" min="1" max="5" step="0.1">
                                </div>
                                
                                <div class="form-group">
                                    <label>Location Type:</label>
                                    <select id="locationType">
                                        <option value="1">1 - Downtown</option>
                                        <option value="2">2 - Suburb</option>
                                        <option value="3">3 - Rural</option>
                                        <option value="4">4 - Uptown</option>
                                        <option value="5">5 - Beachside</option>
                                        <option value="6">6 - Metropolitan</option>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label>Furnishing State:</label>
                                    <select id="furnishingState">
                                        <option value="1">1 - Unfurnished</option>
                                        <option value="2">2 - Semi-Furnished</option>
                                        <option value="3">3 - Furnished</option>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label>Kitchen Type:</label>
                                    <select id="kitchenType">
                                        <option value="0">Closed Kitchen</option>
                                        <option value="1">Open Kitchen</option>
                                    </select>
                                </div>
                                
                                <button class="btn btn-secondary" onclick="predictPrice()">Predict Price</button>
                                
                                <div id="predictResult"></div>
                            </div>
                            
                            <!-- Model Info Card -->
                            <div class="card">
                                <h2> Model Information</h2>
                                <p style="color: #666; margin-bottom: 15px;">
                                    View model coefficients and feature impacts.
                                </p>
                                
                                <button class="btn" onclick="getModelInfo()">Show Model Details</button>
                                
                                <div id="modelInfoResult"></div>
                            </div>
                        </div>
                    </div>
                    
                    <script>
                        const API_BASE = 'http://localhost:8080/api';
                        
                        // Check model status on load
                        window.onload = () => {
                            checkStatus();
                        };
                        
                        async function checkStatus() {
                            try {
                                const response = await fetch(`${API_BASE}/status`);
                                const data = await response.json();
                                
                                const statusEl = document.getElementById('modelStatus');
                                if (data.trained) {
                                    statusEl.textContent = '✓ Model Trained';
                                    statusEl.className = 'status trained';
                                } else {
                                    statusEl.textContent = '✗ Model Not Trained';
                                    statusEl.className = 'status not-trained';
                                }
                            } catch (error) {
                                console.error('Error checking status:', error);
                            }
                        }
                        
                        async function trainModel() {
                            const resultDiv = document.getElementById('trainResult');
                            resultDiv.innerHTML = '<div class="loading">Training model... Please wait.</div>';
                            
                            try {
                                const numSamples = parseInt(document.getElementById('numSamples').value);
                                
                                const response = await fetch(`${API_BASE}/train`, {
                                    method: 'POST',
                                    headers: {'Content-Type': 'application/json'},
                                    body: JSON.stringify({ samples: numSamples })
                                });
                                
                                const data = await response.json();
                                
                                if (response.ok) {
                                    resultDiv.innerHTML = `
                                        <div class="success">
                                            <h3>✓ Training Successful!</h3>
                                            <p>Model trained with ${data.samples} samples</p>
                                        </div>
                                    `;
                                    checkStatus();
                                } else {
                                    resultDiv.innerHTML = `<div class="error">Error: ${data.error}</div>`;
                                }
                            } catch (error) {
                                resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
                            }
                        }
                        
                        async function predictPrice() {
                            const resultDiv = document.getElementById('predictResult');
                            resultDiv.innerHTML = '<div class="loading">Predicting price...</div>';
                            
                            try {
                                const requestBody = {
                                    squareFootage: parseFloat(document.getElementById('squareFootage').value),
                                    bedrooms: parseInt(document.getElementById('bedrooms').value),
                                    bathrooms: parseInt(document.getElementById('bathrooms').value),
                                    age: parseInt(document.getElementById('age').value),
                                    neighborhood: parseFloat(document.getElementById('neighborhood').value),
                                    parkingSpaces: parseInt(document.getElementById('parkingSpaces').value),
                                    locationType: parseInt(document.getElementById('locationType').value),
                                    furnishingState: parseInt(document.getElementById('furnishingState').value),
                                    kitchenType: parseInt(document.getElementById('kitchenType').value)
                                };
                                
                                const response = await fetch(`${API_BASE}/predict`, {
                                    method: 'POST',
                                    headers: {'Content-Type': 'application/json'},
                                    body: JSON.stringify(requestBody)
                                });
                                
                                const data = await response.json();
                                
                                if (response.ok) {
                                    resultDiv.innerHTML = `
                                        <div class="result">
                                            <h3>Prediction Result</h3>
                                            <div class="price-display">
                                                $${data.predictedPrice.toLocaleString()}
                                            </div>
                                            <div class="metric">
                                                <span class="metric-label">Square Footage:</span>
                                                <span class="metric-value">${requestBody.squareFootage} sq ft</span>
                                            </div>
                                            <div class="metric">
                                                <span class="metric-label">Bedrooms:</span>
                                                <span class="metric-value">${requestBody.bedrooms}</span>
                                            </div>
                                            <div class="metric">
                                                <span class="metric-label">Bathrooms:</span>
                                                <span class="metric-value">${requestBody.bathrooms}</span>
                                            </div>
                                            <div class="metric">
                                                <span class="metric-label">Age:</span>
                                                <span class="metric-value">${requestBody.age} years</span>
                                            </div>
                                        </div>
                                    `;
                                } else {
                                    resultDiv.innerHTML = `<div class="error">Error: ${data.error}</div>`;
                                }
                            } catch (error) {
                                resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
                            }
                        }
                        
                        async function getModelInfo() {
                            const resultDiv = document.getElementById('modelInfoResult');
                            resultDiv.innerHTML = '<div class="loading">Loading model information...</div>';
                            
                            try {
                                const response = await fetch(`${API_BASE}/evaluate`);
                                const data = await response.json();
                                
                                if (response.ok) {
                                    let metricsHTML = '<div class="result"><h3>Model Coefficients</h3>';
                                    
                                    data.coefficients.forEach((coeff, index) => {
                                        const featureNames = [
                                            "Square Footage", "Bedrooms", "Bathrooms", "Age", 
                                            "Neighborhood", "Parking Spaces", "Location", "Furnishing", "Kitchen Type"
                                        ];
                                        metricsHTML += `
                                            <div class="metric">
                                                <span class="metric-label">${featureNames[index]}:</span>
                                                <span class="metric-value">${coeff.toFixed(2)}</span>
                                            </div>
                                        `;
                                    });
                                    
                                    metricsHTML += `
                                        <div class="metric">
                                            <span class="metric-label">Intercept:</span>
                                            <span class="metric-value">${data.intercept.toFixed(2)}</span>
                                        </div>
                                    </div>`;
                                    
                                    resultDiv.innerHTML = metricsHTML;
                                } else {
                                    resultDiv.innerHTML = `<div class="error">Error: ${data.error}</div>`;
                                }
                            } catch (error) {
                                resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
                            }
                        }
                    </script>
                </body>
                </html>
                """;
        }
    }
    
    // API Handlers
    static class TrainHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, "Method not allowed", 405);
                return;
            }
            
            try {
                // Parse request
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, Object> request = parseJson(requestBody);
                
                int numSamples = 20; // default
                if (request.containsKey("samples")) {
                    numSamples = ((Number) request.get("samples")).intValue();
                }
                
                // Generate new training data
                List<UltimateHousingPredictor.House> trainingData = UltimateHousingPredictor.SampleDataGenerator.generateSampleHouses();
                // Note: In a real implementation, you'd generate the specified number of samples
                
                mlSystem = new UltimateHousingPredictor.HousingMLSystem(trainingData);
                isTrained = true;
                
                // Send success response
                String response = String.format("{\"samples\": %d, \"status\": \"trained\"}", trainingData.size());
                sendJsonResponse(exchange, response);
                
            } catch (Exception e) {
                sendError(exchange, "Training failed: " + e.getMessage(), 500);
            }
        }
    }
    
    static class PredictHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, "Method not allowed", 405);
                return;
            }
            
            if (!isTrained) {
                sendError(exchange, "Model not trained", 400);
                return;
            }
            
            try {
                // Parse request
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, Object> request = parseJson(requestBody);
                
                // Create house object from request
                UltimateHousingPredictor.House house = new UltimateHousingPredictor.House(
                    ((Number) request.get("squareFootage")).doubleValue(),
                    ((Number) request.get("bedrooms")).intValue(),
                    ((Number) request.get("bathrooms")).intValue(),
                    ((Number) request.get("age")).intValue(),
                    ((Number) request.get("neighborhood")).doubleValue(),
                    ((Number) request.get("parkingSpaces")).intValue(),
                    getLocationType(((Number) request.get("locationType")).intValue()),
                    getFurnishingState(((Number) request.get("furnishingState")).intValue()),
                    getKitchenType(((Number) request.get("kitchenType")).intValue()),
                    0 // price will be predicted
                );
                
                // Get prediction
                double predictedPrice = mlSystem.predictPrice(house);
                
                // Send response
                String response = String.format("{\"predictedPrice\": %.2f}", predictedPrice);
                sendJsonResponse(exchange, response);
                
            } catch (Exception e) {
                sendError(exchange, "Prediction failed: " + e.getMessage(), 500);
            }
        }
        
        private UltimateHousingPredictor.LocationType getLocationType(int choice) {
            switch (choice) {
                case 1: return UltimateHousingPredictor.LocationType.DOWNTOWN;
                case 2: return UltimateHousingPredictor.LocationType.SUBURB;
                case 3: return UltimateHousingPredictor.LocationType.RURAL;
                case 4: return UltimateHousingPredictor.LocationType.UPTOWN;
                case 5: return UltimateHousingPredictor.LocationType.BEACHSIDE;
                case 6: return UltimateHousingPredictor.LocationType.METROPOLITAN;
                default: return UltimateHousingPredictor.LocationType.SUBURB;
            }
        }
        
        private UltimateHousingPredictor.FurnishingState getFurnishingState(int choice) {
            switch (choice) {
                case 1: return UltimateHousingPredictor.FurnishingState.UNFURNISHED;
                case 2: return UltimateHousingPredictor.FurnishingState.SEMI_FURNISHED;
                case 3: return UltimateHousingPredictor.FurnishingState.FURNISHED;
                default: return UltimateHousingPredictor.FurnishingState.SEMI_FURNISHED;
            }
        }
        
        private UltimateHousingPredictor.KitchenType getKitchenType(int choice) {
            return choice == 1 ? UltimateHousingPredictor.KitchenType.OPEN_KITCHEN : UltimateHousingPredictor.KitchenType.CLOSED_KITCHEN;
        }
    }
    
    static class StatusHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            String response = String.format("{\"trained\": %s}", isTrained);
            sendJsonResponse(exchange, response);
        }
    }
    
    static class EvaluateHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            if (!isTrained) {
                sendError(exchange, "Model not trained", 400);
                return;
            }
            
            try {
                // Get model info using reflection (since coefficients are private)
                java.lang.reflect.Field modelField = mlSystem.getClass().getDeclaredField("model");
                modelField.setAccessible(true);
                UltimateHousingPredictor.LinearRegressionModel model = 
                    (UltimateHousingPredictor.LinearRegressionModel) modelField.get(mlSystem);
                
                // Build response with coefficients
                StringBuilder response = new StringBuilder();
                response.append("{\"coefficients\": [");
                for (int i = 0; i < model.coefficients.length; i++) {
                    if (i > 0) response.append(",");
                    response.append(String.format("%.2f", model.coefficients[i]));
                }
                response.append("], \"intercept\": ").append(String.format("%.2f", model.intercept)).append("}");
                
                sendJsonResponse(exchange, response.toString());
                
            } catch (Exception e) {
                sendError(exchange, "Evaluation failed: " + e.getMessage(), 500);
            }
        }
    }
    
    // Utility methods
    private static void sendJsonResponse(com.sun.net.httpserver.HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
    
    private static void sendError(com.sun.net.httpserver.HttpExchange exchange, String message, int code) throws IOException {
        String response = String.format("{\"error\": \"%s\"}", message);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, response.getBytes(StandardCharsets.UTF_8).length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
    
    private static Map<String, Object> parseJson(String json) {
        // Simple JSON parser for basic objects
        Map<String, Object> result = new HashMap<>();
        json = json.replaceAll("[{}\"]", "").trim();
        String[] pairs = json.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                // Try to parse as number
                try {
                    if (value.contains(".")) {
                        result.put(key, Double.parseDouble(value));
                    } else {
                        result.put(key, Integer.parseInt(value));
                    }
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }
}