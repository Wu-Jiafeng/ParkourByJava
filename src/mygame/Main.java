package mygame;

import java.text.DecimalFormat;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

/**
 * 主程序
 * 
 * @author LiuHanrong, WuJiafeng
 *
 */
public class Main extends SimpleApplication {
	//返回主菜单按钮
	class ReturnCommand implements Command<Button>{
		@Override
        public void execute(Button source) { 
            if(isSetting) {
            	time_cost=0;score=0;life=3;
            	start=false;
            	isSetting=false;
            	InputApp.setEnabled(false);
                CharacterApp.setEnabled(false);
                CubeApp.setEnabled(false);
            }
            else if(isMiniGameSetting) {
            	isMiniGameSetting=false;
            	MiniGameApp.setEnabled(false);
            }
            guiNode.detachAllChildren();
            guiNode.attachChild(MainMenu);
        }
	}
	//退出游戏按钮
	class ExitCommand implements Command<Button>{
		@Override
        public void execute(Button source) { fail=true; }
	}
	
	
	
    //应用长宽/摄像机
    private float Width;
    private float Height;
    public static Camera cam;
    //游戏状态变量
    public static boolean fail=false; //游戏退出标志
    public static boolean start=false; //游戏中标志
    public static boolean isSetting=false; //弹出设置标志
    public static boolean isMiniGameSetting=false; //弹出小游戏设置标志
    public static int score=0; //得分
    public static int life=3; //生命
    public static float time_cost=0;//耗时
    //GUI节点
    private final Node MainMenu=new Node("MainMenu");
    private final Node ScoreBoard=new Node("ScoreBoard");
    private final Node Setting=new Node("Setting");
    private final Node MiniSetting=new Node("MiniSetting");
    private final Node FailureMenu=new Node("FailureMenu");
    private final Node VictoryMenu=new Node("VictoryMenu");
    //APPSTATES
    private final SceneAppState SceneApp=new SceneAppState();
    private final BulletAppState BulletApp=new BulletAppState();
    private final CharacterAppState CharacterApp=new CharacterAppState();
    private final InputAppState InputApp=new InputAppState();
    private final CubeAppState CubeApp=new CubeAppState();
    private final MiniGameAppState MiniGameApp=new MiniGameAppState();
    
    //初始化失败菜单
    private void initFailureMenu(){
        // 创建一个Container作为窗口中其他GUI元素的容器
    	Container myWindow = new Container();
        FailureMenu.attachChild(myWindow);
        // 设置窗口在屏幕上的坐标
        // 注意：Lemur的GUI元素是以控件左上角为原点，向右、向下生成的。
        // 然而，作为一个Spatial，它在GuiNode中的坐标原点依然是屏幕的左下角。
        
        myWindow.setLocalTranslation(17*Width/48, 3*Height/4, 0);
        myWindow.scale(8f);
        // 标题
        myWindow.addChild(new Label("Game Over!!!"));		
        // 返回主菜单
        Button Return=myWindow.addChild(new Button("Main Menu"));
        Return.addClickCommands(new ReturnCommand());
        // 退出游戏
        Button exit = myWindow.addChild(new Button("Exit"));
        exit.addClickCommands(new ExitCommand());
    }
    
    //初始化成功菜单
    private void initVictoryMenu(){
        // 创建一个Container作为窗口中其他GUI元素的容器
    	Container myWindow = new Container();
        VictoryMenu.attachChild(myWindow);
        
        myWindow.setLocalTranslation(17*Width/48, 3*Height/4, 0);
        myWindow.scale(8f);
        // 标题
        myWindow.addChild(new Label("Victory!!!"));		
        // 返回主菜单
        Button Return=myWindow.addChild(new Button("Main Menu"));
        Return.addClickCommands(new ReturnCommand());
        // 退出游戏
        Button exit = myWindow.addChild(new Button("Exit"));
        exit.addClickCommands(new ExitCommand());
    }
    
    //初始化计分板
    private void initScoreBoard(){
        // 创建一个Container作为窗口中其他GUI元素的容器
    	DecimalFormat df =new DecimalFormat("#0.00");
    	Container myWindow = new Container();
        ScoreBoard.attachChild(myWindow);
        
        myWindow.setLocalTranslation(0, Height, 0);
        myWindow.scale(4f);
        // 生命-得分-用时
        myWindow.addChild(new Label("Life: "+ String.valueOf(life)));	
        myWindow.addChild(new Label("Score: "+ String.valueOf(score)));	
        myWindow.addChild(new Label("Time: "+df.format(time_cost)+"s"));	
    }
    
