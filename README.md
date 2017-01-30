hub-admin
====

[![CircleCI](https://circleci.com/gh/NOMS-DIGITAL-STUDIO/hub-admin.svg?style=svg)](https://circleci.com/gh/NOMS-DIGITAL-STUDIO/hub-admin)

REST microservice for the The Hub.

This service is responsible for content metadata and media persistence.

Ministry of Justice.
National Offender Management Service.

Content upload endpoint
----
POST /hub-admin/content-items 
Multipart form endpoint that takes a 'file' and a 'title'

Examples
----
Request:

```curl -v -F title=foo -F -Fcategory= "file=@1-pixel.png" http://localhost:8080/hub-admin/content-items```

Response:

```
Status: 201
Location: http://localhost:8080/hub-admin/content-items/7a2b215c-0e2d-4631-8f02-59e3a7c7539c
```

Content Items list endpoint
----
GET /hub-admin/content-items

Examples
----
Request:

```curl http://localhost:8080/hub-admin/content-items```

Response:
```
{
   "contentItems:" [
      {
        "id": "588b6821aba1db4eb9d87cb3",
        "title": "aUri1",
        "mediaUri": "aTitle1",
        "filename": "hub-admin-1-pixel.png",
        "category": "aCategory1"
      },
      {
        "id": "588b6821aba1db4eb9d87cb4",
        "title": "aUri2",
        "mediaUri": "aTitle2",
        "filename": "hub-admin-2-pixel.png",
        "category": "aCategory2"
      }
    ]
}
```

Monitoring endpoint
----
GET /hub-admin/health

e.g.
```
http://localhost:8080/hub-admin/health
http://hub-admin.herokuapp.com/hub-admin/health
```

Environment variable required by the application
----
```
MONGODB_CONNECTION_URI - The MongoDb connection string. Defaults to mongodb://localhost:27017
e.g. mongodb:foo:<key>==@bar.documents.azure.com:10250/?ssl=true
```

```
AZURE_BLOB_STORE_CONNECTION_URI - The Azure blob store connection string. 
e.g. DefaultEndpointsProtocol=http;AccountName=<account name>;AccountKey=<key>
```

```
AZURE_BLOB_STORE_PUBLIC_URL_BASE - The base URL for items in the blob store.   
e.g. https://<account name>.blob.core.windows.net
```

Environment variable required by the functional tests
----
```
APPLICATION_URL - The transport, host, port and context path that the application is running on. Default to http://localhost:8080/hun-admin
e.g. https://hub-admin.herokuapp.com/hub-admin
```

```
MONGODB_CONNECTION_URI - The MongoDb connection string. Defaults to mongodb://localhost:27017
e.g. mongodb:<user>:<key>==@bar.documents.azure.com:10250/?ssl=true
```

```
AZURE_BLOB_STORE_CONNECTION_URI - The Azure blob store connection string. 
e.g. DefaultEndpointsProtocol=http;AccountName=<account name>;AccountKey=<key>
```

```
AZURE_BLOB_STORE_PUBLIC_URL_BASE - The base URL for items in the blob store.   
e.g. https://<account name>.blob.core.windows.net
```