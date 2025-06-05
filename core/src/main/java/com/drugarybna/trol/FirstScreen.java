package com.drugarybna.trol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.ray3k.stripe.FreeTypeSkin;

import static com.badlogic.gdx.Gdx.gl;

public class FirstScreen implements Screen {

    private Stage stage;
    private FreeTypeSkin skin;

    @Override
    public void show() {

        skin = new FreeTypeSkin(Gdx.files.internal("skin/logica_skin.json"));

        stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        Gdx.input.setInputProcessor(stage);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.right();
        mainTable.padRight(100);
        stage.addActor(mainTable);

        ImageButton title = new ImageButton(skin, "default");
        TextButton buttonPlay = new TextButton("Play", skin);
        TextButton buttonExit = new TextButton("Exit", skin);
        buttonExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        mainTable.add(title).right().width(360).height(194);
        mainTable.row().padTop(50);
        mainTable.add(buttonPlay).right().width(130).height(50);
        mainTable.row().padTop(20);
        mainTable.add(buttonExit).right().width(130).height(50);

        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

    }

    @Override
    public void render(float delta) {


        Gdx.gl.glClearColor(0.012f, 0.020f, 0.078f, 1f);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {

        stage.getViewport().update(width, height, true);

    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {

        stage.dispose();

    }
}
