package com.drugarybna.trol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ray3k.stripe.FreeTypeSkin;

import java.util.ArrayList;

public class FirstLocation implements Screen {

    private TiledMap map;

    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer renderer;

    private FitViewport viewport;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private FreeTypeSkin skin;
    private Stage stage;
    private Label coordinatesLabel;

    private Window pauseMenuWindow;
    private boolean isPaused = false;

    private Character mainCharacter;

    private Array<Rectangle> collisionRect;
    private Array<Rectangle> playerBounds;

    private Vector2 cursorPos;

    @Override
    public void show() {

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        map = new TmxMapLoader().load("locations/location_initial.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(16, 9, camera);
        viewport.apply();

        skin = new FreeTypeSkin(Gdx.files.internal("skin/logica_skin.json"));

        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        Gdx.input.setInputProcessor(stage);

        coordinatesLabel = new Label("", skin);
        coordinatesLabel.setPosition(10, Gdx.graphics.getHeight() - 100);
        stage.addActor(coordinatesLabel);

        pauseMenuWindow = new Window("Pause", skin);
        pauseMenuWindow.setSize(260, (float) Gdx.graphics.getHeight() / 2);
        pauseMenuWindow.setPosition(64, (float) Gdx.graphics.getHeight() / 2 - pauseMenuWindow.getHeight() / 2);
        pauseMenuWindow.setMovable(false);
        pauseMenuWindow.setVisible(false);
        stage.addActor(pauseMenuWindow);

        mainCharacter = new Character(7, 1);

        collisionRect = new Array<>();
        MapLayer collisionLayer = map.getLayers().get("CollisionObjects");
        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                rect.setPosition(rect.x / 16, rect.y / 16);
                rect.setSize(rect.width / 16, rect.height / 16);
                collisionRect.add(rect);
            }
        }

