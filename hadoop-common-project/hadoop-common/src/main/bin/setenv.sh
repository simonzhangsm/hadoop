#!/bin/bash

if [ -z "$HADOOP" ] 
then
	export HADOOP_HOME="$(cd `dirname $0`/..; pwd)"
	export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
fi

