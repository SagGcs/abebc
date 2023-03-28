# The ABE build client

This project implements a client library for the ABE Build Server (ABEBS). The client library is an open source project, unlike the actual server.

The client library is made up of several parts:

1. The client library core, basically an embeddable Java Bean with the actal implementation.
   The core includes, in particular, a CLI (command line interface) for running the client.
2. A plugin for [Apache Maven](https://maven.apache.org), which encapsulates the core, and is the recommended way to run the client.
   (Nothing more easy, than add a plugin reference to a Maven POM, and adding a few configuration values.)
3. An [Apache Ant](https://ant.apache.org) task, also encapsulating the core, as an alternative for people. who are unfamiliar with
   Maven or refuse Maven.

As an open source repository, the client library is available for download at [Maven central](https://search.maven.org?q=abebc).
