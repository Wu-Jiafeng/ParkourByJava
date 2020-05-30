/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;

/**
 * 场景管理模块
 * @author Wujiafeng
 *
 */
public class SceneAppState extends BaseAppState {

    private Node rootNode;
    private AudioNode audio_nature; //环境音
    private Spatial sceneModel;
    private RigidBodyControl landscape;

    private AssetManager assetManager;
    
    private void initLight() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.298f, 0.2392f, 0.2745f, 1f));
        this.rootNode.addLight(ambient);

        DirectionalLight light = new DirectionalLight();
        light.setDirection((new Vector3f(0.097551f, -0.733139f, -0.673046f)).normalize());
        light.setColor(new ColorRGBA(1, 1, 1, 1));
        this.rootNode.addLight(light);
    }
        /**
     * 初始化天空
     */
    private void initSky() {
        Spatial sky = SkyFactory.createSky(this.assetManager, "Textures/SkySphereMap.jpg",
                SkyFactory.EnvMapType.SphereMap);
        this.rootNode.attachChild(sky);
    }
    
    private void initNatureSound(){
        //环境音
        audio_nature = new AudioNode(assetManager, 
                "Sounds/Ocean Waves.ogg", DataType.Stream);
        audio_nature.setLooping(true);  // activate continuous playing
        audio_nature.setPositional(true);   
        audio_nature.setVolume(3);
        rootNode.attachChild(audio_nature);
        audio_nature.play(); // play continuously!
    }
    
    /**
     * 初始化水面
     */
    private void initWater() {
        FilterPostProcessor fpp = new FilterPostProcessor(this.assetManager);
        ((SimpleApplication) getApplication()).getViewPort().addProcessor(fpp);

        // 水
        WaterFilter waterFilter = new WaterFilter();
        waterFilter.setWaterHeight(50f);// 水面高度
        waterFilter.setWaterTransparency(0.2f);// 透明度
        waterFilter.setWaterColor(new ColorRGBA(0.4314f, 0.9373f, 0.8431f, 1f));// 水面颜色

        fpp.addFilter(waterFilter);
    }

    /**
     * 初始化地形
     */
    
    
    private void initTerrain() {

        // 加载地形的高度图
        Texture heightMapImage = this.assetManager.loadTexture("Scenes/default.png");
        //Texture heightMapImage = this.assetManager.loadTexture("Models/test/Textures/Terrain/splat/mountains512.png");

        // 根据图像内容，生成高度图
        ImageBasedHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
        heightmap.load();

        // 高斯平滑
        GaussianBlur gaussianBlur = new GaussianBlur();

        float[] heightData = heightmap.getHeightMap();
        int width = heightMapImage.getImage().getWidth();
        int height = heightMapImage.getImage().getHeight();

        heightData = gaussianBlur.filter(heightData, width, height);

        /*
         * 根据高度图生成实际的地形。该地形被分解成边长65(64*64)的矩形区块，用于优化网格。高度图的边长为 257，分辨率 256*256。
         */
        TerrainQuad terrain = new TerrainQuad("terrain", 65, 257, heightmap.getHeightMap());

        // 层次细节
        TerrainLodControl control = new TerrainLodControl(terrain, ((SimpleApplication) getApplication()).getCamera());
        control.setLodCalculator(new DistanceLodCalculator(65, 2.7f));
        terrain.addControl(control);

        // 地形材质
        terrain.setMaterial(this.assetManager.loadMaterial("Scenes/default.j3m"));

        terrain.setLocalTranslation(0, -100, 0);
        //this.rootNode.attachChild(terrain);
        this.sceneModel=terrain;
    }
    
    
    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();
        this.rootNode = ((SimpleApplication) getApplication()).getRootNode();
        initLight();
        initSky();
        initWater();
        initNatureSound();
        initTerrain();
        

        // 为地图创建精确网格形状
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
        this.landscape = new RigidBodyControl(sceneShape,0);
        sceneModel.addControl(landscape);
    }

    @Override
    protected void cleanup(Application app) {
        this.initialize(app);
    }

    @Override
    protected void onEnable() {
        rootNode.attachChild(sceneModel);

        BulletAppState bullet = getStateManager().getState(BulletAppState.class);
        if (bullet != null) {
            bullet.getPhysicsSpace().add(landscape);

        }
    }

    @Override
    protected void onDisable() {
        sceneModel.removeFromParent();
        
        PhysicsSpace space = landscape.getPhysicsSpace();
        if (space != null) {
            space.remove(landscape);
        }
    }

}
