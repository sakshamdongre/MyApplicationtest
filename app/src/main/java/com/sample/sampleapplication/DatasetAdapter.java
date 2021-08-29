package com.sample.sampleapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

class DatasetAdapter extends RecyclerView.Adapter<DatasetAdapter.DatasetHolder> {

    private Context context;
    private List<String> imageList;
    private List<String> tagList;

    public DatasetAdapter(Context context) {
        this.context = context;
        imageList = new ArrayList<>();
        tagList = new ArrayList<>();
    }

    @NonNull
    @Override
    public DatasetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dataset, parent, false);
        return new DatasetHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DatasetHolder holder, int position) {
        Glide.with(context).load(imageList.get(position)).into(holder.imageView);
        if (tagList.size() > 0)
            holder.imageName.setText(tagList.get(position));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void updateDataset(ArrayList<String> updatedImageList, ArrayList<String> updatedTagList) {
        imageList.clear();
        tagList.clear();
        tagList.addAll(updatedTagList);
        imageList.addAll(updatedImageList);
        notifyDataSetChanged();
    }

    public class DatasetHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView imageName;

        public DatasetHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_dataset);
            imageName = itemView.findViewById(R.id.image_name);
        }
    }
}
