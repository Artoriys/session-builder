FROM apache/spark:v3.2.2
COPY ./build/libs/session-builder-1.0-SNAPSHOT-all.jar /app/app.jar
COPY ./spark-data /opt/spark/work-dir/spark-data
ENV SPARK_USER root
ENV HADOOP_USER root
USER root
EXPOSE 4040
ENTRYPOINT /opt/spark/bin/spark-submit --class org.session.app.SessionSparkJob --conf spark.jars.ivy=/tmp/.ivy /app/app.jar