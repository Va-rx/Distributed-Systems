from requests import get
from datetime import datetime, timedelta
from api_key import key1, key2
from helper import fahrenheit_to_celsius


def get_current_weather(city: str):
    response1 = get('http://api.weatherapi.com/v1/current.json?key=' + key1 + '&q=' + city + '&aqi=no')
    response2 = get('https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/' + city + '?unitGroup=us&key=' + key2 + '&contentType=json')

    json1 = response1.json()
    json2 = response2.json()

    temp_c1 = json1["current"]["temp_c"]
    temp_c2 = fahrenheit_to_celsius(json2["days"][0]["temp"])

    fltemp_c1 = json1["current"]["feelslike_c"]
    fltemp_c2 = fahrenheit_to_celsius(json2["days"][0]["feelslike"])

    cloud1 = json1["current"]["cloud"]
    cloud2 = json2["days"][0]["cloudcover"]

    humidity1 = json1["current"]["humidity"]
    humidity2 = json2["days"][0]["humidity"]

    temp_avg = round((temp_c1 + temp_c2) / 2, 1)
    fltemp_avg = round((fltemp_c1 + fltemp_c2) / 2, 1)
    cloud = max(cloud1, cloud2)
    humidity = round((humidity1 + humidity2) / 2, 1)

    return temp_avg, fltemp_avg, cloud, humidity


def get_current_extended_weather(city: str):
    yesterday = datetime.now() - timedelta(days=1)
    response1 = get('http://api.weatherapi.com/v1/history.json?key=' + key1 + '&q=' + city + '&dt=' + yesterday.strftime("%Y-%m-%d"))
    response2 = get('https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/' + city + '?unitGroup=us&key=' + key2 + '&contentType=json')

    json1 = response1.json()
    json2 = response2.json()

    is_mud = json1["forecast"]["forecastday"][0]["day"]["daily_will_it_rain"]
    if is_mud == 1:
        is_mud = True
    else:
        is_mud = False

    temp_max_c = round(fahrenheit_to_celsius(json2["days"][0]["tempmax"]), 1)
    temp_min_c = round(fahrenheit_to_celsius(json2["days"][0]["tempmin"]), 1)
    sunrise = json2["days"][0]["sunrise"]
    sunset = json2["days"][0]["sunset"]

    return temp_min_c, temp_max_c, is_mud, sunrise, sunset



def get_forecast_weather(city: str):
    pass