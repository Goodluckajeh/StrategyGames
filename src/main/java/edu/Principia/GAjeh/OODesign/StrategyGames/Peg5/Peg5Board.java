package edu.Principia.GAjeh.OODesign.StrategyGames.Peg5;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.Principia.OODesign.StrategyGames.Board;

public class Peg5Board implements Board, Serializable {
    public class Peg5Move implements Move, Serializable {
        private int row;
        private int col;
        private Integer SourceRow;
        private Integer SourceCol;
        private String type;

        public Peg5Move(int row, int col, String type) {
            this.row = row;
            this.col = col;
            this.type = type;
        }

        public Peg5Move() {
            row = -1;
            col = -1;
        }

        public Peg5Move(Peg5Move m) {
            this.row = m.row;
            this.col = m.col;
            this.SourceRow = m.SourceRow;
            this.SourceCol = m.SourceCol;
            this.type = m.type;
        }

        public Peg5Move(int row2, int col2, String string, int row3, int col3) {
            this.row = row2;
            this.col = col2;
            this.type = string;
            this.SourceRow = row3;
            this.SourceCol = col3;
        }

        public Integer getSourceRow() {
            return SourceRow;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setSourceRow(Integer sourceRow) {
            SourceRow = sourceRow;
        }

        public Integer getSourceCol() {
            return SourceCol;
        }

        public void setSourceCol(Integer sourceCol) {
            SourceCol = sourceCol;
        }

        @Override
        public void write(OutputStream os) throws java.io.IOException {
            os.write(row);
            os.write(col);
        }

        @Override
        public void read(InputStream is) throws java.io.IOException {
            row = is.read();
            col = is.read();
        }

        // Moves take the string form peg|open|closed (row, col) giving the type of the
        // piece and the 1-based row/col location of play. E.g. peg (2,3) or open (4,5).
        // Moving a piece (once all pieces of that type have been placed) adds an arrow
        // and source after the move, e.g. peg (5,3) <- (2,3) means to move the peg from
        // (2,3) to (5,3)
        @Override
        public void fromString(String s) {
            // Normalize the string to ensure consistent formatting
            s = s.trim().replaceAll("\\s+", " ");

            // Split the string based on spaces to differentiate parts
            String[] parts = s.split(" ");

            // This checks if there is a basic format error
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid format for move.");
            }

            // Split the coordinates of the destination from the type (assumed to be in
            // format like "peg (2,3)")
            type = parts[0]; // This would capture "peg", "open", or "closed"
            String coordPart = parts[1].replaceAll("[()]", ""); // Remove parentheses for easier parsing
            String[] coords = coordPart.split(",");
            if (coords.length != 2) {
                throw new IllegalArgumentException("Destination coordinates format error.");
            }

            // Parse the destination coordinates
            try {
                row = Integer.parseInt(coords[0].trim()) - 1; // Convert to 0-based index
                col = Integer.parseInt(coords[1].trim()) - 1; // Convert to 0-based index
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Destination coordinates must be integers.");
            }

            // Check for the existence of a source part (indicating a move)
            if (parts.length > 2) {
                // Expected move format: "peg (5,3) <- (2,3)"
                if (parts.length != 4 || !parts[2].equals("<-")) {
                    throw new IllegalArgumentException("Invalid format for move.");
                }
                String sourcePart = parts[3].replaceAll("[()]", ""); // Remove parentheses
                String[] sourceCoords = sourcePart.split(",");
                if (sourceCoords.length != 2) {
                    throw new IllegalArgumentException("Source coordinates format error.");
                }

                // Parse the source coordinates
                try {
                    SourceRow = Integer.parseInt(sourceCoords[0].trim()) - 1; // Convert to 0-based index
                    SourceCol = Integer.parseInt(sourceCoords[1].trim()) - 1; // Convert to 0-based index
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Source coordinates must be integers.");
                }
            } else {
                // If no source part, reset these values to indicate no move source
                SourceRow = null;
                SourceCol = null;
            }
        }

        // Moves are sorted in order first by location, in row-major order, and then by
        // piece type, in order peg, open tube, closed tube. Sort transfer moves by
        // destination first, then source.
        @Override
        public int compareTo(Move m) {
            Peg5Move other = (Peg5Move) m;
            if (row != other.row) {
                return row - other.row;
            } else if (col != other.col) {
                return col - other.col;
            } else if (!type.equals(other.type)) {
                return type.compareTo(other.type);
            } else if (SourceRow != null && other.SourceRow != null) {
                if (SourceRow != other.SourceRow) {
                    return SourceRow - other.SourceRow;
                } else {
                    return SourceCol - other.SourceCol;
                }
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            if (SourceRow != null && SourceCol != null) {
                // If the move has a source, include it in the string
                return type + " (" + (row + 1) + "," + (col + 1) + ") <- (" + (SourceRow + 1) + "," + (SourceCol + 1)
                        + ")";
            } else {
                // If the move doesn't have a source, don't include it
                return type + " (" + (row + 1) + "," + (col + 1) + ")";
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Peg5Move) {
                Peg5Move other = (Peg5Move) o;
                return row == other.row && col == other.col && type.equals(other.type);
            }
            return false;
        }
    }

    // write a cell class that will be used to initialize the board using the rows
    // and cols of the cell.
    // the cell class will also keep track of the location of the cell on the board

    public static class Cell implements Serializable {
        public enum Type {
            EMPTY, GREEN_PEG, YELLOW_PEG, GREEN_OPEN_TUBE, GREEN_CLOSED_TUBE, YELLOW_OPEN_TUBE, YELLOW_CLOSED_TUBE,
            GREEN_PEG_YELLOW_OPEN_TUBE, YELLOW_PEG_GREEN_OPEN_TUBE // New types for combined pieces
            , WILDCARD
        }

        private int row;
        private int col;
        private Type type;
        private Type secondPieceType;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.type = Type.EMPTY; // Default to empty
        }

