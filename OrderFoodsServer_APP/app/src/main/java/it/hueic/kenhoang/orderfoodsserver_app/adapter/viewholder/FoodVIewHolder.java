package it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;

import it.hueic.kenhoang.orderfoodsserver_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoodsserver_app.R;
import it.hueic.kenhoang.orderfoodsserver_app.common.Common;


/**
 * Created by kenhoang on 27/01/2018.
 */

public class FoodVIewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener{
    public TextView tvFoodName, tvFoodPrice;
    public KenBurnsView imgFood;
    private ItemClickListener itemClickListener;
    public FoodVIewHolder(View itemView) {
        super(itemView);
        tvFoodName  = itemView.findViewById(R.id.food_name);
        tvFoodPrice  = itemView.findViewById(R.id.food_price);
        imgFood  = itemView.findViewById(R.id.food_image);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");
        contextMenu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
