[![Build Status](https://travis-ci.org/Magystius/spring-pet-project.svg?branch=master)](https://travis-ci.org/Magystius/spring-pet-project)
[![codecov](https://codecov.io/gh/Magystius/spring-pet-project/branch/master/graph/badge.svg)](https://codecov.io/gh/Magystius/spring-pet-project)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/df636c27882d4bc28dd7247090dc3394)](https://www.codacy.com/app/Magystius/spring-pet-project?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Magystius/spring-pet-project&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/github/license/magystius/spring-pet-project.svg)](https://github.com/magystius/spring-pet-project/blob/master/LICENSE.md)
[![Docker Stars](https://img.shields.io/docker/stars/tdekarz/spring-pet-project.svg)](https://hub.docker.com/r/tdekarz/spring-pet-project/)
[![Docker Pulls](https://img.shields.io/docker/pulls/tdekarz/spring-pet-project.svg)](https://hub.docker.com/r/tdekarz/spring-pet-project/)

# Spring Pet Project

Personal pet project for a spring-based microservice

## Requirements

- Java 21+ (requires JVM arguments for module compatibility)
- Maven 3.6+

## Running the Application

### Local Development

```bash
# Build the application
mvn clean compile

# Run with required JVM arguments for Java 21 compatibility
MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED" mvn spring-boot:run

# Alternative: Package and run JAR
mvn clean package
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -jar target/spring-pet-project-0.0.0.jar
```

### Docker

This project is available on **dockerhub**:
````
docker run -p 8080:8080/tcp tdekarz/spring-pet-project:latest
````

## Authentication

The application uses HTTP Basic Authentication with the following predefined users:

- **admin/admin** - ADMIN role (access to all endpoints)
- **user/user** - USER role (access to /user and /group endpoints)  
- **monitoring/monitoring** - MONITORING role (access to /internal endpoints)

## API Endpoints

- `/user` - User management (requires USER or ADMIN role)
- `/group` - Group management (requires USER or ADMIN role)
- `/internal/health` - Health check (requires MONITORING or ADMIN role)
- `/internal/info` - Application info (requires MONITORING or ADMIN role)
- `/internal/metrics` - Application metrics (requires MONITORING or ADMIN role)

### Usage:

**GET ALL**
````
curl -i -H "Accept: application/json" -u admin:admin -X GET http://localhost:8080/user
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
curl -i -H "Accept: application/json" -u admin:admin -X GET http://localhost:8080/user/{userId}
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
curl -i -H "Content-Type: application/json" -u admin:admin -X POST http://localhost:8080/user
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
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -u admin:admin -X PUT http://localhost:8080/user/{userId}
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
curl -i -u admin:admin -X DELETE http://localhost:8080/user/{userId}
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
