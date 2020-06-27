package com.example.android.weather;

/**
 * Created by apoorv on 20-Jun-20.
 */

class WeatherCondition {
    private int id;
    private double curTemp;
    // private double maxTemp, minTemp;
    private double feelsLike;
    private double pressure;
    private double windSpeed;
    private double humidity;
    private String description;
    private String cityName;
    private String weatherStatus;
    private String iconId;
    private double windDirection;

    WeatherCondition() {
        this.id = -1;
        this.curTemp = 0.0;
        this.feelsLike = 0.0;
        this.pressure = 0.0;
        this.windSpeed = 0.0;
        this.humidity = 0.0;
        this.description = "";
        this.cityName = "";
        this.weatherStatus = "";
        this.windDirection = 0.0;
    }

    WeatherCondition(int id, double curTemp, double feelsLike, double pressure, double windSpeed,
                     double humidity, String description, String cityName, String weatherStatus,
                     String iconId, double windDirection) {
        initializeObject(id, curTemp, feelsLike, pressure, windSpeed, humidity, description,
                cityName,weatherStatus,iconId,windDirection);
    }

    void initializeObject(int id, double curTemp, double feelsLike, double pressure, double windSpeed,
                     double humidity, String description, String cityName, String weatherStatus,
                     String iconId, double windDirection) {
        this.id = id;
        this.curTemp = curTemp;
        this.feelsLike = feelsLike;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.description = description;
        this.cityName = cityName;
        this.weatherStatus = weatherStatus;
        this.iconId = iconId;
        this.windDirection = windDirection;
    }

    int getId() {
        return id;
    }

    double getCurTemp() {
        return curTemp;
    }

    double getFeelsLikeTemp() {
        return feelsLike;
    }

    double getPressure() {
        return pressure;
    }

    double getWindSpeed() {
        return (windSpeed*18.0)/5.0; // from m/s to km/h
    }

    double getHumidity() {
        return humidity;
    }

    String getDescription() {
        return description;
    }

    String getCityName() {
        return cityName;
    }

    String getWeatherStatus() {
        return weatherStatus;
    }

    String getIconId() {
        return iconId;
    }

    String getWindDirection() {
        String windDirectionStr="";
        if(windDirection<23 || windDirection>=337) {
            windDirectionStr = " towards South";
        } else if(windDirection>=23 && windDirection<68) {
            windDirectionStr = " towards South-West";
        } else if(windDirection>=68 && windDirection<113) {
            windDirectionStr = " towards West";
        } else if(windDirection<=113 || windDirection>158) {
            windDirectionStr = " towards North-West";
        } else if(windDirection>=158 && windDirection<202) {
            windDirectionStr = " towards North";
        } else if(windDirection>=202 && windDirection<247) {
            windDirectionStr = " towards Noth-East";
        } else if(windDirection<=247 || windDirection>292) {
            windDirectionStr = " towards East";
        } else if(windDirection>=292 && windDirection<337) {
            windDirectionStr = " towards South-East";
        }
        return windDirectionStr;
    }

}