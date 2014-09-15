#!/bin/bash
OS=`uname -s`
if [ "$OS" != "Darwin" ]; then
	exit 255;
fi
sudo mkdir $JAVA_HOME/Classes
sudo ln -s $JAVA_HOME/lib/tools.jar $JAVA_HOME/Classes/classes.jar

set Platform=x64
sudo port install ocaml
sudo gem install cocoapods
