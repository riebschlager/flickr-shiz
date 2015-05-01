import processing.core.*;
import java.util.ArrayList;

public class FlickrShiz extends PApplet {

	private static final long serialVersionUID = 1L;
	PGraphics canvas;
	int segmentWidth = 100;
	int segmentHeight = 100;
	int currentGridpoint = 0;
	float inc = 0;
	ArrayList<PVector> gridPoints = new ArrayList<PVector>();
	PImage src;

	public void setup() {
		size(1000, 1000);
		background(0);
		src = loadImage("http://img.ffffound.com/static-data/assets/6/0685f0a621f18aca1fcba2a95974f8997745d981_m.jpg");

		canvas = createGraphics(this.width, this.height);
		canvas.beginDraw();
		canvas.background(0);
		canvas.endDraw();

		for (int ix = 0; ix < width + this.segmentWidth; ix += segmentWidth) {
			for (int iy = 0; iy < height; iy += segmentHeight) {
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
		Tile tile = new Tile(segmentWidth, segmentHeight);
		canvas.beginDraw();
		canvas.pushMatrix();
		canvas.translate(_x, _y);
		canvas.image(tile.canvas, 0, 0, this.segmentWidth, this.segmentHeight);
		canvas.popMatrix();
		canvas.endDraw();
	}

	class Tile {

		int width;
		int height;
		PImage[] slices = new PImage[8];
		PGraphics canvas;

		Tile(int _width, int _height) {

			this.width = _width;
			this.height = _height;
			this.canvas = createGraphics(segmentWidth, segmentHeight);
			this.canvas.beginDraw();
			this.canvas.endDraw();

			PVector[] p = new PVector[9];
			p[0] = new PVector(0, 0); // NW
			p[1] = new PVector(this.width / 2, 0); // N
			p[2] = new PVector(this.width, 0); // NE
			p[3] = new PVector(0, this.height / 2); // W
			p[4] = new PVector(this.width / 2, this.height / 2); // C
			p[5] = new PVector(this.width, this.height / 2); // E
			p[6] = new PVector(0, this.height); // SW
			p[7] = new PVector(this.width / 2, this.height); // S
			p[8] = new PVector(this.width, this.height); // SE

			slices[0] = renderSlice(p[0].x, p[0].y, p[1].x, p[1].y, p[4].x, p[4].y, 1); // nw n c
			slices[1] = renderSlice(p[0].x, p[0].y, p[3].x, p[3].y, p[4].x, p[4].y, 0); // nw w c
			slices[2] = renderSlice(p[2].x, p[2].y, p[1].x, p[1].y, p[4].x, p[4].y, 1); // ne n c
			slices[3] = renderSlice(p[2].x, p[2].y, p[5].x, p[5].y, p[4].x, p[4].y, 0); // ne e c
			slices[4] = renderSlice(p[6].x, p[6].y, p[7].x, p[7].y, p[4].x, p[4].y, 1); // sw s c
			slices[5] = renderSlice(p[6].x, p[6].y, p[3].x, p[3].y, p[4].x, p[4].y, 0); // sw w c
			slices[6] = renderSlice(p[8].x, p[8].y, p[5].x, p[5].y, p[4].x, p[4].y, 0); // se e c
			slices[7] = renderSlice(p[8].x, p[8].y, p[7].x, p[7].y, p[4].x, p[4].y, 1); // se s c

			this.canvas.beginDraw();
			for (int i = 0; i < slices.length; i++) {
				this.canvas.image(slices[i], 0, 0);
			}
			this.canvas.endDraw();
		}

		PImage renderSlice(float x1, float y1, float x2, float y2, float x3, float y3, int ord) {
			inc = (inc > src.width || inc > src.height) ? 0 : inc + 0.3f;
			PGraphics tempCanvas = createGraphics(this.width, this.height);
			tempCanvas.beginDraw();
			PImage slice = src.get(0, floor(inc), src.width, 1);
			if (ord == 0) {
				tempCanvas.image(slice, 0, 0, this.width, this.height);
			} else {
				tempCanvas.translate(this.width, this.height);
				tempCanvas.rotate(HALF_PI);
				tempCanvas.image(slice, 0, 0, -this.width, this.height);
			}
			tempCanvas.endDraw();
			PGraphics mask = createGraphics(this.width, this.height);
			mask.beginDraw();
			mask.noStroke();
			mask.triangle(x1, y1, x2, y2, x3, y3);
			mask.endDraw();
			PImage tri = tempCanvas.get();
			tri.mask(mask);
			return tri;
		}

	}
}
