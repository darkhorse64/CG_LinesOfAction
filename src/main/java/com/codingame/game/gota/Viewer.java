package com.codingame.game.gota;

import com.codingame.game.Player;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Line;
import com.codingame.gameengine.module.toggle.ToggleModule;

import java.util.ArrayList;
import java.util.List;

public class Viewer {
    MultiplayerGameManager<Player> gameManager;
    GraphicEntityModule graphics;
    Rectangle[][] rectangles;
    Line[] lines;
    List<UnitUI> units;
    Rectangle[] lastActions;
    public PlayerUI[] playerUIS;

    int HEIGHT;
    int WIDTH;
    int VIEWER_WIDTH;
    int VIEWER_HEIGHT;
    int RECTANGLE_SIZE;
    int CIRCLE_RADIUS;
    int GAP;
    final int[] BOARDCOLORS = new int[]{0xB09673, 0xD2C2A8};
    final int BACKGROUNDCOLOR = 0x7F7F7F;
    final int[] HIGHLIGHTCOLOR = new int[]{0x7e9ec2, 0x7e9ec2, 0xFF0000};
    final double MOVE_TIME = 700;

    public Viewer(GraphicEntityModule graphics, Board board, MultiplayerGameManager<Player> gameManager, ToggleModule toggleModule) {
        this.graphics = graphics;
        this.gameManager = gameManager;

        VIEWER_WIDTH = this.graphics.getWorld().getWidth();
        VIEWER_HEIGHT = this.graphics.getWorld().getHeight();
        HEIGHT = board.HEIGHT;
        WIDTH = board.WIDTH;

        playerUIS = new PlayerUI[2];
        rectangles = new Rectangle[HEIGHT][WIDTH];
        lines = new Line[HEIGHT + WIDTH + 2];
        lastActions = new Rectangle[3];
        units = new ArrayList<>();

        this.graphics.createRectangle().setWidth(VIEWER_WIDTH).setHeight(VIEWER_HEIGHT).setFillColor(BACKGROUNDCOLOR);

        RECTANGLE_SIZE = VIEWER_HEIGHT / -~HEIGHT;
        int START_X = VIEWER_WIDTH / 2 - RECTANGLE_SIZE * WIDTH / 2;
        int FONT_SIZE = RECTANGLE_SIZE / 3;

        for (int y = 0; y < HEIGHT; ++y) {
            int yG = HEIGHT - y - 1;
            for (int x = 0; x < WIDTH; ++x) {
                int xG = x;
                rectangles[y][x] = graphics.createRectangle().setZIndex(1)
                        .setWidth(RECTANGLE_SIZE).setHeight(RECTANGLE_SIZE).setX(START_X + xG * RECTANGLE_SIZE).setY((int) (RECTANGLE_SIZE / 2) + yG * RECTANGLE_SIZE - FONT_SIZE / 2);
                if (x == 0) {
                    int length = -~y < 10 ? 1 : (int) (Math.log10(x) + 1);
                    graphics.createText(Integer.toString(-~y)).setX(rectangles[y][x].getX() - (int) (RECTANGLE_SIZE / 1.5) + (int) (FONT_SIZE / length * .5)).setY(rectangles[y][x].getY() + FONT_SIZE).setFontFamily("Verdana").setFontSize(FONT_SIZE).setFillColor(0xFEFEFE);
                }

                if (y == 0) {
                    graphics.createText(Character.toString((char) (97 + x))).setX(rectangles[y][x].getX() + (int) (FONT_SIZE * (1.25))).setY(rectangles[y][x].getY() + RECTANGLE_SIZE + FONT_SIZE / 4).setFontFamily("Verdana").setFontSize(FONT_SIZE).setFillColor(0xFEFEFE);
                }
                if (x == 0 || x == WIDTH - 1 || y == 0 || y == HEIGHT - 1)
                    rectangles[y][x].setFillColor(BOARDCOLORS[0]);
                else
                    rectangles[y][x].setFillColor(BOARDCOLORS[1]);
            }
        }

        for (int y = 0; y <= HEIGHT; ++y) {
            lines[y] = graphics.createLine().setFillColor(0x0).setLineWidth(3.0).setZIndex(3)
                    .setX(START_X).setX2(START_X + WIDTH * RECTANGLE_SIZE)
                    .setY((int)(RECTANGLE_SIZE / 2) + y * RECTANGLE_SIZE - FONT_SIZE / 2)
                    .setY2((int)(RECTANGLE_SIZE / 2) + y * RECTANGLE_SIZE - FONT_SIZE / 2);
        }

        for (int x = 0; x <= WIDTH; ++x) {
            lines[WIDTH + 1 + x] = graphics.createLine().setFillColor(0x0).setLineWidth(3.0).setZIndex(3)
                    .setX(START_X + x * RECTANGLE_SIZE).setX2(START_X + x * RECTANGLE_SIZE)
                    .setY((int)(RECTANGLE_SIZE / 2) - FONT_SIZE / 2)
                    .setY2((int)(RECTANGLE_SIZE / 2) + HEIGHT * RECTANGLE_SIZE - FONT_SIZE / 2);
        }

        CIRCLE_RADIUS = (int)(RECTANGLE_SIZE * .42);
        GAP = (RECTANGLE_SIZE - CIRCLE_RADIUS * 2) / 2;

        for (int i = 0; i < 3; ++i) {
            lastActions[i] = graphics.createRectangle().setFillColor(HIGHLIGHTCOLOR[i]).setWidth(RECTANGLE_SIZE).setHeight(RECTANGLE_SIZE).setX(-RECTANGLE_SIZE).setY(-RECTANGLE_SIZE).setFillAlpha(i < 2 ? 0.75 : 0.45).setZIndex(2);
            toggleModule.displayOnToggleState(lastActions[i], "debugToggle", true);
        }

        for (Unit unit : board.units) {
            units.add(new UnitUI(unit, this));
        }

        for (int i = 0; i < 2; ++i) {
            playerUIS[i] = new PlayerUI(this, gameManager.getPlayer(i));
        }

        for (int y = 0; y < HEIGHT; ++y) {
            for (int x = 0; x < WIDTH; ++x) {
                graphics.commitEntityState(0.4, rectangles[y][x]);
            }
        }

        for (int i = 0; i < 3; ++i) {
            lastActions[i].setX(-RECTANGLE_SIZE, Curve.IMMEDIATE);
        }

        for (int i = 0; i < 24; ++i) {
            graphics.commitEntityState(0.4, units.get(i).sprite);
            units.get(i).unit = board.units.get(i);
            int x = board.units.get(i).getX();
            int y = board.units.get(i).getY();
            units.get(i).sprite.setX(rectangles[y][x].getX() + GAP).setY(rectangles[y][x].getY() + GAP).setZIndex(3);
            graphics.commitEntityState(0.8, units.get(i).sprite);
        }

        for (int i = 0; i < 2; ++i) {
            playerUIS[i].msg.setText("");
            playerUIS[i].action.setText("");
        }

        playerUIS[0].piece.setImage("b.png");
        playerUIS[1].piece.setImage("w.png");
    }

