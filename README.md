common-libs
===========

main api for trading/risk/marketdata etc

Example mains:
1. Turn any of your code into an http server see main in: 
   com.billybyte.clientserver.httpserver.HttpCsvQueryServer

2. Turn any of your code into a WebService, and build java clients that
   can consume your web service:
   com.billybyte.clientserver.webserver.RunTestWebServiceServer
   com.billybyte.clientserver.webserver.RunTestWebServiceClient

3. Examples of using lots of different math libraries (org.apache.commons.math3, Jama, org.paukov.combinatorics, etc):
   com.billybyte.mathstuff.MathStuff
   
4. Java Mongo wrappers to facilate accessing mongo dbs using java:
   com.billybyte.mongo.MongoWrapper
   com.billybyte.mongo.MongoXml for easily turning any pojo into a mongo doc that can be stored in a mongodb

5. Using Neodatis in memory sql db:
   com.billybyte.neodatis
   
6. Example of using Spring to launch instantiate java classes form Spring Beans xml files
   com.billybyte.spring.BeansLaunch
   
7. MessageBox routine for modal and non-modal Message Boxes with and without Swing
   com.billybyte.ui

DerivativeSetEngine stuff:
DeriviativeSetEngine allows you to compute prices and greeks for exchange traded commodity options
   using the following syntax:
             
