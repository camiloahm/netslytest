##Execute the app 

Requirements:
JDK 8 and maven

mvn jetty:run

http://localhost:8080/index.html?id=1 


## Docker build

There´s a dockerfile inside the project just in case you want to execute the app without installing maven and java 8

```sh
docker build -t jettyslightly .
```

```sh
docker run -it --rm -p 8080:8080 jettyslightly .
```




