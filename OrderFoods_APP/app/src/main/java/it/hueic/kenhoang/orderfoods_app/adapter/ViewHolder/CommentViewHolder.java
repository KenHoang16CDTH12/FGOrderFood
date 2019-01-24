package it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import it.hueic.kenhoang.orderfoods_app.R;

/**
 * Created by kenhoang on 13/02/2018.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {
    public TextView tvUserPhone, tvComment, timeComment;
    public RatingBar ratingBar;
    public CommentViewHolder(View itemView) {
        super(itemView);
        tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
        tvComment = itemView.findViewById(R.id.tvComment);
        ratingBar = itemView.findViewById(R.id.ratingBarByPhone);
        timeComment = itemView.findViewById(R.id.timeComment);
    }
}
