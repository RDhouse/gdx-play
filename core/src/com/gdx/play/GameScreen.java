package com.gdx.play;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

/**
 * Created by RDHouse on 15-4-2017.
 */
public class GameScreen implements Screen {

    final Drop game;

    private Texture bucketImage, raindropImage;
    private Sound dropSound;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private Rectangle bucketObject;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private int dropsCollected;

    public GameScreen(final Drop game) {
        this.game = game;

        // Load images and music
        bucketImage = new Texture(Gdx.files.internal("images/bucket.png"));
        raindropImage = new Texture(Gdx.files.internal("images/droplet.png"));

        dropSound = Gdx.audio.newSound(Gdx.files.internal("fx/click.wav"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Music box.mp3"));
        backgroundMusic.setLooping(true);

        // Camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Create bucket
        bucketObject = new Rectangle();
        bucketObject.x = 800 / 2 - 64 / 2;
        bucketObject.y = 20;
        bucketObject.width = 64;
        bucketObject.height = 64;

        raindrops = new Array<Rectangle>();
        spawnRaindrop();

        backgroundMusic.setVolume(0.1f);
        backgroundMusic.play();
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        // begin a new batch and draw the bucket and
        // all drops
        game.batch.begin();
        game.font.draw(game.batch, "Drops Collected: " + dropsCollected, 0, 480);
        game.batch.draw(bucketImage, bucketObject.x, bucketObject.y, bucketObject.width, bucketObject.height);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(raindropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        // process user input
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucketObject.x = touchPos.x - 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            bucketObject.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            bucketObject.x += 200 * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if (bucketObject.x < 0)
            bucketObject.x = 0;
        if (bucketObject.x > 800 - 64)
            bucketObject.x = 800 - 64;

        // check if we need to create a new raindrop
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
            spawnRaindrop();

        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we increase the
        // value our drops counter and add a sound effect.
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + 64 < 0)
                iter.remove();
            if (raindrop.overlaps(bucketObject)) {
                dropsCollected++;
                dropSound.play();
                iter.remove();
            }
        }
    }

    @Override
    public void resize(int width, int height) {

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
        raindropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        backgroundMusic.dispose();
    }
}