        playerBounds = new Array<>();

    }

    @Override
    public void render(float v) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        renderer.setView(camera);
        renderer.render(new int[] {0, 1, 2, 3});

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderOverlayObjects(true);
        batch.draw(mainCharacter.getCurrentFrame(), mainCharacter.position.x, mainCharacter.position.y, 2, 2);
        renderOverlayObjects(false);
        batch.end();

        /*Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        for (Rectangle rect : collisionRect) {
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        for (Rectangle rect : playerBounds) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);*/

        renderer.render(new int[] {4, 6, 7});

        if (!isPaused) {
            logic(v);
            input(v);
            drawTileHover();
            for (Actor actor : stage.getActors()) {
                if (actor instanceof TextButton) {
                    actor.remove();
                }
            }
        }

        stage.getViewport().apply();
        stage.act();
        stage.draw();

    }

    private void drawTileHover() {

        Vector2 cursorPos = getCursorPos();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glLineWidth(12f);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(2.28f, 2.28f, 2.28f, 0.2f);
        shapeRenderer.rect(cursorPos.x, cursorPos.y, 1, 1);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

    }

    private void renderOverlayObjects(boolean behindCharacter) {
        MapLayer overlayLayer = map.getLayers().get("DetailsOverlay");

        if (!(overlayLayer instanceof TiledMapTileLayer)) return;

        TiledMapTileLayer layer = (TiledMapTileLayer) overlayLayer;
        float charY = mainCharacter.position.y;

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 16; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                if ((behindCharacter && (float) y > charY)
                    || (!behindCharacter && (float) y <= charY)) {
                    TiledMapTile tile = cell.getTile();
                    TextureRegion region = tile.getTextureRegion();

                    batch.draw(region, x, y, 1, 1);
                }
            }
        }
    }

    private void logic(float delta) {

        //coordinatesLabel.setText("Coordinates: " + (int) getCursorPos().x + ", " + (int) getCursorPos().y);
        cursorPos = getCursorPos();
        selectItem();

    }

    private void selectItem() {

        MapLayer interactiveLayer = map.getLayers().get("InteractiveObjects");
        MapObject panelStoryline = interactiveLayer.getObjects().get("Panel_Storyline");
        MapObject itemHint = interactiveLayer.getObjects().get("Item_Hint");
        MapObject zonePuzzle = interactiveLayer.getObjects().get("Zone_Puzzle");

        if (cursorPos.x == (float) panelStoryline.getProperties().get("x") / 16 && cursorPos.y == (float) panelStoryline.getProperties().get("y") / 16) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                isPaused = true;
                newInteractiveWindow("Altar of Wisdom", """
                    Lorem ipsum dolor sit amet,
                    consectetur adipiscing elit, sed do eiusmod
                    tempor incididunt ut labore et
                    dolore magna aliqua.
                """);
            }
        }

        if (cursorPos.x == (float) itemHint.getProperties().get("x") / 16 && cursorPos.y == (float) itemHint.getProperties().get("y") / 16) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                isPaused = true;
                newInteractiveWindow("Scroll of knowledge", """
                    Lorem ipsum dolor sit amet,
                    consectetur adipiscing elit, sed do eiusmod
                    tempor incididunt ut labore et
                    dolore magna aliqua.
                """);
            }
        }

        if (cursorPos.x == (float) zonePuzzle.getProperties().get("x") / 16 && cursorPos.y == (float) zonePuzzle.getProperties().get("y") / 16
            || cursorPos.x == ((float) zonePuzzle.getProperties().get("x") / 16) + 1 && cursorPos.y == (float) zonePuzzle.getProperties().get("y") / 16) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                isPaused = true;
                puzzle();
            }
        }

    }

    private void puzzle() {

        class RuneSlot {

            public Rectangle bounds;
            public TextButton storedRune = null;

            public RuneSlot(float x, float y, float width, float height) {
                bounds = new Rectangle(x, y, width, height);
            }

            public boolean isOverlapping(Rectangle runeBounds) {
                return bounds.overlaps(runeBounds);
            }

            public void setRune(TextButton rune) {
                this.storedRune = rune;
            }

            public String getRuneSymbol() {
                return storedRune != null ? storedRune.getText().toString() : "";
            }

            public boolean isEmpty() {
                return storedRune == null;
            }

        }

        int marginRight = 48;
        int marginTop = 96;

        ArrayList<RuneSlot> runeSlots = new ArrayList<>();

        Window puzzle = newInteractiveWindow("The Runes of Subtraction", "");
        puzzle.setWidth(Gdx.graphics.getWidth() * 0.8f);
        puzzle.setHeight(Gdx.graphics.getHeight() * 0.8f);
        puzzle.setY((float) Gdx.graphics.getHeight() /2 - puzzle.getHeight() / 2);
        puzzle.getTitleLabel().setFontScale(1.5f);

        Table mainRunes = new Table();
        for (int i = 0; i < 8; i++) {
            TextButton rune = new TextButton("|", skin, "semi-rune");
            rune.getLabel().setFontScale(1.5f);
            if (i == 4) rune.setText("#");
            mainRunes.add(rune).padRight(marginRight);
        }
        puzzle.row().padTop(marginTop);
        puzzle.add(mainRunes).colspan(1).left();

        Table expressionRunes1 = new Table();
        for (int i = 0; i < 3; i++) {
            TextButton rune = new TextButton("", skin, "semi-rune-expr");
            expressionRunes1.add(rune).padRight(marginRight);
        }
        TextButton arrow1 = new TextButton("=====>", skin, "rune");
        arrow1.getLabel().setFontScale(1.2f);
        expressionRunes1.add(arrow1).pad(0, 48, 0, 48);
        TextButton changeRune1 = new TextButton("", skin, "semi-rune-expr");
        expressionRunes1.add(changeRune1).padLeft(48);
        puzzle.row().padTop(marginTop);
        puzzle.add(expressionRunes1).colspan(1).left();
        Gdx.app.postRunnable(() -> {
            for (Actor actor : expressionRunes1.getChildren()) {
                if (actor instanceof TextButton && !((TextButton) actor).getText().toString().contains("=")) {
                    Vector2 pos = actor.localToStageCoordinates(new Vector2(0, 0));
                    RuneSlot slot = new RuneSlot(pos.x, pos.y, actor.getWidth(), actor.getHeight());
                    runeSlots.add(slot);
                }
            }
        });

        Table expressionRunes2 = new Table();
        TextButton runeToChg = new TextButton("", skin, "semi-rune-expr");
        expressionRunes2.add(runeToChg).padRight(marginRight);
        TextButton arrow2 = new TextButton("=====>", skin, "rune");
        arrow1.getLabel().setFontScale(1.2f);
        expressionRunes2.add(arrow2).pad(0, 48, 0, 48);
        TextButton changeRune2 = new TextButton("", skin, "semi-rune-expr");
        expressionRunes2.add(changeRune2).padLeft(48);
        puzzle.row().padTop((float) marginTop /2);
        puzzle.add(expressionRunes2).colspan(1).left();
        Gdx.app.postRunnable(() -> {
            for (Actor actor : expressionRunes2.getChildren()) {
                if (actor instanceof TextButton && !((TextButton) actor).getText().toString().contains("=")) {
                    Vector2 pos = actor.localToStageCoordinates(new Vector2(0, 0));
                    RuneSlot slot = new RuneSlot(pos.x, pos.y, actor.getWidth(), actor.getHeight());
                    runeSlots.add(slot);
                }
            }
        });

        Table chooseRunes = new Table();
        for (int i = 0; i < 8; i++) {
            TextButton rune = new TextButton("|", skin, "semi-rune");
            rune.getLabel().setFontScale(1.5f);
            switch (i) {
                case 2: rune.setText("+"); break;
                case 3: rune.setText("a"); break;
                case 4: rune.setText("#"); break;
                case 5: rune.setText(""); break;
                case 6: rune.setText("b"); break;
            }
            chooseRunes.add(rune).padRight(marginRight);
        }
        puzzle.row().padTop(marginTop);
        puzzle.add(chooseRunes).colspan(1).left();

        puzzle.row().padTop(marginTop);

        puzzle.getChild(2).remove();

        Table buttons = new Table();
        TextButton backButton = new TextButton("Back", skin, "rune");
        buttons.add(backButton).padRight(marginRight);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                runeSlots.clear();
                isPaused = false;
                puzzle.remove();
            }
        });
        TextButton enterButton = new TextButton("Enter", skin, "rune");
        buttons.add(enterButton);
        puzzle.add(buttons).colspan(1).left();

        for (Actor rune : chooseRunes.getChildren()) {
            rune.addListener(new ClickListener() {
                TextButton draggingRune;
                TextButton origRune;
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (button == Input.Buttons.LEFT) {
                        origRune = (TextButton) event.getListenerActor();
                        origRune.setVisible(false);
                        draggingRune = new TextButton(origRune.getText().toString(), skin, "semi-rune");
                        draggingRune.getLabel().setFontScale(1.5f);
                        Vector2 stageCoords = new Vector2();
                        stageCoords.x = event.getStageX();
                        stageCoords.y = event.getStageY();
                        draggingRune.setPosition(stageCoords.x - draggingRune.getWidth() / 2f,
                            stageCoords.y - draggingRune.getHeight() / 2f);
                        stage.addActor(draggingRune);
                        return true;
                    }
                    return false;
                }
                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if (draggingRune != null) {
                        Vector2 stageCoords = new Vector2();
                        stageCoords.x = event.getStageX();
                        stageCoords.y = event.getStageY();
                        draggingRune.setPosition(stageCoords.x - draggingRune.getWidth() / 2f,
                            stageCoords.y - draggingRune.getHeight() / 2f);
                    }
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (button == Input.Buttons.LEFT && draggingRune != null) {
                        Rectangle curr = new Rectangle(
                            draggingRune.getX(), draggingRune.getY(),
                            draggingRune.getWidth(), draggingRune.getHeight()
                        );
                        boolean inserted = false;
                        for (RuneSlot slot : runeSlots) {
                            if (slot.isOverlapping(curr) && slot.isEmpty()) {
                                inserted = true;
                                TextButton visual = new TextButton(draggingRune.getText().toString(), skin, "semi-rune");
                                Vector2 localPos = puzzle.stageToLocalCoordinates(new Vector2(slot.bounds.x, slot.bounds.y));
                                visual.setPosition(localPos.x, localPos.y);
                                visual.setSize(slot.bounds.width, slot.bounds.height);
                                puzzle.addActor(visual);
                                slot.setRune(visual);
                                break;
                            }
                        }
                        draggingRune.remove();
                        draggingRune = null;
                        if (!inserted) {
                            origRune.setVisible(true);
                        }
                    }
                }
            });
        }

    }

    private Window newInteractiveWindow(String title, String description) {
        Window interactiveWindow = new Window(title, skin);
        interactiveWindow.setMovable(false);
        stage.addActor(interactiveWindow);
        interactiveWindow.left().top().padTop(64);
        Label interactiveLabel = new Label(description, skin);
        interactiveLabel.setFontScale(0.85f);
        interactiveWindow.add(interactiveLabel);
        interactiveWindow.row().padTop(16);
        TextButton resumeButton = new TextButton("Resume", skin);
        interactiveWindow.add(resumeButton).left();
        interactiveWindow.setSize(interactiveLabel.getWidth(), interactiveLabel.getHeight()+112+130);
        interactiveWindow.setPosition((float) Gdx.graphics.getWidth() /2 - interactiveWindow.getWidth()/2,
            (float) Gdx.graphics.getHeight() /2 - interactiveWindow.getHeight()/2);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                interactiveWindow.remove();
            }
        });
        return interactiveWindow;
    }

    private Vector2 getCursorPos() {
        Vector3 cursorPos = viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        float x = (float) Math.floor(cursorPos.x);
        float y = (float) Math.floor(cursorPos.y);

        return new Vector2(x, y);
    }

    private void input(float delta) {

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            isPaused = true;
            pauseMenu();
        }

        characterMovement(delta);

    }

    private void characterMovement(float delta) {

        limitMoveByMap(16, 9);

        float posX = mainCharacter.position.x;
        float posY = mainCharacter.position.y;

        boolean isTopBlocked = false;
        boolean isBottomBlocked = false;
        boolean isLeftBlocked = false;
        boolean isRightBlocked = false;

        float boundsSide = 0.05f;

        playerBounds.clear();
        Rectangle playerBoundsTop = new Rectangle(posX + 2/3f + boundsSide, posY + 2/3f + 1/10f - boundsSide, 2/3f - boundsSide*2, boundsSide);
        playerBounds.add(playerBoundsTop);
        Rectangle playerBoundsBottom = new Rectangle(posX + 2/3f + boundsSide, posY + 1/10f, 2/3f - boundsSide*2, boundsSide);
        playerBounds.add(playerBoundsBottom);
        Rectangle playerBoundsLeft = new Rectangle(posX + 2/3f, posY + 1/10f + boundsSide, boundsSide, 2/3f - boundsSide*2);
        playerBounds.add(playerBoundsLeft);
        Rectangle playerBoundsRight = new Rectangle(posX + 4/3f - boundsSide, posY  + 1/10f + boundsSide, boundsSide, 2/3f - boundsSide*2);
        playerBounds.add(playerBoundsRight);

        for (Rectangle rect : collisionRect) {
            if (rect.overlaps(playerBoundsTop)) {
                isTopBlocked = true;
                break;
            }
            if (rect.overlaps(playerBoundsBottom)) {
                isBottomBlocked = true;
                break;
            }
            if (rect.overlaps(playerBoundsLeft)) {
                isLeftBlocked = true;
                break;
            }
            if (rect.overlaps(playerBoundsRight)) {
                isRightBlocked = true;
                break;
            }
        }


        if (Gdx.input.isKeyPressed(Input.Keys.W) && !isTopBlocked) {
            mainCharacter.move(Character.Direction.UP, delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.S) && !isBottomBlocked) {
            mainCharacter.move(Character.Direction.DOWN, delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) && !isLeftBlocked) {
            mainCharacter.move(Character.Direction.LEFT, delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) && !isRightBlocked) {
            mainCharacter.move(Character.Direction.RIGHT, delta);
        } else {
            mainCharacter.stop();
        }

    }

    private void limitMoveByMap(float mapWidth, float mapHeight) {
        if (mainCharacter.position.x < -1) {
            mainCharacter.position.x = -1;
        }
        if (mainCharacter.position.x > mapWidth - 1) {
            mainCharacter.position.x = mapWidth - 1;
        }

        if (mainCharacter.position.y < -0.5f) {
            mainCharacter.position.y = -0.5f;
        }
        if (mainCharacter.position.y > mapHeight - 1) {
            mainCharacter.position.y = mapHeight - 1;
        }
    }

    private void pauseMenu() {

        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                pauseMenuWindow.setVisible(false);
                pauseMenuWindow.clear();
            }
        });
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        pauseMenuWindow.left();
        pauseMenuWindow.add(resumeButton).width(260).height(100).left();
        pauseMenuWindow.row();
        pauseMenuWindow.add(exitButton).width(260).height(100).padTop(16).left();

        pauseMenuWindow.setVisible(true);

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
    }
}
