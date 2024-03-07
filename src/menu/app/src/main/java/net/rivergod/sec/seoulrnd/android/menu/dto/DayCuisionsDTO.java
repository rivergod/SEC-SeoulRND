package net.rivergod.sec.seoulrnd.android.menu.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rivergod on 15. 12. 3.
 */
public class DayCuisionsDTO {

    private String date;
    private final Map<String, CuisineDTO> cuisines;

    public DayCuisionsDTO() {
        cuisines = new HashMap<>();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CuisineDTO> getCuisines() {
        return new ArrayList<CuisineDTO>(cuisines.values());
    }

    public boolean addCuisine(String id, CuisineDTO cuisine) {
        return cuisines.put(id, cuisine) != null;
    }

    public CuisineDTO getCuisineById(String id) {
        return cuisines.get(id);
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
