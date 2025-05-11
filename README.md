
# Weather Tracking App 🌦️

An Android weather tracking application that displays current weather based on GPS location. The app integrates with the [Visual Crossing Weather API](https://www.visualcrossing.com/weather-api) and is built using Kotlin without third-party libraries.

## Features

- Displays current weather data (temperature, condition, etc.)
- Supports swipe-to-refresh
- Fetches GPS location for weather updates
- Uses native Android libraries only

## Requirements

- Android Studio Flamingo or newer
- Android SDK 34
- Kotlin 1.9+
- Internet permission and location permission

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/EtharAlshrqawy/Weather.git
   ```
2. Open the project in Android Studio.
3. Add your Visual Crossing API key in the designated location in the code.
4. Build and run the app on an emulator or physical device.

## Usage

- On launch, the app requests location permission.
- After granting permission, current weather data is fetched and displayed.
- Pull down to refresh weather data manually.

## API Integration

This app uses the [Visual Crossing Weather API](https://www.visualcrossing.com/weather-api) to retrieve real-time weather data.


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
