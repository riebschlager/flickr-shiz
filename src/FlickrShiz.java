import processing.core.*;
import processing.data.*;
import java.util.ArrayList;

public class FlickrShiz extends PApplet {

	private static final long serialVersionUID = 1L;
	PImage source;
	PGraphics canvas;
	int segmentWidth = 192;
	int segmentHeight = 192;
	int currentGridpoint = 0;
	ArrayList<PVector> gridPoints = new ArrayList<PVector>();

	public void setup() {
		this.size(1920, 1080);
		this.background(0);

		canvas = this.createGraphics(this.width, this.height);
		canvas.beginDraw();
		canvas.background(0);
		canvas.endDraw();

		this.source = loadImage("data/Wallpapers-room_com___Annas_Nebula_WS_by_casperium_1680x1050.jpg");
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
		} else {
			long unixTime = System.currentTimeMillis() / 1000L;
			canvas.save("output/" + unixTime + ".png");
			this.noLoop();
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
		canvas.translate(_x, _y);
		canvas.image(tile.canvas, 0, 0, this.segmentWidth, this.segmentHeight);
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
		PImage[] slices = new PImage[8];
		PGraphics canvas;

		Tile(int[] colors, int _width, int _height) {

			this.width = _width;
			this.height = _height;
			this.canvas = createGraphics(segmentWidth, segmentHeight);
			this.canvas.beginDraw();
			this.canvas.endDraw();

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
				String tineyeUrl = "http://img.tineye.com/flickr-images/?size=800&filepath=labs-flickr-public/images/" + filename;
				this.flickrUrls.add(flickrUrl);
				this.tineyeUrls.add(tineyeUrl);
			}

			PVector[] p = new PVector[9];
			p[0] = new PVector(0, 0);
			p[1] = new PVector(this.width / 2, 0);
			p[2] = new PVector(this.width, 0);
			p[3] = new PVector(0, this.height / 2);
			p[4] = new PVector(this.width / 2, this.height / 2);
			p[5] = new PVector(this.width, this.height / 2);
			p[6] = new PVector(0, this.height);
			p[7] = new PVector(this.width / 2, this.height);
			p[8] = new PVector(this.width, this.height);

			slices[0] = renderSlice(p[0].x, p[0].y, p[1].x, p[1].y, p[3].x, p[3].y);
			slices[1] = renderSlice(p[3].x, p[3].y, p[1].x, p[1].y, p[4].x, p[4].y);
			slices[2] = renderSlice(p[1].x, p[1].y, p[2].x, p[2].y, p[5].x, p[5].y);
			slices[3] = renderSlice(p[1].x, p[1].y, p[4].x, p[4].y, p[5].x, p[5].y);
			slices[4] = renderSlice(p[3].x, p[3].y, p[4].x, p[4].y, p[7].x, p[7].y);
			slices[5] = renderSlice(p[3].x, p[3].y, p[6].x, p[6].y, p[7].x, p[7].y);
			slices[6] = renderSlice(p[4].x, p[4].y, p[5].x, p[5].y, p[7].x, p[7].y);
			slices[7] = renderSlice(p[5].x, p[5].y, p[8].x, p[8].y, p[7].x, p[7].y);

			this.canvas.beginDraw();
			for (int i = 0; i < slices.length; i++) {
				this.canvas.image(slices[i], 0, 0);
			}
			this.canvas.endDraw();
		}

		PImage renderSlice(float x1, float y1, float x2, float y2, float x3, float y3) {
			int i = floor(random(10));
			PImage img = loadImage(this.tineyeUrls.get(i));
			img.resize((int) (img.width * 1.5), (int) (img.height * 1.5));
			img = img.get(0, 0, this.width, this.height);
			PGraphics mask = createGraphics(this.width, this.height);
			mask.beginDraw();
			mask.noStroke();
			mask.triangle(x1, y1, x2, y2, x3, y3);
			mask.endDraw();
			img.mask(mask);
			return img;
		}

	}
}
