import requests
from fastapi import FastAPI, Query
from fastapi.responses import HTMLResponse
from datetime import datetime, timedelta
from api_keys import key1, key2

app = FastAPI()

def get_current_avg_weather_raw(city: str):
    response1 = requests.get('http://api.weatherapi.com/v1/current.json?key=' + key1 + '&q=' + city + '&aqi=no')
    response2 = requests.get(
        'https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/' + city + '?unitGroup=us&key=' + key2 + '&contentType=json')
    json1 = response1.json()
    json2 = response2.json()
    temp_c1 = json1["current"]["temp_c"]
    temp_c2 = (json2["days"][0]["temp"] - 32) * 5 / 9

    fltemp_c1 = json1["current"]["feelslike_c"]
    fltemp_c2 = (json2["days"][0]["feelslike"] - 32) * 5 / 9

    cloud1 = json1["current"]["cloud"]
    cloud2 = json2["days"][0]["cloudcover"]

    humidity1 = json1["current"]["humidity"]
    humidity2 = json2["days"][0]["humidity"]

    yesterday = datetime.now() - timedelta(days=1)
    response3 = requests.get('http://api.weatherapi.com/v1/history.json?key=' + key1 + '&q=' + city + '&dt=' + yesterday.strftime("%Y-%m-%d"))
    json3 = response3.json()
    temp_avg = (temp_c1 + temp_c2) / 2
    fltemp_avg = (fltemp_c1 + fltemp_c2) / 2
    cloud = max(cloud1, cloud2)
    humidity = (humidity1 + humidity2) / 2
    is_mud = json3["forecast"]["forecastday"][0]["day"]["daily_will_it_rain"]
    return temp_avg, fltemp_avg, cloud, humidity, is_mud


@app.get("/", response_class=HTMLResponse)
async def root():
    return """
        <html>
            <head>
                <title>Weather App</title>
            </head>
            <body>
                <form method="get">
                    <label for="city">Enter city name:</label><br>
                    <input type="text" id="city" name="city"><br><br>
                    <button type="submit" formaction="/current_weather">Current Weather</button>
                    <button type="submit" formaction="/forecast_weather">Forecast Weather</button>
                </form>
            </body>
        </html>
    """


@app.get("/current_weather", response_class=HTMLResponse)
async def current_weather(city: str = Query(..., title="city")):
    temp, fltemp, cloud, humidity, is_mud = get_current_avg_weather_raw(city)
    temp = round(temp, 1)
    fltemp = round(fltemp, 1)
    humidity = round(humidity, 1)
    if is_mud == 1:
        is_mud = True
    else:
        is_mud = False
    return f"""
    <h1>Current weather Information for {city}</h1>
    <p>Temperature: {temp}C</p>
    <p>Feels like temperature: {fltemp}C</p>
    <p>Clouds: {cloud}%</p>
    <p>Humidity: {humidity}</p>
    <p>Mud: {is_mud}</p>
    
    <form action="/current_weather/extended" method="get">
    <input type="hidden" name="city" value="{ city }">
    <button type="submit">Extended details</button>
    </form>
    """


@app.get("/current_weather/extended", response_class=HTMLResponse)
async def current_weather_extended(city: str = Query(..., title="city")):
    return f"""
    <h1>Extended Current weather Information for {city}</h1>
    <p>{city}</p>
    """
