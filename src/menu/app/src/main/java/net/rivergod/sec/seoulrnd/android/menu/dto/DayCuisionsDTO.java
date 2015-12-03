package net.rivergod.sec.seoulrnd.android.menu.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rivergod on 15. 12. 3.
 */
public class DayCuisionsDTO {

    private String date;
    private final List<CuisineDTO> cuisines;

    public DayCuisionsDTO() {
        cuisines = new ArrayList<CuisineDTO>();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CuisineDTO> getCuisines() {
        return cuisines;
    }

    public boolean addCuisine(CuisineDTO cuisine) {
        return cuisines.add(cuisine);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DayCuisionsDTO)) return false;

        DayCuisionsDTO that = (DayCuisionsDTO) o;

        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        return cuisines.equals(that.cuisines);

    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + cuisines.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DayCuisionsDTO{" +
                "date='" + date + '\'' +
                ", cuisines=" + cuisines +
                '}';
    }
}
