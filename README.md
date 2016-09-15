# card-game

We are going to make a digital card game. Our initial goal with this project is just to get some experience programming with different languages and frameworks, and to have some fun, but we're hoping to eventually create a fun, playable card game.

# Status

We currently have a bare-bones Java server and a UWP client (with lots of boilerplate code from the Visual Studio template) that sends messages to the server over TCP.

# Directory structure

`client` contains projects for each of the different clients for the card game. Currently it contains a UWP client.

`server` contains the project for our Java server (in the `CardGameServer` folder). If we add any other server-side projects, they will go in here.

# Build instructions

For the UWP client, open the solution in Visual Studio and use Visual Studio to build it.

The server is a Maven project. Either run `mvn package` on the command line inside the project root directory (the directory containing `pom.xml`), or import the project into Eclipse as a Maven project and build it from within Eclipse.

# Code formatting guideline (for contributors)

Use soft tabs with a tab/indentation size of 4 for Java, C# code, and XML code (with the exception of `pom.xml` in `CardGameServer`: for this file, use soft tabs with a tab/indentation size of 2).

# Message protocol

Clients communicate with the server by exchanging messages over TCP. Each message has at least four bytes. The first four bytes form a 32-bit integer holding the number of bytes in the rest of the message.
