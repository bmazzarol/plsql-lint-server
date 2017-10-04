# plsql-lint-server
A RESTful server wrapper for the [Trivadis PL/SQL Cop](https://www.salvis.com/blog/plsql-cop/). This is used by the [Atom PL/SQL Linter](https://github.com/bmazzarol/atom-plsql-linter).

This implements the [Message v2](http://steelbrain.me/linter/types/linter-message-v2.html) format defined by the Atom Linter.

The server is started with a given name and port.

This is built against version 2.1.3 of the PL/SQL COP. 

## Building Project
The project uses SBT and can be built by calling 

`sbt universal:packageBin`

This will create a zip file under /target/universal called plsql-lint-server-1.0.zip.

## Running the Server
After extracting the built zip file the server can be run by calling the correct executable for your environment.

for example on linux call,

`bin/plsql-lint-server 9999`

This will require you have java installed on your path.

The server can then access at localhost:9999/

## Server Resources
The following resources are available.

@GET  /version     - returns the current running version

@GET  /check-alive - returns ok if the server is active

@GET  /shutdown    - kills the running server

@POST /lint-file/  - lints the file contents passed in as the message body returning the validation messages. 
                           See the [Message v2](http://steelbrain.me/linter/types/linter-message-v2.html) format for more information on what is returned.
                           
See the bmazzarol.plsql.AST.LintFileRequest for the json request format to pass to the server.