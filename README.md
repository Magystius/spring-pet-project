[![Build Status](https://travis-ci.org/Magystius/spring-val-demo.svg?branch=master)](https://travis-ci.org/Magystius/spring-val-demo)
[![Coverage Status](https://coveralls.io/repos/github/Magystius/spring-val-demo/badge.svg?branch=master)](https://coveralls.io/github/Magystius/spring-val-demo?branch=master)
[![codecov](https://codecov.io/gh/Magystius/spring-val-demo/branch/master/graph/badge.svg)](https://codecov.io/gh/Magystius/spring-val-demo)
[![codebeat badge](https://codebeat.co/badges/95aead95-65be-4bc4-874c-3faf22c04fbb)](https://codebeat.co/projects/github-com-magystius-spring-val-demo-master)
[![License](https://img.shields.io/github/license/magystius/spring-val-demo.svg)](https://github.com/magystius/spring-val-demo/blob/master/LICENSE.md)

# Spring Validation Demo

Demo App für Spring Boot

### Usage:

**GET ALL**
````
curl -i -H "Accept: application/json"  -X GET /user
````
_Response_ - `200`
````json
{
  "users": [
    {
      "id": 1,
      "firstName": "AWS",
      "lastName": "AWS",
      "age": 30,
      "vip": false,
      "login": {
        "id": 1,
        "mail": "max.mustermann@otto.de",
        "password": "somePassword"
      }
    },
    {
      "id": 2,
      "firstName": "AWS4",
      "lastName": "AWS2",
      "age": 30,
      "vip": false,
      "login": {
        "id": 2,
        "mail": "max.mustermann@otto.de",
        "password": "somePassword"
      }
    },
    {
      "id": 3,
      "firstName": "Lara",
      "lastName": "Lavendel",
      "age": 30,
      "vip": false,
      "login": {
        "id": 3,
        "mail": "max.mustermann@otto.de",
        "password": "somePassword"
      }
    }
  ]
}
````

**GET ONE**
````
curl -i -H "Accept: application/json"  -X GET /user/{userId}
````
_Response_ - `200`
````json
{
  "id": 1,
  "firstName": "AWS",
  "lastName": "AWS",
  "age": 30,
  "vip": false,
  "login": {
    "id": 1,
    "mail": "max.mustermann@otto.de",
    "password": "somePassword"
  }
}
````

**CREATE**
````
curl -i -H "Content-Type: application/json" -X POST /user
````
_Body_
````json
{
  "firstName": "AWS",
  "secondName": "Joachim",
  "lastName": "AWS",
  "age": 30,
  "vip": false,
  "login": {
  "mail": "max.mustermann@otto.de",
  "password": "somePassword"
  },
  "bio": "some text"
}
````
_Response_ `201 Location: /user/4`

**UPDATE**
````
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X PUT /user/{userId}
````
_Body_
````json
{
  "firstName": "NEU",
  "secondName": "Joachim",
  "lastName": "AWS",
  "age": 30,
  "vip": false,
  "login": {
  "mail": "max.mustermann@otto.de",
  "password": "somePassword"
  },
  "bio": "some text"
}
````
_Response_ `200`
````json
{
  "firstName": "NEU",
  "secondName": "Joachim",
  "lastName": "AWS",
  "age": 30,
  "vip": false,
  "login": {
  "mail": "max.mustermann@otto.de",
  "password": "somePassword"
  },
  "bio": "some text"
}
````
**DELETE**
````
curl -i  -X DELETE /user/{userId}
````
_Response_ - `204`

### Errors
If possible the following error object will returned on any proccessing error

_Response_ - `400`
````json
{
  "errors": [
    {
      "attribute": "user",
      "errorMessage": "Das Password ist unsicher"
    },
    {
      "attribute": "user",
      "errorMessage": "Alter muss mindestens 18 sein"
    },
    {
      "attribute": "user",
      "errorMessage": "Das Pssword ist verpflichtend"
    }
  ],
  "user": {
    "firstName": "NEU",
    "secondName": "Joachim",
    "lastName": "AWS",
    "age": 30,
    "vip": false,
    "login": {
    "mail": "max.mustermann@otto.de",
    "password": "somePassword"
    },
    "bio": "some text"
  }
}
````
