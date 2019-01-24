package it.hueic.kenhoang.orderfoods_app.Interface;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by kenhoang on 22/02/2018.
 */

public interface RecyclerItemTouchHelperListtener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
