package edu.Principia.OODesign.StrategyGames;

import java.util.List;

import edu.Principia.OODesign.StrategyGames.Board.InvalidMoveException;
public class AISolver {
    // provide a minimax solution for board classes

    public static class MMResult {
        public Board.Move move;
        public int value;
    }
    
    static void miniMax(Board board, int min, int max, int level, MMResult result) throws InvalidMoveException {
        // get the valid moves and store it in a variable
        //System.out.printf("MM (%d, %d) lvl %d\n", min, max, level);

        List<? extends Board.Move> moves = board.getValidMoves();

        // check if the level is 0, if it is, set the value of the board to the result
        // value and return
        if (level == 0 || moves.isEmpty()) {
            result.value = board.getValue();
            result.move = null;
            return;
        }

        if (board.getCurrentPlayer() == Board.PLAYER_0) {
            result.value = min;
            for (Board.Move move : moves) {
                try {board.applyMove(move);} catch (edu.Principia.OODesign.StrategyGames.Board.InvalidMoveException e) {e.printStackTrace();}
                MMResult childResult = new MMResult();
                miniMax(board, result.value, max, level - 1, childResult);
                if (childResult.value > result.value) {
                    result.value = childResult.value;
                    result.move = move;
                }
                board.undoMove();
                if (result.value >= max) {
                    return;
                }
            }
        } 
        else {
            result.value = max;
            for (Board.Move move : moves) {
                board.applyMove(move);
                MMResult childResult = new MMResult();
                miniMax(board, min, result.value, level - 1, childResult);
                if (childResult.value < result.value) {
                    result.value = childResult.value;
                    result.move = move;
                }
                board.undoMove();
                if (result.value <= min) {
                    return;
                }
            }
        }
    }
}