        public boolean hasSecondPiece() {
            return secondPieceType != null;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public void merge(Type newType) {
            if (this.type == Type.GREEN_OPEN_TUBE && newType == Type.YELLOW_PEG) {
                this.type = Type.YELLOW_PEG_GREEN_OPEN_TUBE;
            } else if (this.type == Type.YELLOW_OPEN_TUBE && newType == Type.GREEN_PEG) {
                this.type = Type.GREEN_PEG_YELLOW_OPEN_TUBE;
            }
        }

        @Override
        public String toString() {
            switch (type) {
                case GREEN_PEG:
                    return "G";
                case YELLOW_PEG:
                    return "Y";
                case GREEN_OPEN_TUBE:
                    return "Og";
                case GREEN_CLOSED_TUBE:
                    return "-g";
                case YELLOW_OPEN_TUBE:
                    return "Oy";
                case YELLOW_CLOSED_TUBE:
                    return "-y";
                case GREEN_PEG_YELLOW_OPEN_TUBE:
                    return "Gy";
                case YELLOW_PEG_GREEN_OPEN_TUBE:
                    return "Yg";
                default:
                    return ".";
            }
        }
    }

    public static class CellLoc {
        public int row;
        public int col;

        public CellLoc(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private Cell[][] board;
    private int currentPlayer;
    private List<Peg5Move> moveHistory;
    private final int SIZE = 7;
    private int peg = 0b0001;
    private int openTube = 0b0010;
    private int closedTube = 0b0100;
    private int green = 0b1000;
    private int yellow = 0b10000;
    public int greenPeg;
    public int yellowPeg;
    public int openGreenTube;
    public int closedGreenTube;
    public int openYellowTube;
    public int closedYellowTube;
    // keep a list of pieces for each player
    private int greenPegCount;
    private int yellowPegCount;
    private int greenOpenTubeCount;
    private int greenClosedTubeCount;
    private int yellowOpenTubeCount;
    private int yellowClosedTubeCount;
    public final int numPegs = 10;
    public final int numTubes = 8;
    public final int openTubes = 4;
    public final int closedTubes = 4;
    public final int DRAW = 100;
    public int fourPatternReturn = 10;
    public int fourPattern = 4;
    public int fivePattern = 5;
    public int threePattern = 3;
    public int groupSize = 5;

    // initialize the board as empty cells
    public Peg5Board() {
        board = new Cell[SIZE][SIZE];
        currentPlayer = PLAYER_0;
        moveHistory = new ArrayList<>();
        greenPeg = green | peg;
        yellowPeg = yellow | peg;
        openGreenTube = green | openTube;
        closedGreenTube = green | closedTube;
        openYellowTube = yellow | openTube;
        closedYellowTube = yellow | closedTube;
        greenPegCount = numPegs;
        yellowPegCount = numPegs;
        greenOpenTubeCount = openTubes;
        greenClosedTubeCount = closedTubes;
        yellowOpenTubeCount = openTubes;
        yellowClosedTubeCount = closedTubes;

        // initialize the board with empty cells
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = new Cell(i, j);
            }
        }

    }

    @Override
    public Peg5Move createMove() {
        return new Peg5Move();
    }

