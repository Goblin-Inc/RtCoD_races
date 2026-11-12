package com.darkorigins.client;

import com.darkorigins.DarkOriginsMod;
import com.darkorigins.network.PacketHandler;
import com.darkorigins.network.SelectRaceC2SPacket;
import com.darkorigins.race.RaceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaceSelectionScreen extends Screen {
    private final boolean canCancel;
    private final List<RaceType> races = new ArrayList<>();
    private RaceType selectedRace = RaceType.HUMAN;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 24;

    public RaceSelectionScreen(boolean canCancel) {
        super(Component.literal("Выбор Происхождения — RtCoD_races"));
        this.canCancel = canCancel;
        for (RaceType r : RaceType.values()) {
            if (r != RaceType.NONE) {
                races.add(r);
            }
        }
    }

    private ResourceLocation getRaceIcon(RaceType race) {
        return new ResourceLocation(DarkOriginsMod.MODID, "textures/gui/races/" + race.name().toLowerCase() + ".png");
    }

    @Override
    protected void init() {
        super.init();
        int btnY = this.height - 32;
        int rightCenterX = 180 + (this.width - 195) / 2;

        // Кнопка выбора расы
        this.addRenderableWidget(Button.builder(Component.literal("✔ Выбрать расу"), btn -> {
            PacketHandler.sendToServer(new SelectRaceC2SPacket(selectedRace));
            this.onClose();
        }).bounds(rightCenterX - 115, btnY, 110, 20).build());

        // Кнопка отмены (активна, если открыто командой /race в мире)
        Button cancelBtn = Button.builder(Component.literal("✖ Отмена"), btn -> {
            if (this.canCancel) {
                this.onClose();
            }
        }).bounds(rightCenterX + 5, btnY, 110, 20).build();
        cancelBtn.active = this.canCancel;
        this.addRenderableWidget(cancelBtn);
    }

    private void renderStarfieldBackground(GuiGraphics g) {
        // Глубокий тёмно-синий космический градиент (закрывает непрогруженный мир на 100%)
        g.fillGradient(0, 0, this.width, this.height, 0xFF040714, 0xFF0E1738);

        // Детерминированные пиксельные звёзды в стиле ночного неба Minecraft
        Random rand = new Random(421337L);
        long time = System.currentTimeMillis();
        for (int i = 0; i < 135; i++) {
            int sx = rand.nextInt(Math.max(1, this.width));
            int sy = rand.nextInt(Math.max(1, this.height));
            int size = (i % 9 == 0) ? 2 : 1;
            float speed = 0.002f + (i % 5) * 0.0012f;
            float phase = i * 1.7f;
            float pulse = 0.45f + 0.55f * Mth.sin(time * speed + phase);
            int alpha = Mth.clamp((int) (pulse * 245), 45, 255);

            int rgb = switch (i % 4) {
                case 0 -> 0xDCE8FF; // Голубовато-белая звезда
                case 1 -> 0xFFF4D2; // Тёплая золотистая звезда
                case 2 -> 0xA9C4FF; // Холодный синий гигант
                default -> 0xFFFFFF; // Чистая белая звезда
            };
            int color = (alpha << 24) | rgb;
            g.fill(sx, sy, sx + size, sy + size, color);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 1. Фон со звёздами
        renderStarfieldBackground(g);

        // Заголовок
        g.drawCenteredString(this.font, "✦ ВЫБЕРИТЕ СВОЮ РАСУ (RtCoD_races) ✦", this.width / 2, 10, 0xFFD700);

        // Координаты панелей
        int listX = 14;
        int listY = 28;
        int listW = 155;
        int listH = this.height - 68;

        int infoX = listX + listW + 12;
        int infoY = 28;
        int infoW = this.width - infoX - 14;
        int infoH = this.height - 68;

        // 2. Левая панель (Список рас с прокруткой и головами)
        g.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, 0xFF3A4E7A);
        g.fill(listX, listY, listX + listW, listY + listH, 0xD90A1024);

        int maxVisible = listH / ITEM_HEIGHT;
        int maxScroll = Math.max(0, races.size() - maxVisible);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        for (int i = 0; i < maxVisible && (i + scrollOffset) < races.size(); i++) {
            RaceType r = races.get(i + scrollOffset);
            int itemY = listY + i * ITEM_HEIGHT;
            boolean hovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT;
            boolean isSelected = (r == selectedRace);

            int bgColor = isSelected ? 0xFF253B6E : (hovered ? 0xFF162447 : 0x00000000);
            if (bgColor != 0) {
                g.fill(listX + 2, itemY + 1, listX + listW - 6, itemY + ITEM_HEIGHT - 1, bgColor);
            }
            if (isSelected) {
                g.fill(listX + 2, itemY + 1, listX + 5, itemY + ITEM_HEIGHT - 1, 0xFFFFD700);
            }

            // Иконка головы расы 16x16 слева от названия
            int iconX = listX + 9;
            int iconY = itemY + 4;
            g.fill(iconX - 1, iconY - 1, iconX + 17, iconY + 17, 0xFF000000);
            g.blit(getRaceIcon(r), iconX, iconY, 0, 0, 16, 16, 16, 16);

            // Название расы
            int textColor = isSelected ? 0xFFFFD700 : (hovered ? 0xFFFFFFFF : 0xFFCFD8DC);
            g.drawString(this.font, r.displayName, iconX + 22, itemY + 8, textColor);
        }

        // Ползунок скроллбара
        if (maxScroll > 0) {
            int barX = listX + listW - 4;
            g.fill(barX, listY, barX + 3, listY + listH, 0xFF070B18);
            int thumbH = Math.max(20, (int) ((float) maxVisible / races.size() * listH));
            int thumbY = listY + (int) ((float) scrollOffset / maxScroll * (listH - thumbH));
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF6488EA);
        }

        // 3. Правая панель (Карточка выбранной расы)
        g.fill(infoX - 1, infoY - 1, infoX + infoW + 1, infoY + infoH + 1, 0xFF3A4E7A);
        g.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0xD90A1024);

        if (selectedRace != null) {
            // Крупная иконка головы 32x32 в карточке описания
            int bigIconX = infoX + 12;
            int bigIconY = infoY + 10;
            g.fill(bigIconX - 2, bigIconY - 2, bigIconX + 34, bigIconY + 34, 0xFFFFD700);
            g.fill(bigIconX - 1, bigIconY - 1, bigIconX + 33, bigIconY + 33, 0xFF090D1A);
            g.pose().pushPose();
            g.pose().translate(bigIconX, bigIconY, 0);
            g.pose().scale(2.0f, 2.0f, 1.0f);
            g.blit(getRaceIcon(selectedRace), 0, 0, 0, 0, 16, 16, 16, 16);
            g.pose().popPose();

            // Имя расы и статы рядом с большой головой
            g.drawString(this.font, "§l" + selectedRace.displayName.toUpperCase(), bigIconX + 42, bigIconY + 4, 0xFFFFD700);

            int hearts = (int) ((20.0 + selectedRace.healthBonus) / 2.0);
            int speedPct = (int) Math.round((1.0 + selectedRace.speedBonus) * 100);
            int scalePct = (int) Math.round(selectedRace.scale * 100);
            String statsLine = String.format("§c♥ %d сердец   §b⚡ Скор: %d%%   §6⚔ Урон: %+.1f   §7🛡 Броня: %+.0f   §e↕ Рост: %d%%",
                    hearts, speedPct, selectedRace.attackBonus, selectedRace.armorBonus, scalePct);
            g.drawString(this.font, statsLine, bigIconX + 42, bigIconY + 18, 0xFFFFFFFF);

            // Разделительная линия
            int curY = bigIconY + 42;
            g.fill(infoX + 10, curY, infoX + infoW - 10, curY + 1, 0xFF28385E);
            curY += 8;

            // Многострочный вывод описания, баффов, дебаффов и активки
            int textWidth = infoW - 24;
            g.drawString(this.font, "§9§lОПИСАНИЕ И МЕХАНИКИ РАСЫ:", infoX + 12, curY, 0xFF82B1FF);
            curY += 12;

            List<FormattedCharSequence> lines = this.font.split(Component.literal(selectedRace.lore), textWidth);
            for (FormattedCharSequence line : lines) {
                g.drawString(this.font, line, infoX + 12, curY, 0xFFE0E6ED);
                curY += 11;
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = 14;
        int listY = 28;
        int listW = 155;
        int listH = this.height - 68;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int clickedIndex = (int) ((mouseY - listY) / ITEM_HEIGHT) + scrollOffset;
            if (clickedIndex >= 0 && clickedIndex < races.size()) {
                selectedRace = races.get(clickedIndex);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listH = this.height - 68;
        int maxVisible = listH / ITEM_HEIGHT;
        int maxScroll = Math.max(0, races.size() - maxVisible);
        scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll);
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.canCancel;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}