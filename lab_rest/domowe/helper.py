def fahrenheit_to_celsius(fahr):
    return (fahr - 32) * 5 / 9


def generate_paragraph(days_hours_temps):
    paragraphs = ""
    n = len(days_hours_temps)
    m = len(days_hours_temps[0])
    for i in range(n):
        for j in range(m):
            paragraphs += f"<p>Date: {days_hours_temps[i][j][0]}, temp: {days_hours_temps[i][j][1]}C</p>"
        paragraphs += "<br></br>"
    return paragraphs