    // using the fromString method, apply the move to the board
    // if the move is a peg, place the peg on the board
    // if the move is an open tube, place an open tube on the board
    // if the move is a closed tube, place a closed tube on the board respective to
    // the player
    // so player_0 will have green pegs and tubes and player_1 will have yellow pegs
    // and tubes
    // keep track of the number of pegs and tubes for each player
    @Override
    public void applyMove(Move m) throws InvalidMoveException {
        Peg5Move move = new Peg5Move((Peg5Move) m);
        Cell targetCell = board[move.row][move.col];

        // System.out.println("Attempting to apply move: " + move + " at cell state: " +
        // targetCell.getType());
        // if a player has won, do not allow any more moves
        if (getValue() == WIN || getValue() == -WIN) {
            throw new InvalidMoveException("Game has already been won.");
        }

        if (moveHistory.size() == DRAW) {
            throw new InvalidMoveException("Game is a draw.");
        }

        // if the movehistory size is 100, do not allow any more moves and say the game
        // is a draw

        // Handling placement of a new piece
        if (move.getSourceRow() == null && move.getSourceCol() == null) {
            // Check for existing piece compatibility
            switch (targetCell.getType()) {
                case GREEN_PEG:
                    // don't allow a peg move if all peg pieces have been played
                    if (currentPlayer == PLAYER_0 && greenPegCount == 0) {
                        throw new InvalidMoveException("Invalid move: All Green pegs have been placed.");
                    }
                    if (currentPlayer == PLAYER_1) {
                        if (move.getType().equalsIgnoreCase("open")) {
                            // Allow yellow player to place an open tube on a green peg
                            placePiece(currentPlayer, move.getType(), targetCell);
                            targetCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                            break;
                        } else if (move.getType().equalsIgnoreCase("closed")
                                || move.getType().equalsIgnoreCase("peg")) {
                            throw new InvalidMoveException(
                                    "Invalid move: Yellow player cannot place a peg or closed tube on a green peg.");
                        }
                    } else if (currentPlayer == PLAYER_0) {
                        throw new InvalidMoveException(
                                "Invalid move: Green player cannot place a piece on a cell that has a Green piece.");
                    }
                    break;
                case YELLOW_PEG:
                    if (currentPlayer == PLAYER_1 && yellowPegCount == 0) {
                        throw new InvalidMoveException("Invalid move: All Yellow pegs have been placed.");
                    }
                    if (currentPlayer == PLAYER_0) {
                        if (move.getType().equalsIgnoreCase("open")) {
                            // Allow green player to place an open tube on a yellow peg
                            placePiece(currentPlayer, move.getType(), targetCell);
                            targetCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                            break;
                        } else if (move.getType().equalsIgnoreCase("closed")
                                || move.getType().equalsIgnoreCase("peg")) {
                            throw new InvalidMoveException(
                                    "Invalid move: Green player cannot place a peg or closed tube on a yellow peg.");
                        }
                    } else if (currentPlayer == PLAYER_1) {
                        throw new InvalidMoveException(
                                "Invalid move: Yellow player cannot place a piece on a cell that has a yellow piece.");
                    }
                    break;
                case GREEN_OPEN_TUBE:
                    if (currentPlayer == PLAYER_0 && greenOpenTubeCount == 0) {
                        throw new InvalidMoveException("Invalid move: All Green open tubes have been placed.");
                    }
                    if (currentPlayer == PLAYER_1) { // yellow player
                        if (move.getType().equalsIgnoreCase("peg")) {
                            // Allow yellow player to place a peg on a green open tube
                            placePiece(currentPlayer, move.getType(), targetCell);
                            targetCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                            break;
                        } else if (move.getType().equalsIgnoreCase("closed")
                                || move.getType().equalsIgnoreCase("open")) {
                            throw new InvalidMoveException(
                                    "Invalid move: Yellow player cannot place a closed/open tube on a green open tube.");
                        }
                    } else if (currentPlayer == PLAYER_0) { // green player
                        throw new InvalidMoveException(
                                "Invalid move: Green player cannot place a piece on a cell that has a green piece.");
                    }
                    break;
                case YELLOW_OPEN_TUBE:
                    if (currentPlayer == PLAYER_1 && yellowOpenTubeCount == 0) {
                        throw new InvalidMoveException("Invalid move: All Yellow open tubes have been placed.");
                    }

                    if (currentPlayer == PLAYER_0) { // green player
                        if (move.getType().equalsIgnoreCase("peg")) {
                            // Allow green player to place a peg in a yellow open tube
                            placePiece(currentPlayer, move.getType(), targetCell); // Add this line
                            targetCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                            break;
                        } else if (move.getType().equalsIgnoreCase("closed")
                                || move.getType().equalsIgnoreCase("open")) {
                            throw new InvalidMoveException(
                                    "Invalid move: Green player cannot place a closed/open tube on a yellow open tube.");
                        }

                    } else if (currentPlayer == PLAYER_1) { // yellow player
                        throw new InvalidMoveException(
                                "Invalid move: Yellow player cannot place a piece on a cell that has a yellow piece.");
                    }
                    break;
                case GREEN_CLOSED_TUBE:
                    if (currentPlayer == PLAYER_0 && greenClosedTubeCount == 0) {
                        throw new InvalidMoveException("Invalid move: All Green closed tubes have been placed.");
                    }
                    if (currentPlayer == PLAYER_0) { // green player
                        throw new InvalidMoveException(
                                "Invalid move: Green player cannot place a piece on a cell that has a green piece.");
                    } else if (currentPlayer == PLAYER_1 && (move.getType().equalsIgnoreCase("peg")
                            || move.getType().equalsIgnoreCase("open") || move.getType().equalsIgnoreCase("closed"))) {
                        throw new InvalidMoveException(
                                "Invalid move: Yellow player cannot place a piece on a green closed tube.");
                    }
                    break;
                case YELLOW_CLOSED_TUBE:
                    if (currentPlayer == PLAYER_1 && yellowClosedTubeCount == 0) {
                        throw new InvalidMoveException("Invalid move: All Yellow closed tubes have been placed.");
                    }
                    if (currentPlayer == PLAYER_1) { // yellow player
                        throw new InvalidMoveException(
                                "Invalid move: Yellow player cannot place a piece on a cell that has a yellow piece.");
                    } else if (currentPlayer == PLAYER_0 && (move.getType().equalsIgnoreCase("peg")
                            || move.getType().equalsIgnoreCase("open") || move.getType().equalsIgnoreCase("closed"))) {
                        throw new InvalidMoveException(
                                "Invalid move: Green player cannot place a piece on a yellow closed tube.");
                    }
                    break;
                default:
                    if (targetCell.getType() != Cell.Type.EMPTY) {
                        throw new InvalidMoveException(
                                "Invalid move: target cell is not empty. Current type: " + targetCell.getType());
                    }
                    // Apply new piece if the cell is empty
                    placePiece(currentPlayer, move.getType(), targetCell);
            }
        } else {
            // Handling a move of an existing piece
            Cell sourceCell = board[move.getSourceRow()][move.getSourceCol()];
            if (sourceCell.getType() == Cell.Type.EMPTY) {
                throw new InvalidMoveException("Invalid move: source cell is empty.");
            }

            // Check for existing piece compatibility
            switch (sourceCell.getType()) {
                case GREEN_PEG:
                    if (currentPlayer == PLAYER_0 && greenPegCount > 0) {
                        throw new InvalidMoveException("Invalid move: All Green pegs have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_0 && (targetCell.getType() == Cell.Type.YELLOW_OPEN_TUBE
                            || targetCell.getType() == Cell.Type.EMPTY)) {
                        // Allow green peg to move to a yellow open tube
                        targetCell.setType(targetCell.getType() == Cell.Type.YELLOW_OPEN_TUBE
                                ? Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE
                                : Cell.Type.GREEN_PEG);
                        sourceCell.setType(Cell.Type.EMPTY);
                    } else {
                        throw new InvalidMoveException(
                                "Invalid move: Green peg can only move to a yellow open tube or an empty cell.");
                    }
                    break;
                case YELLOW_PEG:
                    if (currentPlayer == PLAYER_1 && yellowPegCount > 0) {
                        throw new InvalidMoveException("Invalid move: All Yellow pegs have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_1 && (targetCell.getType() == Cell.Type.GREEN_OPEN_TUBE
                            || targetCell.getType() == Cell.Type.EMPTY)) {
                        // Allow yellow peg to move to a green open tube
                        targetCell.setType(
                                targetCell.getType() == Cell.Type.GREEN_OPEN_TUBE ? Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE
                                        : Cell.Type.YELLOW_PEG);
                        sourceCell.setType(Cell.Type.EMPTY);
                    } else {
                        throw new InvalidMoveException(
                                "Invalid move: Yellow peg can only move to a green open tube or an empty cell.");
                    }
                    break;
                case GREEN_OPEN_TUBE:
                    if (currentPlayer == PLAYER_0 && (greenOpenTubeCount > 0 || greenClosedTubeCount > 0)) {
                        throw new InvalidMoveException("Invalid move: All Green tubes have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_0 && (targetCell.getType() == Cell.Type.YELLOW_PEG
                            || targetCell.getType() == Cell.Type.EMPTY)) {
                        // Allow green open tube to move to a yellow peg or an empty cell
                        targetCell.setType(
                                targetCell.getType() == Cell.Type.YELLOW_PEG ? Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE
                                        : Cell.Type.GREEN_OPEN_TUBE);
                        sourceCell.setType(Cell.Type.EMPTY);
                    } else {
                        throw new InvalidMoveException(
                                "Invalid move: Green open tube can only move to a yellow peg or an empty cell.");
                    }
                    break;
                case YELLOW_OPEN_TUBE:
                    if (currentPlayer == PLAYER_1 && (yellowOpenTubeCount > 0 || yellowClosedTubeCount > 0)) {
                        throw new InvalidMoveException("Invalid move: All Yellow tubes have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_1 && (targetCell.getType() == Cell.Type.GREEN_PEG
                            || targetCell.getType() == Cell.Type.EMPTY)) {
                        // Allow yellow open tube to move to a green peg or an empty cell
                        targetCell.setType(
                                targetCell.getType() == Cell.Type.GREEN_PEG ? Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE
                                        : Cell.Type.YELLOW_OPEN_TUBE);
                        sourceCell.setType(Cell.Type.EMPTY);
                    } else {
                        throw new InvalidMoveException(
                                "Invalid move: Yellow open tube can only be moved to an empty cell or a green peg.");
                    }
                    break;
                case GREEN_CLOSED_TUBE:
                    if (currentPlayer == PLAYER_0 && (greenOpenTubeCount > 0 || greenClosedTubeCount > 0)) {
                        throw new InvalidMoveException("Invalid movesss: All Green tubes have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_0 && targetCell.getType() == Cell.Type.EMPTY) {
                        // Allow green closed tube to be moved to an empty cell
                        targetCell.setType(Cell.Type.GREEN_CLOSED_TUBE);
                        sourceCell.setType(Cell.Type.EMPTY);
                    } else {
                        throw new InvalidMoveException(
                                "Invalid move: Green closed tube can only be moved to an empty cell.");
                    }
                    break;
                case YELLOW_CLOSED_TUBE:
                    if (currentPlayer == PLAYER_1 && (yellowOpenTubeCount > 0 || yellowClosedTubeCount > 0)) {
                        throw new InvalidMoveException("Invalid move: All Yellow tubes have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_1 && targetCell.getType() == Cell.Type.EMPTY) {
                        // Allow yellow closed tube to be moved to an empty cell
                        targetCell.setType(Cell.Type.YELLOW_CLOSED_TUBE);
                        sourceCell.setType(Cell.Type.EMPTY);
                    } else {
                        throw new InvalidMoveException(
                                "Invalid move: Yellow closed tube can only be moved to an empty cell.");
                    }
                    break;

                case GREEN_PEG_YELLOW_OPEN_TUBE:
                    if (currentPlayer == PLAYER_1 && move.getType().equalsIgnoreCase("open") && (yellowOpenTubeCount > 0
                            || yellowClosedTubeCount > 0)) {
                        throw new InvalidMoveException("Invalid move: All Yellow open tubes have not been placed yet.");
                    } else if (currentPlayer == PLAYER_0 && move.getType().equalsIgnoreCase("peg")
                            && greenPegCount > 0) {
                        throw new InvalidMoveException("Invalid move: All Green pegs have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_1 && move.getType().equalsIgnoreCase("open")) { // allow yellow to move
                                                                                                // the open tube
                        // Move yellow open tube, leaving green peg behind
                        if (targetCell.getType() == Cell.Type.EMPTY) {
                            targetCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                            sourceCell.setType(Cell.Type.GREEN_PEG);
                        } else if (targetCell.getType() == Cell.Type.GREEN_PEG) {
                            targetCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                            sourceCell.setType(Cell.Type.GREEN_PEG);
                        } else {
                            throw new InvalidMoveException("Target cell is not compatible for YELLOW OPEN TUBE move.");
                        }
                    } else if (currentPlayer == PLAYER_0 && move.getType().equalsIgnoreCase("peg")) {
                        // Move green peg, leaving yellow open tube behind
                        if (targetCell.getType() == Cell.Type.EMPTY) {
                            targetCell.setType(Cell.Type.GREEN_PEG);
                            sourceCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                        } else if (targetCell.getType() == Cell.Type.YELLOW_OPEN_TUBE) {
                            targetCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                            sourceCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                        } else {
                            throw new InvalidMoveException("Target cell is not compatible for GREEN PEG move.");
                        }
                    } else {
                        throw new InvalidMoveException("Invalid move: code broke here.");
                    }
                    break;
                case YELLOW_PEG_GREEN_OPEN_TUBE:
                    if (currentPlayer == PLAYER_0 && move.getType().equalsIgnoreCase("open")
                            && (greenOpenTubeCount > 0 || greenClosedTubeCount > 0)) {
                        throw new InvalidMoveException("Invalid move: All Green open tubes have not been placed yet.");
                    } else if (currentPlayer == PLAYER_1 && move.getType().equalsIgnoreCase("peg")
                            && yellowPegCount > 0) {
                        throw new InvalidMoveException("Invalid move: All Yellow pegs have not been placed yet.");
                    }
                    if (currentPlayer == PLAYER_0 && move.getType().equalsIgnoreCase("open")) {
                        // Move green open tube, leaving yellow peg behind
                        if (targetCell.getType() == Cell.Type.EMPTY) {
                            targetCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                            sourceCell.setType(Cell.Type.YELLOW_PEG);
                        } else if (targetCell.getType() == Cell.Type.YELLOW_PEG) {
                            targetCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                            sourceCell.setType(Cell.Type.YELLOW_PEG);
                        } else {
                            throw new InvalidMoveException("Target cell is not compatible for GREEN OPEN TUBE move.");
                        }

                    } else if (currentPlayer == PLAYER_1 && move.getType().equalsIgnoreCase("peg")) {
                        // Move yellow peg, leaving green open tube behind
                        if (targetCell.getType() == Cell.Type.EMPTY) {
                            targetCell.setType(Cell.Type.YELLOW_PEG);
                            sourceCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                        } else if (targetCell.getType() == Cell.Type.GREEN_OPEN_TUBE) {
                            targetCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                            sourceCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                        } else {
                            throw new InvalidMoveException("Target cell is not compatible for YELLOW PEG move.");
                        }
                    } else {
                        throw new InvalidMoveException("ERROR: code broke here.");
                    }
                    break;
                default:
                    throw new InvalidMoveException("Invalid move: source cell does not contain a movable piece.");
            }
        }

        // Switch player after a successful move
        moveHistory.add(move);
        currentPlayer = (currentPlayer == PLAYER_0) ? PLAYER_1 : PLAYER_0;
    }

    private void placePiece(int player, String pieceType, Cell targetCell) throws InvalidMoveException {
        // Determine which piece type and player is placing the piece
        if (player == PLAYER_0) { // Assuming PLAYER_0 is green
            if (pieceType.equalsIgnoreCase("peg") && greenPegCount > 0) {
                targetCell.setType(Cell.Type.GREEN_PEG);
                greenPegCount--;
            } else if (pieceType.equalsIgnoreCase("open") && greenOpenTubeCount > 0) {
                targetCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                greenOpenTubeCount--;
            } else if (pieceType.equalsIgnoreCase("closed") && greenClosedTubeCount > 0) {
                targetCell.setType(Cell.Type.GREEN_CLOSED_TUBE);
                greenClosedTubeCount--;
            } else {
                throw new InvalidMoveException("No available pieces to place or unknown piece type.");
            }
        } else { // Assuming PLAYER_1 is yellow
            if (pieceType.equalsIgnoreCase("peg") && yellowPegCount > 0) {
                targetCell.setType(Cell.Type.YELLOW_PEG);
                yellowPegCount--;
            } else if (pieceType.equalsIgnoreCase("open") && yellowOpenTubeCount > 0) {
                targetCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                yellowOpenTubeCount--;
            } else if (pieceType.equalsIgnoreCase("closed") && yellowClosedTubeCount > 0) {
                targetCell.setType(Cell.Type.YELLOW_CLOSED_TUBE);
                yellowClosedTubeCount--;
            } else {
                throw new InvalidMoveException("No available pieces to place or unknown piece type.");
            }
        }
    }

    @Override
    public int getValue() {
        // Call the method that computes scores for both players
        return checkWinForPlayer(Cell.Type.GREEN_PEG, Cell.Type.GREEN_OPEN_TUBE, Cell.Type.GREEN_CLOSED_TUBE,
                Cell.Type.YELLOW_PEG, Cell.Type.YELLOW_OPEN_TUBE, Cell.Type.YELLOW_CLOSED_TUBE);
    }

    // write a method that iterates through the board. for each cell, it gets the
    // next 4 cells in the row, column and diagonal directions
    // and forms a group of 5 cells. If a group of 5 cells cannot be formed, break.
    // if the group of 5 cells contains one of these three patterns: (P)eg and
    // (t)ube: PPtPP, PtPtP, PtttP
    // then the player who matches those patterns has won the game.

    private int checkWinForPlayer(Cell.Type greenPegType, Cell.Type greenOpenTubeType, Cell.Type greenClosedTubeType,
            Cell.Type yellowPegType, Cell.Type yellowOpenTubeType, Cell.Type yellowClosedTubeType) {
        int greenScore = 0;
        int yellowScore = 0;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (i + (groupSize - 1) < SIZE) { // Check vertical
                    greenScore += evaluatePattern(i, j, 1, 0, greenPegType, greenOpenTubeType, greenClosedTubeType,
                            true);
                }
                if (j + (groupSize - 1) < SIZE) { // Check horizontal
                    greenScore += evaluatePattern(i, j, 0, 1, greenPegType, greenOpenTubeType, greenClosedTubeType,
                            true);
                }
                if (i + (groupSize - 1) < SIZE && j + (groupSize - 1) < SIZE) { // Check diagonal down-right
                    greenScore += evaluatePattern(i, j, 1, 1, greenPegType, greenOpenTubeType, greenClosedTubeType,
                            true);
                }
                if (i - (groupSize - 1) >= 0 && j + (groupSize - 1) < SIZE) { // Check diagonal top-right to bottom-left
                    greenScore += evaluatePattern(i, j, -1, 1, greenPegType, greenOpenTubeType, greenClosedTubeType,
                            true);
                }
            }
        }

        // calculate the score for the yellow player
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (i + (groupSize - 1) < SIZE) { // Check vertical
                    yellowScore += evaluatePattern(i, j, 1, 0, yellowPegType, yellowOpenTubeType, yellowClosedTubeType,
                            false);
                }
                if (j + (groupSize - 1) < SIZE) { // Check horizontal
                    yellowScore += evaluatePattern(i, j, 0, 1, yellowPegType, yellowOpenTubeType, yellowClosedTubeType,
                            false);
                }
                if (i + (groupSize - 1) < SIZE && j + (groupSize - 1) < SIZE) { // Check diagonal down-right
                    yellowScore += evaluatePattern(i, j, 1, 1, yellowPegType, yellowOpenTubeType, yellowClosedTubeType,
                            false);
                }
                if (i - (groupSize - 1) >= 0 && j + (groupSize - 1) < SIZE) { // Check diagonal top-right to bottom-left
                    yellowScore += evaluatePattern(i, j, -1, 1, yellowPegType, yellowOpenTubeType, yellowClosedTubeType,
                            false);
                }
            }
        }

