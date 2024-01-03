package tech.resilientgym.resilient;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandlerj{
    private Connection connection;

    public DatabaseHandlerj(String url, String user, String password) {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean login(String userEmail, String inputPassword) {
        String sql = "SELECT pass FROM users WHERE email = ?";
        boolean loginSuccess = false;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("pass");
                    loginSuccess = BCrypt.checkpw(inputPassword, storedHashedPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loginSuccess;
    }

    public User fetchUserByEmail(String userEmail) {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    int id = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String level = rs.getString("level");
                    String email = rs.getString("email");
                    int xp = rs.getInt("xp");

                    user = new User(id, name, level, email, xp);

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }

        return user;
    }

    public List<Meal> fetchMealsForDay(int userId, Date date) {
        List<Meal> meals = new ArrayList<>();
        String sql = "SELECT m.meal_id, m.meal_type, m.date, f.name, f.calories " +
                "FROM meals m INNER JOIN food f ON m.food_id = f.food_id " +
                "WHERE m.user_id = ? AND m.date = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int mealId = rs.getInt("meal_id");
                    String mealType = rs.getString("meal_type");
                    Date mealDate = rs.getDate("date");
                    String foodName = rs.getString("name");
                    int calories = rs.getInt("calories");

                    Meal meal = new Meal(mealId, mealType, mealDate.toString(), foodName, calories);
                    meals.add(meal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meals;
    }

    public List<Workout> fetchWorkoutsForDay(int userId, Date date) {
        List<Workout> workouts = new ArrayList<>();
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND date = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int workoutId = rs.getInt("workout_id");
                    String workoutType = rs.getString("workout_type");
                    String exercise = rs.getString("exercise");
                    int sets = rs.getInt("sets");
                    int reps = rs.getInt("reps");

                    Workout workout = new Workout(workoutId, userId, workoutType, exercise, date, sets, reps);
                    workouts.add(workout);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return workouts;
    }

    public String fetchData() {
        String sql = "SELECT * FROM users";
        StringBuilder result = new StringBuilder();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("name");
                String level = rs.getString("level");

                result.append("User ID: ").append(id)
                        .append(", Name: ").append(name)
                        .append(", Level: ").append(level)
                        .append("\n"); // New line for each user
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error fetching data: " + e.getMessage();
        }

        return result.toString();
    }

    public boolean insertUser(String name, String level) {

        if (connection == null) {
            // Handle the error appropriately
            // For example, log an error message and return false to indicate the operation was not successful
            System.err.println("No database connection available.");
            return false; // Indicate the insert was not successful
        }

        String sql = "INSERT INTO users (name, level) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, level);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
