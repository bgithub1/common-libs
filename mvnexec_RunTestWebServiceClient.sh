export MAVEN_OPTS='-Xmx1500m -Xms300m'
mvn exec:java -Dexec.mainClass="com.billybyte.clientserver.webserver.RunTestWebServiceClient" -Dexec.args="portOfService=7000 urlOfService=http://127.0.0.1 nameOfService=TestService"