        // Translate scores to game value
        if (greenScore >= WIN)
            return WIN; // Win condition for green
        if (yellowScore >= WIN)
            return -WIN; // Win condition for yellow

         System.out.println("Green Score: " + greenScore + " Yellow Score: " +
         yellowScore);
        return greenScore - yellowScore;
    }

    private int evaluatePattern(int startRow, int startCol, int rowInc, int colInc, Cell.Type pegType,
            Cell.Type openTubeType, Cell.Type closedTubeType, boolean isGreenPlayer) {

        if (startRow + (groupSize - 1) * rowInc >= SIZE || startRow + (groupSize - 1) * rowInc < 0 ||
                startCol + (groupSize - 1) * colInc >= SIZE || startCol + (groupSize - 1) * colInc < 0) {
            return 0; // Pattern out of bounds
        }
        if (moveHistory.size() == DRAW) {
            return 0; // Game is a draw
        }

        Cell.Type wildCardType = Cell.Type.WILDCARD;
        // Define winning patterns
        Cell.Type[] pattern1 = { pegType, pegType, wildCardType, pegType, pegType };
        Cell.Type[] pattern2 = { pegType, wildCardType, pegType, wildCardType, pegType };
        Cell.Type[] pattern3 = { pegType, wildCardType, wildCardType, wildCardType, pegType };

        List<Cell.Type[]> winningPatterns = new ArrayList<>();
        winningPatterns.add(pattern1);
        winningPatterns.add(pattern2);
        winningPatterns.add(pattern3);

        Cell.Type[] pattern = new Cell.Type[5];
        List<String> locations = new ArrayList<>();

        for (int i = 0; i < groupSize; i++) {
            Cell cell = board[startRow + i * rowInc][startCol + i * colInc];
            pattern[i] = determineRelevantPiece(cell, isGreenPlayer);
            locations.add("(" + ((startRow + i * rowInc) + 1) + ", " + ((startCol + i * colInc) + 1) + ")");
        }
        for (Cell.Type[] winningPattern : winningPatterns) {
            int matchCount = 0;
            int playAbleCellCount = 0;

            for (int i = 0; i < groupSize; i++) {
                if (pattern[i] == winningPattern[i]) {
                    matchCount++;
                } else if (winningPattern[i] == wildCardType) {
                    if (matchesWildCard(pattern[i], isGreenPlayer)) {
                        matchCount++;
                    }
                }
                if (pattern[i] != winningPattern[i]) {
                    // if the current piece does not match the winning pattern,
                    // get the required piece from the winning pattern and check if the player
                    // can play that piece in the current cell. do this without calling any method
                    if (winningPattern[i] == pegType) {
                        if (pattern[i] == Cell.Type.EMPTY && canPlayOn(pattern[i], winningPattern[i], isGreenPlayer)) {
                            playAbleCellCount++;
                        } else if (pattern[i] != closedTubeType
                                && canPlayOn(pattern[i], winningPattern[i], isGreenPlayer)) {
                            playAbleCellCount++;
                        }
                    } else if (winningPattern[i] == wildCardType) {
                        if (pattern[i] == Cell.Type.EMPTY && canPlayOn(pattern[i], pegType, isGreenPlayer)) {
                            playAbleCellCount++;
                        } else if (pattern[i] != closedTubeType && canPlayOn(pattern[i], openTubeType, isGreenPlayer)) {
                            playAbleCellCount++;
                        }
                    }

                }
            }

            if (matchCount == fivePattern) {
                return WIN;
            } else if (matchCount == fourPattern && playAbleCellCount == 1) {
                return fourPatternReturn;
            } else if (matchCount == threePattern && playAbleCellCount == 2) {
                return 1;
            }
        }
        return 0;
    }

