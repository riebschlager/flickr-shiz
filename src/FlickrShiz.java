import processing.core.*;
import processing.data.*;
import java.util.ArrayList;

public class FlickrShiz extends PApplet {

	private static final long serialVersionUID = 1L;
	PImage source;
	PGraphics canvas;
	int segmentWidth = 120;
	int segmentHeight = 120;
	int currentGridpoint = 0;
	ArrayList<PVector> gridPoints = new ArrayList<PVector>();

	public void setup() {
		this.size(1920, 1080);
		this.background(0);

		canvas = this.createGraphics(this.width, this.height);
		canvas.beginDraw();
		canvas.background(0);
		canvas.endDraw();

		this.source = loadImage("data/FreeGreatPicture.com-19439-hd-color-background-wallpaper.jpg");
		this.source.resize(width, height);

		for (int ix = 0; ix < source.width + this.segmentWidth; ix += segmentWidth) {
			for (int iy = 0; iy < source.height; iy += segmentHeight) {
				gridPoints.add(new PVector(ix, iy));
			}
		}

	}

	public void draw() {
		if (currentGridpoint < gridPoints.size()) {
			PVector point = gridPoints.get(currentGridpoint);
			render((int) point.x, (int) point.y);
			currentGridpoint++;
		}
		image(canvas, 0, 0);
	}

	public void render(int _x, int _y) {
		int segmentX = _x;
		if (segmentX >= this.width) {
			segmentX = this.width - this.segmentWidth;
		}
		PImage segment = source.get(segmentX, _y, segmentWidth, segmentHeight);
		segment.loadPixels();
		int[] colors = new int[5];
		colors[0] = segment.pixels[PApplet.parseInt(random(0, segment.pixels.length))];
		colors[1] = segment.pixels[PApplet.parseInt(random(0, segment.pixels.length))];
		colors[2] = segment.pixels[PApplet.parseInt(random(0, segment.pixels.length))];
		colors[3] = segment.pixels[PApplet.parseInt(random(0, segment.pixels.length))];
		colors[4] = segment.pixels[PApplet.parseInt(random(0, segment.pixels.length))];
		Tile tile = new Tile(colors, this.segmentWidth, this.segmentHeight);
		canvas.beginDraw();
		canvas.pushMatrix();
		canvas.translate(_x - 60, _y);
		canvas.image(tile.slice1, 0, 0, 120, 120);
		canvas.image(tile.slice2, 60, 0, 120, 120);
		canvas.popMatrix();
		canvas.endDraw();
	}

	public void keyPressed() {
		if (key == 's') {
			long unixTime = System.currentTimeMillis() / 1000L;
			canvas.save("output/" + unixTime + ".png");
		}
	}

	class Tile {

		ArrayList<String> flickrUrls = new ArrayList<String>();
		ArrayList<String> tineyeUrls = new ArrayList<String>();
		int width;
		int height;
		PImage slice1;
		PImage slice2;

		Tile(int[] colors, int _width, int _height) {

			this.width = _width;
			this.height = _height;

			String url = "http://labs.tineye.com/multicolr/rest/color_search/?";
			String params = "return_metadata=<serverID%2f><photoID%2f><farmID%2f><imageHeight%2f><imageWidth%2f>";
			params += "&colors[0]=" + hex(colors[0], 6);
			params += "&colors[1]=" + hex(colors[1], 6);
			params += "&colors[2]=" + hex(colors[2], 6);
			params += "&colors[3]=" + hex(colors[3], 6);
			params += "&colors[4]=" + hex(colors[4], 6);
			params += "&weights[0]=" + 20;
			params += "&weights[1]=" + 20;
			params += "&weights[2]=" + 20;
			params += "&weights[3]=" + 20;
			params += "&weights[4]=" + 20;
			params += "&limit=" + 10;

			JSONObject tineye = loadJSONObject(url + params);
			JSONArray photos = tineye.getJSONArray("result");

			for (int i = 0; i < photos.size(); i++) {
				JSONObject photo = photos.getJSONObject(i);
				String filename = photo.getString("filepath");
				String farmID = photo.getJSONObject("metadata").getString("farmID");
				String serverID = photo.getJSONObject("metadata").getString("serverID");
				String photoID = photo.getJSONObject("metadata").getString("photoID");
				String secret = filename.split("_")[1];
				String imageExt = filename.split("\\.")[1];
				String flickrUrl = "https://farm" + farmID + ".staticflickr.com/" + serverID + "/" + photoID + "_" + secret + "." + imageExt;
				String tineyeUrl = "http://img.tineye.com/flickr-images/?size=300&filepath=labs-flickr-public/images/" + filename;
				this.flickrUrls.add(flickrUrl);
				this.tineyeUrls.add(tineyeUrl);
			}

			renderSlice1();
			renderSlice2();
		}

		void renderSlice1() {
			int i = floor(random(10));
			PImage img = loadImage(this.tineyeUrls.get(i)).get(0, 0, this.width, this.height);
			PGraphics mask = createGraphics(this.width, this.height);
			mask.beginDraw();
			mask.noStroke();
			mask.triangle(0, this.height, this.width / 2, 0, this.width, this.height);
			mask.endDraw();
			img.mask(mask);
			this.slice1 = img;
		}

		void renderSlice2() {
			int i = floor(random(10));
			PImage img = loadImage(this.tineyeUrls.get(i)).get(0, 0, this.width, this.height);
			PGraphics mask = createGraphics(this.width, this.height);
			mask.beginDraw();
			mask.noStroke();
			mask.triangle(0, 0, this.width / 2, this.height, this.width, 0);
			mask.endDraw();
			img.mask(mask);
			this.slice2 = img;
		}
	}
}
