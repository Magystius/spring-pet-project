[![Build Status](https://travis-ci.org/Magystius/spring-pet-project.svg?branch=master)](https://travis-ci.org/Magystius/spring-pet-project)
[![codecov](https://codecov.io/gh/Magystius/spring-pet-project/branch/master/graph/badge.svg)](https://codecov.io/gh/Magystius/spring-pet-project)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/df636c27882d4bc28dd7247090dc3394)](https://www.codacy.com/app/Magystius/spring-pet-project?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Magystius/spring-pet-project&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/github/license/magystius/spring-pet-project.svg)](https://github.com/magystius/spring-pet-project/blob/master/LICENSE.md)

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
      "href": "http://localhost:8080/user/59b1a63fee411e372464dd7a"
    }
  },
  "total": 3,
  "content": [
    {
      "_links": {
        "self": {
          "href": "http://localhost:8080/user/59b1a63fee411e372464dd7a"
        }
      },
      "content": {
        "id": "59b1a63fee411e372464dd7a",
        "firstName": "AWS1",
        "lastName": "AWS"
      }
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:8080/user/59b1a63fee411e372464dd7b"
        }
      },
      "content": {
        "id": "59b1a63fee411e372464dd7b",
        "firstName": "AWS4",
        "lastName": "AWS2"
      }
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:8080/user/59b1a63fee411e372464dd7c"
        }
      },
      "content": {
        "id": "59b1a63fee411e372464dd7c",
        "firstName": "Lara",
        "lastName": "Lavendel"
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
