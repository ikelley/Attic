#!/bin/bash

#
# author Kieran Evans 
# email keyz182 (at) gmail (dot) com
#

HELP="
./attic-setup setup
	This sets up the directories needed, downloads the code and compiles it.
	It also asks one done if you would like to run attic (equivelent to calling
	./attic-setup run

./attic-setup run
	This will run some instances of attic on the current machine. It will create
	a set of *.pid files under ~/.attic/pid

./attic-setup kill
	This will shut any attic instances down."

SVN=https://svn.atticfs.org/projects/attic/trunk/
HOMEDIR=~
CONFDIR=$HOMEDIR/.attic
LOGDIR=$CONFDIR/log
LOGCONFDIR=$LOGDIR/config
CODEDIR=$HOMEDIR/attic-code
#EXECUTEDIR=$CODEDIR/attic/core/target/test-classes
EXECUTEDIR=$CODEDIR/attic/core/all/target
PIDDIR=$CONFDIR/pid

#Function Definitions

function check_guid {
	if [[ "$1" =~ ^(\{){0,1}[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}(\}){0,1}$ ]]
	then 
		return 0
	else
		return 1
	fi
}

function run_dlookup {
	touch $PIDDIR/dl.pid
	#nohup java -Djava.util.logging.config.file=$LOGCONFDIR/dl.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dlookup 9090 $CONFDIR &> /dev/null  &
	java -Djava.util.logging.config.file=$LOGCONFDIR/dl.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dlookup 9090 $CONFDIR  &> /dev/null &
	echo $! > $PIDDIR/dl.pid
}

function run_dpublish {
	touch $PIDDIR/dp.pid
	#nohup java -Djava.util.logging.config.file=$LOGCONFDIR/dp.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dpublisher http://localhost:9090/dl/meta/pointer 9191 $CONFDIR 100MB.dat &> /dev/null  &
	java -Djava.util.logging.config.file=$LOGCONFDIR/dp.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dpublisher http://localhost:9090/dl/meta/pointer 9191 $CONFDIR 100MB.dat $1 &> /dev/null  &
	echo $! > $PIDDIR/dp.pid	
}

function run_dcenter {
	if [ $# == 0 ]
	then 
		PORT=$RANDOM
		let "PORT %= 1000"
		let "PORT %= 9000"
	else
		PORT=$1
	fi

	touch $PIDDIR/dc.$PORT.pid
	#nohup java -Djava.util.logging.config.file=$LOGCONFDIR/dc.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dcenter http://lhost:9090/dl/meta/pointer $PORT 60 $CONFDIR  &> /dev/null &
	java -Djava.util.logging.config.file=$LOGCONFDIR/dc.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dcenter http://localhost:9090/dl/meta/pointer $PORT 60 $CONFDIR/dc-$PORT  &> /dev/null &
	echo $! > $PIDDIR/dc.$PORT.pid	
}

function run_dworker {
	touch $PIDDIR/dw.pid
	#nohup java -Djava.util.logging.config.file=$LOGCONFDIR/dw.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dworker http://localhost:9090/dl/meta/pointer/$1  &> /dev/null &
	java -Djava.util.logging.config.file=$LOGCONFDIR/dw.log.properties -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.Dworker http://localhost:9090/dl/meta/pointer/$1  &> /dev/null &
	echo $! > $PIDDIR/dw.pid
}

#Runs some attic instances
function run_attic {
	cd $EXECUTEDIR
	
	run_dlookup
	
	echo "Creating Initial file for the Data Publisher"
	dd if=/dev/urandom of=100MB.dat bs=1024 count=102400
	
	GUID=`java -classpath .:../../all/target/attic-all-0.3.jar org.atticfs.roleservices.run.IdGen`
	echo "GUID Generated is:"
	echo $GUID
	
	run_dpublish $GUID
	
	run_dcenter 9292
	run_dcenter 9393
	run_dcenter 9494
	
	#echo "Please open your browser and navigate to http://localhost:9090/dl/meta/pointer and enter the GUID (data identifier) for the file here:"
	#read GUID
	
	#check_guid $GUID
	#GUIDRET=$?
	
	#while [ $GUIDRET -eq 1 ]
	#do
		#echo "That was incorrect, please try again, or type \"no\" to exit"
		#read GUID
		#if [ "$GUID" = "no" ]
		#then
		#	return -1
		#fi
		#check_guid $GUID
		#GUIDRET=$?
	#done
	
	run_dworker $GUID
}
#end of run_attic()

#Download, compile and set the attic config
function setup_attic {

	check_cmd
	if [ $? -ne 0 ]
	then
		return -1
	fi
	
	#Make the necessary directories
	mkdir -p $LOGCONFDIR && mkdir $CODEDIR && mkdir $PIDDIR
	if  [ $? -ne 0 ]
	then
		echo "Making Directories Failed."
		return -1
	fi
	
	#Create logging properties
	
	LOG1="#The following line outputs to file and console\n
	#handlers= java.util.logging.FileHandler, java.util.logging.ConsoleHandler\n
	handlers=java.util.logging.FileHandler\n
	.level=FINE\n
	java.level=OFF\n
	javax.level=OFF\n
	java.util.logging.ConsoleHandler.level=FINE\n
	java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\n
	java.util.logging.FileHandler.level=ALL\n
	java.util.logging.FileHandler.pattern=%h/.attic/log/"
	
	LOG2=".%u.log\n
	java.util.logging.FileHandler.limit=50000\n
	java.util.logging.FileHandler.count=1\n
	java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter\n"
	
	DLLOG=$LOG1"dl"$LOG2
	DPLOG=$LOG1"dp"$LOG2
	DCLOG=$LOG1"dc"$LOG2
	DWLOG=$LOG1"dw"$LOG2
	
	printf "%b" $DLLOG > $LOGCONFDIR/dl.log.properties && printf "%b"  $DPLOG > $LOGCONFDIR/dp.log.properties && printf "%b"  $DCLOG > $LOGCONFDIR/dc.log.properties && printf "%b"  $DWLOG > $LOGCONFDIR/dw.log.properties
	
	if  [ $? -ne 0 ]
	then
		echo "Failed to write logging properties files."
		return -1
	fi
	
	cd $CODEDIR
	
	
	read -p "Press enter to start svn checkout"
	
	svn checkout $SVN attic
	cd attic
	
	read -p "Press enter to start compilation"
	mvn install
	
	read "Would you like to run some instances of attic? (yes/no)" ANS
	if [ "$ANS" = "yes" ]
	then
		run_attic
	fi
	
	echo "Goodbye"
	
}
#end of setup_attic()

function check_cmd() {
	#Check if subversion, java and maven are installed.

	if command -v svn help &>/dev/null
	then
	     if command -v java -version help &>/dev/null
		then   
			if command -v mvn --version help &>/dev/null
			then   
				echo "svn, java and mvn found!"
			else
				echo "Maven not found"
				return -1
			fi
		else
			echo "Java not found"
			return -1
		fi
	else
		echo "Subversion not found"
		return -1
	fi
}

function kill_attic {
	echo "Shutting down attic"
	cd $PIDDIR
	PIDS="*.pid"
	
	for PID in "$PIDS"
	do
		kill `cat $PID`
	done
}
	

#Start of execution

if [ "$1" = "setup" ]
then
	setup_attic
elif [ "$1" = "run" ]
then
	run_attic
elif [ "$1" = "kill" ]
then
	kill_attic
elif [ "$1" = "help" ]
then
	echo "
./attic-setup setup
	This sets up the directories needed, downloads the code and compiles it.
	It also asks one done if you would like to run attic (equivelent to calling
	./attic-setup run

./attic-setup run
	This will run some instances of attic on the current machine. It will create
	a set of *.pid files under ~/.attic/pid

./attic-setup kill
	This will shut any attic instances down."
else
	echo "
./attic-setup setup
	This sets up the directories needed, downloads the code and compiles it.
	It also asks one done if you would like to run attic (equivelent to calling
	./attic-setup run

./attic-setup run
	This will run some instances of attic on the current machine. It will create
	a set of *.pid files under ~/.attic/pid

./attic-setup kill
	This will shut any attic instances down."
fi
	










