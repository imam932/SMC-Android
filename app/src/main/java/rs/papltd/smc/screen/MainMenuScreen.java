package rs.papltd.smc.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import rs.papltd.smc.*;
import rs.papltd.smc.model.*;
import rs.papltd.smc.model.enemy.Enemy;
import rs.papltd.smc.utility.*;

import rs.papltd.smc.model.Sprite;

/**
 * Created by pedja on 2/17/14.
 */
public class MainMenuScreen extends AbstractScreen implements InputProcessor
{
    Texture gameLogo;
    Texture gdxLogo;
    TextureRegion play, playP, musicOn, musicOff, musicOnP, musicOffP, soundOn, soundOff, soundOnP, soundOffP;
    Rectangle playR, musicR, soundR;
    OrthographicCamera drawCam, debugCam, hudCam;
    SpriteBatch batch;
    MaryoGame game;
	Background bgr1, bgr2;
	BackgroundColor bgColor;
    LevelLoader loader;
    private BitmapFont debugFont;
    private boolean playT = false, musicT = false, soundT = false, debug = false, playMusic, playSound;

    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();
    Music music;
    Sound audioOn;
    WorldWrapper worldWrapper;
    private ParticleEffect cloudsPEffect;

    public MainMenuScreen(MaryoGame game)
    {
		super(game);
        this.game = game;
        batch = new SpriteBatch();
        drawCam = new OrthographicCamera(Constants.CAMERA_WIDTH, Constants.CAMERA_HEIGHT);
        drawCam.position.set(Constants.CAMERA_WIDTH/2 + (Constants.DRAW_WIDTH - Constants.CAMERA_WIDTH) / 2, Constants.CAMERA_HEIGHT/2, 0);
        drawCam.update();
        debugCam = new OrthographicCamera(1280, 720);
        debugCam.position.set(1280/2, 720/2, 0);
        debugCam.update();
        hudCam = new OrthographicCamera(screenWidth, screenHeight);
        hudCam.position.set(screenWidth/2, screenHeight/2, 0);
        hudCam.update();

        loader = new LevelLoader();
        debugFont = new BitmapFont();
        debugFont.setColor(Color.RED);
        debugFont.setScale(1.3f);
        worldWrapper = new WorldWrapper();
    }

    @Override
    public void show()
    {
        Gdx.input.setInputProcessor(this);
        music = Assets.manager.get(loader.getLevel().getMusic().first());
    }

    @Override
    public void render(float delta)
    {
		Gdx.gl20.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		bgColor.render(drawCam);

        batch.setProjectionMatrix(drawCam.combined);
        batch.begin();

		bgr1.render(batch);
        bgr2.render(batch);

        cloudsPEffect.draw(batch, delta);

		drawSprites();
        drawEnemies(delta);

        Utility.draw(batch, gameLogo, 2f, 5f, 2f);
        worldWrapper.getMario().render(batch);

        batch.end();

        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        batch.draw(playT ? playP : play, playR.x, playR.y, playR.width, playR.height);
        batch.draw(soundT ? soundOnP : soundOn, soundR.x, soundR.y,soundR.width, soundR.height);
        batch.draw(musicT ? (Assets.playMusic ? musicOnP : musicOffP) : (Assets.playMusic ? musicOn : musicOff), musicR.x, musicR.y, musicR.width, musicR.height);
        batch.draw(gdxLogo, (screenWidth/100*2), (screenHeight/100*2),
                screenWidth/10f, (screenWidth/10f)/4);

        batch.end();

        if (debug)
        {
            batch.setProjectionMatrix(debugCam.combined);
            batch.begin();
            debugFont.drawMultiLine(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 50f, 670f);
            batch.end();
            if(debug)worldWrapper.getDebugRenderer().render(worldWrapper.getWorld(), drawCam.combined);
        }
        if(Assets.playMusic)
        {
            music.play();
        }
        else
        {
            music.pause();
        }
        worldWrapper.getWorld().step(Gdx.graphics.getDeltaTime(), 6, 2);
    }

	private void drawSprites()
    {
        for (Sprite sprite : loader.getLevel().getSprites())
        {
            TextureRegion region = Assets.loadedRegions.get(sprite.getTextureName());
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Utility.draw(batch, region, sprite.getPosition().x, sprite.getPosition().y, sprite.getBounds().height);
        }
    }

    private void drawEnemies(float deltaTime)
    {
        for (Enemy enemy : loader.getLevel().getEnemies())
        {
            enemy.render(batch, deltaTime);
        }
    }

    @Override
    public void resize(int width, int height)
    {
    }

    @Override
    public void hide()
    {

    }

    @Override
    public void pause()
    {
    }

    @Override
    public void resume()
    {
    }

    @Override
    public void dispose()
    {
        Gdx.input.setInputProcessor(null);
        Assets.dispose();
        bgColor.dispose();
        batch.dispose();
        bgr1.dispose();
    }