    public void applyAction(Unit unit, Cell target) {
        double distance = Math.pow(Math.max(Math.abs(unit.getX() - target.x),Math.abs(unit.getY() - target.y)), 0.45);
        double frameDuration = distance * MOVE_TIME;
        gameManager.setFrameDuration((int)frameDuration);
        double commitTime = 1.0;

        for (UnitUI u : units) {
            if (u.unit != unit) continue;
            lastActions[0].setX(rectangles[unit.getY()][unit.getX()].getX(), Curve.IMMEDIATE).setY(rectangles[unit.getY()][unit.getX()].getY(), Curve.IMMEDIATE);

            int x = target.x;
            int y = target.y;
            lastActions[1].setX(rectangles[y][x].getX(), Curve.IMMEDIATE).setY(rectangles[y][x].getY(), Curve.IMMEDIATE);

            u.sprite.setZIndex(4);
            graphics.commitEntityState(0.0, u.sprite);
            u.sprite.setX(rectangles[y][x].getX() + GAP).setY(rectangles[y][x].getY() + GAP);
            u.sprite.setZIndex(3);
            graphics.commitEntityState(commitTime, u.sprite);
            break;
        }

        for (UnitUI u : units) {
            if (u.unit != target.unit) continue;
            u.sprite.setVisible(false);
            graphics.commitEntityState(commitTime, u.sprite);
            break;
        }
    }
}
