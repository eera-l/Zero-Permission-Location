# Zero Permission Location
Even though Android apps typically require user's permission to access geolocation via GPS, Android versions older than 9 (Android Pie - API level 28) [1] allow apps to locate the user with a considerable degree of accuracy through the **WiFi Access Point BSSID** or the **mobile cell tower ID**, without needing to ask for permission. **The user might therefore not be aware of being located** [2].

This app demonstrates user geolocation via WiFi BSSID and cell tower ID. The GPS coordinates corresponding to the WiFi BSSID are retrieved thanks to Alexander Mylnikov's open source API (see credits). 

Geolocation via WiFi BSSID |  Geolocation via cell tower ID
:-------------------------:|:-------------------------:
<img src="https://www.webshare.hkr.se/FECO0002/got_wifi.jpg" alt="Geolocation via WiFi BSSID" height="500">  |  <img src="https://www.webshare.hkr.se/FECO0002/stockh_cell.jpg" alt="Geolocation via cell tower ID" height="500">

Cell tower ID geolocation is only for demonstration purposes. I could not find an API for it, so it is based only on a small portion of the 7Gb open source CSV file hosted by Alexander Mylnikov: [Mobile Towers Geolocation data](https://www.mylnikov.org/download).


It seems like not all smartphone brands give access to cell tower ID information. Samsung is one example: [G-NetTrack Android OS Netmonitor](https://www.finetopix.com/showthread.php/30787-G-NetTrack-Android-OS-Netmonitor/page2)

The exact address at selected GPS coordinates is retrieved thanks to TomTom API: [Reverse Geocode API](https://developer.tomtom.com/search-api/search-api-documentation-reverse-geocoding/reverse-geocode). 

The app was tested on a Huawei phone with **Android 4.4**.

## Credits

 - WiFi BSSID and cell tower Geolocation and API: [Public WiFi database by Alexander Mylnikov](https://www.mylnikov.org/archives/1170)
 - Map and map API: [TomTom](https://developer.tomtom.com/), [TomTom Maps SDK for Android examples](https://github.com/tomtom-international/maps-sdk-for-android-examples)
 - HTTP requests: [Volley](https://github.com/google/volley)
 - JSON parsing: [Klaxon](https://github.com/cbeust/klaxon)
 - Reading from CSV: [Kotlin-csv](https://github.com/doyaaaaaken/kotlin-csv)
 - App icon: [Smartphone signal vectors](https://www.vecteezy.com/vector-art/86322-smartphone-signal-vectors) by [Miguel Angel](https://www.vecteezy.com/members/miguelap)

## References
[1] [Android 9 - Behavior changes: all apps](https://developer.android.com/about/versions/pie/android-9.0-changes-all)<br/>
[2] [Zhou, X. et al. Identity, Location, Disease and More: Inferring Your Secrets from Android Public Resources , 2013](https://homes.luddy.indiana.edu/xw7/papers/zhou2013identity.pdf%20Your%20Secrets%20from%20Android%20Public%20Resources)
