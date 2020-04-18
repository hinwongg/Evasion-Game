package com.tutorial.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class FastEnemy extends GameObject {

	private Handler handler;
	
	private BufferedImage enemy_image;
	
	public FastEnemy(int x, int y, ID id, Handler handler) {
		super(x, y, id);
		
		this.handler = handler;
		
		velX = 2;
		velY = 9;
		
		/*
		//RealGML's optional graphics section. Can take out
		SpriteSheet ss = new SpriteSheet(Game.sprite_sheet);
		
		enemy_image = ss.grabImage(1, 3, 16, 16);
		//RealGML's optional graphics section. Can take out
		 */
		 
	}

	public Rectangle getBounds() {
		return new Rectangle((int) x, (int) y, 16, 16);
	}
	
	public void tick() {
		x = x + velX; 
		y = y + velY;
		
		if (y <= 0 || y >= Game.HEIGHT - 32) velY = velY * -1;
		if (x <= 0 || x >= Game.WIDTH - 32) velX = velX * -1;
			
		handler.addObject(new Trail(x, y, ID.Trail, Color.cyan, 16, 16, 0.02f, handler));
	}

	public void render(Graphics g) {
		
		g.setColor(Color.CYAN);
		g.fillRect((int) x , (int) y, 16, 16);
		
		//RealGML's optional graphics section. Can take out
		//g.drawImage(enemy_image, (int)x, (int)y, null);
	}
	
	

}
