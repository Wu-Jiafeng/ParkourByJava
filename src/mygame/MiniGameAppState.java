package mygame;
import java.lang.Math;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.ui.Picture;

import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

public class MiniGameAppState extends BaseAppState implements ActionListener{
	//自定义节点类
	class myNode {
		Node node;
		Node nowNode;
		Node defualtNode;
		boolean isNormal;
		boolean isAddScore=false;
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
	
	class CubeMaker {
		// 正常方块
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

		//RGB方块
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
	
	class RoleMaker {
		// 玩家小球
		Role makeRole(int x) {
			Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
			mat.setColor("Diffuse", ColorRGBA.Red);
			mat.setColor("Ambient", ColorRGBA.Red);
			mat.setColor("Specular", ColorRGBA.White);
			mat.setFloat("Shininess", 32);
			Geometry geom = new Geometry("Player Ball", new Sphere(20, 40, 0.3f));
			geom.setMaterial(mat);
			Node n = new Node();
			n.attachChild(geom);
			n.move(0, 0, 1f);
			Role role = new Role(n);
			return role;

		}

	}
	
	class Role {
		Node roleNode;
		int Hstate;// -1 left//0 mid// 1 right
		int Mstate;// -1 slide//1 jump
		float keeptime;// 锟斤拷锟酵伙拷锟叫的筹拷锟斤拷时锟斤拷

		Role(Node n) {
			roleNode = n;
			Hstate = 0;
			Mstate = 0;
			float keeptime = 0;
		}
	}
	
	class CrashCheck {
		// 碰撞检测
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
	
	//全局变量
	private AssetManager assetManager;
	private AppStateManager stateManager;
	private InputManager inputManager;
	private Node rootNode;
	private Node miniGameNode = new Node("miniGame");
	private Camera cam;
	
	private CubeMaker Cmaker = new CubeMaker();
	private CrashCheck checker = new CrashCheck();
	private RoleMaker Rmaker = new RoleMaker();
	private Role role;
	
	private myNode nodes[];
	private Queue<Integer> opPicQueue=new ConcurrentLinkedQueue<Integer>(); //指示图片队列
	private Picture[] pics = {null, null, null, null, null, null}; 
	private float length = 0;
	private float speed = 10;
	private int interval = 20;
	private int count=0;
	private int win=10;
	private int cnt=0;
	private boolean running = false;
	private boolean isNextPic = true;
	
	private final static float CRASHDISTANCE = 0.8f;
	private final static float MOVELIMIT = 0.8f;
	private final static float MOVESPEED = 10;
	private final static float MOVEMENTTIME = 1.5f;
	private final static int NUMOFTYPES = 3;
	
	private final static String LEFT = "Left";
	private final static String RIGHT = "Right";
	private final static String SLIDE = "Slide";
	private final static String JUMP = "Jump";
	private final static String START = "Start";
	private final static String SWITCH = "Switch";
	private final static String SETTING = "Setting";
	
	private final static Trigger TRIGGER_KEY_LEFT = new KeyTrigger(KeyInput.KEY_A);
	private final static Trigger TRIGGER_KEY_RIGHT = new KeyTrigger(KeyInput.KEY_D);
	private final static Trigger TRIGGER_KEY_JUMP = new KeyTrigger(KeyInput.KEY_W);
	private final static Trigger TRIGGER_KEY_SLIDE = new KeyTrigger(KeyInput.KEY_S);
	private final static Trigger TRIGGER_KEY_START = new KeyTrigger(KeyInput.KEY_F1);
	private final static Trigger TRIGGER_KEY_SETTING = new KeyTrigger(KeyInput.KEY_ESCAPE);
	
	private Picture loadPicture(String name, String path) {
    	Picture pic = new Picture(name);
        pic.setImage(assetManager, path, true);
        pic.setWidth(300);
        pic.setHeight(300);
        
        return pic;
    }
	
