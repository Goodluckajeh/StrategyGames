package edu.Principia.GAjeh.OODesign.StrategyGames.Blokus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.Principia.OODesign.StrategyGames.Board;

/* 
Blokus game rules and design notes
Official Duo Rules – except we’re not doing bonus points at the end (see Game Over below).

1. Player 1 is "X", player 2 "O"

2. Display the board as a 14x14 grid of . (dot) for empty squares, and X or O for squares occupied by pieces. Empty starting squares are marked by s, not . (dot)

3. Board display - match the following style. Random sample position shown at end.
   1 2 3 4 5 6 7 8 9 t 1 2 3 4
 1 . . . . . . . . . . . . . . 
 2 . . . . . . . . . . . . . . 
 3 . . . . . . . . . . . . . . 
 4 . . . . . . . . . . . . . . 
 5 . . . . s . . . . . . . . . 
 6 . . . . . . . . . . . . . . 
 7 . . . . . . . . . . . . . . 
 8 . . . . . . . . . . . . . . 
 9 . . . . . . . . . . . . . . 
10 . . . . . . . . . s . . . .
11 . . . . . . . . . . . . . .
12 . . . . . . . . . . . . . .
13 . . . . . . . . . . . . . . X's hand: I1 I2 I3 L3 I4 L4 O4 S4 T4 I5 L5 P5 R5 S5 T5 U5 V5 W5 X5 Y5 Z5
14 . . . . . . . . . . . . . . O's hand: I1 I2 I3 L3 I4 L4 O4 S4 T4 I5 L5 P5 R5 S5 T5 U5 V5 W5 X5 Y5 Z5

4. Piece placement is described relative to the "base position" which is what is shown in the diagram, with origin at the top-left corner of the bounding rectangle surrounding the piece, and the squares of the piece at corresponding r,c locations. For instance, piece S4 has a 2x3 bounding rectangle, and squares at (0,1), (0,2), (1,0), and (1,1).

5. Up to eight possible "orientations", varying from base position, are possible, depending on the symmetry of the piece. Orientations are formed first by the four 90 degree rotations of the piece. Then the piece can be mirrored left-right. For a piece like S5, for instance, all of these possibilities make a difference, and there are 8 orientations. For a piece like O4, however, none of them make a difference, and there is only one orientation.

6. An orientation description is a two-char sequence: ^, >, v, < to indicate unrotated, 90-degree clockwise rotation, 180-degree rotation, and 270-degree rotation. Then, there’s an optional | (pipe) for left-right mirror, with no blanks between the characters. So for instance:
^ = base position -- no rotation
>| = clockwise rotate, mirror

7. A move is given by a piece number, an orientation description, and an (r,c) location on the board on which to place the (0,0) corner of the bounding rectangle (which actually might or might not be a square), with spaces between the three parts. Or “pass,” if there are no valid placing moves. 

A move like Y5 >| (3,4) for X would place the top left corner of the pattern at board location (3,4). That means it would fill board squares (3,5), (4,4), (4,5), (4,6), and (4,7).
 X
XXXX

8. Valid moves (and move comparisons) are ordered first by piece number (backwards, from Z5 to I1 for aiPlay speed), then by orientation, and finally by row-major order of the placement position. Orientations are ordered thus: ^, >, v, <, ^|, >|, v|, <|. A pass move (if possible) comes last in comparison order. (See random showBoard and showMoves at end.)

9. Valid moves include only unique orientations. If two orientations are equivalent, only the first in the orientation sequence is used. For instance, pieces I1 and O4 have only orientation: ^. Piece L3 has orientations ^, >, v, and <. Orientation ^| for piece L3 is not used, for instance, since it duplicates <. However, the string representation of a move may use any rotation.  So, for instance, if ^| were entered as the rotation for a move in the prior case, map it to <.  (This avoids the irritation of a user being badgered to use exactly the right rotation when several are equivalent)

10. Evaluate the board during the game by finding the value for each player as follows: 
50 x (how many squares that player controls)
+ the number of squares in all valid moves possible for that player at present.  Include all pieces and orientations. This represents how many options they still have: the more, the better.
Calculate the board evaluation as X’s value minus O’s.

11. Game Over once both players pass in a row. The evaluation becomes +WIN or -WIN, depending on who controls more squares, or 0 for a draw (if the counts are equal).

12. Here’s a random showBoard and showMoves (with X to move): 
   1 2 3 4 5 6 7 8 9 t 1 2 3 4
 1 X . O O O . . O . . . . . .
 2 . X . . O O . O O O O . O .
 3 . X . . . . O . . . . O . .
 4 . X . . . . O . . . O O O O
 5 . . X O O . O O . O . . . .
 6 . . X X O O . . O O . . O O
 7 . . X . O . . . . O . . O .
 8 . . . X X X X . . . O O . .
 9 X X X . . X . X X X . O . .
10 . . X X . . . . X X O O . .
11 . X . . . X . X . . X . O .
12 X X . . . X . X X . X . O .
13 . X X . X X X . X . X X X . X's hand: I2 L3 I4 L4 O4 I5 L5 U5 W5 X5 Z5
14 . . . . . . . . . . . . . . O's hand: I3 I4 O4 S4 I5 P5 T5 V5 W5 X5 Z5

Z5 >| (3,4)  U5 ^ (3,4)   U5 v (3,4)   L5 v (9,13)  I5 ^ (8,14)  O4 ^ (3,4)   
L4 ^ (2,4)   L4 > (3,4)   L4 v (10,13) L4 < (3,4)   L4 >| (4,4)  L4 <| (3,4)
I4 ^ (9,14)  L3 ^ (3,4)   L3 ^ (6,8)   L3 > (3,4)   L3 v (6,7)   L3 < (3,4)
I2 ^ (3,4)   I2 ^ (5,1)   I2 ^ (6,8)   I2 ^ (11,14) I2 > (4,4)   I2 > (7,8)

13. Pieces - picture (printout versions below)
*/

