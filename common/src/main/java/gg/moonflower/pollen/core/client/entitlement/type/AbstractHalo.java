package gg.moonflower.pollen.core.client.entitlement.type;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.moonflower.pollen.core.Pollen;
import gg.moonflower.pollen.core.client.entitlement.ModelEntitlement;
import gg.moonflower.pollen.core.client.entitlement.TexturedEntitlement;
import gg.moonflower.pollen.pinwheel.api.common.texture.GeometryModelTextureTable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

@ApiStatus.Internal
public abstract class AbstractHalo extends Cosmetic implements ModelEntitlement, TexturedEntitlement {

    private boolean emissive;
    private Visibility visibility;
    private Display display;

    @Override
    public void updateSettings(JsonObject settings) {
        super.updateSettings(settings);
        if (settings.has("emissive"))
            this.emissive = GsonHelper.getAsBoolean(settings, "emissive");
        if (settings.has("visibility"))
            this.visibility = Visibility.byName(GsonHelper.getAsString(settings, "visibility"));
        if (settings.has("display"))
            this.display = Display.byName(GsonHelper.getAsString(settings, "display"));
    }

    @Override
    public JsonObject saveSettings() {
        JsonObject settings = super.saveSettings();
        settings.addProperty("emissive", this.emissive);
        settings.addProperty("visibility", this.visibility.name().toLowerCase(Locale.ROOT));
        settings.addProperty("display", this.display.name().toLowerCase(Locale.ROOT));
        return settings;
    }

    @Nullable
    protected abstract HaloData getData();

    @Nullable
    @Override
    public ResourceLocation getModelKey() {
        HaloData data = this.getData();
        return data != null ? this.display.getter.apply(data) : null;
    }

    @Override
    public boolean isEnabled() {
        return this.visibility != Visibility.OFF;
    }

    public boolean isEmissive() {
        return emissive;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public Display getDisplay() {
        return display;
    }

    public void setEmissive(boolean emissive) {
        this.emissive = emissive;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public enum Visibility {
        OFF, ON, MOONLIGHT;

        public static Visibility byName(String name) {
            for (Visibility type : values())
                if (name.equals(type.name().toLowerCase(Locale.ROOT)))
                    return type;
            return OFF;
        }
    }

    public enum Display {
        DEFAULT(HaloData::getDefaultModelKey),
        HORIZONTAL(HaloData::getHorizontalModelKey),
        VERTICAL(HaloData::getVerticalModelKey);

        private final Function<HaloData, ResourceLocation> getter;

        Display(Function<HaloData, ResourceLocation> getter) {
            this.getter = getter;
        }

        public static Display byName(String name) {
            for (Display type : values())
                if (name.equals(type.name().toLowerCase(Locale.ROOT)))
                    return type;
            return DEFAULT;
        }
    }

    public static class HaloData {

        public static final Codec<HaloData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("modelUrl").forGetter(HaloData::getModelUrl),
                Codec.STRING.fieldOf("defaultModelKey").forGetter(data -> data.getDefaultModelKey().getPath()),
                Codec.STRING.fieldOf("horizontalModelKey").forGetter(data -> data.getHorizontalModelKey().getPath()),
                Codec.STRING.fieldOf("verticalModelKey").forGetter(data -> data.getVerticalModelKey().getPath()),
                GeometryModelTextureTable.CODEC.fieldOf("textureTable").forGetter(HaloData::getTextureTable)
        ).apply(instance, HaloData::new));

        private final String modelUrl;
        private final ResourceLocation defaultModelKey;
        private final ResourceLocation horizontalModelKey;
        private final ResourceLocation verticalModelKey;
        private final GeometryModelTextureTable textureTable;

        public HaloData(String modelUrl, String defaultModelKey, String horizontalModelKey, String verticalModelKey, GeometryModelTextureTable textureTable) {
            this.modelUrl = modelUrl;
            this.defaultModelKey = new ResourceLocation(Pollen.MOD_ID, defaultModelKey);
            this.horizontalModelKey = new ResourceLocation(Pollen.MOD_ID, horizontalModelKey);
            this.verticalModelKey = new ResourceLocation(Pollen.MOD_ID, verticalModelKey);
            this.textureTable = textureTable;
        }

        public String getModelUrl() {
            return modelUrl;
        }

        public ResourceLocation getDefaultModelKey() {
            return defaultModelKey;
        }

        public ResourceLocation getHorizontalModelKey() {
            return horizontalModelKey;
        }

        public ResourceLocation getVerticalModelKey() {
            return verticalModelKey;
        }

        public GeometryModelTextureTable getTextureTable() {
            return textureTable;
        }
    }
}