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
    private int infoScrollOffset = 0;
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
        int btnY = this.height - 28;
        int rightCenterX = 180 + (this.width - 195) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("✔ Выбрать расу"), btn -> {
            PacketHandler.sendToServer(new SelectRaceC2SPacket(selectedRace));
            this.onClose();
        }).bounds(rightCenterX - 115, btnY, 110, 20).build());

        Button cancelBtn = Button.builder(Component.literal("✖ Отмена"), btn -> {
            if (this.canCancel) {
                this.onClose();
            }
        }).bounds(rightCenterX + 5, btnY, 110, 20).build();
        cancelBtn.active = this.canCancel;
        this.addRenderableWidget(cancelBtn);
    }

    private void renderStarfieldBackground(GuiGraphics g) {
        g.fillGradient(0, 0, this.width, this.height, 0xFF040714, 0xFF0E1738);
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
                case 0 -> 0xDCE8FF;
                case 1 -> 0xFFF4D2;
                case 2 -> 0xA9C4FF;
                default -> 0xFFFFFF;
            };
            int color = (alpha << 24) | rgb;
            g.fill(sx, sy, sx + size, sy + size, color);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderStarfieldBackground(g);

        g.drawCenteredString(this.font, "✦ ВЫБЕРИТЕ СВОЮ РАСУ (RtCoD_races) ✦", this.width / 2, 8, 0xFFD700);

        int listX = 14;
        int listY = 24;
        int listW = 155;
        int listH = this.height - 58;

        int infoX = listX + listW + 12;
        int infoY = 24;
        int infoW = this.width - infoX - 14;
        int infoH = this.height - 58;

        // 1. Левая панель (Список рас)
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

            int iconX = listX + 9;
            int iconY = itemY + 4;
            g.fill(iconX - 1, iconY - 1, iconX + 17, iconY + 17, 0xFF000000);
            g.blit(getRaceIcon(r), iconX, iconY, 0, 0, 16, 16, 16, 16);

            int textColor = isSelected ? 0xFFFFD700 : (hovered ? 0xFFFFFFFF : 0xFFCFD8DC);
            g.drawString(this.font, r.displayName, iconX + 22, itemY + 8, textColor);
        }

        if (maxScroll > 0) {
            int barX = listX + listW - 4;
            g.fill(barX, listY, barX + 3, listY + listH, 0xFF070B18);
            int thumbH = Math.max(20, (int) ((float) maxVisible / races.size() * listH));
            int thumbY = listY + (int) ((float) scrollOffset / maxScroll * (listH - thumbH));
            g.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF6488EA);
        }

        // 2. Правая панель (Полная информация о расе)
        g.fill(infoX - 1, infoY - 1, infoX + infoW + 1, infoY + infoH + 1, 0xFF3A4E7A);
        g.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0xD90A1024);

        if (selectedRace != null) {
            int bigIconX = infoX + 12;
            int bigIconY = infoY + 8;
            g.fill(bigIconX - 2, bigIconY - 2, bigIconX + 34, bigIconY + 34, 0xFFFFD700);
            g.fill(bigIconX - 1, bigIconY - 1, bigIconX + 33, bigIconY + 33, 0xFF090D1A);
            g.pose().pushPose();
            g.pose().translate(bigIconX, bigIconY, 0);
            g.pose().scale(2.0f, 2.0f, 1.0f);
            g.blit(getRaceIcon(selectedRace), 0, 0, 0, 0, 16, 16, 16, 16);
            g.pose().popPose();

            // Заголовок расы
            int statsX = bigIconX + 42;
            g.drawString(this.font, "§l" + selectedRace.displayName.toUpperCase(), statsX, bigIconY, 0xFFFFD700);

            // Статы в ДВЕ аккуратные строки, чтобы никогда не вылезали за край окна
            int hearts = (int) ((20.0 + selectedRace.healthBonus) / 2.0);
            int speedPct = (int) Math.round((1.0 + selectedRace.speedBonus) * 100);
            int scalePct = (int) Math.round(selectedRace.scale * 100);

            String statsRow1 = String.format("§c♥ %d сердец   §b⚡ Скорость: %d%%   §e↕ Рост: %d%%",
                    hearts, speedPct, scalePct);
            String statsRow2 = String.format("§6⚔ Бонус урона: %+.1f   §7🛡 Бонус брони: %+.0f",
                    selectedRace.attackBonus, selectedRace.armorBonus);

            g.drawString(this.font, statsRow1, statsX, bigIconY + 12, 0xFFFFFFFF);
            g.drawString(this.font, statsRow2, statsX, bigIconY + 23, 0xFFFFFFFF);

            // Разделительная линия под шапкой
            int headerBottom = bigIconY + 39;
            g.fill(infoX + 10, headerBottom, infoX + infoW - 10, headerBottom + 1, 0xFF28385E);

            // Область прокрутки подробного описания
            int contentTop = headerBottom + 5;
            int contentBottom = infoY + infoH - 5;
            int textWidth = infoW - 24;

            g.enableScissor(infoX + 2, contentTop, infoX + infoW - 2, contentBottom);

            int curY = contentTop - infoScrollOffset;

            // Блок 1: Описание
            curY = drawSection(g, "§9§lОПИСАНИЕ:", selectedRace.description, 0xFFB0BEC5, infoX + 12, curY, textWidth);
            curY += 4;

            // Блок 2: Баффы и Пассивки
            curY = drawSection(g, "§a§l[+] БАФФЫ И ПАССИВНЫЕ СПОСОБНОСТИ:", selectedRace.buffs, 0xFFE8F5E9, infoX + 12, curY, textWidth);
            curY += 4;

            // Блок 3: Дебаффы и Слабости
            curY = drawSection(g, "§c§l[-] ДЕБАФФЫ И СЛАБОСТИ:", selectedRace.debuffs, 0xFFFFEBEE, infoX + 12, curY, textWidth);
            curY += 4;

            // Блок 4: Активная способность [R]
            curY = drawSection(g, "§d§l[R] АКТИВНАЯ СПОСОБНОСТЬ:", selectedRace.ability, 0xFFF3E5F5, infoX + 12, curY, textWidth);

            g.disableScissor();
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private int drawSection(GuiGraphics g, String title, String body, int bodyColor, int x, int y, int width) {
        g.drawString(this.font, title, x, y, 0xFFFFFFFF);
        y += 11;
        List<FormattedCharSequence> lines = this.font.split(Component.literal(body), width);
        for (FormattedCharSequence line : lines) {
            g.drawString(this.font, line, x, y, bodyColor);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = 14;
        int listY = 24;
        int listW = 155;
        int listH = this.height - 58;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int clickedIndex = (int) ((mouseY - listY) / ITEM_HEIGHT) + scrollOffset;
            if (clickedIndex >= 0 && clickedIndex < races.size()) {
                selectedRace = races.get(clickedIndex);
                infoScrollOffset = 0; // Сбрасываем прокрутку текста при переключении расы
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listX = 14;
        int listW = 155;
        int listH = this.height - 58;

        // Если курсор над левым списком — крутим список рас
        if (mouseX >= listX && mouseX <= listX + listW) {
            int maxVisible = listH / ITEM_HEIGHT;
            int maxScroll = Math.max(0, races.size() - maxVisible);
            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll);
        } else {
            // Если курсор над правой панелью — плавно крутим описание
            infoScrollOffset = Mth.clamp(infoScrollOffset - (int) (delta * 14), 0, 140);
        }
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
