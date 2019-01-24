package it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;
import it.hueic.kenhoang.orderfoods_app.common.Common;

/**
 * Created by kenhoang on 28/01/2018.
 */

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener{
    public TextView txt_card_name, txt_price;
    public ImageView img_item_cart;
    public ElegantNumberButton btnQuantity;
    public RelativeLayout view_background;
    public LinearLayout view_foreground;
    public ItemClickListener itemClickListener;
    public CartViewHolder(View itemView) {
        super(itemView);
        txt_card_name = itemView.findViewById(R.id.cart_item_name);
        txt_price = itemView.findViewById(R.id.cart_item_price);
        img_item_cart = itemView.findViewById(R.id.cart_item_image);
        btnQuantity = itemView.findViewById(R.id.btnQuantity);
        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById(R.id.view_foreground);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select action");
        contextMenu.add(0, 0, getAdapterPosition(), Common.DELETE);
    }
}
