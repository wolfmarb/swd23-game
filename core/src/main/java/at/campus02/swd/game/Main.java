package at.campus02.swd.game;

import at.campus02.swd.game.gameobjects.*;
import at.campus02.swd.game.input.*;
import at.campus02.swd.game.observer.ConsoleObserver;
import at.campus02.swd.game.observer.UIObserver;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.Observable;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	private SpriteBatch batch;

	private ExtendViewport viewport = new ExtendViewport(480.0f, 480.0f, 480.0f, 480.0f);
	private GameInput gameInput = new GameInput();

	private Array<GameObject> gameObjects = new Array<>();

	private final float updatesPerSecond = 60;
	private final float logicFrameTime = 1 / updatesPerSecond;
	private float deltaAccumulator = 0;
	private BitmapFont font;
    private ConsoleObserver consoleObserver;
    private UIObserver uiObserver;

    private float elapsedTime = 0;

    private boolean isEnemySpeedIncreased = false;




    private AssetRepository repository = AssetRepository.getInstance();



	@Override
	public void create() {
        repository.preloadAssets();
		batch = new SpriteBatch();
        FactoryMethod factory = new FactoryMethod();
        int fieldSize = 6;
        for (int i = 1; i < fieldSize +1 ; i++) {
            for (int j = 1; j < fieldSize + 1; j++) {
                GameObject rt = null;
                if (j == 1) {
                    if (i == 1) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Rechts_Oben"));
                    } else if (i < 6) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Mitte_Oben"));
                    } else if (i == 6) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Links_Oben"));
                    }
                } else if (j < 6) {
                    if (i == 1) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Rechts_Mitte"));
                    } else if (i < 6) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Mitte"));
                    } else if (i == 6) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Links_Mitte"));
                    }
                } else if (j == 6) {
                    if (i == 1) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Rechts_Unten"));
                    } else if (i < 6) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Mitte_Unten"));
                    } else if (i == 6) {
                        rt = factory.createObject("tile", repository.getTexture("Sand_Links_Unten"));
                    }
                }
                rt.setPosition(((fieldSize / 2) - i) * 75, ((fieldSize / 2) - j) * 75); //TODO Hauptvariable für TileSize anlegen, falls es noch keine gibt, kein Hardcode wie bei mir
                gameObjects.add(rt);
            }
        }

        GameObject pb = factory.createObject("player", repository.getTexture("Sand_Links_Mitte"));
        pb.setPosition(0,0);
        gameObjects.add(pb);
        gameInput.setPlayer(pb);
		font = new BitmapFont();
		font.setColor(Color.WHITE);
        Gdx.input.setInputProcessor(this.gameInput);
        Command moveUpCommand = new MoveUpCommand();
        Command moveDownCommand = new MoveDownCommand();
        Command moveLeftCommand = new MoveLeftCommand();
        Command moveRightCommand = new MoveRightCommand();
        Command deleteCommand = new DeleteCommand(gameObjects, pb);
        gameInput.setMoveUpCommand(moveUpCommand);
        gameInput.setMoveDownCommand(moveDownCommand);
        gameInput.setMoveLeftCommand(moveLeftCommand);
        gameInput.setMoveRightCommand(moveRightCommand);
        gameInput.setDeleteCommand(deleteCommand);
        gameInput.setGameObjects(gameObjects);

        GameObject eb = factory.createObject("enemy", repository.getTexture("Sand_Links_Mitte"));
        eb.setPosition(0,0);
        gameObjects.add(eb);

        GameObject eb2 = factory.createObject("enemy2", repository.getTexture("Sand_Links_Mitte"));
        eb2.setPosition(0,0);
        gameObjects.add(eb2);

        //Observer:
        consoleObserver = new ConsoleObserver();
        ((Observable) pb).addObserver(consoleObserver);

        //UI Observer
        uiObserver = new UIObserver();
        ((Observable) pb).addObserver(uiObserver);

    }

    private void increaseEnemySpeed() {
        // Iterate over the gameObjects and find the enemy object
        for (GameObject gameObject : gameObjects) {
            if (gameObject instanceof EnemyBoy2) {
                EnemyBoy2 enemy2 = (EnemyBoy2) gameObject;
                enemy2.increaseSpeed(100);
            } else if (gameObject instanceof EnemyBoy) {
                EnemyBoy enemy = (EnemyBoy) gameObject;
                enemy.increaseSpeed(100);
            }
        }
    }

	private void act(float delta) {
		for(GameObject gameObject : gameObjects) {
			gameObject.act(delta);
		}
	}

	private void draw() {
		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		for(GameObject gameObject : gameObjects) {
			gameObject.draw(batch);
		}
		font.draw(batch, "Hello Game", -220, -220);

        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, "Hello");
        float textWidth = layout.width;
        float textX = -20;
        //float textY = viewport.getWorldHeight();
        float textY = -220;

        String ausgabePosition;

        if (uiObserver.getAusgabePosition() != null) {
            ausgabePosition = uiObserver.getAusgabePosition() + " " + uiObserver.getAusgabeAction();
        } else {
            ausgabePosition = "Waiting for Position..";
        }

        font.draw(batch, ausgabePosition, textX, textY);
		batch.end();


	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float delta = Gdx.graphics.getDeltaTime();
		deltaAccumulator += delta;
        elapsedTime += delta;
		while(deltaAccumulator > logicFrameTime) {
			deltaAccumulator -= logicFrameTime;
            gameInput.update(logicFrameTime);
			act(logicFrameTime);

            if (!isEnemySpeedIncreased && elapsedTime >= 10f) {
                increaseEnemySpeed();
                isEnemySpeedIncreased = true;
            }
		}
		draw();

	}

	@Override
	public void dispose() {
		batch.dispose();
        repository.dispose();
	}

	@Override
	public void resize(int width, int height){
		viewport.update(width,height);
	}
}
