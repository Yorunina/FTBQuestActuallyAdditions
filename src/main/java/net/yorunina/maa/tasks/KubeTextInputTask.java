package net.yorunina.maa.tasks;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yorunina.maa.networks.KubeTextInputSubmitMessage;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;


public class KubeTextInputTask extends KubeTask {
    private String textBoxRawTitle;

    public KubeTextInputTask(long id, Quest quest) {
        super(id, quest);
        this.textBoxRawTitle = "";
    }

    public TaskType getType() {
        return TasksRegistry.KUBE_TEXT_INPUT;
    }

    @OnlyIn(Dist.CLIENT)
    public void onButtonClicked(Button button, boolean canClick) {
        if (ClientQuestFile.INSTANCE != null && ClientQuestFile.INSTANCE.selfTeamData != null && ClientQuestFile.INSTANCE.selfTeamData.isCompleted(this)) {
            return;
        }
        button.playClickSound();

        StringConfig c = new StringConfig(null);
        EditStringConfigOverlay<String> overlay = new EditStringConfigOverlay<>(button.getGui(), c, accepted -> {
            if (accepted) {
                new KubeTextInputSubmitMessage(this.id, this.getKubeId(), c.getValue()).sendToServer();
            }
        }, this.textBoxRawTitle.isEmpty() ? this.getTitle() : TextUtils.parseRawText(this.textBoxRawTitle));
        overlay.setWidth(Mth.clamp(overlay.getWidth(), 150, button.getScreen().getGuiScaledWidth() - 20));
        overlay.setPos(button.getX() + button.width / 2, button.getY() + 16);
        button.getGui().pushModalPanel(overlay);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("textBoxRawTitle", this.textBoxRawTitle);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.textBoxRawTitle = nbt.getString("textBoxRawTitle");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.textBoxRawTitle);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.textBoxRawTitle = buffer.readUtf();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("textBoxRawTitle", this.textBoxRawTitle, (v) -> this.textBoxRawTitle = v, "");
    }
}
