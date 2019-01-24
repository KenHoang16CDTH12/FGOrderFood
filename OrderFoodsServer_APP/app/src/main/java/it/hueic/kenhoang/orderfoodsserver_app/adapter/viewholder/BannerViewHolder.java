package it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;

import it.hueic.kenhoang.orderfoodsserver_app.R;
import it.hueic.kenhoang.orderfoodsserver_app.common.Common;

/**
 * Created by kenhoang on 13/02/2018.
 */

public class BannerViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

    public TextView tvBannerName;
    public KenBurnsView imgBanner;
    public BannerViewHolder(View itemView) {
        super(itemView);
        tvBannerName  = itemView.findViewById(R.id.banner_name);
        imgBanner  = itemView.findViewById(R.id.banner_image);
        itemView.setOnCreateContextMenuListener(this);
    }


    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");
        contextMenu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
