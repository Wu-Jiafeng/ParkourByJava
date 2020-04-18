
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;


public class HelloJME3 extends SimpleApplication {

	/**
	 * 初始化3D场景，显示一个方块。
	 */
	private Geometry geom[];
	private float length = 0;

	@Override
	public void simpleInitApp() {

		// 设置摄影机
		cam.setFrame(new Vector3f(0, 0, 2), new Vector3f(-1, 0, 0), new Vector3f(0, 0.2f, 0.8f),
				new Vector3f(0, 0.8f, -0.2f));
		// #1 创建一个地砖
		Mesh box = new Box(4, 1, 1);

		// #2 加载感光材质
		Material mat[] = new Material[3];
		mat[0] = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat[1] = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat[2] = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat[0].setColor("Color", ColorRGBA.Blue);
		mat[1].setColor("Color", ColorRGBA.Green);
		mat[2].setColor("Color", ColorRGBA.Red);
		// #3 创建一堆几何体，排成一列，应用刚才和网格和材质。
		geom = new Geometry[42];
		for (int i = 0; i < 42; i++) {
			geom[i] = new Geometry();
			geom[i].setMesh(box);
			geom[i].setMaterial(mat[i % 3]);
			geom[i].scale(0.5f);
			rootNode.attachChild(geom[i]);
			geom[i].move(0, i, 0);

		}
		// #4 创建一束定向光，并让它斜向下照射，好使我们能够看清那个方块。
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-1, -2, -3));
		rootNode.addLight(sun);
	}

	public void simpleUpdate(float deltaTime) {
		float speed = 5;
		length = length + deltaTime * speed;
		// 让地砖们动起来
		for (int i = 0; i < 42; i++) {
			geom[i].move(0, -1 * deltaTime * speed, 0);
			if (geom[i].getLocalTranslation().y < -1) {
				geom[i].move(0, 42, 0);
			}
		}

	}

	public static void main(String[] args) {
		//配置参数
		AppSettings settings = new AppSettings(true);
		settings.setTitle("跑道");// 标题
		settings.setResolution(480, 720);// 分辨率

		// 启动jME3程序
		HelloJME3 app = new HelloJME3();
		app.setSettings(settings);// 应用参数
		// app.setShowSettings(false);
		app.start();
	}

}