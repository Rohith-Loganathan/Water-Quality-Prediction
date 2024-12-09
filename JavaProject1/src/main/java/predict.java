

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

@WebServlet("/predict")
public class predict extends HttpServlet {
	

    private static final String DB_url = "jdbc:mysql://localhost:3306/water_quality_db"; 
    private static final String DB_user = "root"; 
    private static final String DB_password = "Rohith@8"; 

    private Connection connectToDatabase() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_url, DB_user, DB_password);
    }

    private String predictWaterQuality(double pH, double turbidity, double tds, double temperature, double dissolvedOxygen) {
        if (pH < 6.5 || pH > 8.5 || temperature > 30 || dissolvedOxygen < 5) {
            return "Poor";
        } else if (turbidity > 5 || tds > 500) {
            return "Moderate";
        } else {
            return "Good";
        }
    }
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
		try {
            double pH = Double.parseDouble(request.getParameter("ph"));
            double turbidity = Double.parseDouble(request.getParameter("turbidity"));
            double tds = Double.parseDouble(request.getParameter("tds"));
            double temperature = Double.parseDouble(request.getParameter("temperature"));
            double dissolvedOxygen = Double.parseDouble(request.getParameter("dissolvedOxygen"));

            String waterQuality = predictWaterQuality(pH, turbidity, tds, temperature, dissolvedOxygen);

            try (Connection connection = connectToDatabase()) {
                String sql = "INSERT INTO water_samples (pH, Turbidity, TDS, Temperature, Dissolved_Oxygen, Water_Quality) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setDouble(1, pH);
                statement.setDouble(2, turbidity);
                statement.setDouble(3, tds);
                statement.setDouble(4, temperature);
                statement.setDouble(5, dissolvedOxygen);
                statement.setString(6, waterQuality);
                statement.executeUpdate();
            }

            response.setContentType("text/html");

            response.getWriter().write("<html><head>"
                    + "<style>"
                    + "body {"
                    + "   font-family: Arial, sans-serif;"
                    + "   background-color: #f0f8ff;"
                    + "   text-align: center;"
                    + "   padding: 20px;"
                    + "}"
                    + "h1 {"
                    + "   font-size: 2.5em;"
                    + "   color: #007BFF;"
                    + "   font-weight: bold;"
                    + "   margin-bottom: 30px;"
                    + "}"
                    + "p {"
                    + "   font-size: 1.2em;"
                    + "   color: #333;"
                    + "   margin: 10px 0;"
                    + "}"
                    + ".result-box {"
                    + "   background-color: rgba(255, 255, 255, 0.9);"
                    + "   padding: 20px;"
                    + "   border-radius: 10px;"
                    + "   box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);"
                    + "   width: 80%;"
                    + "   margin: 20px auto;"
                    + "}"
                    + ".water-quality {"
                    + "   font-weight: bold;"
                    + "   font-size: 1.3em;"
                    + "   color: #28a745;" 
                    + "}"
                    + "</style>"
                    + "</head><body>");

            response.getWriter().write("<h1>Water Quality Prediction</h1>");
            response.getWriter().write("<div class='result-box'>");

            response.getWriter().write("<p>pH: " + pH + "</p>");
            response.getWriter().write("<p>Turbidity: " + turbidity + " NTU</p>");
            response.getWriter().write("<p>TDS: " + tds + " ppm</p>");
            response.getWriter().write("<p>Temperature: " + temperature + " °C</p>");
            response.getWriter().write("<p>Dissolved Oxygen: " + dissolvedOxygen + " mg/L</p>");
            response.getWriter().write("<p class='water-quality'>Predicted Quality: " + waterQuality + "</p>");

            response.getWriter().write("</div>");
            response.getWriter().write("</body></html>");


		}
		
		catch (Exception e) {
          e.printStackTrace();
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred");
      }
	}

}
