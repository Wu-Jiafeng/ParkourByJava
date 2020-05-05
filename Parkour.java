
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

/**
 * 你的第一个jME3程序
 * 
 * @author \
 */
class myNode {
	Node node;
	Node nowNode;
	Node defualtNode;
	boolean isNormal;
	int type;

	void move(float x, float y, float z) {
		node.move(x, y, z);
	}

	void putSpecial(Node n, int Type) {
		nowNode = n;
		isNormal = false;
		node.detachAllChildren();
		node.attachChild(nowNode);
		type = Type;
	}

	void putNormal() {
		isNormal = true;
		node.detachAllChildren();
		node.attachChild(defualtNode);
	}
}

public class Parkour extends SimpleApplication {

	// 其他数值
	final static float CRASHDISTANCE = 0.8f;
	final static float MOVELIMIT = 0.8f;
	final static float MOVESPEED = 10;
	final static float MOVEMENTTIME = 1.5f;
	final static int NUMOFTYPES = 3;

	class CubeMaker {

		// 返回一个节点，对应一个基本块
		Node makeNormal(int type) {
			Mesh box;
			box = new Box(2, 0.5f, 0.5f);
			Material mat_brick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat_brick.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
			Geometry geom = new Geometry();
			geom.setMesh(box);
			geom.setMaterial(mat_brick);
			Node n = new Node();
			type = -1;
			n.attachChild(geom);
			return n;
		}

		// 返回一个节点，对应一个特殊块
		Node makeSpcial(int type) {
			Mesh box;
			Material mat[] = new Material[3];
			mat[0] = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat[1] = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat[2] = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat[0].setColor("Color", ColorRGBA.Blue);
			mat[1].setColor("Color", ColorRGBA.Green);
			mat[2].setColor("Color", ColorRGBA.Red);
			box = new Box(2, 0.5f, 0.5f);
			Geometry geom = new Geometry();
			geom.setMesh(box);
			geom.setMaterial(mat[type]);
			Node n = new Node();
			n.attachChild(geom);
			return n;
		}

	}

	CubeMaker Cmaker = new CubeMaker();

	class CrashCheck {
		// 碰撞检查，只使用块类型type和人物状态role
		// true crash//false not crash
		boolean check(int type, Role role) {
			if (type == 0) {
				if (role.Mstate == 1)
					return false;
				else
					return true;

			}
			if (type == 1) {
				if (role.Hstate == -1)
					return false;
				else
					return true;
			}
			if (type == 2) {
				if (role.Hstate == 1)
					return false;
				else
					return true;
			}
			return false;
		}
	}

	CrashCheck checker = new CrashCheck();

	class RoleMaker {
		// 返回一个包装过的人物节点，参考class Role
		Role makeRole(int x) {
			Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
			mat.setColor("Diffuse", ColorRGBA.Red);// 在漫射光照射下反射的颜色。
			mat.setColor("Ambient", ColorRGBA.Red);// 在环境光照射下，反射的颜色。
			mat.setColor("Specular", ColorRGBA.White);// 镜面反射时，高光的颜色
			mat.setFloat("Shininess", 32);
			Geometry geom = new Geometry("文艺小球", new Sphere(20, 40, 0.3f));
			geom.setMaterial(mat);
			Node n = new Node();
			n.attachChild(geom);
			n.move(0, 0, 1f);
			Role role = new Role(n);
			return role;

		}

	}

	RoleMaker Rmaker = new RoleMaker();

	class Role {
		Node roleNode;
		int Hstate;// -1 left//0 mid// 1 right
		int Mstate;// -1 slide//1 jump
		float keeptime;// 跳和滑行的持续时间

		Role(Node n) {
			roleNode = n;
			Hstate = 0;
			Mstate = 0;
			float keeptime = 0;
		}
	}

	/**
	 * 初始化3D场景，显示一个方块。
	 */
	private myNode nodes[];
	private float length = 0;
	float speed = 5;
	int interval = 20;
	int count;
	Role role;
	boolean running = false;

	public final static String LEFT = "Left";
	public final static String RIGHT = "Right";
	public final static String SLIDE = "Slide";
	public final static String JUMP = "Jump";
	public final static String START = "Start";

	// 水平事件监听器
	class MyActionListener1 implements ActionListener {
		public void onAction(String name, boolean isPressed, float tpf) {
			if (LEFT.equals(name) && isPressed) {
				role.Hstate = -1;
			}
			if (LEFT.equals(name) && !isPressed && role.Hstate == -1) {
				role.Hstate = 0;
			}
			if (RIGHT.equals(name) && isPressed) {
				role.Hstate = 1;
			}
			if (RIGHT.equals(name) && !isPressed && role.Hstate == 1) {
				role.Hstate = 0;
			}
			if (JUMP.equals(name) && isPressed) {
				role.Mstate = 1;
				role.keeptime = MOVEMENTTIME;
			}
			if (SLIDE.equals(name) && isPressed) {
				role.Mstate = -1;
				role.keeptime = MOVEMENTTIME;
			}
			if (START.equals(name) && isPressed && running == false) {
				running = true;
			}
		}
	}

