/**
 * Copyright 2020- Mark C. Slee, Heron Arts LLC
 * <p>
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 * <p>
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 * <p>
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.app;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.github.brotchie.StarpusherOutputGroup;
import com.github.brotchie.effect.InstantSparkleEffect;
import com.github.brotchie.pattern.FirePattern;
import com.github.brotchie.pattern.SnowPattern;
import com.github.brotchie.pattern.TreeRingPattern;
import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioComponent;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulation.LXCompoundModulation;
import heronarts.lx.modulation.LXParameterModulation;
import heronarts.lx.parameter.NormalizedParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.structure.JsonFixture;
import heronarts.lx.studio.LXStudio;
import processing.core.PApplet;

/**
 * This is an example top-level class to build and run an LX Studio
 * application via an IDE. The main() method of this class can be
 * invoked with arguments to either run with a full Processing 4 UI
 * or as a headless command-line only engine.
 */
public class LXStudioApp extends PApplet implements LXPlugin {

    private static final String WINDOW_TITLE = "LX Studio";

    private static int WIDTH = 1280;
    private static int HEIGHT = 800;
    private static boolean FULLSCREEN = false;

    private static int WINDOW_X = 0;
    private static int WINDOW_Y = 0;

    private static boolean HAS_WINDOW_POSITION = false;

    private GraphicMeter meter;

