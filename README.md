# Simple Chess Front
A Java Swing application that acts as a *chess front* (also known as a *Chess GUI*) for *chess engines* supporting the Universal Chess Interface protocol (UCI).

A chess front is a program that does not play chess by itself but instead it knows how to connect to a chess engine
 which do know how to play, however it does not have any visible user interface (not a user-friendly one anyway).

## Supported features
Currently the following features are supported:
* Manage chess engines' configurations
* Play against a chess engine
* Have two engines play against each other
* Play against a remote engine i.e. connect to a UCI server
* Expose a chess engine as a server
* Setup board with a custom position, supporting copy/paste of positions in Forsyth-Edwards Notation (FEN)
* Provide the ability to stop/pause/close a game
* Save/Load/Browse games in Portable Game Notation (PGN)


## How to play
To be able to play you first need to have a UCI compatible chess engine (For example [Stockfish](https://stockfishchess.org/download/)
is a popular open source choice). Assuming you have already downloaded the executable file of your chess engine (e.g. an `.exe` file in Windows)
you can now register it with the chess front as following:

1. From the application's menu, navigate to `Engine -> Manage engines` and then click at the `Add` button
2. Click at the "open folder" icon ![Open folder icon](./src/main/resources/open.png) to
select the desired executable
3. Click `OK`

*Note: The `JFileChooser` seems to have some issues in Mac OS when changing directories. If you encounter this issue, save
your executable in the user's home folder which is the starting folder opened by default.*

The connection with the engine will be automatically tested and you should see a successful message. After that,
you will be able to start a new game and select the engine as your opponent.