	private void showPicture(int idx) {
    	pics[idx].setLocalTranslation(0, 0, cnt);
    	miniGameNode.attachChild(pics[idx]);
    	cnt++;
    }
	
	public void addInputs() {
        inputManager.addMapping(LEFT, TRIGGER_KEY_LEFT);
        inputManager.addMapping(RIGHT, TRIGGER_KEY_RIGHT);
        inputManager.addMapping(JUMP, TRIGGER_KEY_JUMP);
        inputManager.addMapping(SLIDE, TRIGGER_KEY_SLIDE);
        inputManager.addMapping(START, TRIGGER_KEY_START);
        inputManager.addMapping(SETTING, TRIGGER_KEY_SETTING);

        inputManager.addListener(this, LEFT, RIGHT, JUMP, SLIDE,START,SETTING);
    }

    public void removeInputs() {
        inputManager.deleteTrigger(LEFT, TRIGGER_KEY_LEFT);
        inputManager.deleteTrigger(RIGHT, TRIGGER_KEY_RIGHT);
        inputManager.deleteTrigger(JUMP, TRIGGER_KEY_JUMP);
        inputManager.deleteTrigger(SLIDE, TRIGGER_KEY_SLIDE);
        inputManager.deleteTrigger(START, TRIGGER_KEY_START);
        inputManager.deleteTrigger(SETTING, TRIGGER_KEY_SETTING);
        
        inputManager.removeListener(this);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
		if (LEFT.equals(name) && isPressed) {
			role.Hstate =-1;
		}
		else if (LEFT.equals(name) && !isPressed && role.Hstate == -1) {
			role.Hstate = 0;
		}
		else if (RIGHT.equals(name) && isPressed) {
			role.Hstate = 1;
		}
		else if (RIGHT.equals(name) && !isPressed && role.Hstate == 1) {
			role.Hstate = 0;
		}
		else if (JUMP.equals(name) && isPressed) {
			role.Mstate = 1;
			role.keeptime = MOVEMENTTIME;
		}
		else if (SLIDE.equals(name) && isPressed) {
			role.Mstate = -1;
			role.keeptime = MOVEMENTTIME;
		}
		else if (START.equals(name) && isPressed && running == false) {
			running = true;
		}
		//else if (SWITCH.equals(name) && isPressed) {
		//	switchToMiniGame(10, 5);
		//}
		else if (SETTING.equals(name) && isPressed) {
            running=false;
            Main.isMiniGameSetting=true;
        } 
	}
	
	@Override
    protected void initialize(Application app) {
		this.assetManager = app.getAssetManager();
		this.inputManager = app.getInputManager();
        this.stateManager = app.getStateManager();
        this.rootNode = ((SimpleApplication) getApplication()).getRootNode();
        this.cam = app.getCamera();
        
        //初始化指示图片
        pics[0] = loadPicture("left", "resource/left.png");
        pics[1] = loadPicture("right", "resource/right.png");
        pics[2] = loadPicture("up", "resource/up.png");
        pics[3] = loadPicture("down", "resource/down.png");
        pics[4] = loadPicture("win", "resource/win.png");
        pics[5] = loadPicture("lose", "resource/lose.png");
	}
	
