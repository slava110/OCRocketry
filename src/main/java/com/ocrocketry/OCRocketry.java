package com.ocrocketry;

import com.ocrocketry.driver.*;
import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = OCRocketry.MODID, name = OCRocketry.NAME)
public class OCRocketry {
    public static final String MODID = "ocrocketry";
    public static final String NAME = "OCRocketry";

    public static Logger logger = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Driver.add(new WarpControllerDriver());
        Driver.add(new HoloPlanetSelectorDriver());
        Driver.add(new BiomeScannerDriver());
        Driver.add(new GravityControllerDriver());
        Driver.add(new AltitudeControllerDriver());
        Driver.add(new OrientationControllerDriver());

        Driver.add(new MonitoringStationDriver());
        Driver.add(new MiningLaserDriver());
        Driver.add(new AtmosphereDetectorDriver());

        //Driver.add(new LandingPadDriver());
    }

}