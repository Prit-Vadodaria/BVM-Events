package com.bvm.events.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bvm.events.databinding.ItemPendingOrganizerBinding;
import com.bvm.events.model.User;
import com.bvm.events.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class PendingOrganizerAdapter extends RecyclerView.Adapter<PendingOrganizerAdapter.ViewHolder> {

    public interface OnOrganizerActionListener {
        void onApprove(User user);
        void onReject(User user);
    }

    private final List<User> organizers = new ArrayList<>();
    private final OnOrganizerActionListener listener;

    public PendingOrganizerAdapter(OnOrganizerActionListener listener) {
        this.listener = listener;
    }

    public void setOrganizers(List<User> newOrganizers) {
        organizers.clear();
        if (newOrganizers != null) {
            organizers.addAll(newOrganizers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPendingOrganizerBinding binding = ItemPendingOrganizerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(organizers.get(position));
    }

    @Override
    public int getItemCount() {
        return organizers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPendingOrganizerBinding binding;

        ViewHolder(ItemPendingOrganizerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.tvOrganizerName.setText(user.getName());
            binding.tvOrganizerEmail.setText(user.getEmail());

            if (user.getCreatedAt() != null && !user.getCreatedAt().isEmpty()) {
                binding.tvOrganizerRegisteredAt.setText("Registered: " + DateUtils.formatDisplayDate(user.getCreatedAt()));
            } else {
                binding.tvOrganizerRegisteredAt.setText("");
            }

            binding.btnApprove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(user);
                }
            });

            binding.btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(user);
                }
            });
        }
    }
}
