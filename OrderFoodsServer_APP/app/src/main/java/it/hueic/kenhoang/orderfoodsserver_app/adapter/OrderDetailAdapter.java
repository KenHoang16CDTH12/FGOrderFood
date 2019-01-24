package it.hueic.kenhoang.orderfoodsserver_app.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.hueic.kenhoang.orderfoodsserver_app.R;
import it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder.OrderDetailViewHolder;
import it.hueic.kenhoang.orderfoodsserver_app.model.Order;

/**
 * Created by kenhoang on 08/02/2018.
 */

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailViewHolder>{
    List<Order> orders;

    public OrderDetailAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public OrderDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_food_detail, parent, false);
        return new OrderDetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrderDetailViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.name.setText(String.format("Name: %s", order.getProductName()));
        holder.quantity.setText(String.format("Quantity: %s", order.getQuantity()));
        holder.price.setText(String.format("Price: %s$", order.getPrice()));
        holder.discount.setText(String.format("Discount: %s", order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
