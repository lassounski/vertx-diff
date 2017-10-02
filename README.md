# vertx-diff
This is a web application that makes a diff of two files.
* If the files have the same length -> they are equal
* If the files differ in length -> they are not equal
* If the files have the same length and different content -> an array of position and offset will be returned indicating where the differences are

The files are stored in MongoDb that is started by using docker (instructions below).
Docker runs on its default `27017` port and it must be started before running the application and the integration tests.
 
There are three endpoints:
* POST `http://localhost:4004/v1/diff/{id}/left` -> to upload the left document
* POST `http://localhost:4004/v1/diff/{id}/right` -> to upload the right document
* GET `http://localhost:4004/v1/diff/{id}` -> to make the diff

In the POST methods the body must be `application/json` and have one attribute `text`. This attribute
must have the text encoded in B64.

## Starting MongoDB
`$ docker pull mongo`

`$ docker run -d -p 27017:27017 mongo`

## Running the tests
`$ mvn clean verify`

This will run both unit tests and integration tests

## Starting application
`$ cd diff-app; mvn vertx:run`


