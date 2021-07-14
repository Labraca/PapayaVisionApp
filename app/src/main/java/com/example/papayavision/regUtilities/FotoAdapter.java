package com.example.papayavision.regUtilities;

import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.papayavision.R;
import com.example.papayavision.entidades.Foto;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.ViewHolder> {

    private List<Foto> fotos;

    public FotoAdapter(List<Foto> fotos){
        this.fotos = fotos;
    }
    @NonNull
    @NotNull
    @Override
    public FotoAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.foto_rec, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FotoAdapter.ViewHolder holder, int position) {
        holder.foto = fotos.get(position);
        holder.fotoView.setImageURI(Uri.parse(fotos.get(position).getPathImage()));
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        Foto foto;
        ImageView fotoView;
        ViewHolder(View itemView) {
            super(itemView);
            fotoView = itemView.findViewById(R.id.fotoPapaya);

        }

    }
}
