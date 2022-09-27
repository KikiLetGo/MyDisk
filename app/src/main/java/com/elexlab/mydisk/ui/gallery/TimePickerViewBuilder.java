package com.elexlab.mydisk.ui.gallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.FormatUtils;
import com.elexlab.mydisk.R;


/**
 * Created by BruceYoung on 10/18/17.
 */
public class TimePickerViewBuilder {
    public interface OnPickListener{
        void onConfirmed(int day,int hour,int min);
    }
    public Dialog buildView(Context context,OnPickListener listener){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        final View contentView = LayoutInflater.from(context).inflate(R.layout.view_time_picker,null);
        builder.setView(contentView);
        final Dialog dialog = builder.create();
        TextView tvDayAdd = (TextView) contentView.findViewById(R.id.tvDayAdd);
        EditText etDay = contentView.findViewById(R.id.etDay);
        TextView tvDayMin = (TextView) contentView.findViewById(R.id.tvDayMin);
        TextView tvHourAdd = (TextView) contentView.findViewById(R.id.tvHourAdd);
        EditText etHour =  contentView.findViewById(R.id.etHour);
        TextView tvHourMin = (TextView) contentView.findViewById(R.id.tvHourMin);
        TextView tvMinAdd = (TextView) contentView.findViewById(R.id.tvMinAdd);
        EditText etMin = contentView.findViewById(R.id.etMin);
        TextView tvMinMin = (TextView) contentView.findViewById(R.id.tvMinMin);

        tvDayAdd.setOnClickListener((View view)->{
            etDay.setText(String.valueOf(Integer.parseInt(String.valueOf(etDay.getText()))+1));
        });

        tvDayMin.setOnClickListener((View view)->{
            etDay.setText(String.valueOf(Integer.parseInt(String.valueOf(etDay.getText()))-1));
        });

        tvHourAdd.setOnClickListener((View view)->{
            etHour.setText(String.valueOf(Integer.parseInt(String.valueOf(etHour.getText()))+1));
        });

        tvHourMin.setOnClickListener((View view)->{
            etHour.setText(String.valueOf(Integer.parseInt(String.valueOf(etHour.getText()))-1));
        });


        tvMinAdd.setOnClickListener((View view)->{
            etMin.setText(String.valueOf(Integer.parseInt(String.valueOf(etMin.getText()))+1));
        });

        tvMinMin.setOnClickListener((View view)->{
            etMin.setText(String.valueOf(Integer.parseInt(String.valueOf(etMin.getText()))-1));
        });

        contentView.findViewById(R.id.btnCancel).setOnClickListener((View v)->{
            dialog.dismiss();
        });

        contentView.findViewById(R.id.btnConfirm).setOnClickListener((View v)->{
            dialog.dismiss();
            if(listener != null){
                int day = Integer.parseInt(String.valueOf(etDay.getText()));
                int hour = Integer.parseInt(String.valueOf(etHour.getText()));
                int min = Integer.parseInt(String.valueOf(etMin.getText()));
                listener.onConfirmed(day,hour,min);
            }
        });




        return dialog;
    }

}
