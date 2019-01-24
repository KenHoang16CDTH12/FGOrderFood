package it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.R;

/**
 * Created by kenhoang on 23/02/2018.
 */

public class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView tvFoodName, tvFoodPrice;
    public KenBurnsView imgFood;
    public ImageView imgFav, imgShare, btnQuickCart;
    public RelativeLayout view_background;
    public LinearLayout view_foreground;
    private ItemClickListener itemClickListener;
    public FavoriteViewHolder(View itemView) {
        super(itemView);
        tvFoodName  = itemView.findViewById(R.id.food_name);
        tvFoodPrice = itemView.findViewById(R.id.food_price);
        imgFood  = itemView.findViewById(R.id.food_image);
        imgFav  = itemView.findViewById(R.id.fav);
        imgShare = itemView.findViewById(R.id.share);
        btnQuickCart = itemView.findViewById(R.id.btnQuickCart);
        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById(R.id.view_foreground);

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
