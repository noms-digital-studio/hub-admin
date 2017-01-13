hub-admin
====

REST microservice for the The Hub.

This service is responsible for content metadata and media persistence.

Ministry of Justice.
National Offender Management Service.

Content upload endpoint
----
POST /content-items 
Multipart form endpoint that takes a 'file' and a 'title'

Examples
----
Request:

```curl -v -F title=foo -F "file=@1-pixel.png" http://localhost:8080/content-items```

Response:

```
*   Trying ::1...
* Connected to localhost (::1) port 8080 (#0)
> POST /content-items HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.43.0
> Accept: */*
> Content-Length: 1419
> Expect: 100-continue
> Content-Type: multipart/form-data; boundary=------------------------3b6c4bb238587eb9
>
< HTTP/1.1 100 Continue
< HTTP/1.1 201
< Location: http://localhost:8080/content-items/7a2b215c-0e2d-4631-8f02-59e3a7c7539c
< Content-Length: 0
< Date: Fri, 06 Jan 2017 15:39:28 GMT
<
* Connection #0 to host localhost left intact
```

Monitoring endpoint
----
GET /private/health

e.g.
```
http://localhost:8080/private/health
http://hub-admin.herokuapp.com/private/health
```

Environment variable required by the application
----
MONGODB_CONNECTION_URI - The MongoDb connection string. Defaults to mongodb://localhost:27017
e.g. mongodb:foo:<key>==@bar.documents.azure.com:10250/?ssl=true

AZURE_BLOB_STORE_CONNECTION_URI - The Azure blob store connection string. 
e.g. DefaultEndpointsProtocol=http;AccountName=<account name>;AccountKey=<key>

AZURE_BLOB_STORE_PUBLIC_URL_BASE - The base URL for items in the blob store.   
e.g. https://<account name>.blob.core.windows.net/content-items/

Environment variable required by the functional tests
----
APPLICATION_HOSTNAME - The hostname that the application is running on. Defaults to 'localhost'
e.g. hub-admin.herokuapp.com

APPLICATION_PORT - The port where the application is running. Defaults to 8080
e.g. 80
 
MONGODB_CONNECTION_URI - The MongoDb connection string. Defaults to mongodb://localhost:27017
e.g. mongodb:<user>:<key>==@bar.documents.azure.com:10250/?ssl=true



