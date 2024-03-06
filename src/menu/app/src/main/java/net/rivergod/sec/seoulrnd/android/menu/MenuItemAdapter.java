package net.rivergod.sec.seoulrnd.android.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LinearSLM;

import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    private static final int LINEAR = 0;

    private List<GridItem> items;
    private LayoutInflater inflater;

    private Context context;

    public MenuItemAdapter(Context context){
        this.context = context;
        inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    private void sort(List<CuisineDTO> items) {

        SharedPreferences prefs = context.getSharedPreferences(MenuActivity.ALARM_TAG, Context.MODE_PRIVATE);
        final int campus = prefs.getInt(MenuActivity.CAMPUS_TAG, R.id.orderCampus2);

        Collections.sort(items, new Comparator<CuisineDTO>() {
            @Override
            public int compare(CuisineDTO lhs, CuisineDTO rhs) {

                Integer campusL = lhs.getCampusCode();
                Integer campusR = rhs.getCampusCode();

                int ret = 0;
                if(campusL != null && campusR != null) {
                    ret = campusL > campusR ? 1 : campusL < campusR ? -1 : 0;

                    if(campus == R.id.orderCampus2) {
                        ret *= -1;
                    }
                }

                if(ret == 0) {

                    int cafeteriaL = lhs.getCafeteriaCode();
                    int cafeteriaR = rhs.getCafeteriaCode();

                    ret = cafeteriaL > cafeteriaR ? 1 : -1;
                }

                return ret;
            }
        });
    }

    public void setItems(List<CuisineDTO> items) {

        List<CuisineDTO> sorted = new ArrayList<>(items);
        sort(sorted);

        this.items = new ArrayList<>();

        Integer lastCafeteria = -1;
        int sectionManager = -1;
        int headerCount = 0;
        int sectionFirstPosition = 0;

        int length = sorted.size();
        for (int i = 0; i < length; i++) {
            CuisineDTO item = sorted.get(i);

            Integer cafeteria = item.getCampusCode();
            if(lastCafeteria != cafeteria) {
                lastCafeteria = cafeteria;

                sectionManager = (sectionManager + 1) % 2;
                sectionFirstPosition = i + headerCount;
                headerCount++;

                this.items.add(new GridItem(sectionManager, sectionFirstPosition, cafeteria));
            }

            this.items.add(new GridItem(sectionManager, sectionFirstPosition, item));
        }

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if(viewType == VIEW_TYPE_HEADER) {
            view = inflater.inflate(R.layout.header_item, parent, false);
        } else {
            view = inflater.inflate(R.layout.menu_item, parent, false);
        }
        return new CusineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final GridItem gridItem = items.get(position);
        final CusineViewHolder cusineViewHolder = (CusineViewHolder) holder;
        final View itemView = holder.itemView;
        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());

        if(gridItem.getViewType() == VIEW_TYPE_CONTENT) {

            final CuisineDTO item = gridItem.item;
            cusineViewHolder.bindItem(item);

        } else {

            final Integer cafeteria = gridItem.cafeteria;
            final String campus = cafeteria == CuisineDTO.CAMPUSCODE_2 ?
                    "2캠퍼스 (D,E,F Tower)" : "1캠퍼스 (A,B,C Tower)";

            cusineViewHolder.bindText(campus);
        }

        lp.setSlm(gridItem.sectionManager == LINEAR ? LinearSLM.ID : GridSLM.ID);
        lp.setNumColumns(2);
        lp.setFirstPosition(gridItem.sectionFirstPosition);
        lp.setSlm(GridSLM.ID);

        itemView.setLayoutParams(lp);
    }

    @Override
    public long getItemId(int position) {
        if (items == null) {
            return 0;
        }

        return position;
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (items == null) {
            return VIEW_TYPE_CONTENT;
        }

        return items.get(position).getViewType();
    }

    class GridItem {

        boolean isHeader = false;
        int sectionManager;
        int sectionFirstPosition;

        Integer cafeteria;
        CuisineDTO item;

        public GridItem(int sectionManager, int sectionFirstPosition, Integer cafeteria) {
            this.sectionManager = sectionManager;
            this.sectionFirstPosition = sectionFirstPosition;
            this.isHeader = true;
            this.cafeteria = cafeteria;
        }

        public GridItem(int sectionManager, int sectionFirstPosition, CuisineDTO item) {
            this.sectionManager = sectionManager;
            this.sectionFirstPosition = sectionFirstPosition;
            this.isHeader = false;
            this.item = item;
        }

        public int getViewType () {
            return isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
        }

    }

    class CusineViewHolder extends RecyclerView.ViewHolder{
        ImageView ivIcon;
        TextView tvName;
        TextView tvSide;
        TextView tvCalorie;

        // for header
        TextView tvCampus;

        public CusineViewHolder(View itemView) {
            super(itemView);

            ivIcon = (ImageView) itemView.findViewById(R.id.menu_icon_place);
            tvName = (TextView) itemView.findViewById(R.id.menu_item_name);
            tvSide = (TextView) itemView.findViewById(R.id.menu_item_side);
            tvCalorie = (TextView) itemView.findViewById(R.id.menu_item_calorie);

            // for header
            tvCampus =  (TextView) itemView.findViewById(R.id.header_menu_item_name);
        }

        // for header
        public void bindText(String text) {
            tvCampus.setText(text);
        }

        public void bindItem(CuisineDTO item) {

            ivIcon.setBackgroundResource(item.getCafeteriaCode());

            String title = item.getTitle();
            if (title.contains("l)")) {
                title = title.substring(0, title.indexOf("("));
            }
            tvName.setText(title);

            if (title.length() > 13) {
                tvName.setTextSize(10);
            } else {
                tvName.setTextSize(13);
            }

            String content = item.getContent();
            int lastIndexComma = content.lastIndexOf(",");
            if (lastIndexComma != -1) {
                tvSide.setText(content.substring(0, lastIndexComma));
            } else {
                tvSide.setText("");
            }

            tvCalorie.setText(content.substring(lastIndexComma + 1, content.length()));
        }
    }
}
