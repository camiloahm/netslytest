##Execute the app 

Requirements:
JDK 8 and maven

mvn jetty:run

http://localhost:8080/index.html?id=1 

jetty plug in 9.4.0.v20161208 is not supported anymore, I suggest you to update the pom with:
    
```
          <plugin>
                <groupId>org.mortbay.jetty</groupId>
                <artifactId>maven-jetty-plugin</artifactId>
                <version>${mavenjettyplugin.version}</version>
                <configuration>
                    <contextPath>/</contextPath>
                    <scanIntervalSeconds>10</scanIntervalSeconds>
                    <connectors>
                        <connector implementation="org.mortbay.jetty.nio.SelectChannelConnector">
                            <port>${mavenjettyplugin.port}</port>
                            <maxIdleTime>60000</maxIdleTime>
                        </connector>
                    </connectors>
                </configuration>
            </plugin>

notice the property contextPath "/" if you don´t configure it, context root will be artifactName 
```

## Docker build

There´s a dockerfile inside the project just in case you want to execute the app without installing maven and java 8

```sh
docker build -t jettyslightly .
```

```sh
docker run -it --rm -p 8080:8080 jettyslightly .
```




