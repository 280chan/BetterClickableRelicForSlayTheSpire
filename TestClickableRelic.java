package sl_example;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardGroup.CardGroupType;
import com.megacrit.cardcrawl.cards.blue.Strike_Blue;
import com.megacrit.cardcrawl.cards.green.Strike_Green;
import com.megacrit.cardcrawl.cards.purple.Strike_Purple;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import basemod.abstracts.CustomRelic;

public class TestClickableRelic extends CustomRelic implements BetterClickableRelic<TestClickableRelic> {
	public static final String ID = "TestClickableRelicID";
	
	private boolean cardSelected = true;
	public static RoomPhase phase;
	private static CurrentScreen pre;
	
	public TestClickableRelic() {
		super(ID, new Texture(Gdx.files.internal("你的遗物图路径")), RelicTier.SPECIAL, LandingSound.MAGICAL);
		// 示例效果，800毫秒内，初次右击无必触发效果，第二次右击增加玩家1生命上限，第三次选卡
		this.setDuration(800).addRightClickActions(null, () -> {
			AbstractDungeon.player.increaseMaxHp(1, false);
		}, () -> this.card()).addRightClickSelections(null, null, null, () -> {
			System.out.println("800毫秒内恰好右击4次时显示");
		}, () -> {
			System.out.println("800毫秒内恰好右击5次时显示");
		});
	}
	
	public String getUpdatedDescription() {
		return DESCRIPTIONS[0];
	}

	public void updateDescription(PlayerClass c) {
		this.tips.clear();
	    this.tips.add(new PowerTip(this.name, this.getUpdatedDescription()));
	    initializeTips();
	}

	public void onEquip() {
		this.counter = -1;
	}
	
	public void update() {
		super.update();
		if (!cardSelected) {
			if (AbstractDungeon.gridSelectScreen.selectedCards.size() == 1) {
				AbstractDungeon.effectList
						.add(new ShowCardAndObtainEffect(AbstractDungeon.gridSelectScreen.selectedCards.get(0),
								Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
			} else if (AbstractDungeon.screen != pre) {
				return;
			}
			cardSelected = true;
			AbstractDungeon.getCurrRoom().phase = phase;
			AbstractDungeon.gridSelectScreen.selectedCards.clear();
		}
	}
	
	@Override
	public void onSingleRightClick() {
		this.relic();
	}

	@Override
	public void onEachRightClick() {
	}

	@Override
	public void onDurationEnd() {
	}
	
	private void relic() {
		if (this.counter == -2)
			return;
		System.out.println("此示例遗物未加入该效果，假装此处触发选遗物");
		this.counter = -2;
	}
	
	private void card() {
		if (this.counter == -2)
			return;
		pre = AbstractDungeon.screen;
		if (AbstractDungeon.isScreenUp) {
			AbstractDungeon.dynamicBanner.hide();
			AbstractDungeon.previousScreen = AbstractDungeon.screen;
		}
		phase = AbstractDungeon.getCurrRoom().phase;
		AbstractDungeon.getCurrRoom().phase = RoomPhase.INCOMPLETE;
		this.cardSelected = false;
		CardGroup g = new CardGroup(CardGroupType.UNSPECIFIED);
		g.group = Stream.of(new Strike_Red(), new Strike_Green(), new Strike_Blue(), new Strike_Purple())
				.collect(Collectors.toCollection(ArrayList::new));
		g.shuffle();
		g.group = g.group.stream().limit(3).peek(a -> UnlockTracker.markCardAsSeen(a.cardID))
				.collect(Collectors.toCollection(ArrayList::new));
		AbstractDungeon.gridSelectScreen.open(g, 1, "选择一张牌", false, false, true, false);
		AbstractDungeon.overlayMenu.cancelButton.show("跳过");
		this.counter = -2;
	}

}