	@Override
    public void update(float deltaTime) {
		length = length + deltaTime * speed;

		// 更新方块
		if (running)
			for (int i = 0; i < 50; i++) {
				nodes[i].move(0, -1 * deltaTime * speed, 0);

				// 将后面的方块移到前面
				if (nodes[i].node.getLocalTranslation().y < -2) {
					nodes[i].isAddScore=false;
					nodes[i].move(0, 50, 0);
					count++;
					if (count >= interval) {
						CubeMaker maker = new CubeMaker();
						count = count % interval;
						int type = (int) (Math.random() * 1999) % NUMOFTYPES;
						nodes[i].putSpecial(maker.makeSpcial(type), type);
						if(type==0) opPicQueue.offer(2);
						else if(type==1) opPicQueue.offer(0);
						else if(type==2) opPicQueue.offer(1);
					} 
					else if (!nodes[i].isNormal) nodes[i].putNormal();
				}

				// 碰撞判断,碰撞前显示操作图片
				if (nodes[i].node.getLocalTranslation().y < 8*CRASHDISTANCE 
						&& nodes[i].node.getLocalTranslation().y > -CRASHDISTANCE
						&& !nodes[i].isNormal && isNextPic && !opPicQueue.isEmpty()) {
					showPicture(opPicQueue.poll());
					isNextPic=false;
				}
				if (nodes[i].node.getLocalTranslation().y < CRASHDISTANCE 
						&& nodes[i].node.getLocalTranslation().y > -CRASHDISTANCE 
						&& !nodes[i].isNormal) {
					if (checker.check(nodes[i].type, role)) {
						showPicture(5);
						running = false;
					}
				}
				else if(nodes[i].node.getLocalTranslation().y <= -CRASHDISTANCE 
						&& !nodes[i].isNormal &&!nodes[i].isAddScore) {
					win--;
					nodes[i].isAddScore=true;
					isNextPic=true;
				} 
				if(win==0) {
					showPicture(4);
					running = false;
				}

			}

		// 角色向左移动
		if (role.Hstate == -1) {
			Vector3f v = role.roleNode.getLocalTranslation();
			if (v.x > -MOVELIMIT && v.x - MOVESPEED * deltaTime < -MOVELIMIT) {
				role.roleNode.setLocalTranslation(-MOVELIMIT, v.y, v.z);
			} else if (v.x > -MOVELIMIT) {
				role.roleNode.move(-MOVESPEED * deltaTime, 0, 0);
			}
		}
		//角色向右移动
		else if (role.Hstate == 1) {
			Vector3f v = role.roleNode.getLocalTranslation();
			if (v.x < MOVELIMIT && v.x + MOVESPEED * deltaTime > MOVELIMIT) {
				role.roleNode.setLocalTranslation(MOVELIMIT, v.y, v.z);
			} else if (v.x < MOVELIMIT) {
				role.roleNode.move(MOVESPEED * deltaTime, 0, 0);
			}
		}
		// 角色水平方向不变
		else if (role.Hstate == 0) {
			Vector3f v = role.roleNode.getLocalTranslation();
			float delta = -v.x / Math.abs(v.x);
			if (v.x != 0 && (v.x + MOVESPEED * delta * deltaTime) * delta > 0) {
				role.roleNode.setLocalTranslation(0, v.y, v.z);
			} else if (v.x != 0) {
				role.roleNode.move(MOVESPEED * deltaTime * delta, 0, 0);
			}
		}
		// 角色跳跃或下蹲
		if (role.Mstate != 0) {
			role.keeptime -= deltaTime;
			if (role.keeptime < 0) {
				role.keeptime = 0;
				role.Mstate = 0;
			}
		}
	}
	
	@Override
    protected void onEnable() {
		//设置键盘监控
		addInputs();
		//设置摄像机视角
		cam.setFrame(new Vector3f(0, -2f, 2), new Vector3f(-1, 0, 0), new Vector3f(0, 0.2f, 0.8f),
				new Vector3f(0, 0.8f, -0.4f));
		//初始化方块
		nodes = new myNode[50];
		opPicQueue=new ConcurrentLinkedQueue<Integer>();
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
		//初始化玩家小球
		role = Rmaker.makeRole(0);
		rootNode.attachChild(role.roleNode);
		rootNode.attachChild(miniGameNode);
		
	}
	
	@Override
	protected void onDisable() {
		removeInputs();
		for(int i=0;i<50;++i) nodes[i].node.removeFromParent();
		role.roleNode.removeFromParent();
		miniGameNode.detachAllChildren();
		miniGameNode.removeFromParent();
		opPicQueue.clear();
		running=false; isNextPic = true;
		length=0; interval=20; win=10; 
		count=0; cnt=0;
	}
	
	@Override
    protected void cleanup(Application app) {}
}
