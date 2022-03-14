package com.battlesnake.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.*;

import static spark.Spark.*;

/**
 * This is a simple Battlesnake server written in Java.
 * 
 * For instructions see
 * https://github.com/BattlesnakeOfficial/starter-snake-java/README.md
 */
public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    /**
     * Main entry point.
     *
     * @param args are ignored.
     */
    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port == null) {
            LOG.info("Using default port: {}", port);
            port = "8080";
        } else {
            LOG.info("Found system provided port: {}", port);
        }
        port(Integer.parseInt(port));
        get("/", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    /**
     * Handler class for dealing with the routes set up in the main method.
     */
    public static class Handler {

        /**
         * For the start/end request
         */
        private static final Map<String, String> EMPTY = new HashMap<>();

        /**
         * Generic processor that prints out the request and response from the methods.
         *
         * @param req
         * @param res
         * @return
         */
        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/")) {
                    snakeResponse = index();
                } else if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }

                LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));

                return snakeResponse;
            } catch (JsonProcessingException e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @return a response back to the engine containing the Battlesnake setup
         *         values.
         */
        public Map<String, String> index() {
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", "aylinnie");
            response.put("color", "#D8A7B1");
            response.put("head", "tongue");
            response.put("tail", "default");
            return response;
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * 
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @param startRequest a JSON data map containing the information about the game
         *                     that is about to be played.
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> start(JsonNode startRequest) {
            LOG.info("START");
            return EMPTY;
        }

        /**
         * This method is called on every turn of a game. It's how your snake decides
         * where to move.
         * 
         * Use the information in 'moveRequest' to decide your next move. The
         * 'moveRequest' variable can be interacted with as
         * com.fasterxml.jackson.databind.JsonNode, and contains all of the information
         * about the Battlesnake board for each move of the game.
         * 
         * For a full example of 'json', see
         * https://docs.battlesnake.com/references/api/sample-move-request
         *
         * @param moveRequest JsonNode of all Game Board data as received from the
         *                    Battlesnake Engine.
         * @return a Map<String,String> response back to the engine the single move to
         *         make. One of "up", "down", "left" or "right".
         */
        public Map<String, String> move(JsonNode moveRequest) {
            JsonNode head = moveRequest.get("you").get("head");
            JsonNode body = moveRequest.get("you").get("body");
            int myLength = moveRequest.get("you").get("length").asInt();
            int board_height = moveRequest.get("board").get("height").asInt();
            int board_width = moveRequest.get("board").get("width").asInt();
            JsonNode otherSnakes = moveRequest.get("board").get("snakes");
            ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

            // avoid my own neck
            ArrayList<String> someMoves = avoidMyNeck(head, body, possibleMoves);

            // avoid the walls
            ArrayList<String> newMoves = avoidTheWalls(head, someMoves, board_height, board_width);

            // avoid my body
            ArrayList<String> lastMoves = avoidMyBody(head, body, newMoves);

            // avoid collide with another Battlesnake
            ArrayList<String> moves = avoidOtherSnakes(head, myLength, otherSnakes, lastMoves);

            // get some food
            JsonNode food = moveRequest.get("board").get("food");

            String move;

            // Choose a random direction to move in
            if (moves.size() > 0) {
                LOG.info("Possible moves left {}", moves);
                final int choice = new Random().nextInt(moves.size());
                move = moves.get(choice);
            } else {
                // no choices left, go right ...
                move = "right";
                LOG.info("Nothing left... so go to the right");
            }

            LOG.info("MOVE {}", move);

            Map<String, String> response = new HashMap<>();
            response.put("move", move);
            return response;
        }

        /**
         * Remove the 'neck' direction from the list of possible moves
         * 
         * @param head          JsonNode of the head position e.g. {"x": 0, "y": 0}
         * @param body          JsonNode of x/y coordinates for every segment of a
         *                      Battlesnake. e.g. [ {"x": 0, "y": 0}, {"x": 1, "y": 0}, {"x": 2, "y": 0} ]
         * @param possibleMoves ArrayList of String. Moves to pick from.
         */
        public ArrayList<String> avoidMyNeck(JsonNode head, JsonNode body, ArrayList<String> possibleMoves) {
            JsonNode neck = body.get(1);

            if (neck.get("x").asInt() < head.get("x").asInt()) {
                LOG.info("Dont go LEFT because of my neck");
                possibleMoves.remove("left");
            } else if (neck.get("x").asInt() > head.get("x").asInt()) {
                LOG.info("Dont go RIGHT because my of my neck");
                possibleMoves.remove("right");
            } else if (neck.get("y").asInt() < head.get("y").asInt()) {
                LOG.info("Dont go DOWN because my of my neck");
                possibleMoves.remove("down");
            } else if (neck.get("y").asInt() > head.get("y").asInt()) {
                LOG.info("Dont go UP because my of my neck");
                possibleMoves.remove("up");
            }

            return possibleMoves;
        }

        /**
         * This method is called when a game your Battlesnake was in ends.
         * 
         * It is purely for informational purposes, you don't have to make any decisions
         * here.
         *
         * @param endRequest a map containing the JSON sent to this snake. Use this data
         *                   to know which game has ended
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> end(JsonNode endRequest) {
            LOG.info("END");
            return EMPTY;
        }

        /**
         * Using information from 'moveRequest', find the edges of the board and
         * don't let your Battlesnake move beyond them board_height = ? board_width = ?
         *
         * @param head
         * @param possibleMoves
         * @param board_height
         * @param board_width
         * @return possible moves
         */
        public ArrayList<String> avoidTheWalls(JsonNode head, ArrayList<String> possibleMoves, int board_height, int board_width) {
            LOG.info("Looking for possible walls.");

            if (head.get("y").asInt() == 0) {
                LOG.info("Dont go DOWN because my head position is on limit: (" + head.get("y").asInt() + ")");
                possibleMoves.remove("down");
            } if (head.get("x").asInt() == 0) {
                LOG.info("Dont go LEFT because my head position is on limit: (" + head.get("x").asInt() + ")");
                possibleMoves.remove("left");
            } if (head.get("y").asInt() == board_height - 1) {
                LOG.info("Dont go UP because my head position is on limit: (" + head.get("y").asInt() + ")");
                possibleMoves.remove("up");
            } if (head.get("x").asInt() == board_width - 1 ) {
                LOG.info("Dont go RIGHT because my head position is on limit: (" + head.get("x").asInt() + ")");
                possibleMoves.remove("right");
            }

            return possibleMoves;
        }

        /**
         * head (X) +1 =/= body part (X)
         * head (X) -1 =/= body part (X)
         * head (Y) +1 =/= body part (Y)
         * head (Y) -1 =/= body part (Y)
         *
         * @param head position
         * @param body position
         * @param possibleMoves
         * @return
         */
        public ArrayList<String> avoidMyBody(JsonNode head, JsonNode body, ArrayList<String> possibleMoves) {
            // start with i = 2 ignore head and neck
            for (int i = 2; i < body.size(); i++) {
                JsonNode bodyPart = body.get(i);
                possibleMoves = avoidDifferentThings(bodyPart, head, possibleMoves, "my body");
            }

            return possibleMoves;
        }

        public ArrayList<String> avoidOtherSnakes(JsonNode head, int myLength, JsonNode snakes, ArrayList<String> possibleMoves) {
            int allSnakes = snakes.size();
            LOG.info("Looking for possible snake head positions next to mine");

            // loop snake array, ignore myself
            for (int i = 0; i < allSnakes; i++) {
                JsonNode otherSnake = snakes.get(i);

                // except me
                if (!otherSnake.get("name").asText().equals("snake-top")) {

                    // if i am longer then the other snake = eat her
                    if (myLength > otherSnake.get("length").asInt() + 1) {
                        LOG.info("I cat eat you");
                    } else {
                        // avoid other snakes head in the next possible position
                        JsonNode otherSnakeHead = otherSnake.get("head");

                        // x - 1
                        if (catchPossibleSnakeHeadPositions(head.get("x").asInt() - 1, head.get("y").asInt(), otherSnakeHead)) {
                            int myX = head.get("x").asInt() - 1;
                            int myY = head.get("y").asInt();
                            LOG.info("Dont go LEFT because my next head position is: (" + myX + " | " + myY + ")");
                            possibleMoves.remove("left");
                        }
                        // x + 1
                        if (catchPossibleSnakeHeadPositions(head.get("x").asInt() + 1, head.get("y").asInt(), otherSnakeHead)) {
                            int myX = head.get("x").asInt() + 1;
                            int myY = head.get("y").asInt();
                            LOG.info("Dont go RIGHT because my next head position is: (" + myX + " | " + myY + ")");
                            possibleMoves.remove("right");
                        }
                        // y - 1
                        if (catchPossibleSnakeHeadPositions(head.get("x").asInt(), head.get("y").asInt() - 1, otherSnakeHead)) {
                            int myX = head.get("x").asInt();
                            int myY = head.get("y").asInt() - 1;
                            LOG.info("Dont go DOWN because my next head position is: (" + myX + " | " + myY + ")");
                            possibleMoves.remove("down");
                        }
                        // y + 1
                        if (catchPossibleSnakeHeadPositions(head.get("x").asInt(), head.get("y").asInt() + 1, otherSnakeHead)) {
                            int myX = head.get("x").asInt();
                            int myY = head.get("y").asInt() + 1;
                            LOG.info("Dont go UP because my next head position is: (" + myX + " | " + myY + ")");
                            possibleMoves.remove("up");
                        }

                        // loop all snake body parts
                        for (int j = 0; j < otherSnake.get("length").asInt(); j++) {
                            JsonNode snakeBody = otherSnake.get("body").get(j);
                            possibleMoves = avoidDifferentThings(snakeBody, head, possibleMoves, "other snakes body");
                        }
                    }
                }
            }

            return possibleMoves;
        }

        public ArrayList<String> avoidDifferentThings(JsonNode bodyPart, JsonNode head, ArrayList<java.lang.String> possibleMoves, String reason) {
            if (bodyPart.get("y").asInt() == head.get("y").asInt() + 1 && bodyPart.get("x").asInt() == head.get("x").asInt()) {
                LOG.info("Dont go UP because of " + reason + " position: (" + bodyPart.get("x").asInt() + " | " + bodyPart.get("y").asInt() + ")");
                possibleMoves.remove("up");
            } if (bodyPart.get("y").asInt() == head.get("y").asInt() - 1 && bodyPart.get("x").asInt() == head.get("x").asInt()) {
                LOG.info("Dont go DOWN because of " + reason + " position: (" + bodyPart.get("x").asInt() + " | " + bodyPart.get("y").asInt() + ")");
                possibleMoves.remove("down");
            } if (bodyPart.get("x").asInt() == head.get("x").asInt() + 1 && bodyPart.get("y").asInt() == head.get("y").asInt()) {
                LOG.info("Dont go RIGHT because of " + reason + " position: (" + bodyPart.get("x").asInt() + " | " + bodyPart.get("y").asInt() + ")");
                possibleMoves.remove("right");
            } if (bodyPart.get("x").asInt() == head.get("x").asInt() - 1 && bodyPart.get("y").asInt() == head.get("y").asInt()) {
                LOG.info("Dont go LEFT because of " + reason + " position: (" + bodyPart.get("x").asInt() + " | " + bodyPart.get("y").asInt() + ")");
                possibleMoves.remove("left");
            }

            return possibleMoves;
        }

        public boolean catchPossibleSnakeHeadPositions(int myX, int myY, JsonNode otherSnakesHead) {
            boolean someoneCouldCatchMyHead = false;

            if (myX == otherSnakesHead.get("x").asInt() - 1 && myY == otherSnakesHead.get("y").asInt()) {
                int snakeX = otherSnakesHead.get("x").asInt() - 1;
                int snakeY = otherSnakesHead.get("y").asInt();
                LOG.info("Avoid possible head position from other snake: (" + snakeX + " | " + snakeY + ")");
                someoneCouldCatchMyHead = true;
            } else if (myX == otherSnakesHead.get("x").asInt() + 1 && myY == otherSnakesHead.get("y").asInt()) {
                int snakeX = otherSnakesHead.get("x").asInt() + 1;
                int snakeY = otherSnakesHead.get("y").asInt();
                LOG.info("Avoid possible head position from other snake: (" + snakeX + " | " + snakeY + ")");
                someoneCouldCatchMyHead = true;
            } else if (myX == otherSnakesHead.get("x").asInt() && myY == otherSnakesHead.get("y").asInt() - 1) {
                int snakeX = otherSnakesHead.get("x").asInt();
                int snakeY = otherSnakesHead.get("y").asInt() - 1;
                LOG.info("Avoid possible head position from other snake: (" + snakeX + " | " + snakeY + ")");
                someoneCouldCatchMyHead = true;
            } else if (myX == otherSnakesHead.get("x").asInt() && myY == otherSnakesHead.get("y").asInt() + 1) {
                int snakeX = otherSnakesHead.get("x").asInt();
                int snakeY = otherSnakesHead.get("y").asInt() + 1;
                LOG.info("Avoid possible head position from other snake: (" + snakeX + " | " + snakeY + ")");
                someoneCouldCatchMyHead = true;
            }

            return someoneCouldCatchMyHead;
        }
    }

}
