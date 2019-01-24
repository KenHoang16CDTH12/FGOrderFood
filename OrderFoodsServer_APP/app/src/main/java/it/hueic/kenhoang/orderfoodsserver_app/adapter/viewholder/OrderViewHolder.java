package it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import it.hueic.kenhoang.orderfoodsserver_app.R;

/**
 * Created by kenhoang on 28/01/2018.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress;
    public Button btnEdit, btnRemove, btnDetail, btnDiretion;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderAddress = itemView.findViewById(R.id.order_address);

        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnDetail = itemView.findViewById(R.id.btnDetail);
        btnRemove = itemView.findViewById(R.id.btnRemove);
        btnDiretion = itemView.findViewById(R.id.btnDirection);
    }

}
