package com.bvm.events.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bvm.events.BuildConfig;
import com.bvm.events.R;
import com.bvm.events.databinding.ItemGalleryImageBinding;
import com.bvm.events.model.EventImage;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {

    private final List<EventImage> images = new ArrayList<>();

    public void setImages(List<EventImage> newImages) {
        images.clear();
        if (newImages != null) {
            images.addAll(newImages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryImageBinding binding = ItemGalleryImageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemGalleryImageBinding binding;

        ImageViewHolder(ItemGalleryImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(EventImage image) {
            Context context = itemView.getContext();
            String fullUrl = BuildConfig.BASE_URL + image.getImageUrl().replaceFirst("^/", "");

            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_event)
                    .error(R.drawable.ic_event)
                    .into(binding.imgGalleryItem);
        }
    }
}

