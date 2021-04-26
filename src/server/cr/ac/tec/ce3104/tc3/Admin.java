package cr.ac.tec.ce3104.tc3;

import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStreamReader;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.gameobjects.Fruit;
import cr.ac.tec.ce3104.tc3.gameobjects.Vines;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.Crocodile;
import cr.ac.tec.ce3104.tc3.gameobjects.FruitType;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.FruitFactory;
import cr.ac.tec.ce3104.tc3.gameobjects.CrocodileType;
import cr.ac.tec.ce3104.tc3.gameobjects.CrocodileFactory;

class BadCommand extends Exception {
    public static final long serialVersionUID = 0;
}

class Admin {
    public Admin(PrintStream realStdout) throws IOException {
        this.realStdout = realStdout;
        this.fakeStdout = new PipedOutputStream();
        this.stdoutSink = new BufferedReader(new InputStreamReader(new PipedInputStream(this.fakeStdout)));

        SwingUtilities.invokeLater(() -> this.start());
    }

    public OutputStream getOutputStream() {
        return this.fakeStdout;
    }

    private final PrintStream realStdout;
    private final PipedOutputStream fakeStdout;
    private final BufferedReader stdoutSink;

    private JFrame frame;
    private JTextArea consoleOutput;
    private JTextField inputLine;
    private Thread sinkThread;

