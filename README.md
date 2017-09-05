[![Build Status](https://travis-ci.org/Magystius/spring-val-demo.svg?branch=master)](https://travis-ci.org/Magystius/spring-val-demo)
[![codecov](https://codecov.io/gh/Magystius/spring-val-demo/branch/master/graph/badge.svg)](https://codecov.io/gh/Magystius/spring-val-demo)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/fde1f58e00ff4068b5c9b7976ad305a0)](https://www.codacy.com/app/Magystius/spring-val-demo?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Magystius/spring-val-demo&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/github/license/magystius/spring-val-demo.svg)](https://github.com/magystius/spring-val-demo/blob/master/LICENSE.md)

# Spring Pet Project

Personal pet project for a spring-based microservice

### Usage:

**GET ALL**
````
curl -i -H "Accept: application/json"  -X GET /user
````
_Response_ - `200`
````json
{
  "_links": {
    "self": {
      "href": "http://localhost:8080/user"
    },
    "start": {
      "href": "http://localhost:8080/user/599f2e1c70f9864e881e27b9"
    }
  },
  "total": 3,
  "content": [
    {
      "id": "599f2e1c70f9864e881e27b9",
      "firstName": "AWS1",
      "secondName": null,
      "lastName": "AWS",
      "age": 30,
      "vip": false,
      "login": {
        "mail": "max.mustermann@otto.de",
        "password": "somePassword"
      },
      "bio": null
    },
    {
      "id": "599f2e1d70f9864e881e27ba",
      "firstName": "AWS4",
      "secondName": null,
      "lastName": "AWS2",
      "age": 30,
      "vip": false,
      "login": {
        "mail": "max.mustermann@otto.de",
        "password": "somePassword"
      },
      "bio": null
    },
    {
      "id": "599f2e1d70f9864e881e27bb",
      "firstName": "Lara",
      "secondName": null,
      "lastName": "Lavendel",
      "age": 30,
      "vip": false,
      "login": {
        "mail": "max.mustermann@otto.de",
        "password": "somePassword"
      },
      "bio": null
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
  "_links": {
    "self": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58fe"
    },
    "start": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58fd"
    },
    "prev": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58fd"
    },
    "next": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58ff"
    }
  },
  "content": {
    "id": "599f2e6170f9864ae00c58fe",
    "firstName": "AWS4",
    "secondName": null,
    "lastName": "AWS2",
    "age": 30,
    "vip": false,
    "login": {
      "mail": "max.mustermann@otto.de",
      "password": "somePassword"
    },
    "bio": null
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
_Response_ `201 Location: http://localhost:8080/user/599f2ec570f9864ae00c5900`
`````json
{
  "_links": {
    "self": {
      "href": "http://localhost:8080/user/599f2ec570f9864ae00c5900"
    },
    "start": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58fd"
    },
    "prev": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58ff"
    }
  },
  "content": {
    "id": "599f2ec570f9864ae00c5900",
    "firstName": "AWS1",
    "secondName": null,
    "lastName": "AWS",
    "age": 30,
    "vip": false,
    "login": {
      "mail": "max.mustermann@otto.de",
      "password": "somePassword"
    },
    "bio": null
  }
}
`````

**UPDATE**
````
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X PUT /user/{userId}
````
_Body_
````json
{
     "id": "599f2ec570f9864ae00c5900",
    "firstName": "AWS10",
    "secondName": null,
    "lastName": "AWS",
    "age": 30,
    "vip": false,
    "login": {
        "mail": "max.mustermann@otto.de",
        "password": "somePassword"
    },
    "bio": null
}
````
_Response_ `200`
````json
{
  "_links": {
    "self": {
      "href": "http://localhost:8080/user/599f2ec570f9864ae00c5900"
    },
    "start": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58fd"
    },
    "prev": {
      "href": "http://localhost:8080/user/599f2e6170f9864ae00c58ff"
    }
  },
  "content": {
    "id": "599f2ec570f9864ae00c5900",
    "firstName": "AWS10",
    "secondName": null,
    "lastName": "AWS",
    "age": 30,
    "vip": false,
    "login": {
      "mail": "max.mustermann@otto.de",
      "password": "somePassword"
    },
    "bio": null
  }
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
