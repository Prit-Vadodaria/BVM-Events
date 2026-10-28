package com.bvm.events.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bvm.events.databinding.ItemAttendeeBinding;
import com.bvm.events.model.Attendee;
import com.bvm.events.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeViewHolder> {

    private final List<Attendee> attendees = new ArrayList<>();

    public void setAttendees(List<Attendee> newAttendees) {
        attendees.clear();
        if (newAttendees != null) {
            attendees.addAll(newAttendees);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendeeBinding binding = ItemAttendeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AttendeeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        holder.bind(attendees.get(position));
    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }

    static class AttendeeViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendeeBinding binding;

        AttendeeViewHolder(ItemAttendeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Attendee attendee) {
            binding.tvAttendeeName.setText(attendee.getName());
            binding.tvAttendeeEnrollment.setText(
                    attendee.getEnrollmentNo().isEmpty() ? "N/A" : attendee.getEnrollmentNo());
            binding.tvAttendeeCheckinTime.setText(
                    "Checked in at: " + DateUtils.formatDisplayDate(attendee.getCheckInTime()));
        }
    }
}
