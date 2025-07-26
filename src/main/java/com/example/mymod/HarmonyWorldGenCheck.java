import com.harmonymod.HarmonyAPI;
import net.minecraft.resources.ResourceLocation;

public class ExampleWorldGen {
    public static void tryModifyBiome() {
        String myModId = "yourmodid";
        ResourceLocation targetBiome = new ResourceLocation("minecraft:plains");

        boolean canModify = HarmonyAPI.canModModifyBiome(myModId, targetBiome);

        if (canModify) {
            // Safe to add features or make changes to this biome!
            System.out.println("Modifying biome: " + targetBiome);
            // ...your biome modification code here...
        } else {
            // Not allowed—Harmony says another mod has rights, so don't change it.
            System.out.println("Cannot modify biome: " + targetBiome);
        }
    }
}