	@Override
	public void simpleInitApp() {
		// 设置初始参数
		length = 0;
		speed = 10;
		interval = 20;
		count = 0;

		//////
		// 设置事件监听
		inputManager.addMapping(LEFT, new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping(RIGHT, new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping(JUMP, new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping(SLIDE, new KeyTrigger(KeyInput.KEY_S));
		// 绑定消息和监听器
		MyActionListener1 horizonListener = new MyActionListener1();
		inputManager.addListener(horizonListener, LEFT);
		inputManager.addListener(horizonListener, RIGHT);
		inputManager.addListener(horizonListener, JUMP);
		inputManager.addListener(horizonListener, SLIDE);
		// 设置摄影机
		flyCam.setEnabled(false);
		cam.setFrame(new Vector3f(0, -2f, 2), new Vector3f(-1, 0, 0), new Vector3f(0, 0.2f, 0.8f),
				new Vector3f(0, 0.8f, -0.4f));
		////// 按空格键开始
		inputManager.addMapping(START, new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(horizonListener, START);
		// 创建一堆节点，排成一列，应用刚才的材质和模型。
		nodes = new myNode[50];

		for (int i = 0; i < 50; i++) {
			nodes[i] = new myNode();
			nodes[i].defualtNode = Cmaker.makeNormal(0);
			nodes[i].isNormal = true;
			nodes[i].type = 0;
			nodes[i].node = new Node();
			nodes[i].node.attachChild(nodes[i].defualtNode);
			nodes[i].move(0, i * 1, 0);
			rootNode.attachChild(nodes[i].node);
		}
		// 创建一束定向光，并让它斜向下照射，好使我们能够看清那个方块。
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(1, 2, -3));
		rootNode.addLight(sun);

		// 载入人物模型
		role = Rmaker.makeRole(0);
		rootNode.attachChild(role.roleNode);

	}

	public void simpleUpdate(float deltaTime) {

		length = length + deltaTime * speed;

		// 让地砖们动起来
		if (running)
			for (int i = 0; i < 50; i++) {
				nodes[i].move(0, -1 * deltaTime * speed, 0);

				// 移动过去的结点补到最前面
				if (nodes[i].node.getLocalTranslation().y < -2) {
					nodes[i].move(0, 50, 0);
					count++;
					if (count >= interval) {
						CubeMaker maker = new CubeMaker();
						count = count % interval;
						int type = (int) (Math.random() * 1999) % NUMOFTYPES;
						nodes[i].putSpecial(maker.makeSpcial(type), type);
					} else if (!nodes[i].isNormal) {
						nodes[i].putNormal();
					}
				}

				// 检测障碍方块碰撞
				if (nodes[i].node.getLocalTranslation().y < CRASHDISTANCE
						&& nodes[i].node.getLocalTranslation().y > -CRASHDISTANCE && !nodes[i].isNormal) {
					if (checker.check(nodes[i].type, role))
						running = false;
				}

			}

		// 人物水平移动
		if (role.Hstate == -1) {
			Vector3f v = role.roleNode.getLocalTranslation();
			if (v.x > -MOVELIMIT && v.x - MOVESPEED * deltaTime < -MOVELIMIT) {
				role.roleNode.setLocalTranslation(-MOVELIMIT, v.y, v.z);
			} else if (v.x > -MOVELIMIT) {
				role.roleNode.move(-MOVESPEED * deltaTime, 0, 0);
			}
		}

		if (role.Hstate == 1) {
			Vector3f v = role.roleNode.getLocalTranslation();
			if (v.x < MOVELIMIT && v.x + MOVESPEED * deltaTime > MOVELIMIT) {
				role.roleNode.setLocalTranslation(MOVELIMIT, v.y, v.z);
			} else if (v.x < MOVELIMIT) {
				role.roleNode.move(MOVESPEED * deltaTime, 0, 0);
			}
		}

		if (role.Hstate == 0) {
			Vector3f v = role.roleNode.getLocalTranslation();
			float delta = -v.x / Math.abs(v.x);
			if (v.x != 0 && (v.x + MOVESPEED * delta * deltaTime) * delta > 0) {
				role.roleNode.setLocalTranslation(0, v.y, v.z);
			} else if (v.x != 0) {
				role.roleNode.move(MOVESPEED * deltaTime * delta, 0, 0);
			}
		}
		// 控制垂直动作
		if (role.Mstate != 0) {
			role.keeptime -= deltaTime;
			if (role.keeptime < 0) {
				role.keeptime = 0;
				role.Mstate = 0;
			}
		}
	}

	public static void main(String[] args) {
		// 配置参数
		AppSettings settings = new AppSettings(true);
		settings.setTitle("Parkour");// 标题
		settings.setResolution(1440, 840);// 分辨率
		// 启动jME3程序
		Parkour app = new Parkour();
		app.setSettings(settings);// 应用参数
		app.setShowSettings(false);
		app.start();
	}

}
