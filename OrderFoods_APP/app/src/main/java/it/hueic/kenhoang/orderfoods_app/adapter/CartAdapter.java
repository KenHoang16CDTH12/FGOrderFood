package it.hueic.kenhoang.orderfoods_app.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.hueic.kenhoang.orderfoods_app.CartActivity;
import it.hueic.kenhoang.orderfoods_app.R;
import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.CartViewHolder;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.model.Order;

/**
 * Created by kenhoang on 28/01/2018.
 */

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {
    private List<Order> listData = new ArrayList<>();
    private CartActivity activity;

    public CartAdapter(List<Order> listData, CartActivity activity) {
        this.listData = listData;
        this.activity = activity;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View itemView = inflater.inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        holder.btnQuantity.setNumber(String.valueOf(listData.get(position).getQuantity()));
        holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(activity).updateCart(order);

                //Calculate total price
                List<Order> carts = new Database(activity).getCarts(Common.currentUser.getPhone());
                int total = 0;
                for (Order orderElement: carts)
                    total += (Integer.parseInt(orderElement.getPrice())) * (Integer.parseInt(orderElement.getQuantity()));
                Locale locale = new Locale("en", "US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                activity.tvTotalPrice.setText(fmt.format(total));
            }
        });
        int price = (Integer.parseInt(listData.get(position).getPrice())) *
                (Integer.parseInt(listData.get(position).getQuantity()));
        holder.txt_price.setText(fmt.format(price));
        holder.txt_card_name.setText(listData.get(position).getProductName());

        Picasso.get()
                .load(listData.get(position).getImage())
                .into(holder.img_item_cart);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public Order getItem(int position) {
        return listData.get(position);
    }

    public void removeItem(int position) {
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position) {
        listData.add(position, item);
        notifyItemInserted(position);
    }
}
