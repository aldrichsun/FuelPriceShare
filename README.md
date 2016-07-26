# FuelPriceShare
FuelPriceShare Android app mobile end code

This server side code contains servlets, whose functions are as follows.
1. LoginServlet: in charge of log in
2. RegisterServlet: for registration in the system
3. RangeQueryServlet: given a location (lat, lng), this query returns the fuel stations within a distance range
4. PathQueryServlet: to find the fuel stations along a direction path
5. LocationHistoryServlet: process (stores and returns) the user's search history, which forms part of the search auto-complete
6. UploadImageServlet: handle image upload request
7. FuelPriceImageProcessServlet: receive the uploaded fuel price image, and process it to extract the fuel infomation including price and type, and retrieve petro station the user currently located at
8. UploadRefinedResultServlet: receive the refined fuel result from the user
9. CouchDBHandlerServlet and CrudServlet: response to the requests to create, retrieve, update and delete data in the underlying database
