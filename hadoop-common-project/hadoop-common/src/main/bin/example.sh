hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.3.0-cdh5.1.0.jar pi 2 5
hadoop fs -put -f $HADOOP_HOME/bin/example/file1.txt $HADOOP_HOME/bin/example/file2.txt /data
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.3.0-cdh5.1.0.jar wordcount /data /output
