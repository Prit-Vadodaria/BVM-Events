package com.bvm.events.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bvm.events.BuildConfig;
import com.bvm.events.R;
import com.bvm.events.databinding.ItemEventBinding;
import com.bvm.events.model.Event;
import com.bvm.events.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnEventActionListener {
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final boolean isOrganizerView;
    private OnEventClickListener clickListener;
    private OnEventActionListener actionListener;

    public EventAdapter(boolean isOrganizerView) {
        this.isOrganizerView = isOrganizerView;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnEventActionListener(OnEventActionListener listener) {
        this.actionListener = listener;
    }

    public void setEvents(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventBinding binding;

        EventViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Event event) {
            Context context = itemView.getContext();

            binding.tvEventTitle.setText(event.getTitle());
            binding.tvCategoryBadge.setText(event.getCategory().toUpperCase());
            binding.tvEventVenue.setText(event.getVenueName());
            binding.tvOrganizerName.setText("By " + event.getOrganizerName());
            binding.tvAttendeeCount.setText(event.getAttendeeCount() + " attended");

            // Format start - end time
            String formattedTime = DateUtils.formatDisplayDate(event.getStartTime());
            binding.tvEventTime.setText(formattedTime);

            // Status Badge coloring
            String status = event.getStatus();
            binding.tvStatusBadge.setText(status.toUpperCase());
            if ("ongoing".equalsIgnoreCase(status)) {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#C8E6C9"));
                binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            } else if ("upcoming".equalsIgnoreCase(status)) {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#BBDEFB"));
                binding.tvStatusBadge.setTextColor(Color.parseColor("#1565C0"));
            } else {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E0E0E0"));
                binding.tvStatusBadge.setTextColor(Color.parseColor("#616161"));
            }

            // Load poster image with Glide
            String posterUrl = event.getPosterUrl();
            if (posterUrl != null && !posterUrl.trim().isEmpty()) {
                String fullUrl = BuildConfig.BASE_URL + posterUrl.replaceFirst("^/", "");
                Glide.with(context)
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_event)
                        .error(R.drawable.ic_event)
                        .into(binding.imgPoster);
            } else {
                binding.imgPoster.setImageResource(R.drawable.ic_event);
            }

            // Organizer actions
            if (isOrganizerView) {
                binding.layoutOrganizerActions.setVisibility(View.VISIBLE);
                binding.btnEditEvent.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onEditClick(event);
                });
                binding.btnDeleteEvent.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onDeleteClick(event);
                });
            } else {
                binding.layoutOrganizerActions.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onEventClick(event);
                }
            });
        }
    }
}
