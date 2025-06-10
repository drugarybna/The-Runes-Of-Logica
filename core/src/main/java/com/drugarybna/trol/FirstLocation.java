package com.drugarybna.trol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ray3k.stripe.FreeTypeSkin;

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

        coordinatesLabel.setText("Coordinates: " + (int) getCursorPos().x + ", " + (int) getCursorPos().y);

        selectItem();

    }

    private void selectItem() {
        Vector2 cursorPos = getCursorPos();

        MapLayer interactiveLayer = map.getLayers().get("InteractiveObjects");
        for (MapObject object : interactiveLayer.getObjects()) {
            float objX = (float) object.getProperties().get("x") / 16;
            float objY = (float) object.getProperties().get("y") / 16;
            if (cursorPos.x == objX && cursorPos.y == objY) {
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    isPaused = true;
                    newInteractiveWindow();
                }
            }
        }
    }

    private void newInteractiveWindow() {
        Window interactiveWindow = new Window("Altar of Wisdom", skin);
        stage.addActor(interactiveWindow);
        interactiveWindow.left().top().padTop(64);
        Label interactiveLabel = new Label("Here is simple example of text \nthat can be written in this section \nlike storyline or something you know.", skin);
        interactiveLabel.setFontScale(0.85f);
        interactiveWindow.add(interactiveLabel);
        interactiveWindow.setSize(interactiveLabel.getWidth(), interactiveLabel.getHeight()+64);
        interactiveWindow.setPosition((float) Gdx.graphics.getWidth() /2 - interactiveWindow.getWidth()/2,
            (float) Gdx.graphics.getHeight() /2 - interactiveWindow.getHeight()/2);
        Image dimBackground = new Image(new TextureRegion(new Texture(Gdx.files.internal("dim_bg.png"))));
        dimBackground.setColor(0, 0, 0, 0.5f);
        dimBackground.setFillParent(true);
        dimBackground.setVisible(false);
        stage.addActor(dimBackground);
        dimBackground.setVisible(true);
        dimBackground.setZIndex(0);
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                interactiveWindow.setVisible(false);
                dimBackground.setVisible(false);
            }
        });
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

        Image dimBackground = new Image(new TextureRegion(new Texture(Gdx.files.internal("dim_bg.png"))));
        dimBackground.setColor(0, 0, 0, 0.5f);
        dimBackground.setFillParent(true);
        dimBackground.setVisible(false);

        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                pauseMenuWindow.setVisible(false);
                dimBackground.setVisible(false);
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
        stage.addActor(dimBackground);
        dimBackground.setVisible(true);
        dimBackground.setZIndex(0);

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
