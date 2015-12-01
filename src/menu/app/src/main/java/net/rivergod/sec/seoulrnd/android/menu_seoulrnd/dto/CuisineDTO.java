package net.rivergod.sec.seoulrnd.android.menu_seoulrnd.dto;

public class CuisineDTO {

    public static final Integer MEALCODE_BREAKFAST = 0;
    public static final Integer MEALCODE_LAUNCH = 1;
    public static final Integer MEALCODE_DINNER = 2;


    private Integer mealCode;
    private String cafeteriaUrl;
    private String title;
    private String content;

    public Integer getMealCode() {
        return mealCode;
    }

    public void setMealCode(Integer mealCode) {
        this.mealCode = mealCode;
    }

    public String getCafeteriaUrl() {
        return cafeteriaUrl;
    }

    public void setCafeteriaUrl(String cafeteriaUrl) {
        this.cafeteriaUrl = cafeteriaUrl;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CuisineDTO that = (CuisineDTO) o;

        if (mealCode != null ? !mealCode.equals(that.mealCode) : that.mealCode != null)
            return false;
        if (cafeteriaUrl != null ? !cafeteriaUrl.equals(that.cafeteriaUrl) : that.cafeteriaUrl != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return !(content != null ? !content.equals(that.content) : that.content != null);

    }

    @Override
    public int hashCode() {
        int result = mealCode != null ? mealCode.hashCode() : 0;
        result = 31 * result + (cafeteriaUrl != null ? cafeteriaUrl.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CuisineDTO{" +
                "mealCode=" + mealCode +
                ", cafeteriaUrl='" + cafeteriaUrl + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
