Lost & Found App
A Lost & Found Android application built with Kotlin that allows users to post and browse lost or found items in their local area.

Users can create adverts for lost or found items, browse all current listings, search by keyword, and filter by category. 
Each post includes a photo, contact details, and an automatic timestamp showing how recently it was posted. Once an item has been returned to its owner, the advert can be removed from the list. 
All data is stored locally on the device using Room, meaning listings persist between app sessions without requiring an internet connection.

The app uses Kotlin with Room Database for local SQLite storage, Jetpack Navigation Component for screen transitions, and follows MVVM architecture using ViewModel and LiveData to keep the UI in sync with the database automatically.
