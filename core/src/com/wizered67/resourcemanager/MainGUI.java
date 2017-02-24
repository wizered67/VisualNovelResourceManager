package com.wizered67.resourcemanager;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.spinner.FloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static com.wizered67.resourcemanager.Resources.*;

public class MainGUI extends ApplicationAdapter {
	Stage uiStage;
	MenuBar menuBar;
	Actions actions;
	FileChooser fileChooser;
	ButtonGroup<TextButton> resourceTypesGroup;
	VerticalGroup resourceTypesButtons;
	ScrollPane scrollPane;
	Table resourceTable;
	final boolean DEBUG = true;

    @Override
	public void create () {
	    actions = new Actions();
		VisUI.load();
        FileChooser.setDefaultPrefsName("com.wizered67.resourcemanager");
		uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(uiStage);
		createUIElements();
	}

	private void createUIElements() {
        final Table root = new Table();
        root.setDebug(DEBUG);
        root.setFillParent(true);
        uiStage.addActor(root);
        createFileChooser();
        menuBar = new MenuBar();
        root.top().add(menuBar.getTable()).expandX().fillX().row();
        //root.add().expand().fill();
        createMenus();
        resourceTable = new Table();
        //resourceTable.add(new Label("Good morning world and all who inhabit it!\n\n\n\n\n\n\n\n\n\n\n\nhello", VisUI.getSkin())).fill();
        scrollPane = new ScrollPane(resourceTable, VisUI.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        uiStage.setScrollFocus(scrollPane);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        VisUI.getSkin().add("white", new Texture(pixmap));
        scrollPane.getStyle().background = //VisUI.getSkin().get(Window.WindowStyle.class).background;
                VisUI.getSkin().newDrawable("white", Color.DARK_GRAY);//new Color(53f/255, 53f/255, 53f/255, 1));
        root.add(scrollPane).expand().fill();
        resourceTypesGroup = new ButtonGroup<TextButton>();
        TextButton b1 = new TextButton("Animations", VisUI.getSkin());
        b1.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollPane.setWidget(makeResourceTable(ANIMATIONS_DIRECTORY, ".pack", ANIMATION_FILES_TAG));
            }
        });
        TextButton b2 = new TextButton("Textures", VisUI.getSkin());
        b2.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollPane.setWidget(makeResourceTable(TEXTURES_DIRECTORY, TEXTURES_TAG));
            }
        });
        TextButton b3 = new TextButton("Music", VisUI.getSkin());
        b3.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollPane.setWidget(makeResourceTable(MUSIC_DIRECTORY, MUSIC_TAG));
            }
        });
        TextButton b4 = new TextButton("Sounds", VisUI.getSkin());
        b4.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollPane.setWidget(makeResourceTable(SOUNDS_DIRECTORY, SOUNDS_TAG));
            }
        });
        TextButton b5 = new TextButton("Characters", VisUI.getSkin());
        TextButton b6 = new TextButton("Groups", VisUI.getSkin());
        resourceTypesGroup.add(b1, b2, b3, b4, b5, b6);
        resourceTypesButtons = new VerticalGroup();
        resourceTypesButtons.space(10);
        resourceTypesButtons.padLeft(15);
        resourceTypesButtons.padRight(15);
        resourceTypesButtons.addActor(b1);
        resourceTypesButtons.addActor(b2);
        resourceTypesButtons.addActor(b3);
        resourceTypesButtons.addActor(b4);
        resourceTypesButtons.addActor(b5);
        resourceTypesButtons.addActor(b6);
        resourceTypesButtons.fill();
        root.add(resourceTypesButtons).right().top();
    }

    private Table makeResourceTable(String directory, String tag) {
        return makeResourceTable(directory, null, tag);
    }

    private Table makeResourceTable(String directory, String suffix, String tag) {
        boolean isAnimations = directory.equals(ANIMATIONS_DIRECTORY);
        Table table = new Table();
        table.setDebug(DEBUG);
        FileHandle dir = Gdx.files.local(directory);
        table.top().add(new Label(directory, VisUI.getSkin())).row();
        if (!dir.isDirectory()) {
            return table;
        }
        Mapping specifiedResources = ResourceXmlLoader.getResources(tag);
        FileHandle[] files = suffix == null ? dir.list() : dir.list(suffix);
        for (FileHandle file : files) {
            final String filename = file.name();
            CheckBox included = new CheckBox("", VisUI.getSkin());
            Actor filenameLabel;
            final CollapsibleWidget animationTable = isAnimations ? addAnimations(filename) : null;
            if (isAnimations) {
                filenameLabel = new TextButton(filename, VisUI.getSkin());
                filenameLabel.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        animationTable.setCollapsed(!animationTable.isCollapsed(), true);
                    }
                });
            } else {
                filenameLabel = new Label(filename, VisUI.getSkin());
            }
            String specifiedIdentifier = specifiedResources.getIdentifier(filename);
            included.setChecked(true);
            if (specifiedIdentifier == null) {
                specifiedIdentifier = "";
                included.setChecked(false);
            }
            VisTextField textField = new VisTextField(specifiedIdentifier);
            textField.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {

                }
            });
            textField.setDisabled(!included.isChecked());
            included.setUserObject(textField);
            included.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    CheckBox cb = (CheckBox) actor;
                    ((VisTextField) cb.getUserObject()).setDisabled(!cb.isChecked());
                }
            });

            HorizontalGroup horizontalGroup = new HorizontalGroup();
            horizontalGroup.addActor(included);
            horizontalGroup.addActor(filenameLabel);
            horizontalGroup.space(10);
            //horizontalGroup.fill();
            horizontalGroup.addActor(textField);
            table.add(horizontalGroup).expandX().fillX().padLeft(10).padBottom(10).padRight(100).left().row();
            textField.setFillParent(true);
            if (isAnimations && animationTable != null) {
                table.add(animationTable).padLeft(10).left();
            }
            /*
            table.left().add(included);
            table.add(filenameLabel).left();
            table.add(textField).expandX().fillX().right().row();
            */
            //table.add(new Label(file.name(), VisUI.getSkin())).row();
        }
        return table;
    }

    private CollapsibleWidget addAnimations(String filename) {
        Table animTable = new Table();
        animTable.setDebug(DEBUG);
        Set<String> animationNames = getAnimationNames(filename);
        for (String name : animationNames) {
            animTable.add(new Label(name, VisUI.getSkin()));
            Spinner spinner = new Spinner("Frame Duration", new FloatSpinnerModel("1", "0", "100", "0.1"));
            animTable.add(spinner).padLeft(30);
            SelectBox<Animation.PlayMode> playModeSelectBox = new SelectBox<Animation.PlayMode>(VisUI.getSkin());
            playModeSelectBox.setItems(Animation.PlayMode.values());
            animTable.add(playModeSelectBox);
            animTable.row();
        }
        //animTable.add(new TextButton("Hello world!", VisUI.getSkin()));
        return new CollapsibleWidget(animTable);
    }

    private Set<String> getAnimationNames(String atlasFilename) {
        Set<String> animationNames = new HashSet<String>();
        FileHandle atlasFile = Gdx.files.internal(ANIMATIONS_DIRECTORY + "/" + atlasFilename);
        try {
            Scanner scanner = new Scanner(atlasFile.file());
            //skip over file name
            scanner.next();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.indexOf(':') < 0 && !line.isEmpty()) { //no colon so it should be the name of an animation
                    animationNames.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return animationNames;
    }

    private void createFileChooser() {
        //chooser creation
        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                System.out.println("Chose " + file.get(0).name());
            }
        });
    }

    private void createMenus() {
	    Menu fileMenu = new Menu("File");
	    fileMenu.addItem(new MenuItem("Save", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                actions.save();
            }
        }).setShortcut("ctrl + s"));
	    fileMenu.addItem(new MenuItem("Save and generate", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                actions.saveAndGenerate();
            }
        }).setShortcut("ctrl + shift + s"));
	    fileMenu.addItem(new MenuItem("Set output location", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               uiStage.addActor(fileChooser.fadeIn());
            }
        }));
	    fileMenu.addItem(new MenuItem("Refresh files", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                actions.refreshFiles();
            }
        }).setShortcut("ctrl + r"));
	    Menu editMenu = new Menu("Edit");
	    editMenu.addItem(new MenuItem("Undo", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                actions.undo();
            }
        }).setShortcut("ctrl + z"));
	    menuBar.addMenu(fileMenu);
	    menuBar.addMenu(editMenu);
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		uiStage.act(Gdx.graphics.getDeltaTime());
		uiStage.draw();
	}
}
