package com.example.plantcaremanager;

import android.app.Activity;
import android.os.Bundle;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    EditText etPlantName, etPlantType, etWateringDays;
    Button btnAdd, btnDelete;
    ListView listPlants;

    DatabaseHelper dbHelper;

    ArrayList<String> plantList;
    ArrayList<Integer> plantIds;
    ArrayAdapter<String> adapter;

    int selectedPlantId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPlantName = findViewById(R.id.etPlantName);
        etPlantType = findViewById(R.id.etPlantType);
        etWateringDays = findViewById(R.id.etWateringDays);

        btnAdd = findViewById(R.id.btnAdd);
        btnDelete = findViewById(R.id.btnDelete);
        listPlants = findViewById(R.id.listPlants);

        dbHelper = new DatabaseHelper(this);

        plantList = new ArrayList<>();
        plantIds = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_activated_1,
                plantList
        );

        listPlants.setAdapter(adapter);
        listPlants.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        btnAdd.setOnClickListener(v -> addPlant());

        btnDelete.setOnClickListener(v -> deletePlant());

        listPlants.setOnItemClickListener((parent, view, position, id) -> {
            selectedPlantId = plantIds.get(position);
            listPlants.setItemChecked(position, true);

            Toast.makeText(
                    this,
                    "Plant selected",
                    Toast.LENGTH_SHORT
            ).show();
        });

        loadPlants();
    }

    private void addPlant() {
        String name = etPlantName.getText().toString().trim();
        String type = etPlantType.getText().toString().trim();
        String daysText = etWateringDays.getText().toString().trim();

        if (name.isEmpty() || type.isEmpty() || daysText.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int days;

        try {
            days = Integer.parseInt(daysText);
        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Enter a valid number",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (days <= 0) {
            Toast.makeText(
                    this,
                    "Watering interval must be greater than zero",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean inserted = dbHelper.addPlant(name, type, days);

        if (inserted) {
            Toast.makeText(
                    this,
                    "Plant added successfully",
                    Toast.LENGTH_SHORT
            ).show();

            etPlantName.setText("");
            etPlantType.setText("");
            etWateringDays.setText("");

            loadPlants();
        } else {
            Toast.makeText(
                    this,
                    "Failed to add plant",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadPlants() {
        plantList.clear();
        plantIds.clear();
        selectedPlantId = -1;

        Cursor cursor = dbHelper.getAllPlants();

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                String type = cursor.getString(
                        cursor.getColumnIndexOrThrow("type")
                );

                int days = cursor.getInt(
                        cursor.getColumnIndexOrThrow("watering_days")
                );

                plantIds.add(id);

                plantList.add(
                        name + "\nType: " + type +
                                "\nWater every " + days + " day(s)"
                );
            }
        } finally {
            cursor.close();
        }

        adapter.notifyDataSetChanged();
        listPlants.clearChoices();
    }

    private void deletePlant() {
        if (selectedPlantId == -1) {
            Toast.makeText(
                    this,
                    "Please select a plant first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean deleted = dbHelper.deletePlant(selectedPlantId);

        if (deleted) {
            Toast.makeText(
                    this,
                    "Plant deleted successfully",
                    Toast.LENGTH_SHORT
            ).show();

            loadPlants();
        } else {
            Toast.makeText(
                    this,
                    "Failed to delete plant",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
