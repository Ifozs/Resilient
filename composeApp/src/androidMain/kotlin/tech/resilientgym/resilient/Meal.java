package tech.resilientgym.resilient;

import java.sql.Date;

public class Meal {
    private int mealId;
    private int userId;
    private String mealType;
    private Date date;
    private int foodId;

    public Meal(int mealId, int userId, String mealType, Date date, int foodId) {
        this.mealId = mealId;
        this.userId = userId;
        this.mealType = mealType;
        this.date = date;
        this.foodId = foodId;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }
}