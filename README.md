hub-admin
====

REST microservice for the The Hub.

This service is responsible for content metadata and media persistence.

Ministry of Justice.
National Offender Management Service.

Endpoints
----
POST /articles 

{"title":"string"}

Example
----
Request:

```curl -i -X POST -H 'Content-Type:application/json' -d '{"title":"foo"}' http://<host>:<port>/articles```

Response:

```
HTTP/1.1 201 
Location: http://localhost:8080/articles/eb62aa48-96ee-474d-a8a2-5ce7deb9fcfb
Content-Length: 0
Date: Fri, 23 Dec 2016 18:50:50 GMT```
