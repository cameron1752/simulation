FROM ibm-semeru-runtimes:open-21-jre
WORKDIR /opt/app

# must be the shaded ("fat") jar that includes the Postgres driver, netcdf, etc.
COPY target/simulation-1.0.jar simulation-1.0.jar

# default: the web app. Trainer containers override this with their own entrypoint in docker-compose.yml
CMD ["java", "-cp", "simulation-1.0.jar", "org.example.neuralnet.WeatherPredicter"]