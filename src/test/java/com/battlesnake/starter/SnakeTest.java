package com.battlesnake.starter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SnakeTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private Snake.Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Snake.Handler();
    }

    @Test
    void indexTest() throws IOException {

        Map<String, String> response = handler.index();
        assertEquals("#D8A7B1", response.get("color"));
        assertEquals("tongue", response.get("head"));
        assertEquals("default", response.get("tail"));
    }

    @Test
    void startTest() throws IOException {
        JsonNode startRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.end(startRequest);
        assertEquals(0, response.size());

    }

    @Test
    void moveTest() throws IOException {
        JsonNode moveRequest = OBJECT_MAPPER.readTree(
                "{\"game\":{\"id\":\"game-00fe20da-94ad-11ea-bb37\",\"ruleset\":{\"name\":\"standard\",\"version\":\"v.1.2.3\"},\"timeout\":500},\"turn\":14,\"board\":{\"height\":11,\"width\":11,\"food\":[{\"x\":5,\"y\":5},{\"x\":9,\"y\":0},{\"x\":2,\"y\":6}],\"hazards\":[{\"x\":3,\"y\":2}],\"snakes\":[{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"My Snake\",\"health\":54,\"body\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":0},{\"x\":2,\"y\":0}],\"latency\":\"111\",\"head\":{\"x\":0,\"y\":0},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"},{\"id\":\"snake-b67f4906-94ae-11ea-bb37\",\"name\":\"Another Snake\",\"health\":16,\"body\":[{\"x\":5,\"y\":4},{\"x\":5,\"y\":3},{\"x\":6,\"y\":3},{\"x\":6,\"y\":2}],\"latency\":\"222\",\"head\":{\"x\":5,\"y\":4},\"length\":4,\"shout\":\"I'm not really sure...\",\"squad\":\"\"}]},\"you\":{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"My Snake\",\"health\":54,\"body\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":0},{\"x\":2,\"y\":0}],\"latency\":\"111\",\"head\":{\"x\":0,\"y\":0},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}}");
        Map<String, String> response = handler.move(moveRequest);

        List<String> options = new ArrayList<String>();
        options.add("up");
        options.add("down");
        options.add("left");
        options.add("right");

        assertTrue(options.contains(response.get("move")));
    }

    @Test
    void moveTest2() throws IOException {
        JsonNode moveRequest = OBJECT_MAPPER.readTree(
                "{\"game\":{\"id\":\"game-00fe20da-94ad-11ea-bb37\",\"ruleset\":{\"name\":\"standard\",\"version\":\"v.1.2.3\"},\"timeout\":500},\"turn\":14,\"board\":{\"height\":11,\"width\":11,\"food\":[{\"x\":5,\"y\":5},{\"x\":9,\"y\":0},{\"x\":2,\"y\":6}],\"hazards\":[{\"x\":3,\"y\":2}]," +
                        "\"snakes\":[{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"My Snake\",\"health\":54,\"body\":[{\"x\":5,\"y\":10},{\"x\":5,\"y\":9},{\"x\":5,\"y\":8}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":10},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}," +
                        "{\"id\":\"snake-b67f4906-94ae-11ea-bb37\",\"name\":\"Another Snake\",\"health\":16,\"body\":[{\"x\":5,\"y\":4},{\"x\":5,\"y\":3},{\"x\":6,\"y\":3},{\"x\":6,\"y\":2}],\"latency\":\"222\",\"head\":{\"x\":5,\"y\":4},\"length\":4,\"shout\":\"I'm not really sure...\",\"squad\":\"\"}]}," +
                        "\"you\":{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"My Snake\",\"health\":54,\"body\":[{\"x\":5,\"y\":10},{\"x\":5,\"y\":9},{\"x\":5,\"y\":8}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":10},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}}");
        Map<String, String> response = handler.move(moveRequest);

        List<String> options = new ArrayList<String>();
        options.add("left");
        options.add("right");

        assertTrue(options.contains(response.get("move")));
    }

    @Test
    void endTest() throws IOException {
        JsonNode endRequest = OBJECT_MAPPER.readTree("{}");
        Map<String, String> response = handler.end(endRequest);
        assertEquals(0, response.size());
    }

    @Test
    void avoidNeckAllTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 5}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

        handler.avoidMyNeck(testHead, testBody, possibleMoves);

        assertTrue(possibleMoves.size() == 4);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidNeckLeftTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 4, \"y\": 5}, {\"x\": 3, \"y\": 5}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "right"));

        handler.avoidMyNeck(testHead, testBody, possibleMoves);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidNeckRightTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 6, \"y\": 5}, {\"x\": 7, \"y\": 5}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left"));

        handler.avoidMyNeck(testHead, testBody, possibleMoves);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidNeckUpTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 6}, {\"x\": 5, \"y\": 7}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("down", "left", "right"));

        handler.avoidMyNeck(testHead, testBody, possibleMoves);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidNeckDownTest() throws IOException {

        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 4}, {\"x\": 5, \"y\": 3}]");
        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "left", "right"));

        handler.avoidMyNeck(testHead, testBody, possibleMoves);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findAllEdgesTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 4);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findLeftWallTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 0, \"y\": 5}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "right"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findTopWallTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 10}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("down", "left", "right"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findRightWallTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 10, \"y\": 5}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findBottomWallTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 0}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "left", "right"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findLeftBottomEdgeTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 0, \"y\": 0}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "right"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findLeftTopEdgeTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 0, \"y\": 10}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("down", "right"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findRightBottomEdgeTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 10, \"y\": 0}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "left"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void findRightTopEdgeTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 10, \"y\": 10}");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("down", "left"));

        handler.avoidTheWalls(testHead, possibleMoves, 11, 11);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidMyBodyLength3Test() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 4}, {\"x\": 6, \"y\": 4}, {\"x\": 6, \"y\": 5}]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left"));

        handler.avoidMyBody(testHead, testBody, possibleMoves);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidMyBodyLength4Test() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");
        JsonNode testBody = OBJECT_MAPPER
                .readTree("[{\"x\": 5, \"y\": 5}, {\"x\": 5, \"y\": 4}, {\"x\": 6, \"y\": 4}, {\"x\": 6, \"y\": 5}, {\"x\": 6, \"y\": 6}]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left"));

        handler.avoidMyBody(testHead, testBody, possibleMoves);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidOtherSnakesTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        JsonNode testSnakes = OBJECT_MAPPER.
                readTree("[" +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"snake-top\",\"health\":54,\"body\":[{\"x\":5,\"y\":5},{\"x\":6,\"y\":5},{\"x\":7,\"y\":5}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":5},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-2344\",\"name\":\"Snake 1\",\"health\":54,\"body\":[{\"x\":5,\"y\":4},{\"x\":6,\"y\":4},{\"x\":7,\"y\":4}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":4},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-3422\",\"name\":\"Snake 2\",\"health\":54,\"body\":[{\"x\":5,\"y\":6},{\"x\":6,\"y\":6},{\"x\":7,\"y\":6}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":6},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}" +
                        "]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("left", "right"));

        handler.avoidOtherSnakes(testHead, 3, testSnakes, possibleMoves);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidOtherSnakes2Test() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        JsonNode testSnakes = OBJECT_MAPPER.
                readTree("[" +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"snake-top\",\"health\":54,\"body\":[{\"x\":5,\"y\":5},{\"x\":6,\"y\":5},{\"x\":7,\"y\":5}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":5},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-2344\",\"name\":\"Snake 1\",\"health\":54,\"body\":[{\"x\":5,\"y\":4},{\"x\":6,\"y\":4},{\"x\":7,\"y\":4}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":4},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-3422\",\"name\":\"Snake 2\",\"health\":54,\"body\":[{\"x\":4,\"y\":5},{\"x\":4,\"y\":6},{\"x\":4,\"y\":7}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":6},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}" +
                        "]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "right"));

        handler.avoidOtherSnakes(testHead, 3, testSnakes, possibleMoves);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidOtherSnakesHeadTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        JsonNode testSnakes = OBJECT_MAPPER.
                readTree("[" +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"snake-top\",\"health\":54,\"body\":[{\"x\":5,\"y\":5},{\"x\":6,\"y\":5},{\"x\":7,\"y\":5}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":5},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-2344\",\"name\":\"Snake 1\",\"health\":54,\"body\":[{\"x\":4,\"y\":4},{\"x\":5,\"y\":4},{\"x\":6,\"y\":4}],\"latency\":\"111\",\"head\":{\"x\":4,\"y\":4},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"} " +
                        "]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "right"));

        handler.avoidOtherSnakes(testHead, 3, testSnakes, possibleMoves);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidOtherSnakesHead2Test() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        JsonNode testSnakes = OBJECT_MAPPER.
                readTree("[" +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"snake-top\",\"health\":54,\"body\":[{\"x\":5,\"y\":5},{\"x\":6,\"y\":5},{\"x\":7,\"y\":5}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":5},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-2344\",\"name\":\"Snake 1\",\"health\":54,\"body\":[{\"x\":4,\"y\":6},{\"x\":4,\"y\":7},{\"x\":4,\"y\":8}],\"latency\":\"111\",\"head\":{\"x\":4,\"y\":6},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"} " +
                        "]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("down", "right"));

        handler.avoidOtherSnakes(testHead, 3, testSnakes, possibleMoves);

        assertTrue(possibleMoves.size() == 2);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void avoidOtherSnakesHead3Test() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        JsonNode testSnakes = OBJECT_MAPPER.
                readTree("[" +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"snake-top\",\"health\":54,\"body\":[{\"x\":5,\"y\":5},{\"x\":5,\"y\":4},{\"x\":5,\"y\":3}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":5},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-2344\",\"name\":\"Snake 1\",\"health\":54,\"body\":[{\"x\":5,\"y\":7},{\"x\":6,\"y\":7},{\"x\":7,\"y\":7}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":7},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"} " +
                        "]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("down", "left", "right"));

        handler.avoidOtherSnakes(testHead, 3, testSnakes, possibleMoves);

        assertTrue(possibleMoves.size() == 3);
        assertTrue(possibleMoves.equals(expectedResult));
    }

    @Test
    void checkOtherSnakesHeadPositionForTheNextRoundTest() throws IOException {
        JsonNode otherSnakeHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 7}");

        boolean expectedResult = handler.catchPossibleSnakeHeadPositions(4, 5, otherSnakeHead);

        assertTrue(!expectedResult);
    }

    @Test
    void checkOtherSnakesHeadPositionForTheNextRound2Test() throws IOException {
        JsonNode otherSnakeHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 7}");

        boolean expectedResult = handler.catchPossibleSnakeHeadPositions(5, 6, otherSnakeHead);

        assertTrue(expectedResult);
    }


    @Test
    void avoidNoSnakesTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        JsonNode testSnakes = OBJECT_MAPPER.
                readTree("[" +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"snake-top\",\"health\":54,\"body\":[{\"x\":5,\"y\":5},{\"x\":6,\"y\":5},{\"x\":7,\"y\":5}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":5},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-2344\",\"name\":\"Snake 1\",\"health\":54,\"body\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":0},{\"x\":2,\"y\":0}],\"latency\":\"111\",\"head\":{\"x\":0,\"y\":0},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-3422\",\"name\":\"Snake 2\",\"health\":54,\"body\":[{\"x\":10,\"y\":10},{\"x\":10,\"y\":9},{\"x\":10,\"y\":8}],\"latency\":\"111\",\"head\":{\"x\":10,\"y\":10},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"}" +
                        "]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

        handler.avoidOtherSnakes(testHead, 3, testSnakes, possibleMoves);

        assertTrue(possibleMoves.size() == 4);
        assertTrue(possibleMoves.equals(expectedResult));
    }


    @Test
    void avoidNOTOtherSnakesHeadIfIAmBiggerTest() throws IOException {
        JsonNode testHead = OBJECT_MAPPER.readTree("{\"x\": 5, \"y\": 5}");

        JsonNode testSnakes = OBJECT_MAPPER.
                readTree("[" +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-bb37\",\"name\":\"snake-top\",\"health\":54,\"body\":[{\"x\":5,\"y\":5},{\"x\":5,\"y\":4},{\"x\":5,\"y\":3},{\"x\":5,\"y\":2},{\"x\":5,\"y\":1}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":5},\"length\":5,\"shout\":\"why are we shouting??\",\"squad\":\"\"}, " +
                        "{\"id\":\"snake-508e96ac-94ad-11ea-2344\",\"name\":\"Snake 1\",\"health\":54,\"body\":[{\"x\":5,\"y\":7},{\"x\":6,\"y\":7},{\"x\":7,\"y\":7}],\"latency\":\"111\",\"head\":{\"x\":5,\"y\":7},\"length\":3,\"shout\":\"why are we shouting??\",\"squad\":\"\"} " +
                        "]");

        ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));

        handler.avoidOtherSnakes(testHead, 5, testSnakes, possibleMoves);

        assertTrue(possibleMoves.size() == 4);
        assertTrue(possibleMoves.equals(expectedResult));
    }

}
