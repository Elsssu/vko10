package com.elssu.autohaku;

import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ListInfoActivity extends AppCompatActivity {

    private TextView infoOutput;
    private TextView yearText;
    private TextView CityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_info);

        infoOutput = findViewById(R.id.CarInfoText);
        yearText = findViewById(R.id.YearText);
        CityText = findViewById(R.id.CityText);
        String cityy = CarDataStorage.getInstance().getCity();
        int yearr = CarDataStorage.getInstance().getYear();
        yearText.setText(String.valueOf(yearr));
        CityText.setText(cityy);
        StringBuilder builder = new StringBuilder();
        ArrayList<CarData> carData= CarDataStorage.getInstance().getCarData();
        if(carData == null){
            return;
        }
        int totalAmount = 0;
        for (CarData entry : carData) {

            totalAmount += entry.getAmount();
            builder.append(entry.getType() + ": " + entry.getAmount()).append("\n");
        }

        builder.append("\n" + "Yhteensä: " + totalAmount);
        infoOutput.setText(builder.toString());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }



    public void infoDisplay(View view) {
        String data = CarDataStorage.getInstance().getCarData().toString();

        infoOutput.setText(data);

    }

    public void switchToMainMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}