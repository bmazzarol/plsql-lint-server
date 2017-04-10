# plsql-lint-server
Restful server wrapper for the Trivadis PL/SQL Cop. This is the server side component to the Atom PL/SQL Linter.

This implements the Message v2 format defined by the Atom Linter.

The server is started with a given name and port and will service request with the format post@/{file-name}/,
passing the document to lint in the post body.

## Building Project
The project uses SBT and can be build by calling 

sbt assemble