public class BlokusBoard implements Board, Serializable {
  public class BlokusMove implements Move, Serializable {
    public String pieceName;
    public String orientation;
    int row, col;
    boolean pass;

    public BlokusMove() {
      pass = false;
    }

    public BlokusMove(String pieceName, String orientation, int row, int col) {
      this.pieceName = pieceName;
      this.orientation = orientation;
      this.row = row;
      this.col = col;
      pass = false;
    }

    public BlokusMove(BlokusMove m) {
      pieceName = m.pieceName;
      orientation = m.orientation;
      row = m.row;
      col = m.col;
      pass = m.pass;
    }

    @Override
    public void write(OutputStream os) throws IOException {
      os.write(pieceName.getBytes());
      os.write(orientation.getBytes());
      os.write(row);
      os.write(col);
    }

    @Override
    public void read(InputStream is) throws IOException {
      byte[] pieceNameBytes = new byte[2];
      is.read(pieceNameBytes);
      pieceName = new String(pieceNameBytes);
      byte[] orientationBytes = new byte[2];
      is.read(orientationBytes);
      orientation = new String(orientationBytes);
      row = is.read();
      col = is.read();
    }

    @Override
    public void fromString(String s) throws IOException {
      String[] parts = s.split(" ");
      if (parts.length != 3) {
        throw new IOException("Invalid format");
      }
      pieceName = parts[0];
      orientation = parts[1];
      // Remove parentheses from the location part
      String locationPart = parts[2].replaceAll("[()]", "");
      String[] location = locationPart.split(",");
      if (location.length != 2) {
        throw new IOException("Invalid format");
      }
      try {
        row = Integer.parseInt(location[0]) - 1;
        col = Integer.parseInt(location[1]) - 1;
      } catch (NumberFormatException e) {
        throw new IOException("Invalid format");
      }
    }

    @Override
    public int compareTo(Move m) {
      BlokusMove other = (BlokusMove) m;
      if (pass && other.pass) {
        return 0;
      }
      if (pass) {
        return 1;
      }
      if (other.pass) {
        return -1;
      }
      if (pieceName.equals(other.pieceName)) {
        if (orientation.equals(other.orientation)) {
          if (row == other.row) {
            return col - other.col;
          }
          return row - other.row;
        }
        return orientation.compareTo(other.orientation);
      }
      return pieceName.compareTo(other.pieceName);
    }

