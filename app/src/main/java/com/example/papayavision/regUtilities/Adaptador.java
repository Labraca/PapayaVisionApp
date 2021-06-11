package com.example.papayavision.regUtilities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.papayavision.MainActivity;
import com.example.papayavision.R;
import com.example.papayavision.RegDetalles;
import com.example.papayavision.entidades.Registro;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Adaptador extends ListAdapter<Registro, Adaptador.ViewHolder>{


    public Adaptador(@NonNull DiffUtil.ItemCallback<Registro> diffCallback) {
        super(diffCallback);
       };

    private final View.OnClickListener clk = new ClickRegListener();
    public class ClickRegListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            RecyclerView rv = ((View)view.getParent()).findViewById(R.id.recycled_RegSem);
            int itemPos = rv.getChildLayoutPosition(view);
            Context c = view.getContext();
            Registro reg = getItem(itemPos);
            Intent i = new Intent(c, RegDetalles.class);
            i.putExtra("idReg",reg.getId());
            c.startActivity(i);
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView volumen;
        private final TextView fechas;
        public ViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            this.volumen = (TextView) itemView.findViewById(R.id.idVolumen);
            this.fechas = (TextView) itemView.findViewById(R.id.idFechas);
        }
        public void bindVol (String text){
            volumen.setText(text);
        }

        public void bindFech (String text){
            fechas.setText(text);
        }
        static ViewHolder create(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reg_lista, parent, false);
            return new ViewHolder(view);
        }

    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reg_lista, parent, false);
        view.setOnClickListener(clk);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull Adaptador.ViewHolder holder, int position) {
        Date fechaActual = getItem(position).getInicioFecha();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaActual);
        cal.add(Calendar.DAY_OF_WEEK, 6);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String fechas = sdf.format(fechaActual)
                + "-"
                + sdf.format(cal.getTime());

        holder.bindFech(fechas);
        holder.bindVol(getItem(position).getVolumen()+"t");
    }
    public static class RegDiff extends DiffUtil.ItemCallback<Registro> {

        @Override
        public boolean areItemsTheSame(@NonNull Registro oldItem, @NonNull Registro newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Registro oldItem, @NonNull Registro newItem) {
            return (oldItem.getId() == newItem.getId());
        }
    }
}
