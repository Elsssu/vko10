package com.elssu.autohaku;

import java.util.ArrayList;

import javax.sql.DataSource;

public class CarDataStorage {

    private String city;
    private int year;

    private ArrayList<CarData> carData = new ArrayList<>();

    private static CarDataStorage Datastorage = null;

    private CarDataStorage() {

    }
    private CarDataStorage(String city, int year){
        this.city = city;
        this.year = year;
    }


    static public CarDataStorage getInstance() {
        if(Datastorage == null) {
            Datastorage = new CarDataStorage();
        }
        return Datastorage;
    }

    public ArrayList<CarData> getCarData() {
        return carData;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }


    public void addCarData(CarData carData){ this.carData.add(carData);

    }
    public void clearData(){
        carData.clear();
    }
}
