package edu.Principia.OODesign.StrategyGames;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Queue;

import edu.Principia.OODesign.StrategyGames.Board.InvalidMoveException;

public class Tournament {
    // result of one two-player competition, showing which player won and the
    // moves made by each player.
    public static class CmpResult {
        Board winner;
        Board loser;
    }

    // Run a competition between the two boards, with brd1 playing as player 0
    // use AISolver for each move. Alternate between the two boards, with one
    // board determining its move via AISolver.minimax, and the other board being
    // given
    // the same move so the two boards track together. Each board intially
    // starts at level initLevel.
    // However, if a board has taken an average of more than timePerMove for its
    // moves, its level is reduced by one (but never < 1) until its average time per
    // move
    // is less then timePerMove, whereupon its level is increased by one
    // each move (but never > initLevel). Time used is measured in nanoseconds
    // via ThreadMXBean.getCurrentThreadCpuTime().
    // The competition ends when AIPlayer.miniMax returns a null bestmove. The
    // returned value from AIPlayer.miniMax determines who has won, or if
    // there is a draw. If a draw results, the winner is the board with the lower
    // average time per move.
    // Return the winner and loser of the competition as a CmpResult. if a draw
    // results, the winner is the board with the lower average time per move.
    static CmpResult runCompetition(Board brd0, Board brd1, double timePerMove, int maxLevel)
            throws InvalidMoveException {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        Board boards[] = { brd0, brd1 };
        double times[] = { 0.0, 0.0 };
        int currentPlayer = 0;
        int levels[] = { maxLevel, maxLevel };
        CmpResult result = new CmpResult();

        while (true) {
            AISolver.MMResult res = new AISolver.MMResult();

            long start = bean.getCurrentThreadCpuTime();
            AISolver.miniMax(boards[currentPlayer], Integer.MIN_VALUE,
                    Integer.MAX_VALUE, levels[currentPlayer], res);
            long end = bean.getCurrentThreadCpuTime();
            double time = (end - start) / 1e9; // convert to seconds

            times[currentPlayer] += time - timePerMove; // compute excess time
            if (times[currentPlayer] > 0) { // using too much time
                levels[currentPlayer] = Math.max(1, levels[currentPlayer] - 1);
            } else if (times[currentPlayer] < 0) { // using too little time
                levels[currentPlayer] = Math.min(maxLevel, levels[currentPlayer] + 1);
            }

            if (res.move == null) { // Game over
                // pick winner based on res.value, or break a tie based on time.
                if (res.value == 0) { // Draw, pick winner based on time
                    result.winner = times[0] < times[1] ? boards[0] : boards[1];
                    result.loser = times[0] < times[1] ? boards[1] : boards[0];
                } else {
                    result.winner = res.value > 0 ? boards[0] : boards[1];
                    result.loser = res.value > 0 ? boards[1] : boards[0];
                }
                break;
            } else {
                try {
                    boards[currentPlayer].applyMove(res.move);
                    boards[1 - currentPlayer].applyMove(res.move);
                } catch (Board.InvalidMoveException e) {
                    e.printStackTrace();
                }
            }
            currentPlayer = 1 - currentPlayer;
        }
        return result;
    }

    static private class CmpNode {
        String name;
        Board brd;
        CmpNode left;
        CmpNode right;

        public CmpNode(String name, Board brd) {
            this.name = name;
            this.brd = brd;
        }

        // run a competition between left and right, setting brd to thq winner
        // and name to the name of the winner
        public CmpNode(CmpNode left, CmpNode right, double timePerMove, int maxLevel) throws InvalidMoveException {
            this.left = left;
            this.right = right;
            CmpResult res = runCompetition(left.brd, right.brd, timePerMove, maxLevel);
            this.brd = res.winner;
            this.name = res.winner == left.brd ? left.name : right.name;
        }
    }

    // run a tournament between players specified in a players file, with
    // time per move and max level specified. The command line arguments are name of
    // file,
    // time per move (as a double), and max level (as an int).
    // The file format is a sequence of lines, each with a name and a class name.
    // Classes mus implement the Board interface. For each line, create a
    // new instance of the class, generate a Board from it, and construct a leaf
    // CmpNode
    // with the name and Board.
    //
    // Add each new CmpNode to a queue of CmpNodes. Then, repeatedly
    // pair up the nodes, creating a new CmpNode with the winner of the competition
    // between the two nodes, and adding this new node to the end of the queue.
    // Continue until there is only one unpaired node which is the winner of the
    // tournament.
    // for each pairwise competition, output a line giving the two names of the
    // winner.
    // Finally, output the name of the winner of the tournament.
    public static void main(String[] args)
            throws InvalidMoveException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        if (args.length != 3) {
            System.err.println("Usage: java Tournament playersFile timePerMove maxLevel");
            return;
        }

        double timePerMove = Double.parseDouble(args[1]);
        int maxLevel = Integer.parseInt(args[2]);

        Queue<CmpNode> queue = new LinkedList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                try {
                    Class<?> cls = Class.forName(parts[1]);
                    Board brd = (Board) cls.getConstructor().newInstance();
                    queue.add(new CmpNode(parts[0], brd));
                    System.out.println("Constructed: " + line);
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found: " + parts[1] + " at line " + lineNumber);
                } catch (NoSuchMethodException e) {
                    System.err.println("No constructor found for class " + parts[1] + " at line " + lineNumber);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.err.println("Failed to create instance of class: " + parts[1] + " at line " + lineNumber);
                } catch (ClassCastException e) {
                    System.err.println("Class does not implement Board: " + parts[1] + " at line " + lineNumber);
                }
                lineNumber++;
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e);
            System.exit(1);
        }

        while (queue.size() > 1) {
            CmpNode left = queue.remove();
            CmpNode right = queue.remove();
            CmpNode winner = new CmpNode(left, right, timePerMove, maxLevel);
            System.out.printf("%s vs %s\n", left.name, right.name, winner.name);
            queue.add(winner);
        }

        System.out.println("Winner: " + queue.remove().name);
    }
}
