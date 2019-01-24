package it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;

/**
 * Created by kenhoang on 27/01/2018.
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView tvFoodName, tvFoodPrice;
    public KenBurnsView imgFood;
    public ImageView imgFav, imgShare, btnQuickCart;
    private ItemClickListener itemClickListener;
    public FoodViewHolder(View itemView) {
        super(itemView);
        tvFoodName  = itemView.findViewById(R.id.food_name);
        tvFoodPrice = itemView.findViewById(R.id.food_price);
        imgFood  = itemView.findViewById(R.id.food_image);
        imgFav  = itemView.findViewById(R.id.fav);
        imgShare = itemView.findViewById(R.id.share);
        btnQuickCart = itemView.findViewById(R.id.btnQuickCart);

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
