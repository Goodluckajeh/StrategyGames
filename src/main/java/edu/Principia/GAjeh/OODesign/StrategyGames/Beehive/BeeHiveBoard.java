package edu.Principia.GAjeh.OODesign.StrategyGames.Beehive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import edu.Principia.OODesign.StrategyGames.Board;

public class BeeHiveBoard implements Board, Serializable {
    public class BeeHiveBoardMove implements Board.Move, Serializable {
        public int row;
        public int col;
        public boolean swap;
        public int previousOwner;

        public BeeHiveBoardMove() {
            row = -1;
            col = -1;
            swap = false;
        }

        public BeeHiveBoardMove(int r, int c) {
            row = r;
            col = c;
            swap = false;
        }

        public BeeHiveBoardMove(boolean s) {
            row = -1;
            col = -1;
            swap = s;
        }

        public BeeHiveBoardMove(BeeHiveBoardMove m) {
            row = m.row;
            col = m.col;
            swap = m.swap;
            previousOwner = m.previousOwner;
        }

        public void write(OutputStream os) throws IOException {
            if (swap) {
                int b = 0;
                os.write(b);
            } else {
                int b = (row << 4) | col;
                os.write(b);
            }
        }

        public void read(InputStream is) throws IOException {
            int b = is.read();
            if (b == -1) {
                throw new IOException("End of stream");
            }
            if (b == 0) {
                swap = true;
            } else {
                swap = false;
                row = (b >> 4) & 0x0F;
                col = b & 0x0F;
            }
        }

        public void fromString(String s) throws IOException {
            if (s.equals("swap")) {
                swap = true;
            } else {
                swap = false;
                String[] parts = s.split(",");
                if (parts.length != 2) {
                    throw new IOException("Invalid format");
                }
                try {
                    row = Integer.parseInt(parts[0].trim());
                    col = Integer.parseInt(parts[1].trim());
                    if (row < 1 || row > 11 || col < 1 || col > 11) {
                        throw new IOException("Invalid move string");
                    }
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid move string");
                }
            }
        }

        public int compareTo(Board.Move m) {
            BeeHiveBoardMove move = (BeeHiveBoardMove) m;
            if (swap && !move.swap) {
                return -1;
            } else if (!swap && move.swap) {
                return 1;
            } else if (swap && move.swap) {
                return 0;
            } else if (row < move.row) {
                return -1;
            } else if (row > move.row) {
                return 1;
            } else if (col < move.col) {
                return -1;
            } else if (col > move.col) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            if (swap) {
                return "swap";
            } else {
                return row + "," + col;
            }
        }
    }

    /*
     * Implementation of Board
     * Location class
     * We agreed in class that Location is an independent concept, used in Moves and
     * in Cells, and includes a means of adding Locations to one another, and to
     * determine if a Location is in bounds.
     */
    public class Location implements Serializable {
        public int row;
        public int col;

        public Location(int r, int c) {
            row = r;
            col = c;
        }

        public Location(Location l) {
            row = l.row;
            col = l.col;
        }

        public Location add(Location l) {
            return new Location(row + l.row, col + l.col);
        }

        public boolean inBounds() {
            return row >= 0 && row < 11 && col >= 0 && col < 11;
        }