    public boolean canPlayOn(Cell.Type currentCell, Cell.Type requiredPiece, boolean isGreenPlayer) {
        if (currentCell == Cell.Type.EMPTY) {
            return true;
        } else if (isGreenPlayer) {
            if (requiredPiece == Cell.Type.GREEN_PEG) {
                return currentCell == Cell.Type.YELLOW_OPEN_TUBE;
            } else if (requiredPiece == Cell.Type.GREEN_OPEN_TUBE) {
                return currentCell == Cell.Type.YELLOW_PEG;
            }
        } else {
            if (requiredPiece == Cell.Type.YELLOW_PEG) {
                return currentCell == Cell.Type.GREEN_OPEN_TUBE;
            } else if (requiredPiece == Cell.Type.YELLOW_OPEN_TUBE) {
                return currentCell == Cell.Type.GREEN_PEG;
            }
        }
        return false;
    }

    private Cell.Type determineRelevantPiece(Cell cell, boolean isGreenPlayer) {
        // Return the type that is relevant for the current player's turn
        if (cell.getType() == Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE) {
            return isGreenPlayer ? Cell.Type.GREEN_PEG : Cell.Type.YELLOW_OPEN_TUBE;
        } else if (cell.getType() == Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE) {
            return isGreenPlayer ? Cell.Type.GREEN_OPEN_TUBE : Cell.Type.YELLOW_PEG;
        } else {
            return cell.getType();
        }
    }

