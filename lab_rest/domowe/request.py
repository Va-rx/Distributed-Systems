from requests import get, RequestException
from datetime import datetime, timedelta
from api_key import key1, key2
from helper import fahrenheit_to_celsius
from typing import List


def fetch_data(url):
    try:
        response = get(url)
        if response.status_code == 200:
            return response.json()
        else:
            print(f"External api request error: {response.status_code}")
            return None
    except RequestException as re:
        print(f"Server error: {re}")
        return None


def round_data(data: List[float]):
    return [round(number, 1) for number in data]


def get_current_weather(city: str):
    json1 = fetch_data('http://api.weatherapi.com/v1/current.json?key=' + key1 + '&q=' + city + '&aqi=no')
    json2 = fetch_data('https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/' + city + '?unitGroup=us&key=' + key2 + '&contentType=json')

    temp_c1 = json1["current"]["temp_c"]
    temp_c2 = fahrenheit_to_celsius(json2["days"][0]["temp"])

    fltemp_c1 = json1["current"]["feelslike_c"]
    fltemp_c2 = fahrenheit_to_celsius(json2["days"][0]["feelslike"])

    humidity1 = json1["current"]["humidity"]
    humidity2 = json2["days"][0]["humidity"]

    cloud1 = json1["current"]["cloud"]
    cloud2 = json2["days"][0]["cloudcover"]

    temp_avg = (temp_c1 + temp_c2) / 2
    fltemp_avg = (fltemp_c1 + fltemp_c2) / 2
    humidity = (humidity1 + humidity2) / 2
    cloud = max(cloud1, cloud2)

    temp_avg, fltemp_avg, humidity = round_data([temp_avg, fltemp_avg, humidity])

    return temp_avg, fltemp_avg, cloud, humidity


def get_current_extended_weather(city: str):
    yesterday = datetime.now() - timedelta(days=1)
    yesterday_formatted = yesterday.strftime("%Y-%m-%d")

    json1 = fetch_data('http://api.weatherapi.com/v1/history.json?key=' + key1 + '&q=' + city + '&dt=' + yesterday_formatted)
    json2 = fetch_data('https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/' + city + '?unitGroup=us&key=' + key2 + '&contentType=json')

    is_mud = json1["forecast"]["forecastday"][0]["day"]["daily_will_it_rain"]
    is_mud = bool(is_mud)

    temp_max_c = fahrenheit_to_celsius(json2["days"][0]["tempmax"])
    temp_min_c = fahrenheit_to_celsius(json2["days"][0]["tempmin"])

    sunrise = json2["days"][0]["sunrise"]
    sunset = json2["days"][0]["sunset"]

    temp_max_c, temp_min_c = round_data([temp_max_c, temp_min_c])

    return temp_min_c, temp_max_c, is_mud, sunrise, sunset


def get_forecast_weather(city: str, days: int):
    json = fetch_data('http://api.weatherapi.com/v1/forecast.json?key=' + key1 + '&q=' + city + '&days=' + str(
        days + 1) + '&aqi=no&alerts=no')

    day = [None for _ in range(days)]
    for i in range(days):
        day[i] = json["forecast"]["forecastday"][i+1]

    dates = [None for _ in range(days)]
    for i in range(days):
        dates[i] = day[i]["date"]

    hours = 5  # 6 10 14 18 22
    hour_temp = [[None for _ in range(hours)] for _ in range(days)]
    for i in range(days):
        for j in range(hours):
            hour_temp[i][j] = (day[i]["hour"][6 + j*4]["time"], day[i]["hour"][6 + j*4]["temp_c"])
    return hour_temp
