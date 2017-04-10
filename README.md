# plsql-lint-server
A RESTful server wrapper for the [Trivadis PL/SQL Cop](https://www.salvis.com/blog/plsql-cop/). This is used by the [Atom PL/SQL Linter](https://github.com/bmazzarol/atom-plsql-linter).

This implements the [Message v2](http://steelbrain.me/linter/types/linter-message-v2.html) format defined by the Atom Linter.

The server is started with a given name and port and will service request with the format post@/{file-name}/,
passing the document to lint in the post body.

This is built against version 2.0.3 of the PL/SQL COP. 

## Building Project
The project uses SBT and can be built by calling 

`sbt assemble`

This will create a zip file under /target/universal called plsql-lint-server-1.0.zip.

## Running the Server
After extracting the built zip file the server can be run by calling the correct executable for your environment.

for example on linux call,

`bin/plsql-lint-server 9999`

This will require you have java installed on your path.

The server can then access at localhost:9999/