    /**
     * Main interface into the program. Two modes are supported, if the --headless
     * flag is supplied then a raw CLI version of LX is used. If not, then we embed
     * in a Processing 4 applet and run as such.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        LX.log("Initializing LX version " + LXStudio.VERSION);
        LX.log("Running java " +
                System.getProperty("java.version") + " " +
                System.getProperty("java.vendor") + " " +
                System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " +
                System.getProperty("os.arch")
        );


        // NOTE(mcslee): Hack for macOS Sonoma!!
        // Hopefully to be removed in a future version
        com.jogamp.opengl.GLProfile.initSingleton();

        boolean headless = false;
        File projectFile = null;
        for (int i = 0; i < args.length; ++i) {
            if ("--help".equals(args[i])) {
            } else if ("--headless".equals(args[i])) {
                headless = true;
            } else if ("--fullscreen".equals(args[i]) || "-f".equals(args[i])) {
                FULLSCREEN = true;
            } else if ("--width".equals(args[i]) || "-w".equals(args[i])) {
                try {
                    WIDTH = Integer.parseInt(args[++i]);
                } catch (Exception x) {
                    LX.error("Width command-line argument must be followed by integer");
                }
            } else if ("--height".equals(args[i]) || "-h".equals(args[i])) {
                try {
                    HEIGHT = Integer.parseInt(args[++i]);
                } catch (Exception x) {
                    LX.error("Height command-line argument must be followed by integer");
                }
            } else if ("--windowx".equals(args[i]) || "-x".equals(args[i])) {
                try {
                    WINDOW_X = Integer.parseInt(args[++i]);
                    HAS_WINDOW_POSITION = true;
                } catch (Exception x) {
                    LX.error("Window X command-line argument must be followed by integer");
                }
            } else if ("--windowy".equals(args[i]) || "-y".equals(args[i])) {
                try {
                    WINDOW_Y = Integer.parseInt(args[++i]);
                    HAS_WINDOW_POSITION = true;
                } catch (Exception x) {
                    LX.error("Window Y command-line argument must be followed by integer");
                }
            } else if (args[i].endsWith(".lxp")) {
                try {
                    projectFile = new File(args[i]);
                } catch (Exception x) {
                    LX.error(x, "Command-line project file path invalid: " + args[i]);
                }
            }
        }
        if (headless) {
            // We're not actually going to run this as a PApplet, but we need to explicitly
            // construct and set the initialize callback so that any custom components
            // will be run
            LX.Flags flags = new LX.Flags();
            flags.initialize = new LXStudioApp();
            if (projectFile == null) {
                LX.log("WARNING: No project filename was specified for headless mode!");
            }
            LX.headless(flags, projectFile);
        } else {
            PApplet.main("heronarts.lx.app.LXStudioApp", args);
        }
    }

    @Override
    public void settings() {
        if (FULLSCREEN) {
            fullScreen(PApplet.P3D);
        } else {
            size(WIDTH, HEIGHT, PApplet.P3D);
        }
        pixelDensity(displayDensity());
    }

    @Override
    public void setup() {
        LXStudio.Flags flags = new LXStudio.Flags(this);
        flags.resizable = true;
        flags.useGLPointCloud = false;
        flags.startMultiThreaded = true;

        // NOTE: it seems like on Windows systems P4LX can end
        // up setting this to the "lib" folder depending on how
        // dependency JARs were loaded. Explicitly set it to "."
        // here and be sure to run explicitly from root folder
        flags.mediaPath = ".";

        new LXStudio(this, flags);
        this.surface.setTitle(WINDOW_TITLE);
        if (!FULLSCREEN && HAS_WINDOW_POSITION) {
            this.surface.setLocation(WINDOW_X, WINDOW_Y);
        }

    }

    @Override
    public void initialize(LX lx) {
        lx.registry.addEffect(InstantSparkleEffect.class);
        lx.registry.addPattern(TreeRingPattern.class);
        lx.registry.addPattern(SnowPattern.class);
        lx.registry.addPattern(FirePattern.class);

        lx.addProjectListener((file, change) -> {
            if (change == LX.ProjectListener.Change.NEW) {
                // Outputs are kept when creating a new project in the UI, but models are destroyed. Re-instantiate
                // the Xmas tree fixture and set the initial channel brightness to 50%;
                lx.structure.addFixture(new JsonFixture(lx, "XmasTree"));
                lx.engine.mixer.getChannel(0).fader.setValue(0.5);
            }
        });
    }

    public void initializeUI(LXStudio lx, LXStudio.UI ui) {
    }


    public void onUIReady(LXStudio lx, LXStudio.UI ui) {
        lx.structure.addFixture(new JsonFixture(lx, "XmasTree"));

        final LXModel model = lx.getModel();

        StarpusherOutputGroup output;
        try {
            output = new StarpusherOutputGroup(lx, model, 500);
        } catch (StarpusherOutputGroup.StarpusherOutputGroupException exception) {
            LX.error(exception);
            return;
        }
        try {
            output.setAddress(InetAddress.getByName("10.1.1.2"));
        } catch (UnknownHostException exception) {
            LX.error(exception);
        }
        output.setPort(6868);
        lx.addOutput(output);

        lx.engine.audio.enabled.setValue(true);

        try {
            LXPattern snow = lx.instantiatePattern(SnowPattern.class);
            LXChannel channel = lx.engine.mixer.addChannel();
            channel.addPattern(snow);
        } catch (LX.InstantiationException exception) {
            LX.error(exception);
            return;
        }


        /*int ringCount = lx.getModel().children[0].children.length;
        meter = new GraphicMeter(lx.engine.audio.input.mix, ringCount);
        meter.running.setValue(true);
        meter.slope.setValue(4.5);
        meter.gain.setValue(48);
        meter.range.setValue(45);
        lx.engine.modulation.addModulator(meter, 0);

        for (int i = 0; i < ringCount; i++) {
            LXPattern pattern;
            try {
                pattern = lx.instantiatePattern(TreeRingPattern.class);
                pattern.getParameter("ring").setValue((i + 1) / (double)(ringCount + 1));
            } catch (LX.InstantiationException exception) {
                LX.error(exception);
                return;
            }
            LXChannel channel = lx.engine.mixer.addChannel(new LXPattern[]{pattern});
            channel.fader.setValue(0);

            NormalizedParameter band = meter.bands[i];
            try {
                LXCompoundModulation modulation = new LXCompoundModulation(lx.engine.modulation, band, channel.fader);
                modulation.range.setValue(1.0);
                lx.engine.modulation.addModulation(modulation);
            } catch (LXParameterModulation.ModulationException exception) {
                LX.error(exception);
            }
        }*/
    }

    @Override
    public void draw() {
        // All handled by core LX engine, do not modify, method exists only so that Processing
        // will run a draw-loop.
    }

}
