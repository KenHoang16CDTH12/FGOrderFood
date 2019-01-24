package it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import it.hueic.kenhoang.orderfoodsserver_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoodsserver_app.R;

/**
 * Created by kenhoang on 08/02/2018.
 */

public class OrderDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView name, quantity, price, discount;
    private ItemClickListener itemClickListener;
    public OrderDetailViewHolder(View itemView) {
        super(itemView);
        name  = itemView.findViewById(R.id.product_name);
        quantity  = itemView.findViewById(R.id.product_quantity);
        price  = itemView.findViewById(R.id.product_price);
        discount  = itemView.findViewById(R.id.product_discount);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
