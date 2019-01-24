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
 * Created by kenhoang on 26/01/2018.
 */

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
View.OnCreateContextMenuListener{
    public TextView tvMenuName;
    public KenBurnsView imgMenu;
    private ItemClickListener itemClickListener;
    public MenuViewHolder(View itemView) {
        super(itemView);
        tvMenuName  = itemView.findViewById(R.id.menu_name);
        imgMenu  = itemView.findViewById(R.id.menu_image);
        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
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
