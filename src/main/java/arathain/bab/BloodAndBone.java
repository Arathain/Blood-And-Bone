package arathain.bab;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class BloodAndBone implements ModInitializer {
	public static String MODID = "bab";

	@Override
	public void onInitialize() {
		GeckoLib.initialize();
	}
}
