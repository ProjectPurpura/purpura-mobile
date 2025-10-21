package com.purpura.app.adapters.postgres;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.postgres.News;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.remote.service.PostgresService;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<News> news;

    public NewsAdapter(List<News> listNews) {
        this.news = listNews != null ? listNews : new ArrayList<>();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news2 = news.get(position);

        Glide.with(holder.newsImage.getContext())
                .load(news2.getUrlImagem())
                .into(holder.newsImage);

        holder.newsTitle.setText(news2.getTitulo());

        holder.newsButton.setOnClickListener(v -> {
            String url = news2.getLink();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            holder.newsButton.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return news != null ? news.size() : 0;
    }

    public void updateList(List<News> listNews) {
        this.news = listNews != null ? listNews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        ImageView newsImage;
        TextView newsTitle;
        Button newsButton;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.notificationCardImage);
            newsTitle = itemView.findViewById(R.id.notificationCardTitle);
            newsButton = itemView.findViewById(R.id.notificationCardSeeNews);
        }
    }
}