    @Override
    public String toString() {
      if (pass) {
        return "pass";
      }
      return pieceName + " " + orientation + " (" + (row + 1) + "," + (col + 1) + ")";
    }
  }

  public static class Location implements Serializable {
    public int row;
    public int col;

    public Location(int r, int c) {
      row = r;
      col = c;
    }

    @Override
    public String toString() {
      return "(" + (1 + row) + "," + (1 + col) + ")";
    }
  }

  public class Piece implements Serializable {
    public String pieceName;
    public String orientation;
    public Location location;
    public int player;
    public List<Location> locations;
    public boolean isPlayed;
    private int[][] piece; // The current configuration of the piece
    List<String> uniqueOrientations = new ArrayList<>();

    public Piece(String pieceName, int player) {
      this.pieceName = pieceName;
      this.player = player;
      resetToBaseConfiguration();
      // this.isPlayed = false;
    }

    private void resetToBaseConfiguration() {
      this.piece = deepCopy(pieceMap.get(pieceName));
      initializeLocations();
    }

    private int[][] deepCopy(int[][] original) {
      int[][] copy = new int[original.length][];
      for (int i = 0; i < original.length; i++) {
        copy[i] = Arrays.copyOf(original[i], original[i].length);
      }
      return copy;
    }

    private void initializeLocations() {
      locations = new LinkedList<>();
      for (int i = 0; i < piece.length; i++) {
        for (int j = 0; j < piece[i].length; j++) {
          if (piece[i][j] == 1) {
            locations.add(new Location(i, j));
          }
        }
      }
    }

    // a method that gets unique orientations based on the piece. It adds to the
    // list of unique orientations. an orientation for a piece is unique
    // if the piece is the same when it's oriented in a different way. For example,
    // a piece that is a square is the same when it's rotated 90 degrees
    // or 180 degrees or 270 degrees. So the unique orientations for a square piece
    // would be just ^ because other orientations are the same as ^.
    // For a piece like L3, the unique orientations would be ^, >, v, < because the
    // piece is different when it's rotated.
    public List<String> getUniqueOrientations() {
      String[] orientations = { "^", ">", "v", "<", "^|", ">|", "v|", "<|" };
      List<String> uniqueOrientations = new ArrayList<>();
      for (String orientation : orientations) {
        Piece p = new Piece(pieceName, player);
        p.setOrientation(orientation);
        boolean isUnique = true;
        for (String uniqueOrientation : uniqueOrientations) {
          Piece q = new Piece(pieceName, player);
          q.setOrientation(uniqueOrientation);
          boolean piecesAreEqual = Arrays.deepEquals(p.piece, q.piece);
          System.out.println(
              "Comparing piece " + p.pieceName + " with orientation " + orientation + " to piece " + q.pieceName
                  + " with orientation " + uniqueOrientation + ". Result: " + (piecesAreEqual ? "equal" : "not equal"));
          if (piecesAreEqual) {
            isUnique = false;
            break;
          }
        }
        if (isUnique) {
          uniqueOrientations.add(orientation);
        }
      }
      return uniqueOrientations;
    }

    public void setOrientation(String newOrientation) {
      resetToBaseConfiguration(); // Reset before applying new transformations
      for (char ch : newOrientation.toCharArray()) {
        if (ch == '|') {
          mirror();
        } else if (ch == '>' || ch == 'v' || ch == '<') {
          rotate(ch);
        }
      }
      orientation = newOrientation; // Update orientation after transformations
    }

    private void mirror() {
      int[][] mirrored = new int[piece.length][piece[0].length];
      for (int i = 0; i < piece.length; i++) {
        for (int j = 0; j < piece[i].length; j++) {
          mirrored[i][piece[i].length - 1 - j] = piece[i][j];
        }
      }
      piece = mirrored;
      initializeLocations(); // Update locations after mirroring
    }

