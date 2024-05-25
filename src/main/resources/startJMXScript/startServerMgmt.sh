port=$1;
re='^[0-9]+$'
if [ -z "$port" ] ; then
	port=1617;
fi

if ! [[ $port =~ $re ]] ; then
	echo "Given argument $1 is not valid port for utility service";
	exit 1;
fi

java -jar -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$port -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false $CRESTEL_P_ENGINE_HOME/modules/mediation/bin/ServerMgmt.jar >> $CRESTEL_P_ENGINE_HOME/modules/mediation/logs/serverMgmtJMX.log
