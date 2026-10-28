package com.bvm.events.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bvm.events.databinding.ItemAttendanceBinding;
import com.bvm.events.model.AttendanceRecord;
import com.bvm.events.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AttendanceRecord record);
    }

    private final List<AttendanceRecord> records = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<AttendanceRecord> newRecords) {
        records.clear();
        if (newRecords != null) {
            records.addAll(newRecords);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceBinding binding = ItemAttendanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AttendanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceBinding binding;

        AttendanceViewHolder(ItemAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AttendanceRecord record) {
            binding.tvAttendanceTitle.setText(record.getTitle());
            binding.tvAttendanceCategory.setText(record.getCategory().toUpperCase());
            binding.tvAttendanceVenue.setText("Venue: " + record.getVenueName());
            binding.tvCheckInTime.setText("Checked in: " + DateUtils.formatDisplayDate(record.getCheckInTime()));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(record);
            });
        }
    }
}
