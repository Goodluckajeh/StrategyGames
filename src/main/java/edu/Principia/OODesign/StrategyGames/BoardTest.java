package edu.Principia.OODesign.StrategyGames;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import edu.Principia.OODesign.StrategyGames.Board.InvalidMoveException;

/*
Board Main Program

1.0 Overview
Build a main function, and supporting functions, in a new class BoardTest, that tests any implementation of Board and Board.Move.

2.0 Main Program Start
The main program accepts one commandline argument giving the class name of the desired Board. For instance:
BoardTest BeehiveBoard
Based on this, it sets up Board and Move pointers to objects of the relevant subclass (e.g. to an BeehiveBoard and BeehiveMove). These two objects are the "current board" and the "current move". It uses these to operate a main loop performing the commands specified below. It uses reflection or factory methods to create the current board and current move, and makes no mention of any of the specific games in the main source code.

3.0 The Commands

showBoard
Uses toString to get and print a textual picture of the current board.

showMoves
Prints a list of strings describing all moves possible from the current board. Calculates the largest move size in the list of possible moves, and then figures how many columns of moves of that largest size can be fit onto an 80-char screen, with one space after each column. It then prints all the moves using that many columns, with moves left-justified within their column, so that they line up neatly. 


enterMove moveString
Assign the contents of moveString, which will fall on the same line, into the current move.

showMove
Print the string-conversion of the current move.

applyMove
Apply the current move to the current board. If the current move is not one of the allowed moves for the board, print "Not a permitted move", followed by a list of all the allowed moves, as in showBoard.
Note, applyMove does not clear the current move. A showMove call after ApplyMove still shows the move.

doMove move
Combines the action of enterMove followed by applyMove, for convenience.

undoMoves count
Reverse the last count moves applied to the current board. Stop automatically if you reach the start of the game. This allows one to easily retract to game start by supplying a very large count.

showVal
Show the value of the current board.

showMoveHist
Show a list of the moves made thus far on the current board, using the same column-sizing logic as described in showMoves.

saveBoard fileName
saveMove fileName
Open fileName and write the binary contents of the current board or move into it. For the current board, simply write all the moves in its history. This list will suffice to rebuild the board when reading in.

loadBoard fileName
loadMove fileName
Open fileName and read binary contents from it into the current board or move. In the case of loading a board, start with a new default-constructed current board, and apply all the moves in the file to it.

compareMove moveString
Compare the current move with the move described by moveString. Print "Current move is less", "Current move is equal", or "Current move is greater".

testPlay seed moveCount
Generate moveCount randomly-selected moves via the following procedure.
Create a java.util.Random object rnd, constructed with seed. Then repeatedly (moveCount times) call GetAllMoves and select the Nth move, where N is the rnd.nextInt value, limited to the size of the allowed move list. Apply this move to the board. End the loop early and without complaint if the game reaches its end.

testRun seed stepCount
Like testPlay, but each time the game ends, randomly select a number between 1 and the current number of moves, retract that many moves, and proceed until a total of stepCount "steps" have been made, where a step is either a single forward move or a retraction of 1 or more moves.
This is intended as a speed check. You should be able to do a run with 100,000 to 1,000,000 moves, depending on the game, in a reasonably short time.

quit
End the main program.

*/
public class BoardTest {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, FileNotFoundException, IOException, InvalidMoveException, ClassNotFoundException {
        if (args.length != 1) {
            System.err.println("Usage: java BoardTest <BoardClassName>");
            System.exit(1);
        }
        String boardClassName = "edu.Principia.GAjeh.OODesign.StrategyGames." + args[0];
        try {
            Class<?> boardClass = Class.forName(boardClassName);
            Board board = (Board) boardClass.getDeclaredConstructor().newInstance();
            Board.Move move = board.createMove();
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String command = scanner.next();
                if (command.equals("showBoard")) {
                    System.out.println(board);
                } else if (command.equals("showMoves")) {
                    List<? extends Board.Move> moves = board.getValidMoves();
                    int maxMoveSize = 0;
                    for (Board.Move m : moves) {
                        maxMoveSize = Math.max(maxMoveSize, m.toString().length());
                    }
                    int cols = 80 / (maxMoveSize + 1);
                    int col = 0; // Counter for columns in output
                    for (Board.Move m : moves) {
                        System.out.printf("%-" + maxMoveSize + "s ", m.toString());
                        if (++col == cols) {
                            System.out.println();
                            col = 0;
                        }
                    }
                    if (col != 0) {
                        System.out.println();
                    }
                }

                else if (command.equals("enterMove")) {
                    // scanner.nextLine(); // Skip the newline
                    if (scanner.hasNextLine()) {
                        String moveString = scanner.nextLine().trim(); // Read the entire move string
                        move.fromString(moveString);
                    }

                }

                else if (command.equals("showMove")) {
                    System.out.println(move);
                }

                else if (command.equals("applyMove")) {
                    try {
                        board.applyMove(move);
                    } catch (Board.InvalidMoveException e) {
                        System.out.println("Not a permitted move");
                        List<? extends Board.Move> moves = board.getValidMoves();
                        for (Board.Move m : moves) {
                            System.out.println(m);
                        }
                    }
                } else if (command.equals("doMove")) {
                    if (scanner.hasNext()) {
                        String moveString = scanner.nextLine().trim(); // Read the entire move string
                        move.fromString(moveString);
                        board.applyMove(move);
                    }
                }

                else if (command.equals("undoMoves")) {
                    int count = scanner.nextInt();
                    for (int i = 0; i < count; i++) {
                        board.undoMove();
                    }
                } else if (command.equals("showVal")) {
                    System.out.println(board.getValue());
                } else if (command.equals("showMoveHist")) {
                    List<? extends Board.Move> moveHistory = board.getMoveHistory();
                    int cols = 2; // Two columns
                    int col = 0; // Counter for columns in output
                    for (Board.Move m : moveHistory) {
                        String moveStr = m.toString();
                        System.out.printf("%-40s", moveStr); // Left-justify in a field of size 40
                        if (++col == cols) {
                            System.out.println();
                            col = 0;
                        }
                    }
                    if (col != 0) {
                        System.out.println();
                    }
                    // print a newline
                    //System.out.println();
                }

                // saveBoard filename (removed, fine if you still have it)
                // Open fileName and write the binary contents of the current board into it. For
                // the current board, simply write all the moves in its history. This list will
                // suffice to rebuild the board when reading in.
                else if (command.equals("saveBoard")) {
                    if (scanner.hasNext()) {
                        String fileName = scanner.next();
                        try (FileOutputStream fileOut = new FileOutputStream(fileName);
                                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                            out.writeObject(board.getMoveHistory());

                            // System.out.println("Board saved to " + fileName);
                        } catch (IOException e) {
                            System.out.println("An error occurred while saving the board: " + e.getMessage());
                            e.printStackTrace();

                        }
                    }
                } else if (command.equals("loadBoard")) {
                    if (scanner.hasNext()) {
                        String fileName = scanner.next();
                        try (FileInputStream fileIn = new FileInputStream(fileName);
                                ObjectInputStream in = new ObjectInputStream(fileIn)) {

                            board = (Board) boardClass.getDeclaredConstructor().newInstance();

                            @SuppressWarnings("unchecked")
                            List<? extends Board.Move> moveHistory = (List<? extends Board.Move>) in.readObject();
                            for (Board.Move moveItem : moveHistory) {
                                board.applyMove(moveItem);
                            }

                            // System.out.println("Board loaded from " + fileName);
                        } catch (IOException | ClassNotFoundException | InvalidMoveException e) {
                            System.out.println("An error occurred while loading the board: " + e.getMessage());
                        }
                    }
                } else if (command.equals("compareMove")) {
                    // scanner.nextLine(); // Skip the newline
                    if (scanner.hasNextLine()) {
                        String moveString = scanner.nextLine().trim();
                        Board.Move other = board.createMove();
                        other.fromString(moveString);
                        int cmp = move.compareTo(other);
                        if (cmp < 0) {
                            System.out.println("Current move is less");
                        } else if (cmp > 0) {
                            System.out.println("Current move is greater");
                        } else {
                            System.out.println("Current move is equal");
                        }
                    }
                }  else if (command.equals("testRun")) {
                    if (scanner.hasNextInt()) {
                        int seed = scanner.nextInt(); // Reads the next integer as seed
                        int stepCount = scanner.nextInt(); // Reads the next integer as stepCount
                        Random rnd = new Random(seed);
                        for (int i = 0; i < stepCount; i++) {
                            List<? extends Board.Move> moves = board.getValidMoves();
                            if (moves.isEmpty()) {
                                int moveNum = rnd.nextInt(board.getMoveHistory().size()) + 1;
                                 //System.out.println("Retracting " + moveNum + " moves at step " + (stepCount -
                                 //i) + " " + i);
                                // System.out.println(board);
                                for (int j = 0; j < moveNum; j++) {
                                    board.undoMove();
                                }
                            } else {
                                int moveNum = rnd.nextInt(moves.size());
                                move = moves.get(moveNum);
                                try {
                                    board.applyMove(move);
                                } catch (Board.InvalidMoveException e) {
                                    System.out.println("Encountered an invalid move during testRun.");
                                }
                            }
                        }
                    }
                } else if (command.equals("testPlay")) {
                    int seed = scanner.nextInt();
                    int moveCount = scanner.nextInt();
                    Random rnd = new Random(seed);
                    for (int i = 0; i < moveCount; i++) {
                        List<? extends Board.Move> moves = board.getValidMoves();
                        int n = rnd.nextInt(moves.size());
                        try {
                            board.applyMove(moves.get(n));
                        } catch (Board.InvalidMoveException e) {
                            System.err.println("Invalid move: " + e.getMessage());
                        }
                        if (board.getValidMoves().isEmpty()) {
                            break;
                        }
                    }
                } else if (command.equals("aiPlay")) {
                    // automatically play the game using the minimax algorithm
                    // apply the move with the highest value to the board and print the board
                    int level = scanner.nextInt();
                    int maxMoves = scanner.nextInt();
                    // System.out.println("Playing the game using the minimax algorithm" + " at
                    // level " + level + " for " + maxMoves + " moves");
                    AISolver.MMResult result = new AISolver.MMResult();
                    for (int i = 0; i < maxMoves; i++) {
                        AISolver.miniMax(board, Integer.MIN_VALUE, Integer.MAX_VALUE, level, result);
                        if (result.move == null) {
                            break;
                        }
                        board.applyMove(result.move);
                        // System.out.printf("MM (%d, %d) lvl %d\n", Integer.MIN_VALUE,
                        // Integer.MAX_VALUE, level);
                        System.out.printf("Move %d: %s MM value %d Board value %d\n", i + 1, result.move, result.value,
                                board.getValue());

                        System.out.println(board);
                    }
                }

                else if (command.startsWith("aiRun")) {
                    int level = scanner.nextInt();
                    int maxMoves = scanner.nextInt();
                    AISolver.MMResult result = new AISolver.MMResult();
                    for (int i = 0; i < maxMoves; i++) {
                        AISolver.miniMax(board, Integer.MIN_VALUE, Integer.MAX_VALUE, level, result);
                        if (result.move == null) {
                            break;
                        }
                        System.out.printf("move %d: %s\n", i + 1, result.move);
                        System.out.println(board);
                    }
                } else if (command.equals("showPlayer")) {
                    System.out.println(board.getCurrentPlayer());
                } else if (command.equals("quit")) {
                    break;
                } else {
                    System.out.println("Unknown command");
                }
            }
            scanner.close();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + boardClassName);
            System.exit(1);
        } catch (InstantiationException e) {
            System.err.println("Class not instantiable: " + boardClassName);
            System.exit(1);
        } catch (IllegalAccessException e) {
            System.err.println("Class not accessible: " + boardClassName);
            System.exit(1);
        } catch (java.io.IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
    }
}
