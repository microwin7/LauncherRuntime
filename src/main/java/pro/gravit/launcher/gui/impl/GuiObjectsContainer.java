package pro.gravit.launcher.gui.impl;

import pro.gravit.launcher.gui.JavaFXApplication;
import pro.gravit.launcher.gui.overlays.AbstractOverlay;
import pro.gravit.launcher.gui.overlays.ProcessingOverlay;
import pro.gravit.launcher.gui.overlays.UploadAssetOverlay;
import pro.gravit.launcher.gui.overlays.WelcomeOverlay;
import pro.gravit.launcher.gui.scenes.AbstractScene;
import pro.gravit.launcher.gui.scenes.console.ConsoleScene;
import pro.gravit.launcher.gui.scenes.debug.DebugScene;
import pro.gravit.launcher.gui.scenes.internal.BrowserScene;
import pro.gravit.launcher.gui.scenes.login.LoginScene;
import pro.gravit.launcher.gui.scenes.options.OptionsScene;
import pro.gravit.launcher.gui.scenes.serverinfo.ServerInfoScene;
import pro.gravit.launcher.gui.scenes.servermenu.ServerMenuScene;
import pro.gravit.launcher.gui.scenes.settings.SettingsScene;
import pro.gravit.launcher.gui.scenes.update.UpdateScene;
import pro.gravit.launcher.gui.stage.ConsoleStage;
import pro.gravit.utils.helper.LogHelper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class GuiObjectsContainer {
    private final JavaFXApplication application;
    private final Set<AbstractOverlay> overlays = new HashSet<>();
    private final Set<AbstractScene> scenes = new HashSet<>();
    public ProcessingOverlay processingOverlay;
    public WelcomeOverlay welcomeOverlay;
    public UploadAssetOverlay uploadAssetOverlay;
    public UpdateScene updateScene;
    public DebugScene debugScene;

    public ServerMenuScene serverMenuScene;
    public ServerInfoScene serverInfoScene;
    public LoginScene loginScene;
    public OptionsScene optionsScene;
    public SettingsScene settingsScene;
    public ConsoleScene consoleScene;

    public ConsoleStage consoleStage;
    public BrowserScene browserScene;

    public GuiObjectsContainer(JavaFXApplication application) {
        this.application = application;
    }

    public void init() {
        loginScene = registerScene(LoginScene.class);
        processingOverlay = registerOverlay(ProcessingOverlay.class);
        welcomeOverlay = registerOverlay(WelcomeOverlay.class);
        uploadAssetOverlay = registerOverlay(UploadAssetOverlay.class);

        serverMenuScene = registerScene(ServerMenuScene.class);
        serverInfoScene = registerScene(ServerInfoScene.class);
        optionsScene = registerScene(OptionsScene.class);
        settingsScene = registerScene(SettingsScene.class);
        consoleScene = registerScene(ConsoleScene.class);

        updateScene = registerScene(UpdateScene.class);
        debugScene = registerScene(DebugScene.class);
        browserScene = registerScene(BrowserScene.class);
    }

    public Stream<AbstractScene> scenes() {
        return scenes.stream();
    }

    public Stream<AbstractOverlay> overlays() {
        return overlays.stream();
    }

    public Set<AbstractOverlay> getOverlays() {
        return Collections.unmodifiableSet(overlays);
    }

    public Set<AbstractScene> getScenes() {
        return Collections.unmodifiableSet(scenes);
    }

    public void reload() throws Exception {
        Class<? extends AbstractScene> scene = application.getCurrentScene().getClass();
        ContextHelper.runInFxThreadStatic(() -> {
            application.getMainStage().setScene(null);
            application.resetDirectory();
            overlays.clear();
            scenes.clear();
            application.getMainStage().resetStyles();
            init();
            for (AbstractScene s : scenes) {
                if (s.getClass() == scene) {
                    application.getMainStage().setScene(s);
                }
            }
        }).get();
    }

    public AbstractScene getSceneByName(String name) {
        for (AbstractScene scene : scenes) {
            if (name.equals(scene.getName())) {
                return scene;
            }
        }
        return null;
    }

    public AbstractOverlay getOverlayByName(String name) {
        for (AbstractOverlay overlay : overlays) {
            if (name.equals(overlay.getName())) {
                return overlay;
            }
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public <T extends AbstractOverlay> T registerOverlay(Class<T> clazz) {
        try {
            T instance = (T) MethodHandles
                    .publicLookup().findConstructor(clazz, MethodType.methodType(void.class, JavaFXApplication.class))
                    .invokeWithArguments(application);
            overlays.add(instance);
            return instance;
        } catch (Throwable e) {
            LogHelper.error(e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractScene> T registerScene(Class<T> clazz) {
        try {
            T instance = (T) MethodHandles
                    .publicLookup().findConstructor(clazz, MethodType.methodType(void.class, JavaFXApplication.class))
                    .invokeWithArguments(application);
            scenes.add(instance);
            return instance;
        } catch (Throwable e) {
            LogHelper.error(e);
            throw new RuntimeException(e);
        }
    }
}