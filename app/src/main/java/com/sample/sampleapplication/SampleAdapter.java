package com.sample.sampleapplication;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleViewHolder> {

    private List<SampleModel> items = new ArrayList<>();
    private ArrayList<SampleModel> temp = new ArrayList<>();
    private SparseBooleanArray itemStateArray= new SparseBooleanArray();
    private Boolean isSearchActive = false;

    SampleAdapter() {

    }

    @Override
    public SampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_item, parent, false);
        return new SampleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SampleViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (isSearchActive) {
            return temp.size();
        }
        return items.size();
    }

    void loadItems(List<SampleModel> tournaments) {
        this.items = tournaments;
        notifyDataSetChanged();
    }

    public void filter(String string) {
        if (string.equals("")){
            isSearchActive = false;
        } else {
            isSearchActive = true;
            items.clear();
            for(SampleModel d: items) {
                if (d.getText().toLowerCase().contains(string)) {
                    temp.add(d);
                }
            }
        }
        notifyDataSetChanged();
    }


    class SampleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CheckedTextView mCheckedTextView;

        SampleViewHolder(View itemView) {
            super(itemView);
            mCheckedTextView = itemView.findViewById(R.id.checked_text_view);
            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            if (!itemStateArray.get(position, false)) {
                mCheckedTextView.setChecked(false);}
            else {
                mCheckedTextView.setChecked(true);
            }
            mCheckedTextView.setText(items.get(position).getText());
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (!itemStateArray.get(adapterPosition, false)) {
                mCheckedTextView.setChecked(true);
                itemStateArray.put(adapterPosition, true);
            } else  {
                mCheckedTextView.setChecked(false);
                itemStateArray.put(adapterPosition, false);
            }
        }

    }
}

