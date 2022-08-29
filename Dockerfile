FROM openjdk:16


COPY target/news-fetch-ms-*.jar news-fetch-ms.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=""

CMD java -jar $JAVA_TOOL_OPTIONS news-fetch-ms.jar