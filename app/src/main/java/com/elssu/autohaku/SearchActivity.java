package com.elssu.autohaku;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private EditText cityNameInputText;

    private EditText yearInputText;

    private TextView statusOutputText;

    private Button SearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        CarDataStorage.getInstance().clearData();

        cityNameInputText = findViewById(R.id.CityNameEdit);
        yearInputText = findViewById(R.id.YearEdit);
        statusOutputText = findViewById(R.id.StatusText);
        SearchButton = findViewById(R.id.SearchButton);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

   // public void getData(Context context, String city, int year){
   //     statusOutputText.setText("Haetaan");
   //     ObjectMapper objectMapper = new ObjectMapper();

   //     JsonNode areas = null;
   //     //https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px
   //     statusOutputText.setText("Haku onnistui");

   //     return ;
    //}

   // public void searchInfo(View view) {
   //     Intent intent = new Intent(this, CarDataStorage.class);
   //     CarDataStorage.getInstance().addCarData(new CarData("Helsinki", 2011));

    //}
    public void getData(Context context, String city, int year) {
        statusOutputText.setText("Haetaan");
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode areas = null;

        try {
            areas = objectMapper.readTree(new URL("https://statfin.stat.fi/PxWeb/api/v1/en/StatFin/synt/statfin_synt_pxt_12dy.px"));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            statusOutputText.setText("Haku epäonnistui");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            statusOutputText.setText("Haku epäonnistui");
            e.printStackTrace();
        }

        //System.out.println(areas.toPrettyString());

        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        for (JsonNode node : areas.get("variables").get(1).get("values")) {
            values.add(node.asText());
        }
        for (JsonNode node : areas.get("variables").get(1).get("valueTexts")) {
            keys.add(node.asText());
        }

        HashMap<String, String> municipalityCodes = new HashMap<>();

        for(int i = 0; i < keys.size(); i++) {
            municipalityCodes.put(keys.get(i), values.get(i));
        }
        if (!municipalityCodes.containsKey(city.trim())) {
            runOnUiThread(() -> statusOutputText.setText("Haku epäonnistui"));
            return;
        }

        String code = null;
        String municipality = city;




        code = null;
        code = municipalityCodes.get(municipality);
        if( code == null){
        }
        try {
            URL url = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            JsonNode jsonInputString = objectMapper.readTree(context.getResources().openRawResource(R.raw.query));//context.getResources().openRawResource(R.raw.query));

            ((ObjectNode) jsonInputString.get("query").get(0).get("selection")).putArray("values").add(code);
            for (JsonNode node : jsonInputString.path("query")) {
                if (node.path("code").asText().equals("Vuosi")) {
                    ((ObjectNode) node.path("selection")).putArray("values").add(String.valueOf(year));
                    break;
                }
            }

            byte[] input = objectMapper.writeValueAsBytes(jsonInputString);
            OutputStream os = con.getOutputStream();
            os.write(input, 0, input.length);


            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }

            JsonNode municipalityData = objectMapper.readTree(response.toString());

            System.out.println(municipalityData.toPrettyString());

            JsonNode labels = municipalityData
                    .path("dimension")
                    .path("Ajoneuvoluokka")
                    .path("category")
                    .path("label");

            JsonNode counts = municipalityData.path("value");

            int index = 0;
            Iterator<String> ass = labels.fieldNames();

            while (ass.hasNext() && index < counts.size()) {
                String asss = ass.next();
                String name = labels.path(asss).asText();
                int count = counts.path(index).asInt();

                CarData car = new CarData(name, count);  // or include city/year if needed
                CarDataStorage.getInstance().addCarData(car);

                index++;
            }
           // JsonNode types = municipalityData.get("dimension").get("Ajoneuvoluokka").get("category").path("label");
           // JsonNode amounts = municipalityData.get("value");
           // for (int i = 1; i <= 5 && i < types.size() && i < amounts.size(); i++) {
           //     String name = types.get(i).asText();
           //     int value = i;
            //    CarData carInfo = new CarData(name, value);
            //    CarDataStorage.getInstance().addCarData(carInfo);


            //}
            CarDataStorage.getInstance().setYear(year);;
            CarDataStorage.getInstance().setCity(city);



            //ArrayList<CarData> carData = new ArrayList<>();

           // for (int i = 0; i < type.size(); i++) {
           //     carData.add(new CarData(type.get(i), Integer.valueOf(amount.get(i))));
           // }



        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            statusOutputText.setText("Haku epäonnistui");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            statusOutputText.setText("Haku epäonnistui");
            e.printStackTrace();
        }


        statusOutputText.setText("Haku onnistui");
    }

    public void searchInfo(View view){
        String cityInput = String.valueOf(cityNameInputText.getText());
        int yearInput = Integer.parseInt(String.valueOf(yearInputText.getText()));
        Context context = this;
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            double value = Double.parseDouble(String.valueOf(yearInput).trim());
        } catch (NumberFormatException e) {
            statusOutputText.setText("Haku epäonnistui");
            return;
        }
        if (!cityInput.trim().isEmpty()){

            service.execute(new Runnable() {
                @Override
                public void run() {
                    getData(context, cityInput, yearInput);

                }
            });


        }else{
            statusOutputText.setText("Haku epäonnistui");
        }
    }

    public void switchToInfoListButton(View view) {
        Intent intent = new Intent(this, ListInfoActivity.class);
        startActivity(intent);
    }

    public void switchToMainMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}