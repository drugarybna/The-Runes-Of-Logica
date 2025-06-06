package com.drugarybna.trol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.ray3k.stripe.FreeTypeSkin;

public class FirstLocation implements Screen {

    public TiledMap map;

    public OrthographicCamera camera;
    public OrthogonalTiledMapRenderer renderer;

    public SpriteBatch batch;

    private FreeTypeSkin skin;
    private Stage stage;

    private Window pauseMenuWindow;
    private boolean isPaused = false;

    @Override
    public void show() {

        batch = new SpriteBatch();

        map = new TmxMapLoader().load("locations/location_initial.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 16, 9);
        camera.update();

        skin = new FreeTypeSkin(Gdx.files.internal("skin/logica_skin.json"));

        stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        Gdx.input.setInputProcessor(stage);

        pauseMenuWindow = new Window("Pause", skin);
        pauseMenuWindow.setSize(260, (float) Gdx.graphics.getHeight() / 2);
        pauseMenuWindow.setPosition(64, (float) Gdx.graphics.getHeight() / 2 - pauseMenuWindow.getHeight() / 2);
        pauseMenuWindow.setMovable(false);
        pauseMenuWindow.setVisible(false);
        stage.addActor(pauseMenuWindow);

    }

    @Override
    public void render(float v) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused) {
            input();
        }

        renderer.setView(camera);
        renderer.render();

        stage.getViewport().apply();
        stage.act();
        stage.draw();

        batch.begin();



        batch.end();

    }

    private void input() {

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            isPaused = true;
            pauseMenu();
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
