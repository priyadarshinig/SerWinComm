package com.example.arfoodview;

import org.json.JSONException;
import org.json.JSONObject;

public class weatherData {
    private String mTemperature, micon,mcity,mweatherType;
    private int mCondition;

    public static weatherData fromJson(JSONObject jsonObject)
    {
        try
        {
            weatherData weatherD=new weatherData();
            weatherD.mcity=jsonObject.getString("name");
            weatherD.mCondition=jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherD.mweatherType=jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
            weatherD.micon=updateweatherIcon(weatherD.mCondition);
            double tempResult=jsonObject.getJSONObject("main").getDouble("temp")-273.15;
            int roundedValue=(int)Math.rint(tempResult);
            weatherD.mTemperature=Integer.toString(roundedValue);
            return weatherD;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String updateweatherIcon(int condition)
    {
        if(condition>=0 &&condition<=300)
        {
            return "thunderstorm";
        }
        else if(condition>=301 &&condition<=500)
        {
            return "lightrain";
        }
        else if(condition>=501 &&condition<=600)
        {
            return "shower";
        }
        else if(condition>=601 &&condition<=700)
        {
            return "snowy";
        }
        else if(condition>=701 &&condition<=771)
        {
            return "fog";
        }
        else if(condition>=772 &&condition<=800)
        {
            return "overcast";
        }
        else if(condition==800)
        {
            return "sunny";
        }
        else if(condition>=801 &&condition<=804)
        {
            return "cloudy";
        }
        else if(condition>=900 &&condition<=902)
        {
            return "thunderStorm";
        }
        else if(condition==903)
        {
            return "snowy";
        }
        else if(condition==904)
        {
            return "sunny";
        }
        else if(condition>=905 &&condition<=1000)
        {
            return "thunderstorm";
        }
        return "Don't know";
    }

    public String getmTemperature() {
        return mTemperature+"°C";
    }

    public String getMicon() {
        return micon;
    }

    public String getMcity() {
        return mcity;
    }

    public String getMweatherType() {
        return mweatherType;
    }
}
