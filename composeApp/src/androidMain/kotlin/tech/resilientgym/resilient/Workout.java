package tech.resilientgym.resilient;

import java.sql.Date;

public class Workout {
    private int workoutId;
    private int userId;
    private String workoutType;
    private String exercise;
    private Date date;
    private int sets;
    private int reps;


    public Workout(int workoutId, int userId, String workoutType, String exercise, Date date, int sets, int reps) {
        this.workoutId = workoutId;
        this.userId = userId;
        this.workoutType = workoutType;
        this.exercise = exercise;
        this.date = date;
        this.sets = sets;
        this.reps = reps;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }
}
