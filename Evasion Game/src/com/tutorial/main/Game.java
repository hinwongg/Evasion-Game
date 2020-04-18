package com.tutorial.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/*
 * Hin this is the MAIN CLASS of the java game (where shit runs)
 * 
 * 
 */
public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1550691097823471818L;

	public static final int WIDTH = 640, HEIGHT = WIDTH / 12 * 9;

	private Thread thread;
	private boolean running = false;

	public static boolean paused = false;
	public int diff = 0;
	
	public int highscore;
	
	// 0 = normal
	// 1 = hard
	
	private Random r;
	private Handler handler;
	private HUD hud;
	private Spawn spawner;
	private Menu menu;
	private Shop shop;
	
	public enum STATE {
		Menu,
		Select,
		Help,
		Shop,
		Game,
		End
	}

	public static STATE gameState = STATE.Menu;
	
	public static BufferedImage sprite_sheet;
	
	public Game() {
		
		/*
		//RealGML's optional graphics section. Can take out
		BufferedImageLoader loader = new BufferedImageLoader();
		
		try {
			sprite_sheet = loader.loadImage("/sprite_sheet.png");
			System.out.println("loaded");
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		//RealGML's optional graphics section. Can take out
		 */
		 
		handler = new Handler();
		hud = new HUD();
		shop = new Shop(handler, hud);
		menu = new Menu(this, handler, hud);
		this.addKeyListener(new KeyInput(handler, this));
		this.addMouseListener(menu);
		this.addMouseListener(shop);
		
		
		AudioPlayer.load(); 
		AudioPlayer.getMusic("music").loop();
		
		new Window(WIDTH, HEIGHT, "Let's Build a Game!", this);
		
		spawner = new Spawn(handler, hud, this);
		r = new Random();

		if (gameState == STATE.Game) {
			handler.addObject(new Player(WIDTH/2-32, HEIGHT/2-32, ID.Player, handler));
			handler.addObject(new BasicEnemy(r.nextInt(Game.WIDTH - 50), r.nextInt(Game.HEIGHT - 50), ID.BasicEnemy, handler));
		} else {
			for (int i = 0; i < 20; i ++) {
				handler.addObject(new MenuParticle(r.nextInt(WIDTH), r.nextInt(HEIGHT), ID.MenuParticle, handler));
			}
		}
	}

	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	public synchronized void stop() {
		try {
			thread.join();
			running = false;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		while (running) {
			long now = System.nanoTime();
			delta = delta + (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				tick();
				delta = delta - 1;
			}
			if (running)
				render();
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer = timer + 1000;
				//System.out.println("FPS: " + frames);
				frames = 0;
			}
		}
		stop();
	}

	private void tick() {
		
		if (gameState == STATE.Game) {
			
			if (!paused) {
				hud.tick();
				spawner.tick();
				handler.tick();

				if (HUD.HEALTH <= 0) { //Optional, add in the highScore from RealGML at Tut#14 at time 5:08
					gameState = STATE.End;
					try {
						loadScore();
					} catch (IOException e) {
						e.printStackTrace();
					}
					HUD.HEALTH = 100;
					handler.clearEnemys();
					for (int i = 0; i < 20; i++) {
						handler.addObject(new MenuParticle(r.nextInt(WIDTH), r.nextInt(HEIGHT), ID.MenuParticle, handler));
					}
					/*
					// Old version w/o highScore but had NO BUGS
					if (HUD.HEALTH <= 0) { 
						HUD.HEALTH = 100;
						gameState = STATE.End;
						handler.clearEnemys();
						for (int i = 0; i < 20; i++) {
							handler.addObject(new MenuParticle(r.nextInt(WIDTH), r.nextInt(HEIGHT), ID.MenuParticle, handler));
						}
					}
					*/
				}
			}

		} else if (gameState == STATE.Menu || gameState == STATE.End || gameState == STATE.Select) {
			menu.tick();
			handler.tick();
		}
		
	}

	//RealGML added in highscore without a vid in tut #14
	public void saveScore() throws IOException {
		FileWriter saveFile = new FileWriter("res/hs.txt");
		saveFile.write(Integer.toString(hud.getScore()));
		saveFile.close();
		
		this.highscore = hud.getScore();
	}
	
	public void loadScore() throws IOException {
		BufferedReader saveFile = new BufferedReader(new FileReader("res/hs.txt"));
		int highscore = saveFile.read();
		System.out.println("HIGHSCORE: " + highscore);
		System.out.println("Current Score: " + hud.getScore());
		saveFile.close();
		
		if (hud.getScore() > highscore) {
			saveScore();
			System.out.println("Saved...");
		} else {
			this.highscore = highscore;
			System.out.println("Not Saved");
		}
	}
	
	
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();

		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);


		if (paused) {
			g.setColor(Color.white);
			g.drawString("PAUSED", 100, 100);
		}
		
		if (gameState == STATE.Game) {
			hud.render(g);
			handler.render(g);
		}
		else if (gameState == STATE.Shop) {
			shop.render(g);
		}
		else if(gameState == STATE.Menu || gameState == STATE.Help || gameState == STATE.End || gameState == STATE.Select) {
			menu.render(g);
			handler.render(g);
		}
		
		g.dispose();
		bs.show();

	}

	public static float clamp(float var, float min, float max) {
		if (var >= max)
			return var = max;
		else if (var <= min)
			return var = min;
		
		else
			return var;
	}
	
	public static void main(String args[]) {
		new Game();
	}
}
