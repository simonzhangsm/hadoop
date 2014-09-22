hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.5.1.jar pi 2 5
hadoop fs -put -f $HADOOP_HOME/share/example/file1.txt $HADOOP_HOME/share/example/file2.txt /data
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.5.1.jar wordcount /data /output
