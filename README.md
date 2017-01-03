hub-admin
====

REST microservice for the The Hub.

This service is responsible for content metadata and media persistence.

Ministry of Justice.
National Offender Management Service.

Endpoints
----
POST /content-items {"title":"string", "dropbox-url":"someurl"}


Examples
----
Request:

```curl -i -X POST -H 'Content-Type:application/json' -d '{"title":"foo"}' http://<host>:<port>/content-items```
```curl -i -X POST -H 'Content-Type:application/json' -d '{"title":"foo"}' http://hub-admin.herokuapp.com/content-items```
```curl -i -X POST -H 'Content-Type:application/json' -d '{"title":"foo"}' http://localhost:8080/content-items```

Response:

```
HTTP/1.1 201 
Location: http://localhost:8080/content-items/eb62aa48-96ee-474d-a8a2-5ce7deb9fcfb
Content-Length: 0
Date: Fri, 23 Dec 2016 18:50:50 GMT
```

Monitoring
----
GET /private/health

```http://localhost:8080/private/health```
```http://hub-admin.herokuapp.com/private/health```