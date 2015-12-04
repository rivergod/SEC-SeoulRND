package net.rivergod.sec.seoulrnd.android.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO;

import java.util.List;

public class MenuItemAdapter extends BaseAdapter {

    class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvSide;
        TextView tvCalorie;
    }

    private List<CuisineDTO> items;
    private LayoutInflater inflater;

    public MenuItemAdapter(Context context){
        inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    public void setItems(List<CuisineDTO> items) {
        this.items = items;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    @Override
    public Object getItem(int position) {

        if (items == null) {
            return null;
        }

        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (items == null) {
            return 0;
        }

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (items == null || inflater == null) {
            return null;
        }

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.menu_item, parent, false);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.menu_icon_place);
            holder.tvName = (TextView) convertView.findViewById(R.id.menu_item_name);
            holder.tvSide = (TextView) convertView.findViewById(R.id.menu_item_side);
            holder.tvCalorie = (TextView) convertView.findViewById(R.id.menu_item_calorie);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CuisineDTO item = items.get(position);

        holder.ivIcon.setBackgroundResource(MenuItemIconResouce.getMenuIcon(item.getCafeteriaUrl()));

        String title = item.getTitle();
        if (title.contains("l)")) {
            title = title.substring(0, title.indexOf("("));
        }
        holder.tvName.setText(title);

        if (title.length() > 13) {
            holder.tvName.setTextSize(10);
        } else {
            holder.tvName.setTextSize(13);
        }

        String content = item.getContent();
        int lastIndexComma = content.lastIndexOf(",");
        if (lastIndexComma != -1) {
            holder.tvSide.setText(content.substring(0, lastIndexComma));
        } else {
            holder.tvSide.setText("");
        }

        holder.tvCalorie.setText(content.substring(lastIndexComma + 1, content.length()));

        return convertView;
    }
}
