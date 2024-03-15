from fastapi import FastAPI, Query
from fastapi.responses import HTMLResponse
import request

app = FastAPI()


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
    temp, fltemp, cloud, humidity = request.get_current_weather(city)
    return f"""
    <h1>Current weather information for {city}</h1>
    <p>Temperature: {temp}C</p>
    <p>Feels like temperature: {fltemp}C</p>
    <p>Clouds: {cloud}%</p>
    <p>Humidity: {humidity}</p>
    
    <form action="/current_weather/extended" method="get">
    <input type="hidden" name="city" value="{city}">
    <button type="submit">Extended details</button>
    </form>
    """


@app.get("/current_weather/extended", response_class=HTMLResponse)
async def current_weather_extended(city: str = Query(..., title="city")):
    temp_min_c, temp_max_c, is_mud, sunrise, sunset = request.get_current_extended_weather(city)

    return f"""
    <h1>Today's weather information for {city}</h1>
    <p>Min temperature: {temp_min_c}C</p>
    <p>Max temperature: {temp_max_c}C</p>
    <p>Mud: {is_mud}</p>
    <p>Sunrise: {sunrise}</p>
    <p>Sunset: {sunset}</p>
    """

@app.get("/forecast_weather", response_class=HTMLResponse)
async def forecast_weather(city: str = Query(..., title="city")):
    pass
