package com.gdx.play;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;


public class GdxPlay extends ApplicationAdapter {

	// Images
	private Texture bucketImage;
	private Texture dropletImage;

	// Camera
	private OrthographicCamera camera;

	// SpriteBatch
	private SpriteBatch batch;

	// Game object
	private Rectangle bucket;

	// Background track
	private Music backgroundTrack;

	// List of droplets
	Array<Rectangle> raindrops;
	// Time since last drop spawn

	private long lastDropTime;
	
	@Override
	public void create () {
		// Load the images
		bucketImage = new Texture(Gdx.files.internal("images/bucket.png"));
		dropletImage = new Texture(Gdx.files.internal("images/droplet.png"));

		// Create camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// Create sprite batch
		batch = new SpriteBatch();

		// Setup bucket game object
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		// Raindrops
		raindrops = new Array<Rectangle>();
		// Spawn first raindrop
		spawnRaindrop();

		// Music
		backgroundTrack = Gdx.audio.newMusic(Gdx.files.internal("music/Music box.mp3"));
		backgroundTrack.setVolume(0.1f);
		backgroundTrack.play();

	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		// Set clear color and clear color buffer
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// In gdxlib its generally a good idea to update the camera once per frame
		camera.update();

		// Tell the batch to use the coordinate system from the camara.
		batch.setProjectionMatrix(camera.combined);

		// A batch handles many textures, so begin() and end() comes with it.
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop : raindrops) {
			batch.draw(dropletImage, raindrop.x, raindrop.y);
		}
		batch.end();

		// Controls for bucket
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = (int) (touchPos.x - 64 / 2);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			bucket.x += 200 * Gdx.graphics.getDeltaTime();
		}

		// Keep the bucket within bounds of screen
		if (bucket.x < 0) {
			bucket.x = 0;
		}
		if (bucket.x > 800 - 64) {
			bucket.x = 800 -64;
		}

		// Spawn new raindrops
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) {
			spawnRaindrop();
		}

		// Raindrops logic
		Iterator<Rectangle> iterator = raindrops.iterator();
		while (iterator.hasNext()) {
			Rectangle raindrop = iterator.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) {
				iterator.remove();
			}
			// collision
			if(raindrop.overlaps(bucket)) {
				iterator.remove();
			}
		}

		// Exit
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}
	
	@Override
	public void dispose () {
		// Dispose all resources
		bucketImage.dispose();
		dropletImage.dispose();
		backgroundTrack.dispose();
		batch.dispose();
	}
}
