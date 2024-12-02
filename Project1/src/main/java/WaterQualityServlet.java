import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/predict")
public class WaterQualityServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/water_quality_db"; 
    private static final String DB_USER = "root"; 
    private static final String DB_PASSWORD = "Rohith@8"; 

    // JDBC Connection method
    private Connection connectToDatabase() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Prediction logic
    private String predictWaterQuality(double pH, double turbidity, double tds) {
        if (pH < 6.5 || pH > 8.5) {
            return "Poor";
        } else if (turbidity > 5 || tds > 500) {
            return "Moderate";
        } else {
            return "Good";
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Retrieve form data
            double pH = Double.parseDouble(request.getParameter("ph"));
            double Turbidity = Double.parseDouble(request.getParameter("turbidity"));
            double Total_Dissolved_Solids  = Double.parseDouble(request.getParameter("tds"));

            // Predict water quality
            String Water_Quality = predictWaterQuality(pH, Turbidity, Total_Dissolved_Solids );

            // Save the data in the database
            try (Connection connection = connectToDatabase()) {
                String sql = "INSERT INTO water_samples (pH, Turbidity, Total_Dissolved_Solids , Water_Quality) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setDouble(1, pH);
                statement.setDouble(2, Turbidity);
                statement.setDouble(3, Total_Dissolved_Solids );
                statement.setString(4, Water_Quality);
                statement.executeUpdate();
            }

            // Respond with the result
            response.setContentType("text/html");
            response.getWriter().write("<h1>Water Quality Prediction</h1>");
            response.getWriter().write("<p>pH: " + pH + "</p>");
            response.getWriter().write("<p>Turbidity: " + Turbidity + " NTU</p>");
            response.getWriter().write("<p>TDS: " + Total_Dissolved_Solids  + " ppm</p>");
            response.getWriter().write("<p>Predicted Quality: " +Water_Quality + "</p>");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred");
        }
    }
}
