/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

/**
 * 输入管理模块
 * 
 * @author LiuHanrong
 *
 */
public class InputAppState extends BaseAppState implements ActionListener {
    /**
     * 游戏中显示设置菜单
     */
    public final static String SETTING = "setting";
    public final static Trigger SETTING_TRIGGER = new KeyTrigger(KeyInput.KEY_ESCAPE);

    // 自动前进、左、右、跳跃、奔跑
    public final static String START = "start";
    public final static Trigger START_TRIGGER = new KeyTrigger(KeyInput.KEY_F1);

    public final static String BACKWARD = "backward";
    public final static Trigger BACKWARD_TRIGGER = new KeyTrigger(KeyInput.KEY_S);

    public final static String LEFT = "left";
    public final static Trigger LEFT_TRIGGER = new KeyTrigger(KeyInput.KEY_A);

    public final static String RIGHT = "right";
    public final static Trigger RIGHT_TRIGGER = new KeyTrigger(KeyInput.KEY_D);

    public final static String JUMP = "jump";
    public final static Trigger JUMP_TRIGGER = new KeyTrigger(KeyInput.KEY_SPACE);
    
    public final static String RUN="run";
    public final static Trigger RUN_TRIGGER = new KeyTrigger(KeyInput.KEY_W);

    private boolean left = false;
    private boolean right = false;
    protected boolean forward = false;
    private boolean backward = false;

    private InputManager inputManager;
    private AppStateManager stateManager;

    @Override
    protected void initialize(Application app) {
        this.inputManager = app.getInputManager();
        this.stateManager = app.getStateManager();
    }

    @Override
    protected void cleanup(Application app) { }

    @Override
    protected void onEnable() {
        inputManager.addMapping(SETTING, SETTING_TRIGGER);
        inputManager.addMapping(LEFT, LEFT_TRIGGER);
        inputManager.addMapping(RIGHT, RIGHT_TRIGGER);
        inputManager.addMapping(START, START_TRIGGER);
        //inputManager.addMapping(BACKWARD, BACKWARD_TRIGGER);
        inputManager.addMapping(JUMP, JUMP_TRIGGER);
        inputManager.addMapping(RUN, RUN_TRIGGER);

        inputManager.addListener(this, SETTING, LEFT, RIGHT, START, RUN, JUMP);
    }

    @Override
    protected void onDisable() {
    	forward=false;
        inputManager.removeListener(this);

        inputManager.deleteTrigger(SETTING, SETTING_TRIGGER);
        inputManager.deleteTrigger(LEFT, LEFT_TRIGGER);
        inputManager.deleteTrigger(RIGHT, RIGHT_TRIGGER);
        inputManager.deleteTrigger(START, START_TRIGGER);
        //inputManager.deleteTrigger(BACKWARD, BACKWARD_TRIGGER);
        inputManager.deleteTrigger(JUMP, JUMP_TRIGGER);
        inputManager.deleteTrigger(RUN, RUN_TRIGGER);
        
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (SETTING.equals(name) && isPressed) {
            Main.start=true;
            Main.isSetting=true;
            forward=false;
        } 
        else if (JUMP.equals(name) && isPressed) { jump(); } 
        else if (LEFT.equals(name)) { left = isPressed; } 
        else if (RIGHT.equals(name)) { right = isPressed; } 
        else if (START.equals(name)&&isPressed) { 
        	Main.start=true;
        	forward = isPressed; 
        }
        else if (RUN.equals(name)) { stateManager.getState(CharacterAppState.class).RunSpeed=(isPressed?1.2f:1f); } 
        /**
        *
        * else if (FORWARD.equals(name)) {
            forward = isPressed;
        } 
        else if (BACKWARD.equals(name)) {
            backward = isPressed;
        }
        */
        
        if ((left || right || forward || backward)&&(Main.start)&&(!Main.isSetting)) { walk(); } 
        else { idle(); }
    }

    private void walk() {
        TempVars tmpVar = TempVars.get();

        Vector3f frontDir = tmpVar.vect1.set(0, 0, 1f);
        Vector3f leftDir = tmpVar.vect2.set(0.0035f, 0, 0);
        Vector3f walkDir = tmpVar.vect3.set(0, 0, 0);

        if (forward) { walkDir.addLocal(frontDir); }
        if (backward) { walkDir.addLocal(frontDir.negateLocal()); }
        if (left) { walkDir.addLocal(leftDir); }
        if (right) { walkDir.addLocal(leftDir.negateLocal()); }

        walkDir.normalizeLocal();

        stateManager.getState(CharacterAppState.class).walk(walkDir);

        tmpVar.release();
    }

    private void idle() {
        stateManager.getState(CharacterAppState.class).idle();
    }

    private void jump() {
        TempVars tmpVar = TempVars.get();
        Vector3f walkDir = tmpVar.vect3.set(0, 0, 0);
        Vector3f jumpDir=tmpVar.vect4.set(0, 7.5f, 0);
        walkDir.addLocal(jumpDir);
        stateManager.getState(CharacterAppState.class).jump(walkDir);
        tmpVar.release();
    }
}