    public boolean matchesWildCard(Cell.Type cellType, boolean isGreenPlayer) {
        // Check if the cell type matches the wild card for the current player
        if (isGreenPlayer) {
            return cellType == Cell.Type.GREEN_OPEN_TUBE || cellType == Cell.Type.GREEN_CLOSED_TUBE;
        } else {
            return cellType == Cell.Type.YELLOW_OPEN_TUBE || cellType == Cell.Type.YELLOW_CLOSED_TUBE;
        }
    }

    // Moves are sorted in order first by location, in row-major order, and then by
    // piece type, in order peg, open tube, closed tube. Sort transfer moves by
    // destination first, then source.
    @Override
    public List<? extends Move> getValidMoves() {
        // “Lowest” moves come first. Design your code to generate moves in this order
        // naturally.
        List<Peg5Move> validMoves = new ArrayList<>();
        List<Cell> emptyCells = new ArrayList<>();
        List<Cell> greenPegs = new ArrayList<>();
        List<Cell> yellowPegs = new ArrayList<>();
        List<Cell> greenOpenTubes = new ArrayList<>();
        List<Cell> yellowOpenTubes = new ArrayList<>();
        List<Cell> greenClosedTubes = new ArrayList<>();
        List<Cell> yellowClosedTubes = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Cell cell = board[i][j];
                if (cell.getType() == Cell.Type.EMPTY) {
                    emptyCells.add(cell);
                } else if (cell.getType() == Cell.Type.GREEN_PEG) {
                    greenPegs.add(cell);
                } else if (cell.getType() == Cell.Type.YELLOW_PEG) {
                    yellowPegs.add(cell);
                } else if (cell.getType() == Cell.Type.GREEN_OPEN_TUBE) {
                    greenOpenTubes.add(cell);
                } else if (cell.getType() == Cell.Type.YELLOW_OPEN_TUBE) {
                    yellowOpenTubes.add(cell);
                } else if (cell.getType() == Cell.Type.GREEN_CLOSED_TUBE) {
                    greenClosedTubes.add(cell);
                } else if (cell.getType() == Cell.Type.YELLOW_CLOSED_TUBE) {
                    yellowClosedTubes.add(cell);
                } else if (cell.getType() == Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE) {
                    // split the pieces in the cell and add the green peg to the green peg list and
                    // the yellow open tube to the yellow open tube list
                    greenPegs.add(cell);
                    yellowOpenTubes.add(cell);
                } else if (cell.getType() == Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE) {
                    yellowPegs.add(cell);
                    greenOpenTubes.add(cell);
                }
            }
        }

        int currentPegCount = (currentPlayer == PLAYER_0) ? greenPegCount : yellowPegCount;
        int currentTubeCount = (currentPlayer == PLAYER_0) ? greenOpenTubeCount + greenClosedTubeCount
                : yellowOpenTubeCount + yellowClosedTubeCount;

        int currentOpenTubeCount = (currentPlayer == PLAYER_0) ? greenOpenTubeCount : yellowOpenTubeCount;
        int currentClosedTubeCount = (currentPlayer == PLAYER_0) ? greenClosedTubeCount : yellowClosedTubeCount;

        // loop through the entire board
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Cell cell = board[i][j];
                // if the cell is empty, add a move to the empty cell
                if (cell.getType() == Cell.Type.EMPTY) {
                    if (currentPegCount > 0) {
                        validMoves.add(new Peg5Move(cell.row, cell.col, "peg"));
                        if (currentOpenTubeCount > 0) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "open"));
                        }
                        if (currentClosedTubeCount > 0) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "closed"));
                        }
                    }
                    if (currentPegCount > 0 && currentTubeCount == 0) {
                        // validMoves.add(new Peg5Move(cell.row, cell.col, "peg"));
                        // for each open and closed tubes on the board, add a move to the empty cell
                        if (currentPlayer == PLAYER_0) {
                            for (Cell ecell : greenOpenTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "open", ecell.row, ecell.col));
                            }
                            for (Cell ecell : greenClosedTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "closed", ecell.row, ecell.col));
                            }
                        } else {
                            for (Cell ecell : yellowOpenTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "open", ecell.row, ecell.col));
                            }
                            for (Cell ecell : yellowClosedTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "closed", ecell.row, ecell.col));
                            }
                        }
                    } else if (currentPegCount == 0 && (currentClosedTubeCount > 0 || currentOpenTubeCount > 0)) {
                        if (currentPlayer == PLAYER_0) {
                            for (Cell ecell : greenPegs) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "peg", ecell.row, ecell.col));
                            }
                        } else {
                            for (Cell ecell : yellowPegs) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "peg", ecell.row, ecell.col));
                            }
                        }
                        if (currentOpenTubeCount > 0) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "open"));
                        }
                        if (currentClosedTubeCount > 0) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "closed"));
                        }
                        // validMoves.add(new Peg5Move(cell.row, cell.col, "open"));
                        // validMoves.add(new Peg5Move(cell.row, cell.col, "closed"));
                        // for each peg on the board, add a move to the empty cell

                    } else if (currentPegCount == 0 && currentTubeCount == 0) {
                        // for each peg on the board, add a move to the empty cell
                        if (currentPlayer == PLAYER_0) {
                            for (Cell ecell : greenPegs) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "peg", ecell.row, ecell.col));
                            }
                            for (Cell ecell : greenOpenTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "open", ecell.row, ecell.col));
                            }
                            for (Cell ecell : greenClosedTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "closed", ecell.row, ecell.col));
                            }
                        } else {
                            for (Cell ecell : yellowPegs) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "peg", ecell.row, ecell.col));
                            }
                            for (Cell ecell : yellowOpenTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "open", ecell.row, ecell.col));
                            }
                            for (Cell ecell : yellowClosedTubes) {
                                validMoves.add(new Peg5Move(cell.row, cell.col, "closed", ecell.row, ecell.col));
                            }
                        }
                    }
                } else if (cell.getType() == Cell.Type.GREEN_PEG) {
                    if (currentPlayer == PLAYER_1 && currentOpenTubeCount > 0) {
                        validMoves.add(new Peg5Move(cell.row, cell.col, "open"));
                    } else if (currentTubeCount == 0 && currentPlayer == PLAYER_1) {
                        for (Cell ecell : yellowOpenTubes) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "open", ecell.row, ecell.col));
                        }
                    }
                } else if (cell.getType() == Cell.Type.YELLOW_PEG) {
                    if (currentPlayer == PLAYER_0 && currentOpenTubeCount > 0) {
                        validMoves.add(new Peg5Move(cell.row, cell.col, "open"));
                    } else if (currentTubeCount == 0 && currentPlayer == PLAYER_0) {
                        for (Cell ecell : greenOpenTubes) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "open", ecell.row, ecell.col));
                        }
                    }
                } else if (cell.getType() == Cell.Type.GREEN_OPEN_TUBE) {
                    if (currentPegCount > 0 && currentPlayer == PLAYER_1) {
                        validMoves.add(new Peg5Move(cell.row, cell.col, "peg"));
                    } else if (currentPegCount == 0 && currentPlayer == PLAYER_1) {
                        for (Cell ecell : yellowPegs) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "peg", ecell.row, ecell.col));
                        }
                    }
                } else if (cell.getType() == Cell.Type.YELLOW_OPEN_TUBE) {
                    if (currentPegCount > 0 && currentPlayer == PLAYER_0) {
                        validMoves.add(new Peg5Move(cell.row, cell.col, "peg"));
                    } else if (currentPegCount == 0 && currentPlayer == PLAYER_0) {
                        for (Cell ecell : greenPegs) {
                            validMoves.add(new Peg5Move(cell.row, cell.col, "peg", ecell.row, ecell.col));
                        }
                    }
                }
            }
        }

        // if a player has won, return an empty list of valid moves.
        if (getValue() == WIN || getValue() == -WIN) {
            return new ArrayList<>();
        }
        if (moveHistory.size() == DRAW) {
            return new ArrayList<>();
        }

        return validMoves;
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
            return;
        }

        currentPlayer = (currentPlayer == PLAYER_0) ? PLAYER_1 : PLAYER_0;

        // Retrieve and remove the last move from the move history
        Peg5Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        // Retrieve the target cell where the last move was applied
        Cell targetCell = board[lastMove.row][lastMove.col];

        // If the move had a source, it was a move, not just a placement
        if (lastMove.getSourceRow() == null && lastMove.getSourceCol() == null) {
            // It was a placement, so just clear the cell and increment the count
            switch (targetCell.getType()) {
                case GREEN_PEG:
                    targetCell.setType(Cell.Type.EMPTY);
                    greenPegCount++;
                    break;
                case YELLOW_PEG:
                    targetCell.setType(Cell.Type.EMPTY);
                    yellowPegCount++;
                    break;
                case GREEN_OPEN_TUBE:
                    targetCell.setType(Cell.Type.EMPTY);
                    greenOpenTubeCount++;
                    break;
                case YELLOW_OPEN_TUBE:
                    targetCell.setType(Cell.Type.EMPTY);
                    yellowOpenTubeCount++;
                    break;
                case GREEN_CLOSED_TUBE:
                    targetCell.setType(Cell.Type.EMPTY);
                    greenClosedTubeCount++;
                    break;
                case YELLOW_CLOSED_TUBE:
                    targetCell.setType(Cell.Type.EMPTY);
                    yellowClosedTubeCount++;
                    break;
                case GREEN_PEG_YELLOW_OPEN_TUBE:
                    if (currentPlayer == PLAYER_0) {
                        targetCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                        greenPegCount++;
                    } else {
                        targetCell.setType(Cell.Type.GREEN_PEG);
                        yellowOpenTubeCount++;
                    }
                    break;
                case YELLOW_PEG_GREEN_OPEN_TUBE:
                    if (currentPlayer == PLAYER_1) {
                        targetCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                        yellowPegCount++;
                    } else {
                        targetCell.setType(Cell.Type.YELLOW_PEG);
                        greenOpenTubeCount++;
                    }
                    break;
                default:
                    break;
            }
        } else {
            // if there's a source, it was a move, not just a placement
            Cell sourceCell = board[lastMove.getSourceRow()][lastMove.getSourceCol()];
            // if the piece was a two-piece cell, separate the pieces
            // use switch cases
            switch (targetCell.getType()) {
                case GREEN_PEG:
                    if (sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.GREEN_PEG);
                        targetCell.setType(Cell.Type.EMPTY);
                    } else if (sourceCell.getType() == Cell.Type.YELLOW_OPEN_TUBE) {
                        sourceCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    }
                    break;
                case YELLOW_PEG:
                    if (sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.YELLOW_PEG);
                        targetCell.setType(Cell.Type.EMPTY);
                    } else if (sourceCell.getType() == Cell.Type.GREEN_OPEN_TUBE) {
                        sourceCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    }
                    break;
                case GREEN_OPEN_TUBE:
                    if (sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    } else if (sourceCell.getType() == Cell.Type.YELLOW_PEG) {
                        sourceCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    }
                    break;
                case YELLOW_OPEN_TUBE:
                    if (sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    } else if (sourceCell.getType() == Cell.Type.GREEN_PEG) {
                        sourceCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    }
                    break;
                case GREEN_CLOSED_TUBE:
                    if (sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.GREEN_CLOSED_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    }
                    break;
                case YELLOW_CLOSED_TUBE:
                    if (sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.YELLOW_CLOSED_TUBE);
                        targetCell.setType(Cell.Type.EMPTY);
                    }
                    break;
                case GREEN_PEG_YELLOW_OPEN_TUBE:
                    if (currentPlayer == PLAYER_0 && sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.GREEN_PEG);
                        targetCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                    } else if (currentPlayer == PLAYER_1 && sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                        targetCell.setType(Cell.Type.GREEN_PEG);
                    } else if (currentPlayer == PLAYER_0 && sourceCell.getType() == Cell.Type.YELLOW_OPEN_TUBE) {
                        sourceCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                        targetCell.setType(Cell.Type.YELLOW_OPEN_TUBE);
                    } else if (currentPlayer == PLAYER_1 && sourceCell.getType() == Cell.Type.GREEN_PEG) {
                        sourceCell.setType(Cell.Type.GREEN_PEG_YELLOW_OPEN_TUBE);
                        targetCell.setType(Cell.Type.GREEN_PEG);
                    }
                    break;
                case YELLOW_PEG_GREEN_OPEN_TUBE:
                    if (currentPlayer == PLAYER_1 && sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.YELLOW_PEG);
                        targetCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                    } else if (currentPlayer == PLAYER_0 && sourceCell.getType() == Cell.Type.EMPTY) {
                        sourceCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                        targetCell.setType(Cell.Type.YELLOW_PEG);
                    } else if (currentPlayer == PLAYER_1 && sourceCell.getType() == Cell.Type.GREEN_OPEN_TUBE) {
                        sourceCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                        targetCell.setType(Cell.Type.GREEN_OPEN_TUBE);
                    } else if (currentPlayer == PLAYER_0 && sourceCell.getType() == Cell.Type.YELLOW_PEG) {
                        sourceCell.setType(Cell.Type.YELLOW_PEG_GREEN_OPEN_TUBE);
                        targetCell.setType(Cell.Type.YELLOW_PEG);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // undo the most current move using the getMoveHistory. if the move merged two
    // pieces together eg an open tube
    // and a peg, return the cell back it the state it was before the move. If a
    // piece was placed in a cell and then undone,
    // add that piece back to the count of the place for such pieces.

    // display the board as a 7x7 grid. It first displays the list of green's
    // unplayed pieces
    // with a row representing green pegs and a second row representing green tubes.
    // green pegs are
    // represented by a 'G' while open tubes are represented by 'Og' and closed
    // tubes as '-g'.
    // The same is done for yellow's unplayed pieces, using symbols 'Y', 'Oy', and
    // '-y' respectively but display
    // the row of tubes first followed by the row of pegs after displaying the
    // board.
    // The board is displayed between the unplayed pieces with row and column
    // indexes and empty cells are represented as '.'.
    // For two-piece cells (one peg and one open tube) replace the O in the tube
    // symbol with the peg symbol.
    // For example, Yg or Gy. Finally, add a line indicating whic player's turn it
    // is. eg "Green to play"
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Display green's unplayed pieces
        for (int i = 0; i < greenPegCount; i++) {
            sb.append("G");
        }
        sb.append("\n");
        for (int i = 0; i < greenOpenTubeCount; i++) {
            sb.append("Og");
        }
        for (int i = 0; i < greenClosedTubeCount; i++) {
            sb.append("-g");
        }
        sb.append("\n");

        // Display the board with row and column indexes
        sb.append("  1  2  3  4  5  6  7\n");
        for (int i = 0; i < SIZE; i++) {
            sb.append(i + 1).append(" "); // 1-based index for display
            for (int j = 0; j < SIZE; j++) {
                Cell cell = board[i][j];
                String cellRepresentation = cell.toString();
                // Adjust space for two-character representations
                if (cellRepresentation.length() == 2) {
                    sb.append(cellRepresentation + " "); // Append a space for alignment
                } else {
                    sb.append(cellRepresentation + "  "); // Double space for single characters to align with
                                                          // two-character cells
                }
            }
            sb.append("\n");
        }
        // Display yellow's unplayed pieces
        for (int i = 0; i < yellowOpenTubeCount; i++) {
            sb.append("Oy");
        }
        for (int i = 0; i < yellowClosedTubeCount; i++) {
            sb.append("-y");
        }
        sb.append("\n");
        for (int i = 0; i < yellowPegCount; i++) {
            sb.append("Y");
        }
        sb.append("\n");

        // Indicate which player's turn it is
        if (moveHistory.size() == 100) {
            sb.append("Draw\n");
        } else if (currentPlayer == PLAYER_0) {
            sb.append("Green to play\n");
        } else {
            sb.append("Yellow to play\n");
        }

        return sb.toString();
    }

}