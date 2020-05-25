package cn.edu.hebtu.software.listendemo.Mine.index.cardbag;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.edu.hebtu.software.listendemo.Entity.Inventory;
import cn.edu.hebtu.software.listendemo.Entity.Item;
import cn.edu.hebtu.software.listendemo.R;

public class CreditBagMineRecyclerAdapter extends RecyclerView.Adapter {
    private List<Inventory> inventories;
    private Context context;
    private int layout_item_id;

    public CreditBagMineRecyclerAdapter(List<Inventory> inventories, Context context, int layout_item_id) {
        this.inventories = inventories;
        this.context = context;
        this.layout_item_id = layout_item_id;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(layout_item_id, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        MyViewHolder holder = (MyViewHolder) viewHolder;
        Inventory inventory = inventories.get(i);
        Item item = inventory.getItem();

        holder.tvCardTagName.setText(inventory.getName());
        if (item != null) {
            holder.tvCardBagItemDescripe.setText(item.getDescription());
        }
        String date = inventory.getExpiryTime();
        String[] arr = date.split("T");
        String[] tarr = arr[1].split(".000");
        String dd = arr[0]+" "+tarr[0];
//        Log.e("过期时间",dd);
        holder.tvCardBagExpirationTime.setText(dd);
        holder.tvCreditBagUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "去使用", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null != inventories) {
            return inventories.size();
        } else {
            return 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llBackground;
        TextView tvCardTagName;
        TextView tvCardBagItemDescripe;
        TextView tvCreditBagUse;
        TextView tvCardBagExpirationTime;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            llBackground = itemView.findViewById(R.id.ll_background);
            tvCardTagName = itemView.findViewById(R.id.tv_card_bag_item_name);
            tvCardBagItemDescripe = itemView.findViewById(R.id.tv_card_bag_item_descripe);
            tvCreditBagUse = itemView.findViewById(R.id.tv_credit_bag_usetv_credit_bag_use);
            tvCardBagExpirationTime = itemView.findViewById(R.id.tv_card_bag_expiration_time);

        }
    }
}