    private void start() {
        this.frame = new JFrame("DonCEy Kong Jr. server");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.PAGE_AXIS));

        Font font = new Font("monospaced", Font.PLAIN, 16);

        this.consoleOutput = new JTextArea(40, 100);
        this.consoleOutput.setFont(font);
        this.consoleOutput.setEditable(false);
        this.consoleOutput.setBackground(Color.BLACK);
        this.consoleOutput.setForeground(Color.WHITE);
        this.frame.getContentPane().add(new JScrollPane(this.consoleOutput));

        ((DefaultCaret)this.consoleOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.frame.getContentPane().add(Box.createHorizontalGlue());

        this.inputLine = new JTextField();
        this.inputLine.setFont(font);
        this.inputLine.addActionListener(event -> this.onCommand());
        this.frame.getContentPane().add(inputLine);

        this.frame.pack();
        this.frame.setVisible(true);

        this.sinkThread = new Thread(() -> this.readSink());
        this.sinkThread.start();
    }

    private void readSink() {
        try {
            while (true) {
                String line = this.stdoutSink.readLine();
                if (line == null) {
                    continue;
                }

                this.realStdout.println(line);
                SwingUtilities.invokeLater(() -> this.consoleOutput.append(line + "\n"));
            }
        } catch (Exception exception) {
            // Ignorado
        }
    }

    private void onCommand() {
        String line = this.inputLine.getText().trim();
        this.inputLine.setText("");

        if (line.isEmpty()) {
            return;
        }

        String[] command = line.split(" +");
        System.out.println("> " + line);

        try {
            switch (command[0]) {
                case "help":
                    System.out.println("=== Available commands ===");
                    System.out.println("help: Show command help");
                    System.out.println("clear: Clears the command line");
                    System.out.println("list-games: Show running games");
                    System.out.println("list-objects <game>: Show all entities in a running games");
                    System.out.println("delete <game> <object>: Delete an object from the game scene");
                    System.out.println("detach-vines <game> <platform>: Removes vines from a platform");
                    System.out.println("attach-vines <game> <platform> <length>: Adds vines to a platform");
                    System.out.println("put-fruit <game> <x> <y> [apple|banana|nispero] <score>: Adds a fruit");
                    System.out.println("put-crocodile <game> <platform> [red|blue]: Adds a crocodile");
                    System.out.println("highlight <game> <object> [yes|no]: (Un)highlights an entity");
                    break;

                case "clear":
                    this.consoleOutput.setText("");
                    break;

                case "list-games":
                {
                    Boolean atLeastOne = false;
                    for (Integer id : Server.getInstance().getGameIds()) {
                        atLeastOne = true;

                        Game game = Server.getInstance().getGame(id);
                        System.out.println("Game " + id + " started by client " + game.getPlayerId());
                    }

                    if (!atLeastOne) {
                        System.out.println("No games are running");
                    }

                    break;
                }

                case "list-objects":
                {
                    Game game = expectGame(command, 1);
                    for (GameObject object : game.getGameObjects().values()) {
                        System.out.println(objectDescription(object));
                    }

                    System.out.println("Total: " + game.getGameObjects().size());
                    break;
                }

                case "delete":
                {
                    Game game = expectGame(command, 1);
                    GameObject object = expectGameObject(game, command, 2);

                    object.delete();
                    System.out.println("Deleted " + objectDescription(object));

                    break;
                }

                case "detach-vines":
                {
                    Game game = expectGame(command, 1);
                    Platform platform = expectPlatform(game, command, 2);

                    String description = objectDescription(platform);
                    if (platform.detach()) {
                        System.out.println("Vines removed from " + description);
                    } else {
                        System.out.println("No vines are attached to " + description);
                    }

                    break;
                }

                case "attach-vines":
                {
                    Game game = expectGame(command, 1);
                    Platform platform = expectPlatform(game, command, 2);
                    Integer length = expectInteger(command, 3);

                    if (length <= 0) {
                        throw new BadCommand();
                    } else if (length >= 20) {
                        System.err.println("Error: too long! Refusing to create");
                        throw new BadCommand();
                    } else if (platform.getAttached() != null) {
                        System.out.println("There are already vines attached to " + objectDescription(platform));
                    } else {
                        Vines[] chain = Vines.makeChain(platform, length);
                        game.spawn(chain);

                        for (Vines vines : chain) {
                            System.out.println("Created " + objectDescription(vines));
                        }
                    }

                    break;
                }

                case "put-fruit":
                {
                    Game game = expectGame(command, 1);
                    Integer x = expectInteger(command, 2);
                    Integer y = expectInteger(command, 3);

                    FruitType type;
                    switch (expectArgument(command, 4)) {
                        case "apple":
                            type = FruitType.APPLE;
                            break;

                        case "banana":
                            type = FruitType.BANANA;
                            break;

                        case "nispero":
                            type = FruitType.NISPERO;
                            break;

                        default:
                            throw new BadCommand();
                    }

                    Position position = new Position(x, y);
                    Integer score = expectInteger(command, 5);

                    Fruit fruit = game.spawn(new FruitFactory().createFruit(type, position, score));
                    System.out.println("Created fruit " + objectDescription(fruit));
                    break;
                }

                case "put-crocodile":
                {
                    Game game = expectGame(command, 1);
                    Platform platform = expectPlatform(game, command, 2);

                    CrocodileType type;
                    switch (expectArgument(command, 3)) {
                        case "blue":
                            type = CrocodileType.BLUE;
                            break;

                        case "red":
                            type = CrocodileType.RED;
                            if (platform.getAttached() == null) {
                                System.err.println("Error: red crocodiles require attached vines");
                                throw new BadCommand();
                            }

                            break;

                        default:
                            throw new BadCommand();
                    }

                    Integer difficulty = game.getDifficulty();
                    System.out.println("Current difficulty: " + difficulty);

                    Crocodile crocodile = game.spawn(new CrocodileFactory().createCrocodile(type, game, platform, difficulty));
                    System.out.println("Created crocodile " + objectDescription(crocodile));
                    break;
                }

                case "highlight":
                {
                    Game game = expectGame(command, 1);
                    GameObject object = expectGameObject(game, command, 2);

                    Boolean highlight;
                    switch (expectArgument(command, 3)) {
                        case "yes":
                            highlight = true;
                            break;

                        case "no":
                            highlight = false;
                            break;

                        default:
                            throw new BadCommand();
                    }

                    game.setHighlight(object.getId(), highlight);
                    System.out.println("Updated highlight state of " + objectDescription(object));

                    break;
                }

                default:
                    System.err.println("Error: unknown command '" + command[0] + "'. Type 'help' for more information.");
                    break;
            }
        } catch (BadCommand badCommand) {
            System.err.println("Error: bad usage. Type 'help' for more information.");
        }
    }

    private static Platform expectPlatform(Game game, String[] command, Integer index) throws BadCommand {
        GameObject object = expectGameObject(game, command, index);
        if (!(object instanceof Platform)) {
            System.err.println("Error: not a platform: " + objectDescription(object));
            throw new BadCommand();
        }

        return (Platform)object;
    }

    private static GameObject expectGameObject(Game game, String[] command, Integer index) throws BadCommand {
        Integer id = expectInteger(command, index);

        GameObject object = game.getGameObjects().get(id);
        if (object == null) {
            System.err.println("Error: no object has ID " + id);
            throw new BadCommand();
        }

        return object;
    }

    private static Game expectGame(String[] command, Integer index) throws BadCommand {
        Integer id = expectInteger(command, index);

        Game game = Server.getInstance().getGame(id);
        if (game == null) {
            System.err.println("Error: no game has ID " + id);
            throw new BadCommand();
        }

        return game;
    }

    private static Integer expectInteger(String[] command, Integer index) throws BadCommand {
        try {
            return Integer.parseInt(expectArgument(command, index));
        } catch (NumberFormatException exception) {
            throw new BadCommand();
        }
    }

    private static String expectArgument(String[] command, Integer index) throws BadCommand {
        if (index >= command.length) {
            throw new BadCommand();
        }

        return command[index];
    }

    private static String objectDescription(GameObject object) {
        Position position = object.getPosition();
        return object.getClass().getSimpleName() + " #" + object.getId()
             + " at (" + position.getX() + ", " + position.getY() + ")";
    }
}
