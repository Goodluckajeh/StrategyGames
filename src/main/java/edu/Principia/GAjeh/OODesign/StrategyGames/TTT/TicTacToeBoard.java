package edu.Principia.GAjeh.OODesign.StrategyGames.TTT;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import edu.Principia.OODesign.StrategyGames.Board;

public class TicTacToeBoard implements Board, Serializable{
   public class TicTacToeMove implements Board.Move, Serializable {
        public int row;
        public int col;
        public TicTacToeMove() {
             row = -1;
             col = -1;
        }

        public TicTacToeMove(int r, int c) {
             row = r;
             col = c;
        }

        public TicTacToeMove(TicTacToeMove m) {
             row = m.row;
             col = m.col;
        }
        // Write the move as a single byte row and column, with row in the high
        // nibble and column in the low nibble
        @Override
        public void write(OutputStream os) throws IOException {
            int b = (row << 4) | col;
            os.write(b);
        }
        // Read the move as a single byte row and column, with row in the high
        // nibble and column in the low nibble
        @Override
        public void read(InputStream is) throws IOException {
             int b = is.read();
             if (b == -1) {
                throw new IOException("End of stream");
             }
             row = b >> 4;
             col = b & 0x0F;
        }

        @Override
        public void fromString(String s) throws IOException {
             String[] parts = s.split(",");
             if (parts.length != 2) {
                throw new IOException("Invalid format");
             }
             try {
                row = Integer.parseInt(parts[0]) - 1;
                col = Integer.parseInt(parts[1]) - 1;
                if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
                    throw new IOException("Invalid move string");
                }
             }
             catch (NumberFormatException e) {
                throw new IOException("Invalid move string");
             }

        }

        @Override
        public int compareTo(Board.Move m) {
             TicTacToeMove other = (TicTacToeMove) m;
             return row == other.row ? col - other.col : row - other.row;
        }

        @Override
        public String toString() {
            return (this.row + 1) + "," + (this.col + 1);
        }
   }

   private int[][] board;
   // 0 or 1 for first or second player
   private int currentPlayer; // 0 or 1 for first or second player
    private List<TicTacToeMove> moveHistory;
    static final int SIZE = 3;

    public TicTacToeBoard() {
        board = new int[SIZE][SIZE];
        currentPlayer = 0;
        moveHistory = new LinkedList<TicTacToeMove>();
    }

   @Override
    public Board.Move createMove() {
        return new TicTacToeMove();
    }

    @Override
    public void applyMove(Board.Move m) throws Board.InvalidMoveException{
        TicTacToeMove move = new TicTacToeMove((TicTacToeMove) m);
        if (move.row < 0 || move.row >= SIZE || move.col < 0 || move.col >= SIZE) {
            throw new Board.InvalidMoveException("Move out of bounds");
        }
        if (board[move.row][move.col] != 0) {
            throw new Board.InvalidMoveException("Invalid move");
        }
        board[move.row][move.col] = currentPlayer == 0 ? 1 : -1;
        moveHistory.add(move);
        currentPlayer = 1 - currentPlayer;
    }

    @Override
    public List<? extends Move> getValidMoves() {
        java.util.List<TicTacToeMove> validMoves = new java.util.ArrayList<TicTacToeMove>();
        for (int x = 0; x < SIZE; x++) {
           for (int y = 0; y < SIZE; y++) {
              if (board[x][y] == 0) {
                TicTacToeMove move = new TicTacToeMove(x, y);
                 
                 validMoves.add(move);
              }
           }
        }
        //if one of the players have won there should be no valid move
        if (getValue() == WIN || getValue() == -WIN){
           validMoves.clear();
        }
        return validMoves;
    }

    //return a value for the current board. if either player has won, return
    //WIN for player 0, -WIN for player 1, or 0 for a draw. Otherwise, return
    //a value based on the number of rows, cols or diagonals for which a
    //player has two in a row and one empty space. Add 1 to the value for each
    // such case for the first player, and subtract 1 for each such case for the second.
    // This will allow the AI to prefer moves that lead to a win.

    @Override
    public int getValue() {
        // compute totals for each row, col, and diagonal
        int[] rowTotal = new int[SIZE];
        int[] colTotal = new int[SIZE];
        int diag1Total = 0;
        int diag2Total = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                rowTotal[row] += board[row][col];
                colTotal[col] += board[row][col];
                if (row == col) {
                    diag1Total += board[row][col];
                }
                if (row + col == SIZE - 1) {
                    diag2Total += board[row][col];
                }
            }
        }
        //check for a win
        if (diag1Total == SIZE || diag2Total == SIZE) {
            return WIN;
        }
        if (diag1Total == -SIZE || diag2Total == -SIZE) {
            return -WIN;
        }
        for (int i = 0; i < SIZE; i++) {
            if (rowTotal[i] == SIZE || colTotal[i] == SIZE) {
                return WIN;
            }
            if (rowTotal[i] == -SIZE || colTotal[i] == -SIZE) {
                return -WIN;
            }
        }
        //compute numbers of filled squares and return 0 if the board is full
        int filledSquares = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] != 0) {
                    filledSquares++;
                }
            }
        }
        if (filledSquares == SIZE * SIZE) {
            return 0;
        }

        //use moveHistory to determine if the game is a draw and return 0 if so
        if (moveHistory.size() == SIZE * SIZE) {
            return 0;
        }

        //check for two in a row and return a value based on the number of such cases
        int value = 0;
        for (int i = 0; i < SIZE; i++) {
                        
            if (rowTotal[i] == 2) {
                value++;
            }
            if (rowTotal[i] == -2) {
                value--;
            }
            if (colTotal[i] == 2) {
                value++;
            }
            if (colTotal[i] == -2) {
                value--;
            }
        }
        if (diag1Total == 2) {
            value++;
        }
        if (diag1Total == -2) {
            value--;
        }
        if (diag2Total == 2) {
            value++;
        }
        if (diag2Total == -2) {
            value--;
        }
        return value;
    }

    @Override
    public int getCurrentPlayer() {
        return currentPlayer == 0 ? PLAYER_0 : PLAYER_1;
    }

    @Override
    public List<? extends Move> getMoveHistory() {
        return moveHistory;
    }

    @Override
    public void undoMove() {
        if (moveHistory.size() > 0) {
            TicTacToeMove lastMove = moveHistory.remove(moveHistory.size() - 1);
            board[lastMove.row][lastMove.col] = 0;
            currentPlayer = 1 - currentPlayer;
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                sb.append(board[i][j] == 0 ? "." : board[i][j] == 1 ? "X" : "O");
                if (j < board[i].length - 1) {
                    sb.append(" | ");
                }
            }
            sb.append("\n");
            if (i < board.length - 1) {
                sb.append("---------\n");
            }
        }
        return sb.toString();
    }
}
