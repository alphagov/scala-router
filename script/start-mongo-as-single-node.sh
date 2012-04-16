#!/bin/bash

cachedir=~/mongo/download-cache
mongodir=~/mongo/mongo-workspace/ertp
mongoversion=2.0.4

echo Stopping mongodb
killall mongos > /dev/null 2>&1
killall mongod > /dev/null 2>&1
killall mongo > /dev/null 2>&1

case $1 in
	clean)
		rm -rf $mongodir
		;;
    reset)
        echo "Cleaning cache"
        rm -rf $cachedir
        echo "Cleaning mongo install"
        rm -rf $mongodir
esac

function error_exit
{
    echo "FATAL: ${1:-"Unknown Error"}" 1>&2
    exit 1
}
mkdir -p $cachedir
mkdir -p $mongodir/db/single-node
mkdir -p $mongodir/logs

case `uname` in
	Linux)
		mongoFile=mongodb-linux-x86_64-${mongoversion}.tgz
		mongoPath=mongodb-linux-x86_64-${mongoversion}
		mongoOs="linux"
		;;
	
	Darwin)
		mongoFile=mongodb-osx-x86_64-${mongoversion}.tgz
		mongoPath=mongodb-osx-x86_64-${mongoversion}
		mongoOs="osx"
		;;
			
	*)
		echo "Unsupported OS"
		exit 1
esac
		
if [ ! -f "$cachedir/$mongoFile" ]; then
        destMongofile=$cachedir/$mongoFile
	echo "Downloading http://fastdl.mongodb.org/$mongoOs/$mongoFile"
	curl "http://fastdl.mongodb.org/$mongoOs/$mongoFile" -o $destMongofile
fi	

if [ ! -d "$mongodir/$mongoPath" ]; then
    echo "Extracting to $mongodir"
	tar -C $mongodir -xzf $cachedir/$mongoFile
fi

echo "Starting mongodb node-1"
$mongodir/$mongoPath/bin/mongod --dbpath $mongodir/db/single-node \
	--logpath $mongodir/logs/mongodb-single-node.log \
	-vvvv \
	--smallfiles \
	--rest \
	--pidfilepath $mongodir/mongo-single.pid \
	--fork \
	--journal \
	--profile 2 \
	--directoryperdb \
	--port 27017



echo "Wait 5 secs to let the nodes start up"
sleep 5

	
