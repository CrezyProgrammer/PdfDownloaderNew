package com.masum.pdfdownloader;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.masum.pdfdownloader.databinding.ReyclerLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public final class RecyclerItemAdapter extends RecyclerView.Adapter<RecyclerItemAdapter.ViewHolder> {
    private final ArrayList<Item> items;
    private final MainActivity context;

    public RecyclerItemAdapter(ArrayList<Item> items, MainActivity context) {
        this.items = items;
        this.context = context;
    }

    @NotNull
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reycler_layout, parent, false));
    }




    @Override
    public  void onBindViewHolder(@NotNull RecyclerItemAdapter.ViewHolder holder, int position) {
        ReyclerLayoutBinding binding = ReyclerLayoutBinding.bind(holder.itemView);
        Item item = items.get(position);

        binding.title.setText(item.getName());
        File file = new File((holder.itemView.getContext().getDataDir().toString())+"/"+(item.getName())+ ".pdf");

        if (file.exists()) {
            binding.downIcon.setImageDrawable(holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_baseline_cloud_download_24_color));
        } else {
         binding.downIcon.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.ic_baseline_cloud_download_24));
        }

        binding.main.setOnClickListener((View.OnClickListener)(it -> {
            ItemClick itemClick = (ItemClick)context;
            itemClick.itemClick(item);
        }));
    }


    public int getItemCount() {
        return this.items.size();
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NotNull View itemView) {

            super(itemView);
        }
    }
}
