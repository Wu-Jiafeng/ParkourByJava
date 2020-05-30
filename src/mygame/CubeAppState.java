/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.Random;
import java.lang.reflect.Method;

/**
 *
 * @author Wujiafeng
 */
public class CubeAppState extends BaseAppState{
    /**
     * 方块构建类
     */
    private class Cube extends Node{
        //判断块类型
        private boolean isNormal;
        private int type;
        private Vector3f creDir;
        private float x,y,z; //方块宽高，长默认为8
        private int L=((new Random().nextDouble())<=0.5?1:-1);//判断小方块是放左还是放右
        //构造函数
        private Cube(int type,Vector3f creDir){
            this.type=type;
            this.isNormal=(type>=5);
            this.creDir=creDir;
            this.x=4;this.y=2;this.z=4;
            this.setName("Cube");
            //生成方块
            if(isZoomOut) {
            	this.x=(creDir.x==0?2:4);
            	this.z=(creDir.z==0?2:4);
            }
            if(new Random().nextDouble()>0.9 ) this.y=4;
            
            if(type>=0&&type<=3) { makeSpecial(type); }
            else if(type==4) makeEmpty(type);
            else if(type>=5) makeNormal(type);
            else System.out.println("Invalid Cube Type!");
            //以0.1的概率生成金币或以0.05d的概率生成忍者
            double isbonus=new Random().nextDouble();
            if(isbonus<=0.1) makeBonus();
            else if(isbonus>=0.95) makeTree();
            delNode.offer(this);
        }
        private Cube(){
            this.type=5;
            this.x=4;this.y=2;this.z=4;
            this.isNormal=true;
            this.creDir=CubeAppState.FORWARD;
            this.setName("Cube");
        }
        public int gettype(){ return type; }
        // 返回一个节点，对应一个基本块
        void makeNormal(int type) {
            Mesh box;
            box = new Box(x,y,z);
            Material mat_brick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat_brick.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
            Geometry geom = new Geometry();
            geom.setMesh(box);
            geom.setMaterial(mat_brick);
            geom.setLocalTranslation(0, 0, 0);
            this.attachChild(geom);
            this.initPhysics();
        }
        // 返回一个节点，对应一个特殊块
        void makeSpecial(int type) {
            Mesh box;
            Material mat[] = new Material[]{
                new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"),
                new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"),
                new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"),
                new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            };
            mat[0].setColor("Color", ColorRGBA.Blue);
            mat[1].setColor("Color", ColorRGBA.Green);
            mat[2].setColor("Color", ColorRGBA.Red);
            mat[3].setColor("Color", ColorRGBA.White);
            box = new Box(x,y,z);
            Geometry geom = new Geometry();
            geom.setMesh(box);
            geom.setMaterial(mat[type]);
            geom.setLocalTranslation(0, 0, 0);
            this.attachChild(geom);
            this.initPhysics();
        }
        void makeEmpty(int type){ latestLocation=latestLocation.add(creDir); }
        void makeBonus(){
            // 创造金币
            Node bonus=new Node("Bonus");
            Geometry geom = new Geometry();
            Mesh ball=new Sphere(8, 12, 0.5f);
            Material mat=new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Yellow);
            geom.setMesh(ball);
            geom.setMaterial(mat);
            geom.setLocalTranslation(0, 0, 0);
            bonus.attachChild(geom);
            node.attachChild(bonus);
            //设置金币的物理属性
            final BulletAppState bullet = stateManager.getState(BulletAppState.class);
            CollisionShape RigidBodyShape= new SphereCollisionShape(0.5f);
            RigidBodyControl RigidBodyBall=new myRigidBodyControl(RigidBodyShape,0);
            bonus.addControl(RigidBodyBall);
            Vector3f BallLocation=new Vector3f(
                    latestLocation.x+(new Random().nextInt(3))*((new Random().nextDouble())<=0.5?1:-1),
                    latestLocation.y+2*y+(new Random().nextInt(3)),
                    latestLocation.z+(new Random().nextInt(3))*((new Random().nextDouble())<=0.5?1:-1)
            );
            RigidBodyBall.setPhysicsLocation(BallLocation);
            bullet.getPhysicsSpace().add(RigidBodyBall);
            delNode.offer(bonus);
        }
        void makeTree() {
        	//创造树障
        	Node tickObject=new Node("Tree");
        	Spatial tree = assetManager.loadModel("Models/Tree/Tree.mesh.xml");
        	tree.scale(4f);
        	tree.setLocalTranslation(0, -0.5f, 0);
        	tickObject.attachChild(tree);
        	node.attachChild(tickObject);
        	//设置树障的物理属性
        	final BulletAppState bullet = stateManager.getState(BulletAppState.class);
        	RigidBodyControl character=new RigidBodyControl(0); //设置为刚体即可
        	tickObject.addControl(character);
        	//character.setGravity(new Vector3f(0, -9.8f, 0));
        	//设置树障位置
        	if(x==2) character.setPhysicsLocation(latestLocation.add(2*L, y, 0));
            else if(z==2) character.setPhysicsLocation(latestLocation.add(0, y, 2*L));
            else character.setPhysicsLocation(latestLocation.add(0,y,0));// 位置
        	bullet.getPhysicsSpace().add(character);
            delNode.offer(tickObject);	
        }
        
        
        //设置方块的物理性质
        void initPhysics(){
            final BulletAppState bullet = stateManager.getState(BulletAppState.class);
            if (bullet == null) return;
            CollisionShape rigidBodyShape = new BoxCollisionShape(new Vector3f(x,y,z));
            RigidBodyControl rigidBodyCube = new myRigidBodyControl(rigidBodyShape,0);
            this.addControl(rigidBodyCube);
            //更新方块位置
            latestLocation=latestLocation.add(creDir);
            if(x==2) rigidBodyCube.setPhysicsLocation(latestLocation.add(2*L, 0, 0));
            else if(z==2) rigidBodyCube.setPhysicsLocation(latestLocation.add(0, 0, 2*L));
            else rigidBodyCube.setPhysicsLocation(latestLocation);// 位置
            bullet.getPhysicsSpace().add(rigidBodyCube);
        }
    }
    
    /**
     * 方块物理性质类，实现监听碰撞
     */
    private class myRigidBodyControl extends RigidBodyControl implements PhysicsCollisionListener{
        final BulletAppState bullet = stateManager.getState(BulletAppState.class);
        final CharacterAppState character=stateManager.getState(CharacterAppState.class);
        private boolean flag=false;
        //构造函数
        public myRigidBodyControl() { bullet.getPhysicsSpace().add(this); }
        public myRigidBodyControl(CollisionShape rigidBodyShape,float mass){
            super(rigidBodyShape,mass);
            bullet.getPhysicsSpace().addCollisionListener(this);
        }
        @Override
        public void collision(PhysicsCollisionEvent event) {
            if(flag) {
            	flag=false;
                return;
            }
            String NodeA=event.getNodeA().getName(),
                   NodeB=event.getNodeB().getName();
            if ( (NodeA+NodeB).equals("CubeCharacter")|| (NodeB+NodeA).equals("CubeCharacter")) {
                //碰到方块，方块需回收消除
                Node block=NodeA.equals("Cube")?(Node)event.getNodeA():(Node)event.getNodeB();
                Class cube=block.getClass();
                flag=true;
                while(!colNode.contains(block)) {
                	colNode.offer(block);
                	try {
                        Method method = block.getClass().getMethod("gettype", new Class[] {});    
                        Object value = method.invoke(block, new Object[] {});
                        if((int)value==0){
                            character.isSPEEDUP=!character.isSPEEDUP;
                            if(character.isSPEEDUP) character.SPEEDUP=1.5f;
                            else character.SPEEDUP=1;
                        }
                        else if((int)value==1){
                            if(Main.life<3) Main.life++;
                        }
                        else if((int)value==2){
                            character.isSLOWDOWN=!character.isSLOWDOWN;
                            if(character.isSLOWDOWN) character.SLOWDOWN=0.75f;
                            else character.SLOWDOWN=1;
                        }
                        else if((int)value==3){
                            Main.life--;
                            audio_reduce.play();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            if(NodeA.equals("Bonus")|| NodeB.equals("Bonus")){
                //碰到金币,加分,金币消失
                Node node=new Node();
                if(NodeA.equals("Bonus")) node = (Node)event.getNodeA();
                else node = (Node)event.getNodeB();
                while(!colNode.contains(node)) {
                	colNode.offer(node);
                	flag=true;
                    Main.score+=5;
                    audio_score.playInstance();
                }
            }
        }
    }

    /**
     * 全局对象
     */
    private Camera cam;
    private AssetManager assetManager;
    private InputManager inputManager;
    private AppStateManager stateManager;
    private Node rootNode;
    private Node node=new Node("CubeSet");
    private AudioNode audio_score; //得分音效
    private AudioNode audio_reduce; //失去生命音效
    private Queue<Node> delNode=new ConcurrentLinkedQueue<Node>(); //删除方块与金币队列
    private Queue<Node> colNode=new ConcurrentLinkedQueue<Node>(); //防止碰撞多次队列
    private float deltime=0f; //消除方块时间
    private float cretime=0f; //生成方块时间
    private int turnRound=25; // 25回合之内不能转向
    private int isTurnRound=0; //是否转向 0直行 1左拐 2右拐
    private int zoomOut=10; //十回合内不能改变方块大小
    private boolean isZoomOut=false;
    private Vector3f latestLocation =new Vector3f(0,50.5f, 142f); //标记最新生成方块的位置
    private static Vector3f LEFT=new Vector3f(8,0,0); //方块生成左转向量
    private static Vector3f FORWARD=new Vector3f(0,0,8); //方块生成直行向量
    
    /**
     *  音效初始化
     */
    protected void initSound(){
        final Node audio=new Node("audio");
        //金币得分音效
        audio_score = new AudioNode(assetManager, 
            "Sounds/Beep.ogg", DataType.Buffer);
        audio_score.setPositional(false);
        audio_score.setLooping(false);
        audio_score.setVolume(2);
        audio.attachChild(audio_score);
        //踩到白块音效
        audio_reduce = new AudioNode(assetManager, 
            "Sounds/hurt.wav", DataType.Stream);
        audio_reduce.setPositional(false);
        audio_reduce.setLooping(false);
        audio_reduce.setVolume(2);
        audio.attachChild(audio_reduce);
        rootNode.attachChild(audio);
    }
    
    /**
     *  游戏开始先生成一些正常方块
     */
    protected void initCube(){
        for(int i=0;i<25;++i){
            Cube cube=new Cube(5,FORWARD);
            cube.setLocalTranslation(0, 0, 0);
            node.attachChild(cube);
        }
        node.setLocalTranslation(0,0,0);
        rootNode.attachChild(node);
    }
    
    @Override
    protected void initialize(Application app) {
        this.cam = app.getCamera();
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.stateManager = app.getStateManager();
        this.rootNode = ((SimpleApplication) getApplication()).getRootNode();
        initSound();
    }
    
    @Override
    public void update(final float tpf) {
        if(!Main.start||(Main.start&&Main.isSetting)) {
        	delNode.clear();
        	return;
        }
        cretime+=tpf;
        if(cretime>0.4f){
            cretime=0f;
            Cube cube;
            //生成新方块
            cube=new Cube(new Random().nextInt(30),FORWARD);
            cube.setLocalTranslation(0,0,0);
            node.attachChild(cube);
            node.setLocalTranslation(0,0,0);
            rootNode.attachChild(node);
            //设置方块生成方向
            turnRound--;
            if(turnRound==0){
                turnRound=25;
                isTurnRound=new Random().nextInt(3);
                Vector3f LEFT_copy=LEFT.clone();
                switch(isTurnRound){
                    case(0):break;
                    case(1):LEFT=FORWARD.negateLocal(); FORWARD=LEFT_copy; break;
                    case(2):LEFT=FORWARD; FORWARD=LEFT_copy.negateLocal(); break;
                    default:break;
                }
            }
            //设置方块生成大小
            zoomOut--;
            if(zoomOut==0) {
            	zoomOut=10;
            	isZoomOut=((new Random().nextInt(4))==0);
            }
        }
        //将碰过的方块与金币消除
        deltime+=tpf;
        if(deltime>2f){
            deltime=0f;
            colNode.clear();
            while(delNode.size()>75){
            	PhysicsSpace space;
                Node node=delNode.poll();
                node.removeFromParent();
                RigidBodyControl rigidBodyCube=node.getControl(RigidBodyControl.class);
                if(rigidBodyCube!=null) space = rigidBodyCube.getPhysicsSpace();
                else continue;
                if(space!=null) space.remove(rigidBodyCube);
            }
        }
    }
    
    @Override
    protected void cleanup(Application app) {
    	//while(!delNode.isEmpty()){ delNode.poll(); }
    	delNode.clear();
    	colNode.clear();
        cretime=0f;deltime=0f;
        turnRound=10;isTurnRound=0;
        latestLocation =new Vector3f(0,50.5f, 142f);
        LEFT=new Vector3f(8,0,0);
        FORWARD=new Vector3f(0,0,8);
        for(Spatial spatial:node.getChildren()) {
        	PhysicsSpace space;
        	spatial.removeFromParent();
        	RigidBodyControl rigidBodyCube=spatial.getControl(RigidBodyControl.class);
        	if(rigidBodyCube!=null) space = rigidBodyCube.getPhysicsSpace();
            else continue;
            if(space!=null) space.remove(rigidBodyCube);
        }
        this.initCube();
    }

    @Override
    protected void onEnable() {
    	initCube();
    }

    @Override
    protected void onDisable() {
    	delNode.clear();
    	colNode.clear();
        cretime=0f;deltime=0f;
        turnRound=10;isTurnRound=0;
        latestLocation =new Vector3f(0,50.5f, 142f);
        LEFT=new Vector3f(8,0,0);
        FORWARD=new Vector3f(0,0,8);
        for(Spatial spatial:node.getChildren()) {
        	PhysicsSpace space;
        	spatial.removeFromParent();
        	RigidBodyControl rigidBodyCube=spatial.getControl(RigidBodyControl.class);
        	if(rigidBodyCube!=null) space = rigidBodyCube.getPhysicsSpace();
            else continue;
            if(space!=null) space.remove(rigidBodyCube);
        }
    }
    
}