    private void rotate(char direction) {
      int rows = piece.length;
      int cols = piece[0].length;
      int[][] rotated;

      switch (direction) {
        case '>': // 90 degrees
          rotated = new int[cols][rows];
          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              rotated[j][rows - 1 - i] = piece[i][j];
            }
          }
          break;
        case 'v': // 180 degrees
          rotated = new int[rows][cols];
          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              rotated[rows - 1 - i][cols - 1 - j] = piece[i][j];
            }
          }
          break;
        case '<': // 270 degrees
          rotated = new int[cols][rows];
          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              rotated[cols - 1 - j][i] = piece[i][j];
            }
          }
          break;
        default:
          throw new IllegalArgumentException("Invalid direction: " + direction);
      }

      piece = rotated;
      initializeLocations(); // Update locations after rotation
    }
  }

  public static final String[] pieceNames = { "I1", "I2", "I3", "L3", "I4", "L4", "O4", "S4", "T4", "I5", "L5", "P5",
      "R5", "S5", "T5", "U5", "V5", "W5", "X5", "Y5", "Z5" };

  public static final int[][][] pieces = {
      // "I1"
      { { 1 } },
      // "I2"
      { { 1 }, { 1 } },
      // "I3"
      { { 1 }, { 1 }, { 1 } },
      // "L3"
      { { 1, 0 }, { 1, 1 } },
      // "I4"
      { { 1 }, { 1 }, { 1 }, { 1 } },
      // "L4"
      { { 1, 0 }, { 1, 0 }, { 1, 1 } },
      // "O4"
      { { 1, 1 }, { 1, 1 } },
      // "S4"
      { { 0, 1, 1 }, { 1, 1, 0 } },
      // "T4"
      { { 1, 1, 1 }, { 0, 1, 0 } },
      // "I5"
      { { 1 }, { 1 }, { 1 }, { 1 }, { 1 } },
      // "L5"
      { { 1, 0 }, { 1, 0 }, { 1, 0 }, { 1, 1 } },
      // "P5"
      { { 1, 1 }, { 1, 1 }, { 1, 0 } },
      // "R5"
      { { 0, 1, 1 }, { 1, 1, 0 }, { 0, 1, 0 } },
      // "S5"
      { { 0, 0, 1, 1 }, { 1, 1, 1, 0 } },
      // "T5"
      { { 1, 1, 1 }, { 0, 1, 0 }, { 0, 1, 0 } },
      // "U5"
      { { 1, 0, 1 }, { 1, 1, 1 } },
      // "V5"
      { { 1, 0, 0 }, { 1, 0, 0 }, { 1, 1, 1 } },
      // "W5"
      { { 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 1 } },
      // "X5"
      { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } },
      // "Y5"
      { { 0, 1 }, { 1, 1 }, { 0, 1 }, { 0, 1 } },
      // "Z5"
      { { 1, 1, 0 }, { 0, 1, 0 }, { 0, 1, 1 } }
  };

  // public static Map<String, [][]Integer> pieceMap = new HashMap<String,
  // Piece>();
  // write a static map that maps using a string as the key and a 2d array of ints
  // as the value
  public static Map<String, int[][]> pieceMap = new HashMap<>();
  static {
    for (int i = 0; i < pieceNames.length; i++) {
      pieceMap.put(pieceNames[i], pieces[i]);
    }
  }

  private int currentPlayer;
  private int[][] board;
  private List<BlokusMove> moveHistory;
  private List<Piece> player1Pieces;
  private List<Piece> player2Pieces;
  private final int DIM = 14;
  int passCount = 0;
  Location unUsedStartingSpot = null;
  Location usedStartingSpot = null;
  int STARTING_SPOT_ROW = 4; // 5 in 1-based indexing
  int STARTING_SPOT_COL = 4; // 5 in 1-based indexing
  int STARTING_SPOT_ROW2 = 9; // 10 in 1-based indexing
  int STARTING_SPOT_COL2 = 9; // 10 in 1-based indexing

  public BlokusBoard() {
    currentPlayer = PLAYER_0;
    board = new int[DIM][DIM];
    moveHistory = new LinkedList<>();
    player1Pieces = new LinkedList<>();
    player2Pieces = new LinkedList<>();
    for (String pieceName : pieceNames) {
      player1Pieces.add(new Piece(pieceName, PLAYER_0));
      player2Pieces.add(new Piece(pieceName, PLAYER_1));
    }
  }

  public BlokusBoard(BlokusBoard b) {
    currentPlayer = b.currentPlayer;
    board = new int[DIM][DIM];
    for (int i = 0; i < DIM; i++) {
      for (int j = 0; j < DIM; j++) {
        board[i][j] = b.board[i][j];
      }
    }
    moveHistory = new LinkedList<>();
    for (BlokusMove m : b.moveHistory) {
      moveHistory.add(new BlokusMove(m));
    }
    player1Pieces = new LinkedList<>();
    for (Piece p : b.player1Pieces) {
      player1Pieces.add(new Piece(p.pieceName, p.player));
    }
    player2Pieces = new LinkedList<>();
    for (Piece p : b.player2Pieces) {
      player2Pieces.add(new Piece(p.pieceName, p.player));
    }
  }

  public void printPiece() {
    String pieceName = "L3"; // replace with the piece name you want to print
    String orientation = "<"; // replace with the desired orientation
    String mirror = "|"; // replace with "|" for mirror, null for no mirror

    int[][] piece = pieceMap.get(pieceName);
    if (piece == null) {
      System.out.println("Piece not found");
      return;
    }

    // Manually rotate the piece
    int[][] newPiece;
    switch (orientation) {
      case ">": // 90 degrees
        newPiece = new int[piece[0].length][piece.length];
        for (int i = 0; i < piece.length; i++) {
          for (int j = 0; j < piece[i].length; j++) {
            newPiece[j][piece.length - 1 - i] = piece[i][j];
          }
        }
        break;
      case "v": // 180 degrees
        newPiece = new int[piece.length][piece[0].length];
        for (int i = 0; i < piece.length; i++) {
          for (int j = 0; j < piece[i].length; j++) {
            newPiece[piece.length - 1 - i][piece[i].length - 1 - j] = piece[i][j];
          }
        }
        break;
      case "<": // 270 degrees
        newPiece = new int[piece[0].length][piece.length];
        for (int i = 0; i < piece.length; i++) {
          for (int j = 0; j < piece[i].length; j++) {
            newPiece[piece[i].length - 1 - j][i] = piece[i][j];
          }
        }
        break;
      default:
        newPiece = piece;
        break;
    }

    // Manually mirror the piece
    if (mirror != null && mirror.equals("|")) {
      int[][] mirroredPiece = new int[newPiece.length][newPiece[0].length];
      for (int i = 0; i < newPiece.length; i++) {
        for (int j = 0; j < newPiece[i].length; j++) {
          mirroredPiece[i][newPiece[i].length - 1 - j] = newPiece[i][j];
        }
      }
      newPiece = mirroredPiece;
    }

    // Print the rotated and possibly mirrored piece
    for (int[] row : newPiece) {
      for (int cell : row) {
        System.out.print(cell == 1 ? "#" : " ");
      }
      System.out.println();
    }
  }

  // Display the board as a 14x14 grid of . (dot) for empty squares, and X or O
  // for squares occupied by pieces. Empty starting squares are marked by s, not .
  // (dot)
  // the starting spots are 5,5 and 10,10
  // here's how the board should look like
  /*
   * 1 2 3 4 5 6 7 8 9 t 1 2 3 4
   * 1 . . . . . . . . . . . . . .
   * 2 . . . . . . . . . . . . . .
   * 3 . . . . . . . . . . . . . .
   * 4 . . . . . . . . . . . . . .
   * 5 . . . . s . . . . . . . . .
   * 6 . . . . . . . . . . . . . .
   * 7 . . . . . . . . . . . . . .
   * 8 . . . . . . . . . . . . . .
   * 9 . . . . . . . . . . . . . .
   * 10 . . . . . . . . . s . . . .
   * 11 . . . . . . . . . . . . . .
   * 12 . . . . . . . . . . . . . .
   * 13 . . . . . . . . . . . . . . X's hand: I1 I2 I3 L3 I4 L4 O4 S4 T4 I5 L5 P5
   * R5 S5 T5 U5 V5 W5 X5 Y5 Z5
   * 14 . . . . . . . . . . . . . . O's hand: I1 I2 I3 L3 I4 L4 O4 S4 T4 I5 L5 P5
   * R5 S5 T5 U5 V5 W5 X5 Y5 Z5
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    // ... existing code ...

    // Add column numbers
    sb.append("   1 2 3 4 5 6 7 8 9 t 1 2 3 4\n");

    for (int i = 0; i < 14; i++) {
      // Add row number
      sb.append((i < 9 ? " " : "") + (i + 1) + " ");

      for (int j = 0; j < 14; j++) {
        if (board[i][j] == 1) { // Check if the square is filled by player 1's piece
          sb.append("X "); // Append "X " if the square is filled by player 1's piece
        } else if (board[i][j] == -1) { // Check if the square is filled by player 2's piece
          sb.append("O "); // Append "O " if the square is filled by player 2's piece
        } else if ((i == 4 && j == 4) || (i == 9 && j == 9)) {
          sb.append("s "); // Mark the starting positions
        } else {
          sb.append(". ");
        }
      }

      if (i == 12) {
        sb.append(" X's hand: ");
        for (Piece p : player1Pieces) {
          if (!p.isPlayed) {
            sb.append(p.pieceName + " ");
          }
        }
      } else if (i == 13) {
        sb.append(" O's hand: ");
        for (Piece p : player2Pieces) {
          if (!p.isPlayed) {
            sb.append(p.pieceName + " ");
          }
        }
      }
      sb.append("\n");
    }
    if (currentPlayer == PLAYER_0) {
      sb.append("X to move\n");
    } else {
      sb.append("O to move\n");
    }

    // Piece piece = new Piece("Y5", PLAYER_1);
    // List<String> uniqueOrientations = piece.getUniqueOrientations();
    // System.out.println("Unique orientations for piece " + piece.pieceName + ": "
    // + uniqueOrientations);
    return sb.toString();
  }

  @Override
  public Move createMove() {
    return new BlokusMove();
  }

  // Piece placement is described relative to the "base position" which is what is
  // shown in the
  // diagram, with origin at the top-left corner of the bounding rectangle
  // surrounding the piece, and
  // the squares of the piece at corresponding r,c locations. For instance, piece
  // S4 has a 2x3.
  /*
   * A move is given by a piece number, an orientation description, and an (r,c)
   * location on the
   * board on which to place the (0,0) corner of the bounding rectangle (which
   * actually might or
   * might not be a square), with spaces between the three parts. Or “pass,” if
   * there are no valid
   * placing moves.
   * A move like Y5 >| (3,4) for X would place the top left corner of the pattern
   * at board location
   * (3,4). That means it would fill board squares (3,5), (4,4), (4,5), (4,6), and
   * (4,7).
   * X
   * XXXX
   */
  // bounding rectangle, and

  // squares at (0,1), (0,2), (1,0), and (1,1)

  // work on making sure pieces can't be placed directly adjacent to player's
  // piece but diagonally adjacent is okay
  // fix the part where a player can play on another player's piece
  @Override
  public void applyMove(Move m) throws InvalidMoveException {
    BlokusMove move = (BlokusMove) m;
    if (passCount == 2) {
      return;
    }

    List<Piece> pieces = currentPlayer == PLAYER_0 ? player1Pieces : player2Pieces;
    Piece piece = null;
    boolean isFirstMove = moveHistory.isEmpty();
    // Define the starting positions

    // Find the piece to be played
    for (Piece p : pieces) {
      if (p.pieceName.equals(move.pieceName) && !p.isPlayed) {
        piece = p;
        break;
      }
    }

    if (piece == null) {
      throw new InvalidMoveException("Invalid piece or piece already played");
    }

    piece.setOrientation(move.orientation);
    boolean isDiagonallyAdjacent = false;

    for (Location location : piece.locations) {
      int r = move.row + location.row;
      int c = move.col + location.col;

      // print the row and column of the piece
      // System.out.println("Row: " + (1+r) + " Column: " + (1+c));

      // Check for valid board position
      if (r < 0 || r >= DIM || c < 0 || c >= DIM) {
        throw new InvalidMoveException("Piece placement is out of board bounds");
      }

      if (board[r][c] != 0) {
        throw new InvalidMoveException("Piece placement overlaps with another piece");
      }

      if (isFirstMove) {
        if ((r == STARTING_SPOT_ROW && c == STARTING_SPOT_COL)) {
          isFirstMove = false;
          usedStartingSpot = new Location(r, c);
          unUsedStartingSpot = new Location(STARTING_SPOT_ROW2, STARTING_SPOT_COL2);
          // store the unused starting spot in a variable
        } else if ((r == STARTING_SPOT_ROW2 && c == STARTING_SPOT_COL2)) {
          isFirstMove = false;
          usedStartingSpot = new Location(r, c);
          unUsedStartingSpot = new Location(STARTING_SPOT_ROW, STARTING_SPOT_COL);
          // store the unused starting spot in a variable
        } else {
          throw new InvalidMoveException("First move must be on the starting spots");
        }
      } else if (moveHistory.size() == 1) {
        if (currentPlayer == PLAYER_1 && unUsedStartingSpot != null) {
          if (r == unUsedStartingSpot.row && c == unUsedStartingSpot.col) {
            unUsedStartingSpot = null;
          } else {
            throw new InvalidMoveException("Second move must be on the unused starting spot");
          }
        }
      }

      // use the diagonal adjacent method to check if the piece is diagonally adjacent
      // to
      // another piece by the same player. If it is, then check if all squares filled
      // by the piece are directly adjacent to the other piece. If they are, then the
      // move is not valid
      // because pieces owned by the same player cannot be directly adjacent to each
      // other
      // if the piece is diagonally adjacent to another piece by the same player, then
      // the move is valid if
      // all squares filled by the piece are not directly adjacent to the other piece
      List<Location> diagonalAdjacentLocations = getDiagonalAdjacentLocations(piece, move);
      System.out.println(diagonalAdjacentLocations + " diagonal adjacent squares");
      for (Location loc : diagonalAdjacentLocations) {
        int newRow = loc.row;
        int newCol = loc.col;
        if (newRow >= 0 && newRow < 14 && newCol >= 0 && newCol < 14) {
          if (board[newRow][newCol] == currentPlayer) {
            isDiagonallyAdjacent = true;
            List<Location> adjacentLocations = getAdjacentLocations(piece, move);
            System.out.println(adjacentLocations + " adjacent squares");
            for (Location loca : adjacentLocations) {
              int actualRow = move.row + loca.row;
              int actualCol = move.col + loca.col;
              if (actualRow >= 0 && actualRow < 14 && actualCol >= 0 && actualCol < 14) {
                System.out.println(board[actualRow][actualCol] + " board square");
                System.out.println(actualRow + " row " + actualCol + " col");
                if (board[actualRow][actualCol] != '.') {
                  throw new InvalidMoveException("Piece placement is directly adjacent to another piece");
                }
              }
            }
          }
        }
      }

      if (moveHistory.size() > 1) {
        if (!isDiagonallyAdjacent) {
          throw new InvalidMoveException("Piece placement is not diagonally adjacent to another piece");
        }
      }
      board[r][c] = currentPlayer;
    }
    // List<Location> adjacentLocations = getDiagonalAdjacentLocations(piece, move);
    // for (Location location : adjacentLocations) {
    // System.out.println("Adjacent square at row: " + (location.row + 1) + " col: "
    // + (location.col + 1));
    // }

    // Place the piece on the board

    piece.isPlayed = true; // Mark the piece as played
    moveHistory.add(new BlokusMove(move)); // Add the move to history
    passCount = 0;
    currentPlayer = -currentPlayer;

  }

  // write a method that checks a piece on the board and returns a list of
  // locations adjacent to the piece
  public List<Location> getAdjacentLocations(Piece piece, BlokusMove move) {
    List<Location> adjacentLocations = new ArrayList<>();
    for (Location location : piece.locations) {
      int r = move.row + location.row;
      int c = move.col + location.col;
      // Check for valid board position
      if (r < 0 || r >= DIM || c < 0 || c >= DIM) {
        continue;
      }
      // Add all squares adjacent to the piece
      addAdjacentLocations(adjacentLocations, r, c);
    }
    return adjacentLocations;
  }

  private void addAdjacentLocations(List<Location> adjacentLocations, int r, int c) {
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (i == 0 && j == 0) {
          continue;
        }
        int newRow = r + i;
        int newCol = c + j;
        if (newRow >= 0 && newRow < DIM && newCol >= 0 && newCol < DIM) {
          adjacentLocations.add(new Location(newRow, newCol));
        }
      }
    }
  }

  public List<Location> getDiagonalAdjacentLocations(Piece piece, BlokusMove move) {
    List<Location> diagonalAdjacentLocations = new ArrayList<>();
    for (Location location : piece.locations) {
      int r = move.row + location.row;
      int c = move.col + location.col;
      // Check for valid board position
      if (r < 0 || r >= DIM || c < 0 || c >= DIM) {
        continue;
      }
      // Add all squares diagonally adjacent to the piece
      addDiagonalAdjacentLocations(diagonalAdjacentLocations, r, c);
    }
    return diagonalAdjacentLocations;
  }

  private void addDiagonalAdjacentLocations(List<Location> diagonalAdjacentLocations, int r, int c) {
    for (int i = -1; i <= 1; i += 2) {
      for (int j = -1; j <= 1; j += 2) {
        int newRow = r + i;
        int newCol = c + j;
        if (newRow >= 0 && newRow < DIM && newCol >= 0 && newCol < DIM) {
          diagonalAdjacentLocations.add(new Location(newRow, newCol));
        }
      }
    }
  }

  @Override
  public int getValue() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getValue'");
  }

  @Override
  public List<? extends Move> getValidMoves() {
    return moveHistory;
  }

  @Override
  public int getCurrentPlayer() {
    return currentPlayer;
  }

  @Override
  public List<? extends Move> getMoveHistory() {
    return moveHistory;
  }

  @Override
  public void undoMove() {
    if (moveHistory.isEmpty()) {
      System.out.println("No moves to undo");
      return;
    }

    BlokusMove lastMove = moveHistory.remove(moveHistory.size() - 1); // Remove the last move from the history
    System.out.println("Undoing move: " + lastMove);

    // Switch player first to get the correct player who made the last move
    currentPlayer = -currentPlayer;

    // Now, retrieve the piece that was played in the last move
    List<Piece> pieces = currentPlayer == PLAYER_0 ? player1Pieces : player2Pieces;
    Piece playedPiece = null;
    for (Piece p : pieces) {
      if (p.pieceName.equals(lastMove.pieceName)) {
        playedPiece = p;
        break;
      }
    }

    if (playedPiece == null) {
      throw new IllegalStateException("Cannot find the last played piece in pieces list");
    }

    // Set the piece as not played
    playedPiece.isPlayed = false;

    // print the player's hand
    // add the piece back to the player's hand

    // Remove the piece from the board by setting the corresponding squares to 0
    // (empty)
    playedPiece.setOrientation(lastMove.orientation); // Ensure orientation matches the move
    for (Location loc : playedPiece.locations) {
      int actualRow = lastMove.row + loc.row;
      int actualCol = lastMove.col + loc.col;
      if (actualRow >= 0 && actualRow < 14 && actualCol >= 0 && actualCol < 14) {
        board[actualRow][actualCol] = 0; // Clear the position on the board
      }
    }

    // Reset the piece's orientation to the base configuration
    playedPiece.resetToBaseConfiguration();

    // Decrement pass count if the last move was a pass
    if (lastMove.pass) {
      passCount--;
    }
  }
}
