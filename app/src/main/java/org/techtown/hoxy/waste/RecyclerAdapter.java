package org.techtown.hoxy.waste;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.hoxy.R;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    Context context;
    List<RecyclerItem> items;
    int item_layout;
    private int lastPosition = -1;

    public RecyclerAdapter(Context context, List<RecyclerItem> items, int item_layout) {
        this.context = context;
        this.items = items;
        this.item_layout = item_layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RecyclerItem item = items.get(position);
        String feeString;
        holder.image.setImageBitmap(item.getImage());
        holder.title.setText(item.getTitle());
        feeString = item.getFee() + "원";
        holder.fee.setText(feeString);
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        setAnimation(holder.image, position);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        CardView cardview;
        TextView fee;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
            fee = itemView.findViewById(R.id.fee);
        }
    }
    private void setAnimation(View viewToAnimate, int position) { // 새로 보여지는 뷰라면 애니메이션을 해줍니다
         if (position > lastPosition) { Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
         viewToAnimate.startAnimation(animation);
         lastPosition = position;
         } }
}
