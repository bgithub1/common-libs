RUNOPT=$1
if [ "$1" = "" ]; then
	RUNOPT=1
fi
sh mvnexec.sh com.billybyte.dse.debundles.RunGreeks example=$RUNOPT "portfolioPath=./bperlman1/testPort.csv"

