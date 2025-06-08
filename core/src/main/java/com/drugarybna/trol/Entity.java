package com.drugarybna.trol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public abstract class Entity {

    public int WIDTH;
    public int HEIGHT;

    public float VELOCITY;

    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();

    protected Texture texture;

    protected enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public abstract void move(Direction direction, float delta);

}

class Character extends Entity {

    private final TextureRegion[] walkDownFrames;
    private final TextureRegion[] walkRightFrames;
    private final TextureRegion[] walkLeftFrames;
    private final TextureRegion[] walkUpFrames;

    private Animation<TextureRegion> animation;

    private float stateTime = 0f;
    private boolean isMoving = false;

    private Direction direction;

    public Character(int x, int y) {

        this.WIDTH = 48;
        this.HEIGHT = 48;
        this.VELOCITY = 15f;

        this.texture = new Texture(Gdx.files.internal("textures/character.png"));

        TextureRegion[][] movingFrames = TextureRegion.split(texture, WIDTH, HEIGHT);
        walkDownFrames = Arrays.copyOfRange(movingFrames[0], 0, 4);
        walkRightFrames = Arrays.copyOfRange(movingFrames[1], 0, 4);
        walkLeftFrames = Arrays.copyOfRange(movingFrames[2], 0, 4);
        walkUpFrames = Arrays.copyOfRange(movingFrames[3], 0, 4);

        this.WIDTH *= 5;
        this.HEIGHT *= 5;

        animation = new Animation<>(0.1f, walkUpFrames);
        animation.setPlayMode(Animation.PlayMode.LOOP);

        position.set(x, y);
        direction = Direction.UP;

    }

    @Override
    public void move(Direction direction, float delta) {
        this.direction = direction;
        isMoving = true;

        stateTime += delta;

        float frameDuration = 0.2f;
        float vel = VELOCITY / 1000f;
        switch (direction) {
            case UP:
                position.y += vel;
                animation = new Animation<>(frameDuration, walkUpFrames);
                break;
            case DOWN:
                position.y -= vel;
                animation = new Animation<>(frameDuration, walkDownFrames);
                break;
            case LEFT:
                position.x -= vel;
                animation = new Animation<>(frameDuration, walkLeftFrames);
                break;
            case RIGHT:
                position.x += vel;
                animation = new Animation<>(frameDuration, walkRightFrames);
                break;
        }

    }

    public void stop() {
        isMoving = false;
    }

    public TextureRegion getCurrentFrame() {
        if (animation != null && isMoving) {
            return animation.getKeyFrame(stateTime, true);
        } else if (animation != null) {
            return animation.getKeyFrame(4, false);
        } else {
            return null;
        }
    }

}
