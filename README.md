Board Main Program
Overview
This program, BoardTest, is designed to test any implementation of Board and Board.Move. It uses reflection or factory methods to dynamically instantiate the desired board and move classes based on a command-line argument. The main program operates a loop to perform various commands to interact with the board and its moves.

Usage
To start the program, execute the following command:

bash
Copy
java BoardTest <BoardClassName>
Replace <BoardClassName> with the name of the desired board class (e.g., TTTBoard).

Commands
The following commands can be used to interact with the program:

showBoard
Displays a textual representation of the current board using the toString method.

showMoves
Prints a list of possible moves from the current board. It organizes the moves into columns that fit neatly on an 80-character screen.

enterMove <moveString>
Sets the current move to the specified moveString.

showMove
Prints the current move.

applyMove
Applies the current move to the current board. If the move is not permitted, it prints a message along with a list of allowed moves.

doMove <moveString>
Combines enterMove followed by applyMove for convenience.

undoMoves <count>
Reverses the last <count> moves applied to the current board, up to the start of the game.

showVal
Shows the value of the current board.

showMoveHist
Displays a list of moves made so far, organized into columns for readability.

saveBoard <fileName>
Writes the binary contents of the current board or move into <fileName>. For the board, it writes all moves in its history.

loadBoard <fileName>
Reads binary contents from <fileName> into the current board or move. It starts with a default-constructed board and applies all moves from the file.

compareMove <moveString>
Compares the current move with <moveString> and prints whether the current move is less, equal to, or greater than <moveString>.

showPlayer
Displays whose turn it is: 1 or -1.

testPlay <seed> <moveCount>
Generates <moveCount> randomly-selected moves using a seed <seed>. Applies these moves to the board, ending early if the game concludes.

testRun <seed> <stepCount>
Similar to testPlay, but continues playing until <stepCount> steps (forward move or retraction) are completed. Randomly selects retraction counts based on the size of the move history.

quit
Ends the main program.