    @Override
    public void loadAssets()
    {
        Array<String[]> data = loader.parseLevelData(Gdx.files.absolute(Assets.mountedObbPath + "/levels/main_menu.data").readString());

        for(String[] s : data)
        {
            if(LevelLoader.isTexture(s[0]))
            {
                Assets.manager.load(s[1], Texture.class, Assets.textureParameter);
            }
            else
            {
                Assets.manager.load(s[1], LevelLoader.getTextureClassForKey(s[0]));
            }
        }
        Assets.manager.load("/hud/controls.pack", TextureAtlas.class);
        Assets.manager.load("/maryo/small.pack", TextureAtlas.class);
        cloudsPEffect = new ParticleEffect();
        cloudsPEffect.load(Gdx.files.absolute(Assets.mountedObbPath + "/animation/particles/clouds_emitter.p"), Gdx.files.absolute(Assets.mountedObbPath + "/clouds/default-1/"));
        cloudsPEffect.setPosition(Constants.CAMERA_WIDTH / 2, Constants.CAMERA_HEIGHT);
        cloudsPEffect.start();
    }

    @Override
    public void afterLoadAssets()
    {
        loader.parseLevel(Gdx.files.absolute(Assets.mountedObbPath + "/levels/main_menu.smclvl").readString(), worldWrapper.getWorld());

        TextureAtlas controlsAtlas = Assets.manager.get("/hud/controls.pack");
        play = controlsAtlas.findRegion("play");
        playP = controlsAtlas.findRegion("play-pressed");
        playR = new Rectangle(screenWidth/2f - (screenWidth/10f) / 2,
                screenHeight/2f - (screenWidth/10f) / 2, screenWidth/10f, screenWidth/10f);

        musicOn = controlsAtlas.findRegion("music-on");
        musicOnP = controlsAtlas.findRegion("music-on-pressed");
        musicOff = controlsAtlas.findRegion("music-off");
        musicOffP = controlsAtlas.findRegion("music-off-pressed");
        musicR = new Rectangle(screenWidth - (screenWidth/18f) * 1.25f,
                (screenWidth/18f)/4, screenWidth/18f, screenWidth/18f);

        soundOn = controlsAtlas.findRegion("sound-on");
        soundOnP = controlsAtlas.findRegion("sound-on-pressed");
        soundOff = controlsAtlas.findRegion("sound-off");
        soundOffP = controlsAtlas.findRegion("sound-off-pressed");
        soundR = new Rectangle(screenWidth - (screenWidth/18f) * 2.5f,
                (screenWidth/18f)/4, screenWidth/18f, screenWidth/18f);

        Texture bgTexture = Assets.manager.get("/game/background/more-hills.png");
        bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bgr1 = new Background(new Vector2(0, 0), bgTexture);
        bgr1.width = 8.7f;
        bgr1.height = 4.5f;
        bgr2 = new Background(bgr1);
        bgr2.position = new Vector2(bgr1.width, 0);

        bgColor = new BackgroundColor();
        bgColor.color1 = new Color(.117f, 0.705f, .05f, 0f);//color is 0-1 range where 1 = 255
        bgColor.color2 = new Color(0f, 0.392f, 0.039f, 0f);

        gameLogo = Assets.manager.get("/game/logo/smc-big-1.png");
        gameLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        gdxLogo = Assets.manager.get("/game/logo/libgdx.png");
        gdxLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Array<Maryo.MarioState> states = new Array<Maryo.MarioState>();
        states.add(Maryo.MarioState.small);
        Maryo maryo = new Maryo(loader.getLevel().getSpanPosition(), new Vector2(0.85f, 0.85f), worldWrapper.getWorld(), states);
        maryo.setFacingLeft(false);
        maryo.loadTextures();
        worldWrapper.setMario(maryo);
        worldWrapper.setLevel(loader.getLevel());

        audioOn = Assets.manager.get("/sounds/audio_on.ogg", Sound.class);

    }

    @Override
    public boolean keyDown(int keycode)
    {
        if(keycode == Input.Keys.ENTER)
        {
            game.setScreen(new LoadingScreen(new GameScreen(game)));
        }
        else if(keycode == Input.Keys.D)
        {
            debug = !debug;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyTyped(char character)
    {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        float x = screenX;//screenX / (screenWidth / Constants.CAMERA_WIDTH);
        float y = screenHeight - screenY;

        if(playR.contains(x, y))
        {
            playT = true;
        }
        if(musicR.contains(x, y))
        {
            musicT = true;
        }
        if(soundR.contains(x, y))
        {
            soundT = true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        float x = screenX;// / (screenWidth / Constants.CAMERA_WIDTH);
        float y = screenHeight - screenY;

        if(playR.contains(x, y))
        {
            playT = false;
            music.stop();
            game.setScreen(new LoadingScreen(new GameScreen(game)));
        }
        if(musicR.contains(x, y))
        {
            musicT = false;
            Utility.toggleMusic();
        }
        if(soundR.contains(x, y))
        {
            soundT = false;
            if(Utility.toggleSound())
            {
                audioOn.play();
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        float x = screenX;// / (screenWidth / Constants.CAMERA_WIDTH);
        float y = screenHeight - screenY;

        playT = playR.contains(x, y);
        musicT = musicR.contains(x, y);
        soundT = soundR.contains(x, y);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        return false;
    }

    @Override
    public boolean scrolled(int amount)
    {
        return false;
    }
}