    //初始化设置菜单
	@SuppressWarnings("unchecked")
	private void initSetting(){
        // 创建一个Container作为窗口中其他GUI元素的容器
		Container myWindow = new Container();
        Setting.attachChild(myWindow);
        
        myWindow.setLocalTranslation(17*Width/48, 5*Height/6, 0);
        myWindow.scale(8f);
        // 标题
        myWindow.addChild(new Label("Setting"));		
        // 添加一个Button控件
        Button Continue = myWindow.addChild(new Button("Continue"));
        Continue.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                start=true;
                isSetting=false;
                Setting.removeFromParent();
                InputApp.onAction("start", true, 0);
            }
        });
        Button Return=myWindow.addChild(new Button("Main Menu"));
        Return.addClickCommands(new ReturnCommand());
        Button exit = myWindow.addChild(new Button("Exit"));
        exit.addClickCommands(new ExitCommand());
    }
	//初始化小游戏设置菜单
	@SuppressWarnings("unchecked")
	private void initMiniSetting(){
		// 创建一个Container作为窗口中其他GUI元素的容器
		Container myWindow = new Container();
		MiniSetting.attachChild(myWindow);
		
		myWindow.setLocalTranslation(17*Width/48, 5*Height/6, 0);
		myWindow.scale(8f);
		// 标题
		myWindow.addChild(new Label("Setting"));
		// 添加一个Button控件
		Button Continue = myWindow.addChild(new Button("Continue"));
		Continue.addClickCommands(new Command<Button>() {
			@Override
			public void execute(Button source) {
				isMiniGameSetting=false;
				MiniSetting.removeFromParent();
				InputApp.onAction("Start", true, 0);
			}
		});
		Button Return=myWindow.addChild(new Button("Main Menu"));
		Return.addClickCommands(new ReturnCommand());
		Button exit = myWindow.addChild(new Button("Exit"));
		exit.addClickCommands(new ExitCommand());
	}
    //初始化主菜单
    @SuppressWarnings("unchecked")
	private void initMainMenu(){
        // 创建一个Container作为窗口中其他GUI元素的容器
    	Container myWindow = new Container();
    	MainMenu.attachChild(myWindow);
    	guiNode.attachChild(MainMenu);
        
    	myWindow.setLocalTranslation(17*Width/48, 4*Height/5, 0);
        myWindow.scale(8f);
        // 标题
        myWindow.addChild(new Label("Parkour3D"));		
        // play按键
        Button play = myWindow.addChild(new Button("Play"));
        play.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                time_cost=0;score=0;life=3;
                InputApp.setEnabled(true);
                CharacterApp.setEnabled(true);
                CubeApp.setEnabled(true);
                MainMenu.removeFromParent();
                ScoreBoard.detachAllChildren();
                initScoreBoard();
                guiNode.attachChild(ScoreBoard);
            }
        });
        //miniGame按键
        Button miniGame=myWindow.addChild(new Button("miniGame"));
        miniGame.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                time_cost=0;score=0;life=3;
                MiniGameApp.setEnabled(true);
                MainMenu.removeFromParent();
            }
        });
        // exit按键
        Button exit = myWindow.addChild(new Button("Exit"));
        exit.addClickCommands(new ExitCommand());
    }
    
    
    public Main() {
        super(new StatsAppState());
    }

    @Override
    public void simpleInitApp() {
        //ESC不再为退出游戏而为调出设置菜单
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        cam=this.getCamera();
        cam.setLocation(new Vector3f(0,70f, 200f));
        Width=cam.getWidth();
        Height=cam.getHeight();
        // 初始化Lemur GUI
        GuiGlobals.initialize(this);
		// 加载 'glass' 样式
		BaseStyles.loadGlassStyle();
		// 将'glass'设置为GUI默认样式
		GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
		//初始化各菜单
        initMainMenu();
        initSetting();
        initMiniSetting();
        initScoreBoard();
        initVictoryMenu();
        initFailureMenu();
        //绑定各state
        stateManager.attachAll(SceneApp,BulletApp,CharacterApp,InputApp,CubeApp,MiniGameApp);
        CharacterApp.setEnabled(false);
        InputApp.setEnabled(false);
        CubeApp.setEnabled(false);
        MiniGameApp.setEnabled(false);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
    	if(start&&(!isSetting)){
            //更新计分板
    		time_cost+=tpf;
            ScoreBoard.detachAllChildren();
            initScoreBoard();
            //得分超过100或坚持5分钟获胜
            if(score>=100||time_cost>=300){
            	InputApp.forward=false;
                start=false;
                guiNode.detachAllChildren();
                guiNode.attachChild(VictoryMenu);
            }
            // 如果生命为0结束游戏
            if(Main.life==0){
            	InputApp.forward=false;
                start=false;
                guiNode.detachAllChildren();
                guiNode.attachChild(FailureMenu);
            }
        }
    	else if((start&&isSetting)){ guiNode.attachChild(Setting); }
    	else if(isMiniGameSetting){ 
    		guiNode.attachChild(MiniSetting); }
        if(fail){ this.stop(); }
    }

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Parkour3D");// 标题
        settings.setResolution(1920, 1080);
        settings.setFullscreen(true);
        Main app = new Main();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }
}