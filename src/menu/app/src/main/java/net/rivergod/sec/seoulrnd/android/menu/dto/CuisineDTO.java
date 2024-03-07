package net.rivergod.sec.seoulrnd.android.menu.dto;

import java.util.Objects;

public class CuisineDTO {

    public static final Integer MEALCODE_BREAKFAST = 0;
    public static final Integer MEALCODE_LAUNCH = 1;
    public static final Integer MEALCODE_DINNER = 2;

    public static final Integer CAMPUSCODE_1 = 0;
    public static final Integer CAMPUSCODE_2 = 1;

    private Integer mealCode;
    private Integer campusCode;
    private Integer cafeteriaCode;

    private String cafeteriaUrl;
    private String title;
    private String content;

    private String calorie;

    public Integer getMealCode() {
        return mealCode;
    }

    public void setMealCode(Integer mealCode) {
        this.mealCode = mealCode;
    }

    public Integer getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(Integer campusCode) {
        this.campusCode = campusCode;
    }

    public void setCafeteriaUrl(String cafeteriaUrl) {
        this.cafeteriaUrl = cafeteriaUrl;
    }

    public void setCafeteriaCode(Integer cafeteriaCode) {
        this.cafeteriaCode = cafeteriaCode;
    }

    public Integer getCafeteriaCode() {
        return cafeteriaCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCalorie() {
        return calorie;
    }

    public void setCalorie(String calorie) {
        this.calorie = calorie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CuisineDTO that = (CuisineDTO) o;
        return Objects.equals(mealCode, that.mealCode) && Objects.equals(campusCode, that.campusCode) && Objects.equals(cafeteriaCode, that.cafeteriaCode) && Objects.equals(cafeteriaUrl, that.cafeteriaUrl) && Objects.equals(title, that.title) && Objects.equals(content, that.content) && Objects.equals(calorie, that.calorie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealCode, campusCode, cafeteriaCode, cafeteriaUrl, title, content, calorie);
    }

    @Override
    public String toString() {
        return "CuisineDTO{" +
                "mealCode=" + mealCode +
                ", campusCode=" + campusCode +
                ", cafeteriaCode=" + cafeteriaCode +
                ", cafeteriaUrl='" + cafeteriaUrl + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", calorie='" + calorie + '\'' +
                '}';
    }
}