        @Override
        public String toString() {
            return "(" + row + ", " + col + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Location location = (Location) obj;
            return row == location.row && col == location.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    /*
     * Cell class
     * Represents one cell in the grid, including a list of its adjacent cells and a
     * list of its bridged cells. These lists are computed once and then cached for
     * future use. The cell also indicates which player has a stone in it, if any,
     * and if it has a stone, which bridge group it is in. (Any cell with a stone
     * must be in exactly one bridge group)
     */

    public class cell implements Serializable {
        public int row;
        public int col;
        public Location loc;
        public int player;
        public int bridgeGroup;
        BridgeGroup group;
        public List<Location> adjacent;
        public List<Location> bridged;
        int owner;
        boolean visited = false;
        boolean printed = false;
        public Location[] adjOffsets = new Location[] { new Location(0, 1), new Location(1, 0), new Location(1, -1),
                new Location(0, -1), new Location(-1, 0), new Location(-1, 1) };
        public Location[] bridgeOffsets = new Location[] { new Location(-1, -1), new Location(-1, 2),
                new Location(-2, 1),
                new Location(1, -2), new Location(1, 1), new Location(2, -1) };

        public cell(int r, int c) {
            player = 0;
            row = r;
            col = c;
            loc = new Location(r, c);
            bridgeGroup = -1;
            adjacent = new ArrayList<Location>();
            bridged = new ArrayList<Location>();
        }

        public void computeAdj() {
            adjacent.clear();
            for (int i = 0; i < 6; i++) {
                Location loc = new Location(row, col).add(adjOffsets[i]);
                if (loc.inBounds()) {
                    adjacent.add(loc);
                }
            }
        }

        public boolean contains(cell o) {
            return (row == o.row && col == o.col);
        }

        // write the same computeAdj method but it should return a list of locations
        // adjacent to the cell
        // use the adjOffsets array to get the adjacent locations
        // add the adjacent locations to the list of adjacent locations
        // return the list of adjacent locations
        public List<Location> computeAdjLoc() {
            List<Location> adj = new ArrayList<Location>();
            for (int i = 0; i < 6; i++) {
                Location loc = new Location(row, col).add(adjOffsets[i]);
                if (loc.inBounds()) {
                    adj.add(loc);
                }
            }
            return adj;
        }

        public void computeBridge() {
            bridged.clear();
            for (int i = 0; i < 6; i++) {
                Location loc = new Location(row, col).add(bridgeOffsets[i]);
                if (loc.inBounds()) {
                    bridged.add(loc);
                }
            }
        }

        public void setOwner(int newOwner) {
            owner = newOwner;
            // System.out.println(
            // "Cell at " + (loc.row + 1) + ", " + (loc.col + 1) + " is now owned by player
            // " + newOwner + ".");
            if (newOwner == 1) {
                player = 1;
            } else if (newOwner == 2) {
                player = 2;
            }

        }

        public int getOwner() {
            return owner;
        }

        @Override
        public String toString() {
            return "Cell at (" + loc.row + ", " + loc.col + ")";
        }

    } // Add closing brace here

    /*
     * Connections and BridgeGroup class
     * Cells have two kinds of connections: an adjacency and a bridge. Two cells are
     * adjacent if they share an edge. Two cells are bridged if they are two steps
     * apart, with exactly two cells between them, to which they are both adjacent.
     * And, both those two cells must be empty. Either case ensures the possibility
     * of a winning path between the cells, since if the opponent takes one of the
     * empty cells in a bridge, the player may immediately take the other, ensuring
     * the connection.
     * 
     * 
     * A bridge group is a maximal set of cells of the same color, all of which are
     * adjacent to at least one other cell in the group, or which have a bridge to
     * another cell in the group.
     */

    public class Connection implements Serializable {
        cell cell1;
        cell cell2;

        public Connection(cell c1, cell c2) {
            this.cell1 = c1;
            this.cell2 = c2;
        }
    }

    public class BridgeGroup implements Serializable {
        private List<cell> cells;
        List<Connection> connections = new ArrayList<Connection>();
        public int minRow;
        public int maxRow;
        public int minCol;
        public int maxCol;
        int span = 0;
        private int owner;
        private cell cell1;
        private cell cell2;

        public cell getOtherCell(cell cell) {
            if (cell.equals(cell1)) {
                return cell2;
            } else if (cell.equals(cell2)) {
                return cell1;
            } else {
                throw new IllegalArgumentException("Cell not found in group");
            }
        }

        public int getOwner() {
            return this.owner;
        }

        public BridgeGroup() {
            cells = new ArrayList<cell>();
            minRow = Integer.MAX_VALUE;
            maxRow = Integer.MIN_VALUE;
            minCol = Integer.MAX_VALUE;
            maxCol = Integer.MIN_VALUE;
        }

        public int getWeight(int player) {
            return cells.size() * getSpan(player);
        }

        public void formBridge(cell c1, cell c2) {
            connections.add(new Connection(c1, c2));
        }

        public void add(cell c) {
            if (this.cells.contains(c)) {
                return;
            }
            this.cells.add(c);
            c.group = this;

            if (c.player == 1) {
                int oldSpan = this.maxCol - this.minCol + 1;
                this.minCol = Math.min(this.minCol, c.col);
                this.maxCol = Math.max(this.maxCol, c.col);
                if (this.maxCol - this.minCol + 1 > oldSpan) {
                    this.span++;
                }
            } else if (c.player == 2) {
                int oldSpan = this.maxRow - this.minRow + 1;
                this.minRow = Math.min(this.minRow, c.row);
                this.maxRow = Math.max(this.maxRow, c.row);
                if (this.maxRow - this.minRow + 1 > oldSpan) {
                    this.span++;
                }
            }

            if (c.group != null && c.group != this) {
                for (cell otherCell : new ArrayList<>(c.group.cells)) {
                    this.add(otherCell);
                }
            }
        }

        public int getSpan(int player) {
            if (player == 1) {
                return maxCol - minCol + 1;
            } else { // Player 2
                return maxRow - minRow + 1;
            }
        }

    }

    private cell[][] board;
    private int currentPlayer;
    private List<BeeHiveBoardMove> moveHistory;
    List<Connection> connections;
    List<BridgeGroup> bridgeGroups;
    boolean hasSwapped;
    static final int SIZE = 11;
    BridgeGroup winningGroup = null;
    private boolean isGameOver = false;
    int totalWeightPlayer1 = 0;
    int totalWeightPlayer2 = 0;

    public BeeHiveBoard() {
        board = new cell[11][11];
        currentPlayer = 1;
        moveHistory = new ArrayList<BeeHiveBoardMove>();
        connections = new ArrayList<Connection>();
        bridgeGroups = new ArrayList<BridgeGroup>();
        hasSwapped = false;

        for (int r = 0; r < 11; r++) {
            for (int c = 0; c < 11; c++) {
                board[r][c] = new cell(r, c);
            }
        }
        for (int r = 0; r < 11; r++) {
            for (int c = 0; c < 11; c++) {
                board[r][c].computeAdj();
                board[r][c].computeBridge();
            }
        }
    }

    public Board.Move createMove() {
        return new BeeHiveBoardMove();
    }

    public void applyMove(Board.Move m) throws Board.InvalidMoveException {
        try {
            if (isGameOver) {
                throw new Board.InvalidMoveException("Game is over");
            }
            BeeHiveBoardMove move = new BeeHiveBoardMove((BeeHiveBoardMove) m);

            if (move.row < 1 || move.row > 11 || move.col < 1 || move.col > 11) {
                throw new Board.InvalidMoveException("Move out of bounds");
            }

            bridgeGroups.clear();

            if (move.swap && moveHistory.size() == 1 && !hasSwapped && currentPlayer == 2) {
                BeeHiveBoardMove firstMove = moveHistory.get(0);
                move.previousOwner = board[firstMove.row - 1][firstMove.col - 1].player;
                board[firstMove.row - 1][firstMove.col - 1].setOwner(currentPlayer);
                currentPlayer = 3 - currentPlayer;
                hasSwapped = true;
            } else if (move.swap && moveHistory.size() == 1 && !hasSwapped && currentPlayer == 2) {
                move.previousOwner = board[move.row - 1][move.col - 1].player;
                board[move.row - 1][move.col - 1].setOwner(currentPlayer);
                currentPlayer = 3 - currentPlayer;
                hasSwapped = true;
            } else {
                if (board[move.row - 1][move.col - 1].player != 0) {
                    throw new Board.InvalidMoveException("Invalid move");
                }
                move.previousOwner = board[move.row - 1][move.col - 1].player;
                board[move.row - 1][move.col - 1].player = currentPlayer;

                Connection connectionToRemove = null;
                for (Connection conn : connections) {
                    if ((conn.cell1.row == move.row - 1 && conn.cell1.col == move.col - 1) ||
                            (conn.cell2.row == move.row - 1 && conn.cell2.col == move.col - 1)) {
                        connectionToRemove = conn;
                        break;
                    }
                }
                if (connectionToRemove != null) {
                    connections.remove(connectionToRemove);
                }
                currentPlayer = 3 - currentPlayer;
            }
            moveHistory.add(move);
            recalculateBridgeGroups();

            int gameState = getValue();
            if (gameState == WIN || gameState == -WIN) {
                isGameOver = true;
            }
        } catch (Board.InvalidMoveException e) {
            System.out.println(e.getMessage());
            System.out.println("Possible moves: " + getValidMoves());
        }
    }

    public void recalculateBridgeGroups() {
        for (cell[] row : board) {
            for (cell c : row) {
                c.visited = false;
                c.group = null;
            }
        }
        bridgeGroups.clear();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!board[row][col].visited && board[row][col].player != 0) {
                    BridgeGroup bg = new BridgeGroup();
                    dfsFindGroups(board[row][col], bg, board[row][col].player);
                    bridgeGroups.add(bg);
                }
            }
        }
    }

    public void dfsFindGroups(cell c, BridgeGroup bg, int player) {
        if (c.visited)
            return;

        c.visited = true;
        if (c.player == player)
            bg.add(c);

        for (Location adjLoc : c.adjacent) {
            cell adjCell = board[adjLoc.row][adjLoc.col];
            if (!adjCell.visited && adjCell.player == player) {
                dfsFindGroups(adjCell, bg, player);
            }
        }

        for (Location bridgeLoc : c.bridged) {
            cell bridgeCell = board[bridgeLoc.row][bridgeLoc.col];
            if (!bridgeCell.visited && bridgeCell.player == player && isValidBridge(c, bridgeCell)) {
                dfsFindGroups(bridgeCell, bg, player);
            }
        }
    }

    public boolean isValidBridge(cell c1, cell c2) {
        if (c1.player != c2.player || c1.adjacent.contains(c2.loc)) {
            return false;
        }
        List<Location> intermediateLocations = getIntermediateLocations(c1, c2);

        for (Location loc : intermediateLocations) {
            if (loc.row > 0 && loc.row <= board.length && loc.col > 0 && loc.col <= board.length) {
                if (board[loc.row - 1][loc.col - 1].player != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Location> getIntermediateLocations(cell c1, cell c2) {
        c1.computeAdj();
        c2.computeAdj();
        List<Location> c1Locs = new ArrayList<Location>();
        List<Location> c2Locs = new ArrayList<Location>();
        for (Location adjCell : c1.adjacent) {
            if (adjCell != null) {
                Location incrementedAdjcell = new Location(adjCell.row + 1, adjCell.col + 1);
                c1Locs.add(incrementedAdjcell);
            }
        }
        for (Location adjCell : c2.adjacent) {
            if (adjCell != null) {
                Location incrementedAdjcell = new Location(adjCell.row + 1, adjCell.col + 1);
                c2Locs.add(incrementedAdjcell);
            }
        }
        List<Location> intermediateLocations = new ArrayList<Location>();
        for (Location loc : c1Locs) {
            if (c2Locs.contains(loc)) {
                intermediateLocations.add(loc);
            }
        }
        return intermediateLocations;
    }

    public List<? extends Board.Move> getValidMoves() {
        List<BeeHiveBoardMove> validMoves = new ArrayList<BeeHiveBoardMove>();
        for (int r = 0; r < 11; r++) {
            for (int c = 0; c < 11; c++) {
                if (board[r][c].player == 0) {
                    validMoves.add(new BeeHiveBoardMove(r + 1, c + 1));
                }
            }
        }
        if (moveHistory.size() == 1) {
            validMoves.add(new BeeHiveBoardMove(true));
        }

        if (getValue() == WIN || getValue() == -WIN) {
            validMoves.clear();
        }
        return validMoves;
    }

    public int getCurrentPlayer() {
        return currentPlayer == 1 ? 1 : -1;
    }

    public List<? extends Board.Move> getMoveHistory() {
        return moveHistory;
    }

    public void undoMove() {
        if (moveHistory.isEmpty()) {
            return;
        }

        BeeHiveBoardMove lastMove = moveHistory.remove(moveHistory.size() - 1);
        BeeHiveBoardMove move = new BeeHiveBoardMove(lastMove);

        if (lastMove.swap) {
            hasSwapped = false;
            if (!moveHistory.isEmpty()) {
                BeeHiveBoardMove firstMove = moveHistory.get(0);
                if (firstMove.row >= 1 && firstMove.row <= 11 && firstMove.col >= 1 && firstMove.col <= 11) {
                    board[firstMove.row - 1][firstMove.col - 1].player = 1;
                }
            }
        } else {
            if (move.row >= 1 && move.row <= 11 && move.col >= 1 && move.col <= 11) {
                board[move.row - 1][move.col - 1].player = 0;
            }
        }

        currentPlayer = 3 - currentPlayer;

        recalculateBridgeGroups();

        isGameOver = false;
        int gameState = getValue();
        if (gameState == WIN || gameState == -WIN) {
            isGameOver = true;
        }
    }

    public int getValue() {
        winningGroup = null;
        totalWeightPlayer1 = 0;
        totalWeightPlayer2 = 0;
        bridgeGroups.clear();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col].visited = false;
            }
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!board[row][col].visited && board[row][col].player != 0) {
                    BridgeGroup bg = new BridgeGroup();
                    dfsFindGroups(board[row][col], bg, board[row][col].player);
                    bridgeGroups.add(bg);

                    if (board[row][col].player == 1) {
                        totalWeightPlayer1 += bg.getWeight(1);
                    } else {
                        totalWeightPlayer2 += bg.getWeight(2);
                    }
                }
            }
        }
        for (BridgeGroup bg : bridgeGroups) {
            if (isWinning(bg)) {
                winningGroup = bg;
                return bg.getOwner() == 1 ? WIN : -WIN;
            }
        }
        return totalWeightPlayer1 - totalWeightPlayer2;
    }

    private boolean isWinning(BridgeGroup bg) {
        if (bg == null || bg.cells.isEmpty()) {
            return false;
        }
        if (bg.cells.get(0).player == 1) {
            return spansLeftToRight(bg);
        } else {
            return spansTopToBottom(bg);
        }
    }

    private boolean spansLeftToRight(BridgeGroup bg) {
        List<cell> leftEdgeCells = bg.cells.stream().filter(c -> c.col == 0).collect(Collectors.toList());
        bg.cells.forEach(c -> c.visited = false);

        for (cell startCell : leftEdgeCells) {
            if (dfsLeftToRight(startCell, bg)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfsLeftToRight(cell c, BridgeGroup bg) {
        if (c.col == 10) {
            return true;
        }
        c.visited = true;
        for (Location loc : c.adjacent) {
            cell adjCell = board[loc.row][loc.col];
            if (adjCell.player == c.player && !adjCell.visited) {
                if (dfsLeftToRight(adjCell, bg)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean spansTopToBottom(BridgeGroup bg) {
        List<cell> topEdgeCells = bg.cells.stream().filter(c -> c.row == 0).collect(Collectors.toList());
        bg.cells.forEach(c -> c.visited = false);

        for (cell startCell : topEdgeCells) {
            if (dfsTopToBottom(startCell, bg)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfsTopToBottom(cell c, BridgeGroup bg) {
        if (c.row == 10) {
            return true;
        }
        c.visited = true;
        for (Location loc : c.adjacent) {
            cell adjCell = board[loc.row][loc.col];
            if (adjCell.player == c.player && !adjCell.visited) {
                if (dfsTopToBottom(adjCell, bg)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("  1 2 3 4 5 6 7 8 9 T E\n");
        for (int r = 0; r < 11; r++) {
            for (int i = 0; i < r; i++) {
                sb.append(" ");
            }
            sb.append(r < 9 ? (char) ('1' + r) : r == 9 ? "T" : "E").append(" ");
            for (int c = 0; c < 11; c++) {
                cell currentCell = board[r][c];
                if (currentCell.player == 0) {
                    sb.append(".");
                } else {
                    char displayChar = currentCell.player == 1 ? 'b' : 'r';
                    if (winningGroup != null && winningGroup.cells.contains(currentCell)) {
                        displayChar = Character.toUpperCase(displayChar);
                    }
                    sb.append(displayChar);
                }
                if (c < 10) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        if (isGameOver) {
            getValue();
        }
        return sb.toString();
    }
}